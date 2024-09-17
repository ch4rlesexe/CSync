package org.charlie.cSync;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

public class PlayerRestrictListener implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!CSync.getInstance().getConfig().getBoolean("enable_world_lock", false)) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!CSync.getInstance().getConfig().getBoolean("enable_world_lock", false)) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!CSync.getInstance().getConfig().getBoolean("enable_world_lock", false)) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (!CSync.getInstance().getConfig().getBoolean("enable_world_lock", false)) {
            return;
        }

        String message = event.getMessage().toLowerCase();
        if (!message.startsWith("/verify")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(CSync.getInstance().translateColorCodes("&cYou can only use the /verify command."));
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (!CSync.getInstance().getConfig().getBoolean("enable_world_lock", false)) {
            return;
        }
        event.setCancelled(true);
    }
}
