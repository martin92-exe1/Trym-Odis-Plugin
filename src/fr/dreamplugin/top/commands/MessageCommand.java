package fr.dreamplugin.top.commands;

import fr.dreamplugin.top.TrymPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageCommand implements CommandExecutor {
    private final Messages messages;
    private final TrymPlugin plugin;

    public MessageCommand(Messages messages, TrymPlugin plugin) {
        this.plugin = plugin;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLangMessage("onlyPlayers").replace("{command}", command.getName()));
            return true;
        }

        Player player = (Player) sender;

        switch (label.toLowerCase()) {
            case "msg":
                if (args.length < 2) {
                    return false;
                }
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null || !target.isOnline()) {
                    player.sendMessage(plugin.getLangMessage("mp.notFound"));
                    return true;
                }
                String message = String.join(" ", args).substring(args[0].length() + 1);
                messages.sendMessage(player, target, message, plugin);
                return true;

            case "r":
                Player last = messages.getLastMessaged(player);
                if (last == null) {
                    player.sendMessage(plugin.getLangMessage("mp.notFoundReply"));
                    return true;
                }
                if (args.length < 1) {
                    return false;
                }
                message = String.join(" ", args);
                messages.sendMessage(player, last, message, plugin);
                return true;

            case "ignore":
                if (args.length < 1) {
                    return false;
                }
                target = Bukkit.getPlayer(args[0]);
                if (target == null || !target.isOnline()) {
                    player.sendMessage(plugin.getLangMessage("mp.notFound"));
                    return true;
                }
                messages.ignorePlayer(player, target, plugin);
                return true;

            case "unignore":
                if (args.length < 1) {
                    return false;
                }
                target = Bukkit.getPlayer(args[0]);
                if (target == null || !target.isOnline()) {
                    player.sendMessage(plugin.getLangMessage("mp.notFound"));
                    return true;
                }
                messages.unignorePlayer(player, target, plugin);
                return true;

            case "ignorelist":
                player.sendMessage(plugin.getLangMessage("mp.ignore.head"));
                for (Player ignored : messages.getIgnoredPlayers(player)) {
                    player.sendMessage(plugin.getLangMessage("mp.ignore.format").replace("{player}", ignored.getName()));
                }
                return true;

            default:
                return false;
        }
    }
}
