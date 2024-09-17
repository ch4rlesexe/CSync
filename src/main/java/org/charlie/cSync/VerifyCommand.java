package org.charlie.cSync;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class VerifyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(translateColorCodes(
                    CSync.getInstance().getMessagesConfig().getString("messages.console_only", "This command can only be run by a player.")));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(translateColorCodes(
                    CSync.getInstance().getMessagesConfig().getString("messages.usage", "Usage: /verify <code>")));
            return true;
        }

        Player player = (Player) sender;
        String code = args[0].trim();

        if (!CSync.getInstance().getConfig().getBoolean("enable_world_lock", false)) {
            if (CSync.getInstance().getVerifiedConfig().contains(player.getName())) {
                String alreadyVerifiedMessage = CSync.getInstance().translateColorCodes(
                        CSync.getInstance().getMessagesConfig().getString(
                                "messages.already_verified",
                                "&aAccount Verified &7(%player_name%)\n&7&oIf you would like to reverify, please contact a staff member."
                        ).replace("%player_name%", player.getName())
                );
                player.sendMessage(alreadyVerifiedMessage);
                return true;
            }

            Bukkit.getScheduler().runTaskAsynchronously(CSync.getInstance(), () -> {
                Map<String, String> verificationData = fetchVerificationData();

                Bukkit.getLogger().info("Fetched verification data: " + verificationData);

                if (verificationData.containsKey(code)) {
                    String discordId = verificationData.get(code);
                    String uuid = player.getUniqueId().toString();

                    boolean includeDashes = CSync.getInstance().getConfig().getBoolean("uuid_include_dashes", true);
                    if (!includeDashes) {
                        uuid = uuid.replace("-", "");
                    }

                    sendDiscordMessages(discordId, uuid, player.getName());
                    String successMessage = translateColorCodes(
                            CSync.getInstance().getMessagesConfig().getString("messages.verify_success", "&aVerification successful!"));
                    player.sendMessage(successMessage);

                    CSync.getInstance().getVerifiedConfig().set(player.getName(), true);
                    CSync.getInstance().saveVerifiedData();
                } else {
                    player.sendMessage(translateColorCodes(
                            CSync.getInstance().getMessagesConfig().getString("messages.invalid_auth", "&cInvalid Auth Code: {code}")
                                    .replace("{code}", code)
                    ));
                }
            });

            return true;
        }

        if (CSync.getInstance().getVerifiedConfig().contains(player.getName())) {
            String alreadyVerifiedMessage = CSync.getInstance().translateColorCodes(
                    CSync.getInstance().getMessagesConfig().getString("messages.already_verified", "&aAccount Verified &7(%player_name%)\n&7&oIf you would like to reverify, please contact a staff member.").replace("%player_name%", player.getName())
            );
            player.kickPlayer(alreadyVerifiedMessage);
            return true;
        }

        Bukkit.getScheduler().runTaskAsynchronously(CSync.getInstance(), () -> {
            Map<String, String> verificationData = fetchVerificationData();

            Bukkit.getLogger().info("Fetched verification data: " + verificationData);

            if (verificationData.containsKey(code)) {
                String discordId = verificationData.get(code);
                String uuid = player.getUniqueId().toString();

                boolean includeDashes = CSync.getInstance().getConfig().getBoolean("uuid_include_dashes", true);
                if (!includeDashes) {
                    uuid = uuid.replace("-", "");
                }

                sendDiscordMessages(discordId, uuid, player.getName());
                String successMessage = translateColorCodes(
                        CSync.getInstance().getMessagesConfig().getString("messages.verify_success", "&aVerification successful!"));
                player.sendMessage(successMessage);

                CSync.getInstance().getVerifiedConfig().set(player.getName(), true);
                CSync.getInstance().saveVerifiedData();

                Bukkit.getScheduler().runTask(CSync.getInstance(), () -> {
                    String alreadyVerifiedMessage = CSync.getInstance().getMessagesConfig().getString("messages.already_verified", "&aAccount Verified &7(%player_name%)\n&7&oIf you would like to reverify, please contact a staff member.");
                    alreadyVerifiedMessage = alreadyVerifiedMessage.replace("%player_name%", player.getName());
                    player.kickPlayer(translateColorCodes(alreadyVerifiedMessage));
                });
            } else {
                player.sendMessage(translateColorCodes(
                        CSync.getInstance().getMessagesConfig().getString("messages.invalid_auth", "&cInvalid Auth Code: {code}").replace("{code}", code)
                ));
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

            URL verificationUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) verificationUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Bukkit.getLogger().warning("Failed to fetch verification data. Response code: " + responseCode);
                return verificationData;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String verificationCode = parts[0].trim();
                    String discordId = parts[1].trim();
                    verificationData.put(verificationCode, discordId);
                }
            }
            in.close();

            Bukkit.getLogger().info("Number of verification codes fetched: " + verificationData.size());

        } catch (Exception e) {
            Bukkit.getLogger().severe("Error fetching verification data: " + e.getMessage());
            e.printStackTrace();
        }
        return verificationData;
    }

    private void sendDiscordMessages(String discordId, String uuid, String ign) {
        CSync plugin = CSync.getInstance();
        for (String messageTemplate : plugin.getConfig().getStringList("discord_messages")) {
            String message = messageTemplate
                    .replace("<discord_id>", discordId)
                    .replace("<uuid>", uuid)
                    .replace("<ign>", ign);

            DiscordWebhookSender.sendMessage(message);
        }
    }

    private String translateColorCodes(String message) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', message);
    }
}
