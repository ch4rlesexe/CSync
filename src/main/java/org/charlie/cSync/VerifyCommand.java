package org.charlie.cSync;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VerifyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(translateColorCodes(CSync.getInstance().getMessagesConfig().getString("messages.console_only", "This command can only be run by a player.")));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(translateColorCodes(CSync.getInstance().getMessagesConfig().getString("messages.usage", "Usage: /verify <code>")));
            return true;
        }

        Player player = (Player) sender;
        String code = args[0];

        Bukkit.getScheduler().runTaskAsynchronously(CSync.getInstance(), () -> {
            Map<String, String> verificationData = fetchVerificationData();

            if (verificationData.containsKey(code)) {
                String discordId = verificationData.get(code);
                String uuid = player.getUniqueId().toString();

                boolean includeDashes = CSync.getInstance().getConfig().getBoolean("uuid_include_dashes", true);
                if (!includeDashes) {
                    uuid = uuid.replace("-", "");
                }

                sendDiscordMessages(discordId, uuid, player.getName());
                String successMessage = translateColorCodes(CSync.getInstance().getMessagesConfig().getString("messages.verify_success", "&aVerification successful!"));
                player.sendMessage(successMessage);
            } else {
                player.sendMessage(translateColorCodes(CSync.getInstance().getMessagesConfig().getString("messages.invalid_auth", "Invalid Auth Code")));
            }
        });

        return true;
    }

    private Map<String, String> fetchVerificationData() {
        Map<String, String> verificationData = new HashMap<>();
        try {
            String url = CSync.getInstance().getConfig().getString("verification_url");

            if (url == null || url.isEmpty()) {
                Bukkit.getLogger().warning("Verification URL is not set in the config.yml!");
                return verificationData;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
            String line;
            while ((line = in.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    verificationData.put(parts[0], parts[1]);
                }
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return verificationData;
    }

    private void sendDiscordMessages(String discordId, String uuid, String ign) {
        List<String> messages = CSync.getInstance().getConfig().getStringList("discord_messages");

        for (String messageTemplate : messages) {
            String message = messageTemplate
                    .replace("<discord_id>", discordId)
                    .replace("<uuid>", uuid)
                    .replace("<ign>", ign);

            DiscordWebhookSender.sendMessage(message);
        }
    }

    private String translateColorCodes(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
