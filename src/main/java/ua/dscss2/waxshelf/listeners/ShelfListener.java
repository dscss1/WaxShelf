package ua.dscss2.waxshelf.listeners;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import ua.dscss2.waxshelf.utils.MessageUtils;

import java.util.Set;

public class ShelfListener implements Listener {

    private final Plugin plugin;
    private final NamespacedKey waxedKey;
    private static final Set<String> SHELF_TYPES = Set.of(
            "OAK_SHELF", "SPRUCE_SHELF", "BIRCH_SHELF", "JUNGLE_SHELF", "ACACIA_SHELF",
            "DARK_OAK_SHELF", "MANGROVE_SHELF", "CHERRY_SHELF", "BAMBOO_SHELF",
            "CRIMSON_SHELF", "WARPED_SHELF", "PALE_OAK_SHELF", "CHISELED_BOOKSHELF"
    );

    public ShelfListener(Plugin plugin) {
        this.plugin = plugin;
        this.waxedKey = new NamespacedKey(plugin, "is_waxed");
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
    public void onShelfInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;

        String typeName = block.getType().name();
        if (!SHELF_TYPES.contains(typeName)) return;

        // Check if it's a chiseled bookshelf and if it's disabled in config
        if (typeName.equals("CHISELED_BOOKSHELF") && !plugin.getConfig().getBoolean("waxing.chiseled-bookshelves", true)) {
            return;
        }

        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!(block.getState() instanceof TileState tileState)) return;

        PersistentDataContainer pdc = tileState.getPersistentDataContainer();
        boolean isWaxed = isWaxed(pdc);
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() == Material.HONEYCOMB) {
            if (plugin.getConfig().getBoolean("waxing.shelves", true) && !isWaxed) {
                if (!player.hasPermission("waxshelf.wax")) {
                    sendMessage(player, "no-permission");
                    return;
                }
                setWaxed(pdc, true);
                tileState.update();
                block.getWorld().playSound(block.getLocation(), Sound.ITEM_HONEYCOMB_WAX_ON, 1f, 1f);
                block.getWorld().spawnParticle(Particle.WAX_ON, block.getLocation().add(0.5, 0.5, 0.5), 5, 0.5, 0.5, 0.5, 0);
                sendMessage(player, "wax-on");
                if (player.getGameMode() != GameMode.CREATIVE) item.subtract(1);
                event.setCancelled(true);
            } else if (isWaxed && !player.hasPermission("waxshelf.bypass")) {
                sendMessage(player, "waxed");
                event.setCancelled(true);
            }
        } else if (Tag.ITEMS_AXES.isTagged(item.getType())) {
            if (plugin.getConfig().getBoolean("waxing.shelves", true) && isWaxed) {
                if (!player.hasPermission("waxshelf.unwax")) {
                    sendMessage(player, "no-permission");
                    return;
                }
                setWaxed(pdc, false);
                tileState.update();
                block.getWorld().playSound(block.getLocation(), Sound.ITEM_AXE_WAX_OFF, 1f, 1f);
                block.getWorld().spawnParticle(Particle.WAX_OFF, block.getLocation().add(0.5, 0.5, 0.5), 5, 0.5, 0.5, 0.5, 0);
                sendMessage(player, "wax-off");
                damageItem(player, item);
                event.setCancelled(true);
            }
        } else {
            if (isWaxed && !player.hasPermission("waxshelf.bypass")) {
                sendMessage(player, "waxed");
                event.setCancelled(true);
            }
        }
    }

    private void damageItem(Player player, ItemStack item) {
        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (item.getItemMeta() instanceof org.bukkit.inventory.meta.Damageable damageable) {
            damageable.setDamage(damageable.getDamage() + 1);
            item.setItemMeta(damageable);
            if (damageable.getDamage() >= item.getType().getMaxDurability()) {
                item.setAmount(0);
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
            }
            player.getInventory().setItemInMainHand(item);
        }
    }
}
