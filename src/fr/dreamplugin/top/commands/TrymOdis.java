package fr.dreamplugin.top.commands;

import fr.dreamplugin.top.TrymPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.io.File;

public class TrymOdis implements CommandExecutor {

    private static final String PERMISSION_RELOAD = "TrymPlugin.reload";
    private static final String PERMISSION_VERSION = "TrymPlugin.version";
    private static final String PERMISSION_RESET = "TrymPlugin.reset";
    private static final String PERMISSION_SUPPORT = "TrymPlugin.support";

    private final TrymPlugin plugin;

    public TrymOdis(TrymPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                return handleReload(sender);
            case "version":
                return handleVersion(sender);
            case "reset":
                return handleReset(sender, args);
            case "support":  // Nouveau cas pour la commande support
                return handleSupport(sender);
            default:
                sendHelpMessage(sender);
                return false;
        }
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage("§e------ §a§lTrymOdis Commands §e------");
        sender.sendMessage(formatHelpLine("reload", plugin.getLangMessage("messages.help_message.reload")));
        sender.sendMessage(formatHelpLine("version", plugin.getLangMessage("messages.help_message.version")));
        // sender.sendMessage(formatHelpLine("editor", plugin.getLangMessage("messages.help_message.editor")));
        sender.sendMessage(formatHelpLine("reset <code>", plugin.getLangMessage("messages.help_message.reset")));
        sender.sendMessage(formatHelpLine("support", plugin.getLangMessage("messages.help_message.support"))); // Ligne d'aide pour la commande support
    }

    private String formatHelpLine(String command, String description) {
        return " §6/§etrym " + command + " §8§l-§7 " + description;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission(PERMISSION_RELOAD)) {
            sendNoPermissionMessage(sender, PERMISSION_RELOAD);
            return false;
        }

        if (!reloadAndRegenerateConfig()) {
            sender.sendMessage(ChatColor.RED + plugin.getLangMessage("messages.reload_failed"));
            return false;
        }

        plugin.loadLangFile(plugin.getConfig().getString("language"));
        if (!plugin.createUserDataFolder()) {plugin.getLogger().severe("t");};

        sender.sendMessage(ChatColor.GREEN + plugin.getLangMessage("messages.reload_success")
                .replace("{version}", plugin.getDescription().getVersion()));
        return true;
    }

    private boolean reloadAndRegenerateConfig() {
        try {
            File configFile = new File(plugin.getDataFolder(), "config.yml");
            if (!configFile.exists()) {
                plugin.saveDefaultConfig();
            }
            plugin.loadConfigs();
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Une erreur est survenue lors du rechargement de config.yml : " + e.getMessage());
            return false;
        }
    }

    private boolean handleVersion(CommandSender sender) {
        if (!sender.hasPermission(PERMISSION_VERSION)) {
            sendNoPermissionMessage(sender, PERMISSION_VERSION);
            return false;
        }

        String versionMessage = plugin.getLangMessage("messages.version_message")
                .replace("{version}", plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', versionMessage));
        return true;
    }

    private boolean handleReset(CommandSender sender, String[] args) {
        if (!sender.hasPermission(PERMISSION_RESET)) {
            sendNoPermissionMessage(sender, PERMISSION_RESET);
            return false;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + plugin.getLangMessage("messages.reset_missing_code"));
            return false;
        }

        String code = args[1];
        // Vérification de l'existence du code
        String resetCode = plugin.getConfig().getString("resetCode");
        if (resetCode == null || !resetCode.equals(code)) {
            sender.sendMessage(ChatColor.RED + plugin.getLangMessage("messages.invalid_reset_code"));
            return false;
        }

        // Effectuer le réinitialisation ici
        sender.sendMessage(ChatColor.GREEN + plugin.getLangMessage("messages.reset_success"));
        return true;
    }

    private boolean handleSupport(CommandSender sender) {
        if (!sender.hasPermission(PERMISSION_SUPPORT)) {
            sendNoPermissionMessage(sender, PERMISSION_SUPPORT);
            return false;
        }

        // Message de support
        String supportMessage = plugin.getLangMessage("messages.support_message")
                .replace("{support_code}", plugin.getConfig().getString("supportCode"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', supportMessage));
        return true;
    }

    private void sendNoPermissionMessage(CommandSender sender, String permission) {
        sender.sendMessage(ChatColor.RED + plugin.getLangMessage("messages.no_permission")
                .replace("{permission}", permission));
    }
}
