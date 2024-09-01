package org.charlie.cSync;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DiscordWebhookSender {

    public static void sendMessage(String message) {
        try {
            String webhookUrl = CSync.getInstance().getConfig().getString("webhook_url", "YOUR_DISCORD_WEBHOOK_URL");
            URL url = new URL(webhookUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String jsonPayload = "{\"content\": \"" + message + "\"}";

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            connection.getInputStream().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
