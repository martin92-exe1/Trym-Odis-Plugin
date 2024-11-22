package fr.dreamplugin.top.commands;

import fr.dreamplugin.top.TrymPlugin;
import fr.dreamplugin.top.events.JoinPlayer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Welcome implements CommandExecutor {

    private final TrymPlugin plugin;
    private final JoinPlayer joinPlayerEvent;

    public Welcome(TrymPlugin plugin, JoinPlayer joinPlayerEvent) {
        this.plugin = plugin;
        this.joinPlayerEvent = joinPlayerEvent;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLangMessage("onlyPlayers").replace("{command}", command.getName()));
            return false;
        }

        Player player = (Player) sender;

        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            UUID joinerUUID = onlinePlayer.getUniqueId();
            UUID welcomerUUID = player.getUniqueId();

            if (joinPlayerEvent.canWelcome(joinerUUID, welcomerUUID)) {
                joinPlayerEvent.addMoney(player.getUniqueId(), plugin.extraConfig.getInt("welcome_reward", 5)); // Récompense
                plugin.getServer().broadcastMessage(plugin.getLangMessage("welcome.message").replace("{player}", player.getName()).replace("{news}", onlinePlayer.getName()));
                joinPlayerEvent.markWelcomed(joinerUUID, welcomerUUID);
                player.sendMessage(plugin.getLangMessage("welcome.rewards").replace("{news}", onlinePlayer.getName()));
                return true;
            }
        }

        player.sendMessage(plugin.getLangMessage("welcome.empty"));
        return true;
    }
}
