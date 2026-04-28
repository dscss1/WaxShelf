package ua.dscss2.waxshelf.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import ua.dscss2.waxshelf.utils.MessageUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WaxCommand implements CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;

    public WaxCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("waxshelf.admin")) {
                String noPerm = plugin.getConfig().getString("messages.no-permission", "&cYou don't have permission to do this!");
                sender.sendMessage(MessageUtils.translateHex(noPerm));
                return true;
            }
            plugin.reloadConfig();
            String reloadMsg = plugin.getConfig().getString("messages.reload", "&aWaxShelf config reloaded!");
            sender.sendMessage(MessageUtils.translateHex(reloadMsg));
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "WaxShelf " + ChatColor.GRAY + "v" + plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.YELLOW + "Usage: /waxshelf reload");
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Stream.of("reload")
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .filter(s -> sender.hasPermission("waxshelf.admin"))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
