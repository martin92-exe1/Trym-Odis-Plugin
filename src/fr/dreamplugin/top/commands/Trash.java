package fr.dreamplugin.top.commands;

import fr.dreamplugin.top.TrymPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class Trash implements CommandExecutor {

    private final TrymPlugin plugin;

    public Trash(TrymPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLangMessage("onlyPlayers").replace("{command}", command.getName()));
            return true;
        }

        Player player = (Player) sender;

        // Vérifie les permissions pour déterminer la taille de l'inventaire
        int size = 0;
        if (player.hasPermission("oracleplus.trash.54")) {
            size = 54; // Double coffre
        } else if (player.hasPermission("oracleplus.trash.27")) {
            size = 27; // Coffre standard
        } else if (player.hasPermission("oracleplus.trash.18")) {
            size = 18; // 2 rangées
        } else if (player.hasPermission("oracleplus.trash.9")) {
            size = 9; // 1 rangée
        } else {
            player.sendMessage(plugin.getLangMessage("noPermission").replace("{command}", command.getName()));
            return true;
        }

        // Crée l'inventaire de poubelle
        Inventory trashInventory = Bukkit.createInventory(null, size, "§8Trash");

        // Ouvre l'inventaire
        player.openInventory(trashInventory);
        player.sendMessage(plugin.getLangMessage("trash.open").replace("{size}", size + ""));

        return true;
    }
}
