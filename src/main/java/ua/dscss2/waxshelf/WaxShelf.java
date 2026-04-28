package ua.dscss2.waxshelf;

import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import ua.dscss2.waxshelf.commands.WaxCommand;
import ua.dscss2.waxshelf.listeners.ItemFrameListener;
import ua.dscss2.waxshelf.listeners.ShelfListener;
import ua.dscss2.waxshelf.utils.ConfigMigrator;
import ua.dscss2.waxshelf.utils.MessageUtils;
import ua.dscss2.waxshelf.utils.UpdateChecker;

import java.util.Objects;

public final class WaxShelf extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        ConfigMigrator.migrate(this);

        // Register Listeners
        getServer().getPluginManager().registerEvents(new ItemFrameListener(this), this);
        getServer().getPluginManager().registerEvents(new ShelfListener(this), this);

        // Register Command
        WaxCommand waxCommand = new WaxCommand(this);
        Objects.requireNonNull(getCommand("waxshelf")).setExecutor(waxCommand);
        Objects.requireNonNull(getCommand("waxshelf")).setTabCompleter(waxCommand);

        // Initialize bStats
        new Metrics(this, 30991);

        // Update Checker
        if (getConfig().getBoolean("update-checker", true)) {
            new UpdateChecker(this, "waxshelf").getVersion(version -> {
                if (!this.getDescription().getVersion().equals(version)) {
                    String msg = getConfig().getString("messages.update-available", "&eA new version of WaxShelf is available: &b%version%&e! Download it here: &b%url%")
                            .replace("%version%", version)
                            .replace("%url%", "https://modrinth.com/plugin/waxshelf");
                    getServer().getConsoleSender().sendMessage(MessageUtils.translateHex(msg));
                }
            });
        }

        sendStartupMessage();
    }

    private void sendStartupMessage() {
        getServer().getConsoleSender().sendMessage(ChatColor.GOLD + "DDD   SSS   CCC  SSS   SSS   22");
        getServer().getConsoleSender().sendMessage(ChatColor.GOLD + "D  D S     C    S     S     2  2");
        getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "D  D  SSS  C     SSS   SSS    2");
        getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "D  D     S C        S     S  2");
        getServer().getConsoleSender().sendMessage(ChatColor.GOLD + "DDD  SSSS   CCC SSSS  SSSS  2222");
        getServer().getConsoleSender().sendMessage(ChatColor.GRAY + "Developed by " + ChatColor.AQUA + "dscss2");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
