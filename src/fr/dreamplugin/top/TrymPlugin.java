package fr.dreamplugin.top;

import fr.dreamplugin.top.commands.*;
import fr.dreamplugin.top.events.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class TrymPlugin extends JavaPlugin {

    private FileConfiguration langConfig;
    public FileConfiguration extraConfig;
    private String language;

    @Override
    public void onEnable() {
        logToConsole("&eTrym Odis Plugin > &7v" + getDescription().getVersion());

        loadConfigs();
        language = extraConfig.getString("language", "en"); // Default to "en" if no language is set
        loadLangFile(language);

        generateResetCode();
        generateSupportCode();
        createUserDataFolder();

        registerEvents();
        registerCommands();

        logToConsole("&eTrymOdis &7> &7Plugin Loaded Successfully!");
    }

    @Override
    public void onDisable() {
        logToConsole("&eTrym Odis Plugin > &7Unloaded!");
    }

    private void registerEvents() {
        Bukkit.getPluginManager().registerEvents(new JoinPlayer(this), this);
        getServer().getPluginManager().registerEvents(new Invsee(this), this);
        getServer().getPluginManager().registerEvents(new PlayerChat(this), this);
    }

    private void registerCommands() {
        Messages messages = new Messages();
        getCommand("hat").setExecutor(new Cosmetics(this));
        getCommand("rtp").setExecutor(new Teleport(this));
        getCommand("trym").setExecutor(new TrymOdis(this));
        getCommand("tphere").setExecutor(new Teleport(this));
        getCommand("balance").setExecutor(new Economy(this, new JoinPlayer(this)));
        getCommand("eco").setExecutor(new Economy(this, new JoinPlayer(this)));
        getCommand("pay").setExecutor(new Economy(this, new JoinPlayer(this)));
        getCommand("welcome").setExecutor(new Welcome(this, new JoinPlayer(this)));
        getCommand("trash").setExecutor(new Trash(this));
        getCommand("msg").setExecutor(new MessageCommand(messages, this));
        getCommand("r").setExecutor(new MessageCommand(messages, this));
        getCommand("ignore").setExecutor(new MessageCommand(messages, this));
        getCommand("unignore").setExecutor(new MessageCommand(messages, this));
        getCommand("ignorelist").setExecutor(new MessageCommand(messages, this));
        getCommand("calendar").setExecutor(new Calendar(this));
        getCommand("invsee").setExecutor(new Invsee(this));
        getCommand("craft").setExecutor(new Craft(this));
        getCommand("spawn").setExecutor(new SpawnCommand(getConfig(), this));
        getCommand("setspawn").setExecutor(new SpawnCommand(getConfig(), this));
    }

    private void generateResetCode() {
        File configFile = new File(getDataFolder(), "config.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        String resetCode = config.getString("resetCode", "{code_reset}");
        if ("{code_reset}".equals(resetCode)) {
            config.set("resetCode", UUID.randomUUID().toString());
            saveConfigFile(config, configFile);
        }
    }

    private void generateSupportCode() {
        File configFile = new File(getDataFolder(), "config.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        String supportCode = config.getString("supportCode", "{support_code}");
        if ("{support_code}".equals(supportCode)) {
            String newSupportCode = UUID.randomUUID().toString();
            config.set("supportCode", newSupportCode);
            saveConfigFile(config, configFile);
            logToConsole("&f[Support] &7Support code generated: &e" + newSupportCode);
        }
    }

    private void saveConfigFile(FileConfiguration config, File file) {
        try {
            config.save(file);
        } catch (IOException e) {
            getLogger().severe("Failed to save configuration file: " + file.getName());
            e.printStackTrace();
        }
    }

    private void logToConsole(String msg) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }

    public boolean createUserDataFolder() {
        File userdataFolder = new File(getDataFolder(), "userdata");
        if (!userdataFolder.exists()) {
            try {
                if (userdataFolder.mkdirs()) {
                    getLogger().info("User data folder created successfully.");
                    return true;
                } else {
                    getLogger().warning("Failed to create user data folder.");
                }
            } catch (SecurityException e) {
                getLogger().warning("Permission error while creating the user data folder.");
            }
        }
        return false;
    }

    public void loadLangFile(String lang) {
        File langFile = new File(getDataFolder(), "langs/" + lang + ".yml");
        if (!langFile.exists()) {
            saveResource("langs/" + lang + ".yml", false);
            logToConsole("&eTrymOdis &7> &7Language file created: " + lang + ".yml");
        } else {
            logToConsole("&eTrymOdis &7> &7Loading language file: " + lang + ".yml");
        }
        langConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    public void loadConfigs() {
        File extraConfigFile = new File(getDataFolder(), "config.yml");
        if (!extraConfigFile.exists()) {
            saveResource("config.yml", false);
        }
        extraConfig = YamlConfiguration.loadConfiguration(extraConfigFile);
    }

    public String getLangMessage(String key) {
        String message = langConfig.contains(key)
                ? langConfig.getString(key)
                : langConfig.getString("notfound")
                .replace("{key}", key)
                .replace("{file}", "langs/" + language + ".yml");

        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
