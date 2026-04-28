package ua.dscss2.waxshelf.listeners;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import ua.dscss2.waxshelf.utils.MessageUtils;
import ua.dscss2.waxshelf.utils.SchedulerUtils;

public class ItemFrameListener implements Listener {

    private final Plugin plugin;
    private final NamespacedKey waxedKey;
    private final NamespacedKey invisibleFrameKey;

    public ItemFrameListener(Plugin plugin) {
        this.plugin = plugin;
        this.waxedKey = new NamespacedKey(plugin, "is_waxed");
        this.invisibleFrameKey = new NamespacedKey(plugin, "invisible_frame");
    }

    private void sendMessage(Player player, String path) {
        String message = plugin.getConfig().getString("messages." + path);
        MessageUtils.sendActionBar(player, message);
    }

    private boolean isWaxed(PersistentDataContainer pdc) {
        Byte value = pdc.get(waxedKey, PersistentDataType.BYTE);
        return value != null && value == 1;
    }

    private void setWaxed(PersistentDataContainer pdc, boolean waxed) {
        if (waxed) pdc.set(waxedKey, PersistentDataType.BYTE, (byte) 1);
        else pdc.remove(waxedKey);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {
        if (!(event.getEntity() instanceof ItemFrame)) return;
        Player player = event.getPlayer();
        if (player == null) return;

        ItemStack item = player.getInventory().getItem(event.getHand());
        if (item == null || item.getType() == Material.AIR) return;

        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.getPersistentDataContainer().has(invisibleFrameKey, PersistentDataType.BYTE)) {
            if (!player.hasPermission("waxshelf.invisible")) {
                sendMessage(player, "no-permission");
                event.setCancelled(true);
                return;
            }
            ItemFrame frame = (ItemFrame) event.getEntity();
            frame.getPersistentDataContainer().set(invisibleFrameKey, PersistentDataType.BYTE, (byte) 1);
            updateFrameState(frame);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent event) {
        if (!(event.getEntity() instanceof ItemFrame)) return;
        ItemFrame frame = (ItemFrame) event.getEntity();
        PersistentDataContainer pdc = frame.getPersistentDataContainer();

        if (isWaxed(pdc)) {
            if (event instanceof HangingBreakByEntityEvent) {
                HangingBreakByEntityEvent entityEvent = (HangingBreakByEntityEvent) event;
                if (entityEvent.getRemover() instanceof Player player) {
                    if (!player.hasPermission("waxshelf.bypass")) {
                        sendMessage(player, "waxed");
                        event.setCancelled(true);
                        return;
                    }
                } else {
                    event.setCancelled(true);
                    return;
                }
            } else if (event.getCause() != HangingBreakEvent.RemoveCause.PHYSICS) {
                event.setCancelled(true);
                return;
            }
        }

        if (pdc.has(invisibleFrameKey, PersistentDataType.BYTE)) {
            event.setCancelled(true);
            if (frame.getItem().getType() != Material.AIR) {
                frame.getWorld().dropItemNaturally(frame.getLocation(), frame.getItem());
            }
            Material frameMaterial = frame.getType() == org.bukkit.entity.EntityType.GLOW_ITEM_FRAME ? Material.GLOW_ITEM_FRAME : Material.ITEM_FRAME;
            ItemStack drop = new ItemStack(frameMaterial);
            ItemMeta meta = drop.getItemMeta();
            if (meta != null) {
                String name = plugin.getConfig().getString("messages.invisible-item-frame", "&fInvisible Item Frame");
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', MessageUtils.translateHex(name)));
                meta.getPersistentDataContainer().set(invisibleFrameKey, PersistentDataType.BYTE, (byte) 1);
                drop.setItemMeta(meta);
            }
            frame.getWorld().dropItemNaturally(frame.getLocation(), drop);
            frame.remove();
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFrameInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof ItemFrame frame)) return;
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getHand());
        boolean isWaxed = isWaxed(frame.getPersistentDataContainer());

