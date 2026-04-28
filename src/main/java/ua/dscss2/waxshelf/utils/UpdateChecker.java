package ua.dscss2.waxshelf.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Consumer;

public class UpdateChecker {

    private final JavaPlugin plugin;
    private final String slug;

    public UpdateChecker(JavaPlugin plugin, String slug) {
        this.plugin = plugin;
        this.slug = slug;
    }

    public void getVersion(final Consumer<String> consumer) {
        SchedulerUtils.runAsync(this.plugin, () -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL("https://api.modrinth.com/v2/project/" + slug + "/version").openConnection();
                connection.setRequestProperty("User-Agent", "WaxShelf UpdateChecker");
                
                try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
                    JsonArray versions = JsonParser.parseReader(reader).getAsJsonArray();
                    if (versions.size() > 0) {
                        String latestVersion = versions.get(0).getAsJsonObject().get("version_number").getAsString();
                        consumer.accept(latestVersion);
                    }
                }
            } catch (Exception exception) {
                plugin.getLogger().info("Unable to check for updates on Modrinth: " + exception.getMessage());
            }
        });
    }
}
