package fr.dreamplugin.top.commands;

import fr.dreamplugin.top.TrymPlugin;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class Teleport implements CommandExecutor {
    private final TrymPlugin plugin;
    private final Random random = new Random();

    public Teleport(TrymPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLangMessage("onlyPlayers").replace("{command}", command.getName()));
            return true;
        }

        Player player = (Player) sender;

        // Gestion des commandes
        switch (label.toLowerCase()) {
            case "rtp":
                handleRtpCommand(player);
                break;

            case "tphere":
                if (args.length < 1) {
                    player.sendMessage(plugin.getLangMessage("teleport.tphere.noPlayer"));
                    return true;
                }

                Player target = Bukkit.getPlayer(args[0]);
                if (target == null || !target.isOnline()) {
                    player.sendMessage(plugin.getLangMessage("teleport.tphere.invalidPlayer"));
                    return true;
                }

                handleTphereCommand(player, target);
                break;

            default:
                return false;
        }

        return true;
    }

    private void handleRtpCommand(Player player) {
        World world = player.getWorld();
        FileConfiguration config = plugin.getConfig();
        int cooldown = config.getInt("teleport-cooldown", 0);

        // Affichage du message de compte à rebours si cooldown est activé
        if (cooldown > 0) {
            player.sendMessage(plugin.getLangMessage("teleport.rtp.preparing").replace("{time}", String.valueOf(cooldown)));

            new BukkitRunnable() {
                int countdown = cooldown;

                @Override
                public void run() {
                    if (countdown <= 0) {
                        performTeleport(player, world);
                        cancel();
                    } else {
                        countdown--;
                    }
                }
            }.runTaskTimer(plugin, 0, 20); // 20 ticks = 1 seconde
        } else {
            performTeleport(player, world); // Téléportation immédiate si cooldown est 0
        }
    }

    private void handleTphereCommand(Player player, Player target) {
        Location playerLocation = player.getLocation();
        target.teleport(playerLocation);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        target.playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        player.sendMessage(plugin.getLangMessage("teleport.tphere.success").replace("{player}", target.getName()));
        target.sendMessage(plugin.getLangMessage("teleport.tphere.teleported").replace("{player}", player.getName()));
    }

    private void performTeleport(Player player, World world) {
        Location safeLocation = findSafeLocation(world);

        if (safeLocation != null) {
            player.teleport(safeLocation);
            player.sendMessage(plugin.getLangMessage("teleport.rtp.success"));

            // Envoi du titre et sous-titre après téléportation avec les coordonnées
            String title = plugin.getLangMessage("teleport.titles.success_title");
            String subtitle = plugin.getLangMessage("teleport.titles.success_subtitle")
                    .replace("{x}", String.valueOf(safeLocation.getBlockX()))
                    .replace("{y}", String.valueOf(safeLocation.getBlockY()))
                    .replace("{z}", String.valueOf(safeLocation.getBlockZ()));

            player.sendTitle(title, subtitle, 10, 70, 20);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        } else {
            player.sendMessage(plugin.getLangMessage("teleport.rtp.failure"));
        }
    }

    private Location findSafeLocation(World world) {
        int maxAttempts = 10;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int x = random.nextInt(9999) - 500;
            int z = random.nextInt(9999) - 500;
            int y = world.getHighestBlockYAt(x, z) + 1;

            Location location = new Location(world, x, y, z);
            Material groundMaterial = world.getBlockAt(x, y - 1, z).getType();
            Material headMaterial = world.getBlockAt(x, y + 1, z).getType();

            if (groundMaterial.isSolid() && headMaterial == Material.AIR) {
                return location;
            }
        }

        return null;
    }
}
