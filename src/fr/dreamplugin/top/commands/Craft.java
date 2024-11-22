package fr.dreamplugin.top.commands;

import fr.dreamplugin.top.TrymPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Craft implements CommandExecutor {

    private final TrymPlugin plugin;

    public Craft(TrymPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLangMessage("onlyPlayers").replace("{command}", command.getName()));
            return true;
        }

        Player player = (Player) sender;

        // Ouvrir la table de craft pour le joueur
        player.openWorkbench(null, true);
        player.sendMessage(plugin.getLangMessage("craft.open"));
        return true;
    }
}
