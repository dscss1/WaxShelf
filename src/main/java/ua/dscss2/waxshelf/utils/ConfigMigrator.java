package ua.dscss2.waxshelf.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public class ConfigMigrator {

    public static void migrate(Plugin plugin) {
        FileConfiguration config = plugin.getConfig();
        double currentVersion = config.getDouble("config-version", 1.0);
        double latestVersion = 1.1;

        if (currentVersion < latestVersion) {
            plugin.getLogger().info("Migrating config from v" + currentVersion + " to v" + latestVersion);
            
            // Add missing keys with defaults if they don't exist
            if (!config.contains("waxing.chiseled-bookshelves")) {
                config.set("waxing.chiseled-bookshelves", true);
            }
            if (!config.contains("update-checker")) {
                config.set("update-checker", true);
            }
            if (!config.contains("messages.update-available")) {
                config.set("messages.update-available", "&eA new version of WaxShelf is available: &b%version%&e! Download it here: &b%url%");
            }
            
            config.set("config-version", latestVersion);
            saveConfig(plugin, config);
        }
    }

    private static void saveConfig(Plugin plugin, FileConfiguration config) {
        try {
            config.save(new File(plugin.getDataFolder(), "config.yml"));
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save config during migration!");
        }
    }
}
