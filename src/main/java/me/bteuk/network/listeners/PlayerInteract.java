package me.bteuk.network.listeners;

import me.bteuk.network.Network;
import me.bteuk.network.gui.NavigatorGui;
import me.bteuk.network.utils.NetworkUser;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInteract implements Listener {

    Network instance;

    public PlayerInteract(Network instance) {

        this.instance = instance;
        Bukkit.getServer().getPluginManager().registerEvents(this, instance);

    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {

        NetworkUser u = instance.getUser(e.getPlayer());

        //Open navigator.
        //If the player has a previous gui, open that.
        if (u.uniqueGui != null) {

            u.uniqueGui.open(u);

        } else {

            //Open the navigator.
            u.uniqueGui = NavigatorGui.createNavigator();
            u.uniqueGui.open(u);

        }

    }
}
