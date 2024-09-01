package org.charlie.cSync;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class CSync extends JavaPlugin implements TabExecutor {

    private static CSync instance;
    private FileConfiguration messagesConfig;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        saveDefaultMessages();
        this.getCommand("verify").setExecutor(new VerifyCommand());
        this.getCommand("csync").setExecutor(this);
        getLogger().info("CSync has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("CSync has been disabled!");
    }

    public static CSync getInstance() {
        return instance;
    }

    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }

    private void saveDefaultMessages() {
        File messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public void reloadConfigs() {
        reloadConfig();
        saveDefaultMessages();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            reloadConfigs();
            String successMessage = translateColorCodes(getMessagesConfig().getString("messages.reload_success", "&aCSync configuration reloaded successfully!"));
            sender.sendMessage(successMessage);
            return true;
        }
        sender.sendMessage(translateColorCodes(getMessagesConfig().getString("messages.reload_usage", "&eUsage: /csync reload")));
        return false;
    }

    private String translateColorCodes(String message) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', message);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Collections.singletonList("reload");
        }
        return Collections.emptyList();
    }
}
