package me.bteuk.network.listeners;

import me.bteuk.network.Main;
import me.bteuk.network.utils.User;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinServer implements Listener {

    Main instance;

    public JoinServer(Main instance) {

        this.instance = instance;
        Bukkit.getServer().getPluginManager().registerEvents(this, instance);

    }

    public void joinServerEvent(PlayerJoinEvent e) {

        //Remove user from list.
        instance.addUser(new User(e.getPlayer()));

    }
}
