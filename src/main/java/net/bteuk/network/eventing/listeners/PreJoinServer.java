package net.bteuk.network.eventing.listeners;

import net.bteuk.network.Network;
import net.bteuk.network.exceptions.NotBannedException;
import net.bteuk.network.utils.staff.Moderation;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class PreJoinServer extends Moderation implements Listener {

    public PreJoinServer(Network instance) {

        Bukkit.getServer().getPluginManager().registerEvents(this, instance);
    }

    @EventHandler
    public void preJoin(AsyncPlayerPreLoginEvent e) {
        //If player is banned, stop them from logging in.
        if (isBanned(e.getUniqueId().toString())) {
            try {
                e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, getBannedComponent(e.getUniqueId().toString()));
            } catch (NotBannedException ex) {
                ex.printStackTrace();
            }
        }

        //Check if server is restarting.
        if (Network.getInstance().allow_shutdown) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Component.text("The server is restarting!"));
        }
    }
}
