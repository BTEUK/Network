package me.bteuk.network.listeners;

import me.bteuk.network.Main;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class LeaveServer implements Listener {

    Main instance;

    public LeaveServer(Main instance) {

        this.instance = instance;
        Bukkit.getServer().getPluginManager().registerEvents(this, instance);

    }

    public void leaveServerEvent(PlayerQuitEvent e) {

        //Remove user from list.
        instance.removeUser(instance.getUser(e.getPlayer()));

    }

}
