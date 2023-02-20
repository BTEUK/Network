package me.bteuk.network.listeners;

import me.bteuk.network.Network;
import me.bteuk.network.staff.Moderation;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class PreJoinServer implements Listener {

    private final Moderation moderation;

    public PreJoinServer(Network instance) {

        moderation = new Moderation();

        Bukkit.getServer().getPluginManager().registerEvents(this, instance);
    }

    @EventHandler
    public void preJoin(AsyncPlayerPreLoginEvent e) {
        //If player is banned, stop them from logging in.
        if (moderation.isBanned(e.getUniqueId().toString())) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, Component.text("You are banned for " +
                    moderation.getBannedReason(e.getUniqueId().toString()) + " until " +
                    moderation.getBanDuration(e.getUniqueId().toString())));
        }

        //Check if server is restarting.
        if (Network.getInstance().allow_shutdown) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Component.text("The server is restarting!"));
        }
    }
}