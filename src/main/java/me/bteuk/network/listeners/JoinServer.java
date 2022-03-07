package me.bteuk.network.listeners;

import me.bteuk.network.Network;
import me.bteuk.network.utils.NetworkUser;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinServer implements Listener {

    Network instance;

    public JoinServer(Network instance) {

        this.instance = instance;
        Bukkit.getServer().getPluginManager().registerEvents(this, instance);

    }

    @EventHandler
    public void joinServerEvent(PlayerJoinEvent e) {

        //Remove user from list.
        instance.addUser(new NetworkUser(e.getPlayer()));

    }
}
