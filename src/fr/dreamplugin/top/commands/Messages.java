package fr.dreamplugin.top.commands;

import fr.dreamplugin.top.TrymPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class Messages {
    private final Map<Player, Player> lastMessage = new HashMap<>();
    private final Map<Player, Set<Player>> ignoredPlayers = new HashMap<>();

    // Envoyer un message
    public void sendMessage(Player sender, Player receiver, String message, TrymPlugin plugin) {
        if (ignoredPlayers.getOrDefault(receiver, Collections.emptySet()).contains(sender)) {
            sender.sendMessage(plugin.getLangMessage("mp.ignore.blocked"));
            return;
        }

        sender.sendMessage(plugin.getLangMessage("mp.format.sent").replace("{player}", receiver.getName()).replace("{message}", message));
        receiver.sendMessage(plugin.getLangMessage("mp.format.receiver").replace("{player}", sender.getName()).replace("{message}", message));
        lastMessage.put(sender, receiver);
        lastMessage.put(receiver, sender);
    }

    // Obtenir le dernier joueur ayant envoyé un message
    public Player getLastMessaged(Player player) {
        return lastMessage.get(player);
    }

    // Ignorer un joueur
    public void ignorePlayer(Player player, Player toIgnore, TrymPlugin plugin) {
        ignoredPlayers.computeIfAbsent(player, k -> new HashSet<>()).add(toIgnore);
        player.sendMessage(plugin.getLangMessage("mp.ignore.add").replace("{player}", toIgnore.getName()));
    }

    // Ne plus ignorer un joueur
    public void unignorePlayer(Player player, Player toUnignore, TrymPlugin plugin) {
        Set<Player> ignored = ignoredPlayers.get(player);
        if (ignored != null && ignored.remove(toUnignore)) {
            player.sendMessage(plugin.getLangMessage("mp.ignore.remove").replace("{player}", toUnignore.getName())); //
        } else {
            player.sendMessage(plugin.getLangMessage("mp.ignore.not_ignore"));
        }
    }

    // Liste des joueurs ignorés
    public Set<Player> getIgnoredPlayers(Player player) {
        return ignoredPlayers.getOrDefault(player, Collections.emptySet());
    }
}