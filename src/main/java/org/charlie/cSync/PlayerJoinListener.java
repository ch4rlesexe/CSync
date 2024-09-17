package org.charlie.cSync;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();

        if (!CSync.getInstance().getConfig().getBoolean("enable_world_lock", false)) {
            return;
        }

        if (CSync.getInstance().getVerifiedConfig().contains(playerName)) {
            String alreadyVerifiedMessage = CSync.getInstance().translateColorCodes(
                    CSync.getInstance().getMessagesConfig().getString("messages.already_verified", "&aAccount Verified &7(%player_name%)\n&7&oIf you would like to reverify, please contact a staff member.").replace("%player_name%", playerName)
            );
            player.kickPlayer(alreadyVerifiedMessage);
            return;
        }

        Bukkit.getLogger().info("PlayerJoinEvent triggered for: " + playerName);

        String worldName = CSync.getInstance().getConfig().getString("teleport_world");
        World world = Bukkit.getWorld(worldName);

        if (world != null) {
            Location teleportLocation = world.getSpawnLocation();
            player.teleport(teleportLocation);
            player.setGameMode(GameMode.ADVENTURE);
            player.setAllowFlight(true);
            player.setFlying(true);

            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 1));

            player.sendMessage(CSync.getInstance().translateColorCodes(
                    CSync.getInstance().getMessagesConfig().getString("messages.please_verify", "&ePlease verify using &b/verify (code)")
            ));

            player.sendTitle(
                    CSync.getInstance().translateColorCodes(CSync.getInstance().getMessagesConfig().getString("messages.title", "&ePlease Verify")), CSync.getInstance().translateColorCodes(CSync.getInstance().getMessagesConfig().getString("messages.subtitle", "&bUse /verify <code>")), 10, 6000, 10);

            Bukkit.getLogger().info("Sent title to player: " + playerName);

            new BukkitRunnable() {
                int timeLeft = 120;

                @Override
                public void run() {
                    if (timeLeft <= 0) {
                        if (player.isOnline()) {
                            String kickMessage = CSync.getInstance().translateColorCodes(
                                    CSync.getInstance().getMessagesConfig().getString("messages.kick_after_time", "You are not allowed to be on the server for more than 2 minutes.")
                            );
                            player.kickPlayer(kickMessage);
                        }
                        cancel();
                    } else {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
                                CSync.getInstance().translateColorCodes(
                                        CSync.getInstance().getMessagesConfig().getString("messages.time_left", "&cTime left: &e{time} seconds")
                                                .replace("{time}", String.valueOf(timeLeft))
                                )
                        ));
                        timeLeft--;
                    }
                }
            }.runTaskTimer(CSync.getInstance(), 0L, 20L);
        } else {
            player.sendMessage(CSync.getInstance().translateColorCodes("&cTeleport world not set or does not exist."));
        }
    }
}

