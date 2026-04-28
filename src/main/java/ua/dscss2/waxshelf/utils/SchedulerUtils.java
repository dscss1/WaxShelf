package ua.dscss2.waxshelf.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

public class SchedulerUtils {

    private static final boolean IS_FOLIA = isClass("io.papermc.paper.threadedregionscheduler.RegionScheduler");

    public static void runAsync(Plugin plugin, Runnable runnable) {
        if (IS_FOLIA) {
            Bukkit.getAsyncScheduler().runNow(plugin, task -> runnable.run());
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
        }
    }

    public static void runLater(Entity entity, Plugin plugin, Runnable runnable, long ticks) {
        if (IS_FOLIA) {
            entity.getScheduler().runDelayed(plugin, task -> runnable.run(), () -> {}, ticks);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, runnable, ticks);
        }
    }

    private static boolean isClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
