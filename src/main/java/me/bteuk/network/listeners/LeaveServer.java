package me.bteuk.network.listeners;

import me.bteuk.network.Network;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.utils.NetworkUser;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class LeaveServer implements Listener {

    Network instance;

    public LeaveServer(Network instance) {

        this.instance = instance;
        Bukkit.getServer().getPluginManager().registerEvents(this, instance);

    }

    @EventHandler
    public void leaveServerEvent(PlayerQuitEvent e) {

        NetworkUser u = instance.getUser(e.getPlayer());

        //Remove user from list.
        instance.removeUser(u);

        //Get player uuid.
        UUID playerUUID = u.player.getUniqueId();

        //If they are currently in an inventory, remove them from the list of open inventories.
        Gui.openInventories.remove(playerUUID);

        //Remove the unique gui, if it exists.
        if (u.uniqueGui != null) {

            u.uniqueGui.delete();

        }

    }
}
