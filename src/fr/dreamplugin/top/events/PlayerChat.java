package fr.dreamplugin.top.events;

import fr.dreamplugin.top.TrymPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.io.File;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.configuration.file.YamlConfiguration;
import java.util.UUID;

public class PlayerChat implements Listener {

    private final TrymPlugin plugin;

    public PlayerChat(TrymPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        // Récupère le format de message depuis la configuration
        String format = plugin.getConfig().getString("chat.format", "{name}: {message}");

        // Récupère le joueur et son message
        String playerName = event.getPlayer().getName();
        UUID playerUUID = event.getPlayer().getUniqueId();  // Récupère l'UUID du joueur
        String message = event.getMessage();

        // Accède au fichier playerdata pour obtenir les données du joueur
        File playerDataFile = new File(plugin.getDataFolder(), "userdata/" + playerUUID.toString() + ".yml");
        YamlConfiguration playerData = YamlConfiguration.loadConfiguration(playerDataFile);

        // Récupère le solde d'argent du joueur dans le fichier playerdata
        double playerMoney = playerData.getDouble("money", 0.0);

        // Récupère les mots-clés configurés dans la section "placeholders"
        Map<String, Object> keywords = plugin.getConfig().getConfigurationSection("chat.placeholders").getValues(false);

        // Remplacer les mots-clés dans le message
        for (Map.Entry<String, Object> entry : keywords.entrySet()) {
            String keyword = entry.getKey(); // Par exemple "money"
            String replacement = (String) entry.getValue(); // Assurez-vous que la valeur est une chaîne de caractères

            replacement = replacement.replace("&", "§"); // Color
            replacement = replacement.replace("{money}", String.format("%.2f", playerMoney)); // Remplace {money} par le solde

            // Utilise une regex pour détecter les mots-clés dans le message
            Pattern pattern = Pattern.compile("\\[" + Pattern.quote(keyword) + "\\]");
            Matcher matcher = pattern.matcher(message);

            // Remplace tous les mots-clés trouvés
            while (matcher.find()) {
                message = message.replace("[" + keyword + "]", replacement);
            }
        }

        // Gestion des mentions de joueurs dans le message
        message = handleMentions(event, message);

        // Formate le message final avec le nom du joueur et son message
        String formattedMessage = format.replace("{name}", playerName).replace("{message}", message);

        // Applique le format au chat
        event.setFormat(formattedMessage);
    }

    private String handleMentions(AsyncPlayerChatEvent event, String message) {
        // Recherche de mentions dans le message (ex : nomDuJoueur)
        Pattern mentionPattern = Pattern.compile("([a-zA-Z0-9_]+)");
        Matcher mentionMatcher = mentionPattern.matcher(message);

        while (mentionMatcher.find()) {
            String mentionedPlayer = mentionMatcher.group(1);
            Player mentioned = Bukkit.getPlayerExact(mentionedPlayer); // Récupère le joueur mentionné

            if (mentioned != null && mentioned.isOnline()) {
                // Envoi un titre au joueur mentionné
                mentioned.sendTitle("§e" + event.getPlayer().getName() + " vous a mentionné !", "", 10, 70, 20);

                // Envoi un son pour alerter le joueur
                mentioned.playSound(mentioned.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

                // Remplacer la mention dans le message par la version colorée
                message = message.replace(mentionedPlayer, "§c" +mentioned.getName()+ "§r");
            }
        }

        // Assurez-vous de renvoyer le message avec les mentions modifiées
        return message;
    }
}
