package fr.dreamplugin.top.commands;

import fr.dreamplugin.top.TrymPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Invsee implements CommandExecutor, Listener {

    private final TrymPlugin plugin;

    public Invsee(TrymPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLangMessage("onlyPlayers").replace("{command}", command.getName()));
            return true;
        }

        Player executor = (Player) sender;

        if (args.length != 1) {
            return false;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            executor.sendMessage(plugin.getLangMessage("staff.errors.player-not-found"));
            return true;
        }

        // Créer un inventaire pour visualiser (6 lignes de 9 slots, soit 54 slots au total)
        Inventory invSee = Bukkit.createInventory(null, 54, ChatColor.GOLD + "/invsee - " + target.getName());

        // Copier l'inventaire principal
        ItemStack[] mainInventory = target.getInventory().getContents();
        for (int i = 0; i < 36; i++) {
            invSee.setItem(i, mainInventory[i]); // Inventaire principal
        }

        // Copier l'équipement d'armure
        ItemStack[] armor = target.getInventory().getArmorContents();
        for (int i = 0; i < armor.length; i++) {
            invSee.setItem(45 + i, armor[i]); // Placer les pièces d'armure en bas à droite
        }

        // Ajouter l'item dans la main secondaire
        invSee.setItem(44, target.getInventory().getItemInOffHand());

        // Ouvrir l'inventaire
        executor.openInventory(invSee);
        return true;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().startsWith(ChatColor.GOLD + "/invsee - ")) {
            event.setCancelled(true); // Annule toute interaction
        }
    }

}
