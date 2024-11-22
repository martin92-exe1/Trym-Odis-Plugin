package fr.dreamplugin.top.commands;

import fr.dreamplugin.top.events.JoinPlayer;
import fr.dreamplugin.top.TrymPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.UUID;

public class Economy implements CommandExecutor {

    private final TrymPlugin plugin;
    private final JoinPlayer joinPlayerEvent;

    public Economy(TrymPlugin plugin, JoinPlayer joinPlayerEvent) {
        this.plugin = plugin;
        this.joinPlayerEvent = joinPlayerEvent;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("balance") || label.equalsIgnoreCase("bal")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.getLangMessage("onlyPlayers").replace("{command}", command.getName()));
                return true;
            }

            Player player = (Player) sender;

            // Si aucun argument, afficher le solde de l'exécutant
            if (args.length == 0) {
                double balance = joinPlayerEvent.getMoney(player.getUniqueId());  // Récupère l'argent du joueur
                player.sendMessage(plugin.getLangMessage("economy.commands.balance.check")
                        .replace("{balance}", "" + balance));
                return true;
            }

            // Si un argument, afficher le solde du joueur cible
            if (args.length == 1) {
                Player target = Bukkit.getPlayer(args[0]);

                if (target == null || !target.isOnline()) {
                    player.sendMessage(plugin.getLangMessage("economy.errors.player-not-found").replace("{player}", args[0]));
                    return true;
                }

                double targetBalance = joinPlayerEvent.getMoney(target.getUniqueId());  // Récupère l'argent du joueur cible
                player.sendMessage(plugin.getLangMessage("economy.commands.balance.otherCheck")
                        .replace("{player}", target.getName())
                        .replace("{balance}", "" + targetBalance));
                return true;
            }

            // Si trop d'arguments, afficher un message d'erreur
            player.sendMessage(plugin.getLangMessage("economy.commands.usage.balance"));
            return true;
        }


        if (label.equalsIgnoreCase("pay")) {
            // Commande /pay <joueur> <montant>
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.getLangMessage("onlyPlayers").replace("{command}", command.getName()));
                return true;
            }

            if (args.length < 2) {
                sender.sendMessage(plugin.getLangMessage("economy.commands.usage.pay"));
                return true;
            }

            Player senderPlayer = (Player) sender;
            Player receiver = Bukkit.getPlayer(args[0]);

            if (receiver == null || !receiver.isOnline()) {
                senderPlayer.sendMessage(plugin.getLangMessage("economy.errors.player-not-found"));
                return true;
            }

            if (senderPlayer.equals(receiver)) {
                senderPlayer.sendMessage(plugin.getLangMessage("economy.commands.pay.cannot-pay-self"));
                return true;
            }

            try {
                double amount = Double.parseDouble(args[1]);
                if (amount <= 0) {
                    senderPlayer.sendMessage(plugin.getLangMessage("economy.errors.invalid-amount"));
                    return true;
                }

                UUID senderUUID = senderPlayer.getUniqueId();
                UUID receiverUUID = receiver.getUniqueId();

                double senderBalance = joinPlayerEvent.getMoney(senderUUID); // Charge le solde du joueur
                if (senderBalance < amount) {
                    senderPlayer.sendMessage(plugin.getLangMessage("economy.errors.not-enough-money"));
                    return true;
                }

                // Transfert de l'argent
                joinPlayerEvent.subtractMoney(senderUUID, amount);  // Soustraction du solde
                joinPlayerEvent.addMoney(receiverUUID, amount);  // Ajout du solde à l'autre joueur

                senderPlayer.sendMessage(plugin.getLangMessage("economy.commands.pay.money-sent").replace("{amount}", "" + amount).replace("{receiver}", receiver.getName()));
                receiver.sendMessage(plugin.getLangMessage("economy.commands.pay.money-received").replace("{amount}", "" + amount).replace("{sender}", senderPlayer.getName()));
                return true;

            } catch (NumberFormatException e) {
                sender.sendMessage(plugin.getLangMessage("economy.errors.invalid-amount"));
                return true;
            }

        } else if (label.equalsIgnoreCase("eco")) {
            // Commandes admin : /eco give|take|set <joueur> <montant>
            if (!sender.hasPermission("oraclex.economy")) {
                sender.sendMessage(plugin.getLangMessage("noPermission"));
                return true;
            }

            if (args.length < 3) {
                sender.sendMessage(plugin.getLangMessage("economy.commands.usage.eco"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null || !target.isOnline()) {
                sender.sendMessage(plugin.getLangMessage("economy.errors.player-not-found"));
                return true;
            }

            try {
                double amount = Double.parseDouble(args[2]);
                UUID targetUUID = target.getUniqueId();

                switch (args[0].toLowerCase()) {
                    case "give":
                        if (amount <= 0) {
                            sender.sendMessage(plugin.getLangMessage("economy.commands.usage.eco"));
                            return true;
                        }
                        joinPlayerEvent.addMoney(targetUUID, amount);
                        sender.sendMessage(plugin.getLangMessage("economy.commands.eco.money-given").replace("{amount}", "" + amount).replace("{player}", target.getName()));
                        target.sendMessage(plugin.getLangMessage("economy.commands.eco.player-money-added").replace("{amount}", "" + amount));
                        break;

                    case "take":
                        if (amount <= 0) {
                            sender.sendMessage(plugin.getLangMessage("economy.errors.amount-negative"));
                            return true;
                        }
                        joinPlayerEvent.subtractMoney(targetUUID, amount);
                        sender.sendMessage(plugin.getLangMessage("economy.commands.eco.money-taken").replace("{amount}", "" + amount).replace("{player}", target.getName()));
                        target.sendMessage(plugin.getLangMessage("economy.commands.eco.player-money-taken").replace("{amount}", "" + amount));
                        break;

                    case "set":
                        if (amount < 0) {
                            sender.sendMessage(plugin.getLangMessage("economy.errors.amount-negative"));
                            return true;
                        }
                        joinPlayerEvent.setMoney(targetUUID, amount);
                        sender.sendMessage(plugin.getLangMessage("economy.commands.eco.money-set").replace("{amount}", "" + amount).replace("{player}", target.getName()));
                        target.sendMessage(plugin.getLangMessage("economy.commands.eco.player-money-set").replace("{amount}", "" + amount));
                        break;

                    default:
                        sender.sendMessage(plugin.getLangMessage("economy.commands.usage.eco"));
                        return true;
                }

                return true;

            } catch (NumberFormatException e) {
                sender.sendMessage(plugin.getLangMessage("economy.errors.invalid-amount"));
                return true;
            }
        }

        sender.sendMessage(plugin.getLangMessage("economy.errors.unknown-command"));
        return true;
    }
}
