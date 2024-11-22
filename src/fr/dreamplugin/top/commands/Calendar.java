package fr.dreamplugin.top.commands;

import fr.dreamplugin.top.TrymPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Calendar implements CommandExecutor {

    private final TrymPlugin plugin;

    public Calendar(TrymPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Vérification si le sender est un joueur
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLangMessage("onlyPlayers").replace("{command}", command.getName()));
            return true;
        }

        Player player = (Player) sender;

        // Vérifie si le mode featureTestingMode est activé
        boolean featureTestingMode = plugin.getConfig().getBoolean("featureTestingMode");

        if (!featureTestingMode) {
            // Mode désactivé : on renvoie un message d'erreur
            player.sendMessage(plugin.getLangMessage("inDev"));
            return true;
        }

        // Mode activé : ouvre le GUI
        openCalendarGUI(player);
        return true;
    }

    private void openCalendarGUI(Player player) {
        // Crée un inventaire GUI
        Inventory calendarGUI = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Calendrier");

        // // Exemple d'item dans le GUI
        // ItemStack exampleItem = new ItemStack(Material.CLOCK); // Un item "horloge"
        // ItemMeta meta = exampleItem.getItemMeta();
        // if (meta != null) {
        //     meta.setDisplayName(ChatColor.YELLOW + "Événement spécial");
        //     meta.setLore(java.util.Arrays.asList(
        //             ChatColor.WHITE + "Description :",
        //             ChatColor.GRAY + "Un événement à venir."
        //     ));
        //     exampleItem.setItemMeta(meta);
        // }
        //
        // // Ajoute l'item dans le GUI
        // calendarGUI.setItem(13, exampleItem); // Position au centre

        // Ouvre l'inventaire pour le joueur
        player.openInventory(calendarGUI);
    }
}
