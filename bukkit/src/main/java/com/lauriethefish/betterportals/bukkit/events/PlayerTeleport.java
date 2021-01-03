package com.lauriethefish.betterportals.bukkit.events;

import com.lauriethefish.betterportals.bukkit.BetterPortals;
import com.lauriethefish.betterportals.bukkit.PlayerData;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

// Used to make sure that if a player teleports in a way where they intersected a portal on the way
// they aren't teleported to the destination of whichever portal
public class PlayerTeleport implements Listener {
    private BetterPortals pl;
    public PlayerTeleport(BetterPortals pl) {
        this.pl = pl;
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        PlayerData player = pl.getPlayerData(event.getPlayer());
        // Sometimes this event is triggered on a player who isn't actually real, but made by an NPC plugin
        // To avoid NullPointerExceptions, we return if no PlayerData was found
        if(player == null) {return;}

        // Set the player's last position to null so that they won't get teleported
        player.setLastPosition(null);
        pl.logDebug("On player teleport");
    }
}
