package fr.dreamplugin.top.commands;

import fr.dreamplugin.top.TrymPlugin;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Cosmetics implements CommandExecutor {

    private final TrymPlugin plugin;

    public Cosmetics(TrymPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLangMessage("onlyPlayers").replace("{command}", command.getName()));
            return false;
        }

        Player player = (Player) sender;
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        // Vérifie si le joueur tient un objet
        if (itemInHand.getType() == Material.AIR) {
            player.sendMessage(plugin.getLangMessage("cosmetics.hat.noItem"));
            return true;
        }

        // Vérifie si l'objet est un type approprié pour être un cosmétique
        if (!isCosmeticItem(itemInHand)) {
            player.sendMessage(plugin.getLangMessage("cosmetics.hat.invalidItem"));
            return true;
        }

        // Récupère le casque actuel
        ItemStack currentHelmet = player.getInventory().getHelmet();

        // Place l'objet en tant que casque
        player.getInventory().setHelmet(itemInHand);

        // Replace l'ancien casque (s'il existe) dans la main du joueur
        if (currentHelmet != null && currentHelmet.getType() != Material.AIR) {
            player.getInventory().setItemInMainHand(currentHelmet);
        } else {
            // Sinon, vide la main du joueur
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        }

        // Envoi d'un titre et d'un message de confirmation
        player.sendTitle(
                plugin.getLangMessage("cosmetics.hat.title"),
                plugin.getLangMessage("cosmetics.hat.subtitle").replace("{hat}", itemInHand.getType().name().toLowerCase()),
                10, 60, 20
        );

        // Message dans le chat
        player.sendMessage(plugin.getLangMessage("cosmetics.hat.use").replace("{hat}", itemInHand.getType().name().toLowerCase()));

        // Effet sonore
        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1.0f, 1.0f);

        return true;
    }

    // Méthode pour vérifier si l'item est un "cosmétique" autorisé
    private boolean isCosmeticItem(ItemStack item) {
        Material type = item.getType();
        return type.isBlock() || type == Material.CARVED_PUMPKIN || type == Material.PLAYER_HEAD;
    }
}