        if (item.getType() == Material.HONEYCOMB) {
            if (plugin.getConfig().getBoolean("waxing.frames", true) && !isWaxed) {
                if (player.isSneaking()) return;
                if (!player.hasPermission("waxshelf.wax")) {
                    sendMessage(player, "no-permission");
                    return;
                }
                setWaxed(frame.getPersistentDataContainer(), true);
                frame.getWorld().playSound(frame.getLocation(), Sound.ITEM_HONEYCOMB_WAX_ON, 1f, 1f);
                frame.getWorld().spawnParticle(Particle.WAX_ON, frame.getLocation(), 5, 0.1, 0.1, 0.1, 0);
                sendMessage(player, "wax-on");
                if (player.getGameMode() != GameMode.CREATIVE) item.subtract(1);
                event.setCancelled(true);
            } else if (isWaxed && !player.hasPermission("waxshelf.bypass")) {
                sendMessage(player, "waxed");
                event.setCancelled(true);
            }
        } else if (Tag.ITEMS_AXES.isTagged(item.getType())) {
            if (plugin.getConfig().getBoolean("waxing.frames", true) && isWaxed) {
                if (player.isSneaking()) return;
                if (!player.hasPermission("waxshelf.unwax")) {
                    sendMessage(player, "no-permission");
                    return;
                }
                setWaxed(frame.getPersistentDataContainer(), false);
                frame.getWorld().playSound(frame.getLocation(), Sound.ITEM_AXE_WAX_OFF, 1f, 1f);
                frame.getWorld().spawnParticle(Particle.WAX_OFF, frame.getLocation(), 5, 0.1, 0.1, 0.1, 0);
                sendMessage(player, "wax-off");
                damageItem(player, item, event.getHand());
                event.setCancelled(true);
            }
        } else if (item.getType() == Material.SHEARS) {
            if (plugin.getConfig().getBoolean("mechanics.shears-invisible-frames", true)) {
                if (isWaxed && !player.hasPermission("waxshelf.bypass")) {
                    sendMessage(player, "waxed");
                    event.setCancelled(true);
                } else if (!frame.getPersistentDataContainer().has(invisibleFrameKey, PersistentDataType.BYTE) && player.isSneaking()) {
                    if (!player.hasPermission("waxshelf.invisible")) {
                        sendMessage(player, "no-permission");
                        return;
                    }
                    frame.getPersistentDataContainer().set(invisibleFrameKey, PersistentDataType.BYTE, (byte) 1);
                    updateFrameState(frame);
                    frame.getWorld().dropItemNaturally(frame.getLocation(), new ItemStack(Material.LEATHER));
                    frame.getWorld().playSound(frame.getLocation(), Sound.ENTITY_SHEEP_SHEAR, 1f, 1f);
                    damageItem(player, item, event.getHand());
                    event.setCancelled(true);
                }
            }
        } else if (item.getType() == Material.LEATHER) {
            if (plugin.getConfig().getBoolean("mechanics.leather-visible-frames", true)) {
                if (isWaxed && !player.hasPermission("waxshelf.bypass")) {
                    sendMessage(player, "waxed");
                    event.setCancelled(true);
                } else if (frame.getPersistentDataContainer().has(invisibleFrameKey, PersistentDataType.BYTE) && player.isSneaking()) {
                    if (!player.hasPermission("waxshelf.invisible")) {
                        sendMessage(player, "no-permission");
                        return;
                    }
                    frame.getPersistentDataContainer().remove(invisibleFrameKey);
                    updateFrameState(frame);
                    if (player.getGameMode() != GameMode.CREATIVE) item.subtract(1);
                    frame.getWorld().playSound(frame.getLocation(), Sound.ENTITY_ITEM_FRAME_PLACE, 1f, 1f);
                    event.setCancelled(true);
                }
            }
        } else {
            if (isWaxed && !player.hasPermission("waxshelf.bypass")) {
                sendMessage(player, "waxed");
                event.setCancelled(true);
            } else {
                updateFrameState(frame);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFrameDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof ItemFrame frame)) return;
        if (isWaxed(frame.getPersistentDataContainer())) {
            if (event.getDamager() instanceof Player player && !player.hasPermission("waxshelf.bypass")) {
                sendMessage(player, "waxed");
            }
            event.setCancelled(true);
        } else {
            updateFrameState(frame);
        }
    }

    private void updateFrameState(ItemFrame frame) {
        SchedulerUtils.runLater(frame, plugin, () -> {
            if (!frame.isValid()) return;
            boolean isInvisible = frame.getPersistentDataContainer().has(invisibleFrameKey, PersistentDataType.BYTE);
            frame.setVisible(!isInvisible);
            if (isInvisible) frame.setGlowing(false);
        }, 1L);
    }

    private void damageItem(Player player, ItemStack item, org.bukkit.inventory.EquipmentSlot slot) {
        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (item.getItemMeta() instanceof org.bukkit.inventory.meta.Damageable damageable) {
            damageable.setDamage(damageable.getDamage() + 1);
            item.setItemMeta(damageable);
            if (damageable.getDamage() >= item.getType().getMaxDurability()) {
                item.setAmount(0);
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
            }
            player.getInventory().setItem(slot, item);
        }
    }
}
