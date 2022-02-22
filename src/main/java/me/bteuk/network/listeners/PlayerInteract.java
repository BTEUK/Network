package me.bteuk.network.listeners;

import me.bteuk.network.Main;
import me.bteuk.network.utils.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInteract implements Listener {

    Main instance;

    public PlayerInteract(Main instance) {

        this.instance = instance;
        Bukkit.getServer().getPluginManager().registerEvents(this, instance);

    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {

        User u = instance.getUser(e.getPlayer());

        //Open navigator.
        instance.navigator.open(u);

    }
}
