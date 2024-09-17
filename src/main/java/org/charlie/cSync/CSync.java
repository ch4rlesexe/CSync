package org.charlie.cSync;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class CSync extends JavaPlugin implements TabExecutor, Listener {

    private static CSync instance;
    private FileConfiguration messagesConfig;
    private File verifiedFile;
    private FileConfiguration verifiedConfig;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        saveDefaultMessages();
        loadVerifiedData();
        this.getCommand("verify").setExecutor(new VerifyCommand());
        this.getCommand("csync").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerRestrictListener(), this);
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

    public FileConfiguration getVerifiedConfig() {
        return verifiedConfig;
    }

    private void saveDefaultMessages() {
        File messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    private void loadVerifiedData() {
        verifiedFile = new File(getDataFolder(), "verified.yml");
        if (!verifiedFile.exists()) {
            try {
                verifiedFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        verifiedConfig = YamlConfiguration.loadConfiguration(verifiedFile);
    }

    public void saveVerifiedData() {
        try {
            verifiedConfig.save(verifiedFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reloadConfigs() {
        reloadConfig();
        saveDefaultMessages();
        loadVerifiedData();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            reloadConfigs();
            String successMessage = translateColorCodes(getMessagesConfig().getString("messages.reload_success", "&aCSync configuration reloaded successfully!"));
            sender.sendMessage(successMessage);
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("setworld")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                String worldName = args[1];
                World world = Bukkit.getWorld(worldName);
                if (world != null) {
                    getConfig().set("teleport_world", worldName);
                    saveConfig();
                    player.sendMessage(translateColorCodes(getMessagesConfig().getString("messages.world_set", "&aWorld set to: {world}").replace("{world}", worldName)));
                } else {
                    player.sendMessage(translateColorCodes(getMessagesConfig().getString("messages.world_not_found", "&cWorld not found: {world}").replace("{world}", worldName)));
                }
            } else {
                sender.sendMessage(translateColorCodes(getMessagesConfig().getString("messages.console_only", "&cOnly players can use this command.")));
            }
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("unverify")) {
            String targetPlayer = args[1];
            if (verifiedConfig.contains(targetPlayer)) {
                verifiedConfig.set(targetPlayer, null);
                saveVerifiedData();
                sender.sendMessage(translateColorCodes(getMessagesConfig().getString("messages.unverify_success", "&aSuccessfully unverified {player}.").replace("{player}", targetPlayer)));

                List<String> unverifyCommands = getConfig().getStringList("unverify_commands");
                for (String cmd : unverifyCommands) {
                    String formattedCommand = cmd.replace("{player}", targetPlayer);
                    DiscordWebhookSender.sendMessage(formattedCommand);
                }
            } else {
                sender.sendMessage(translateColorCodes(getMessagesConfig().getString("messages.player_not_found", "&cPlayer not found in the verified list.")));
            }
            return true;
        }

        sender.sendMessage(translateColorCodes(getMessagesConfig().getString("messages.command_usage", "&eUsage: /csync reload | /csync setworld <world> | /csync unverify <username>")));
        return false;
    }

    public String translateColorCodes(String message) {
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
