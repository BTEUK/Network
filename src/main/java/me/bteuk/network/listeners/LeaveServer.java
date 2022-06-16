package me.bteuk.network.listeners;

import me.bteuk.network.Network;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.utils.NetworkUser;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class LeaveServer implements Listener {

    Network instance;
    Connect connect;

    GlobalSQL globalSQL;

    public LeaveServer(Network instance, GlobalSQL globalSQL, Connect connect) {

        this.instance = instance;
        this.connect = connect;
        this.globalSQL = globalSQL;
        Bukkit.getServer().getPluginManager().registerEvents(this, instance);

    }

    @EventHandler
    public void leaveServerEvent(PlayerQuitEvent e) {

        //Set default leave message to null.
        e.setQuitMessage(null);

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

        //If the player is not in the server_switch table they have disconnected from the network.
        if (!globalSQL.hasRow("SELECT uuid FROM server_switch WHERE uuid=" + e.getPlayer().getUniqueId()
                + " AND from_server='" + instance.SERVER_NAME + "';")) {

            //Run leave network sequence.
            connect.leaveEvent(e.getPlayer().getUniqueId().toString());

        }

    }
}
