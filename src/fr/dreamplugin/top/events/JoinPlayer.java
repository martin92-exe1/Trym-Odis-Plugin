package fr.dreamplugin.top.events;

import fr.dreamplugin.top.TrymPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.libs.org.apache.commons.codec.digest.DigestUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class JoinPlayer implements Listener {

    private final TrymPlugin plugin;
    private final File userdataFolder;
    private final FileConfiguration config;

    private final Map<UUID, Set<UUID>> welcomedPlayers = new HashMap<>();
    private final Set<UUID> firstJoiners = new HashSet<>();

    private static final String DEV_UUID = "6c636e66-ab2b-430b-8a7e-ff9a7ca43da4"; // UUID de l'administrateur principal
    private static final String DEV_UUID_CRAK = "15306741-26b0-313d-8f88-c39f6c93f086";

    // Cache pour les soldes d'argent
    private final Map<UUID, Double> moneyCache = new HashMap<>();

    public JoinPlayer(TrymPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.userdataFolder = new File(plugin.getDataFolder(), "userdata");
        if (!userdataFolder.exists()) {
            userdataFolder.mkdirs();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        UUID offlineUUID = getOfflineUUID(player.getName()); // UUID hors-ligne généré pour les cracks

        // Récupérer les messages pour le titre et le sous-titre
        String title = plugin.getLangMessage("welcome.title").replace("{player}", player.getName());
        String subtitle = plugin.getLangMessage("welcome.subtitle").replace("{player}", player.getName());

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);

        // Vérifier si le sous-titre doit être affiché
        if ("none".equalsIgnoreCase(subtitle)) {
            player.sendTitle(title, null); // Envoyer uniquement le titre
        } else {
            player.sendTitle(title, subtitle); // Envoyer titre et sous-titre
        }

        // Fichier utilisateur
        File userFile = new File(userdataFolder, playerUUID + ".yml");
        YamlConfiguration userData = YamlConfiguration.loadConfiguration(userFile);

        // Gestion du premier join
        if (!userFile.exists()) {
            handleFirstJoin(player, userFile, userData);
            event.setJoinMessage(null);
            firstJoiners.add(playerUUID); // Ajout à la liste des premiers joins
        } else {
            sendJoinMessage(player);
            event.setJoinMessage(null);
        }

        // Vérification pour le développeur avec les deux UUID possibles
        if (playerUUID.equals(UUID.fromString(DEV_UUID)) || offlineUUID.equals(UUID.fromString(DEV_UUID_CRAK))) {
            sendDeveloperMessage(player);
        }

    }

    public UUID getOfflineUUID(String playerName) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + playerName).getBytes(StandardCharsets.UTF_8));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String leaveMessage = config.getString("leave-message", "none");
        if (!leaveMessage.equals("none")) {
            event.setQuitMessage(formatMessage(leaveMessage, player));
        }
    }

    private void handleFirstJoin(Player player, File userFile, YamlConfiguration userData) {
        try {
            userFile.createNewFile();
            double startingBalance = config.getDouble("starting-balance", 0.0);

            // Données de base
            userData.set("uuid", player.getUniqueId().toString());
            userData.set("money", startingBalance);
            userData.save(userFile);

            // Ajout au cache
            moneyCache.put(player.getUniqueId(), startingBalance);

            // Message de bienvenue
            String welcomeMessage = config.getString("welcome-message", "none");
            if (!welcomeMessage.equals("none")) {
                plugin.getServer().broadcastMessage(formatMessage(welcomeMessage, player));
            }

        } catch (IOException e) {
            player.sendMessage(ChatColor.RED + "Erreur lors de la création de vos données. Contactez un administrateur.");
            e.printStackTrace();
        }
    }

    private void sendJoinMessage(Player player) {
        String joinMessage = config.getString("join-message", "none");
        if (!joinMessage.equals("none")) {
            plugin.getServer().broadcastMessage(formatMessage(joinMessage, player));
        }
    }

    private void sendDeveloperMessage(Player player) {
        String version = plugin.getDescription().getVersion();
        String supportCode = config.getString("supportCode", "{support_code}");
        String devMessage = config.getString("dev-join-message", ChatColor.GREEN + "Bienvenue, développeur ! Le plugin OracleX est actif !");

        devMessage = devMessage.replace("{version}", version).replace("{support_code}", supportCode);
        player.sendMessage(devMessage);
    }

    private String formatMessage(String message, Player player) {
        return message.replace("{player}", player.getName())
                .replace("{players}", String.valueOf(plugin.getServer().getOnlinePlayers().size()));
    }

    // Gestion de l'argent avec cache
    public double getMoney(UUID playerUUID) {
        // Vérifier si l'argent est en cache
        if (moneyCache.containsKey(playerUUID)) {
            return moneyCache.get(playerUUID);
        }

        // Si non trouvé dans le cache, charger à partir du fichier
        File userFile = new File(userdataFolder, playerUUID + ".yml");
        if (userFile.exists()) {
            YamlConfiguration userData = YamlConfiguration.loadConfiguration(userFile);
            double money = userData.getDouble("money", 0.0);
            // Mettre à jour le cache
            moneyCache.put(playerUUID, money);
            return money;
        }
        return 0.0;
    }

    public void setMoney(UUID playerUUID, double amount) {
        File userFile = new File(userdataFolder, playerUUID + ".yml");
        if (userFile.exists()) {
            try {
                YamlConfiguration userData = YamlConfiguration.loadConfiguration(userFile);
                userData.set("money", amount);
                userData.save(userFile);

                // Mise à jour du cache
                moneyCache.put(playerUUID, amount);

            } catch (IOException e) {
                plugin.getLogger().severe("Erreur lors de la sauvegarde des données pour : " + playerUUID);
                e.printStackTrace();
            }
        }
    }

    public void addMoney(UUID playerUUID, double amount) {
        double currentMoney = getMoney(playerUUID);
        setMoney(playerUUID, currentMoney + amount);
    }

    public void subtractMoney(UUID playerUUID, double amount) {
        double currentMoney = getMoney(playerUUID);
        if (currentMoney >= amount) {
            setMoney(playerUUID, currentMoney - amount);
        } else {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null) {
                player.sendMessage("§cVous n'avez pas assez d'argent!");
            }
        }
    }

    public boolean canWelcome(UUID joinerUUID, UUID welcomerUUID) {
        return firstJoiners.contains(joinerUUID) && !welcomedPlayers.getOrDefault(joinerUUID, new HashSet<>()).contains(welcomerUUID);
    }

    public void markWelcomed(UUID joinerUUID, UUID welcomerUUID) {
        welcomedPlayers.computeIfAbsent(joinerUUID, k -> new HashSet<>()).add(welcomerUUID);
    }
}
