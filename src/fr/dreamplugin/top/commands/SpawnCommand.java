package fr.dreamplugin.top.commands;

import fr.dreamplugin.top.TrymPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class SpawnCommand implements CommandExecutor {

    private final FileConfiguration config;
    private final TrymPlugin plugin;

    public SpawnCommand(FileConfiguration config, TrymPlugin plugin) {
        this.config = config;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLangMessage("onlyPlayers").replace("{command}", command.getName()));
            return true;
        }

        Player player = (Player) sender;

        switch (label.toLowerCase()) {
            case "spawn":
                return handleSpawn(player);

            case "setspawn":
                return handleSetSpawn(player, command.getName());

            default:
                return false;
        }
    }

    private boolean handleSpawn(Player player) {
        if (!config.contains("spawn")) {
            player.sendMessage(plugin.getLangMessage("teleport.spawn.notset"));
            return true;
        }

        // Récupérer les données du spawn dans une seule ligne
        String spawnData = config.getString("spawn");
        if (spawnData == null) {
            player.sendMessage(plugin.getLangMessage("teleport.spawn.invalid"));
            return true;
        }

        // Décoder les données
        String[] data = spawnData.split(",");
        if (data.length != 6) {
            player.sendMessage(plugin.getLangMessage("teleport.spawn.error-config"));
            return true;
        }

        String worldName = data[0];
        double x = Double.parseDouble(data[1]);
        double y = Double.parseDouble(data[2]);
        double z = Double.parseDouble(data[3]);
        float yaw = Float.parseFloat(data[4]);
        float pitch = Float.parseFloat(data[5]);

        if (Bukkit.getWorld(worldName) == null) {
            player.sendMessage(plugin.getLangMessage("teleport.spawn.worldnotfound"));
            return true;
        }
        String title = plugin.getLangMessage("teleport.titles.success_title");
        String subtitle = plugin.getLangMessage("teleport.titles.success_subtitle")
                .replace("{x}", String.valueOf(x))
                .replace("{y}", String.valueOf(y))
                .replace("{z}", String.valueOf(z));

        player.sendTitle(title, subtitle, 10, 70, 20);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);

        // Téléporter le joueur
        Location spawnLocation = new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
        player.teleport(spawnLocation);
        player.sendMessage(plugin.getLangMessage("teleport.spawn.success"));
        return true;
    }

    private boolean handleSetSpawn(Player player, String commandname) {
        if (!player.hasPermission("spawn.set")) {
            player.sendMessage(plugin.getLangMessage("noPermission").replace("{command}", commandname));
            return true;
        }

        // Récupérer la position actuelle
        Location loc = player.getLocation();
        String worldName = loc.getWorld().getName();
        String spawnData = String.format("%s,%.2f,%.2f,%.2f,%.2f,%.2f",
                worldName, loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());

        // Sauvegarder dans la config
        config.set("spawn", spawnData);
        player.sendMessage(plugin.getLangMessage("teleport.setspawn"));
        return true;
    }
}
