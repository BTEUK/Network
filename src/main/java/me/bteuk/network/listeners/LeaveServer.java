package me.bteuk.network.listeners;

import me.bteuk.network.Network;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Statistics;
import me.bteuk.network.utils.Time;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class LeaveServer implements Listener {

    Network instance;
    Connect connect;

    GlobalSQL globalSQL;

    private boolean blocked;

    public LeaveServer(Network instance, GlobalSQL globalSQL, Connect connect) {

        this.instance = instance;
        this.connect = connect;
        this.globalSQL = globalSQL;
        Bukkit.getServer().getPluginManager().registerEvents(this, instance);

        blocked = false;

    }

    public void block() {
        blocked = true;
    }

    @EventHandler
    public void leaveServerEvent(PlayerQuitEvent e) {

        //Set default leave message to null.
        e.setQuitMessage(null);

        if (blocked) {
            return;
        }

        NetworkUser u = instance.getUser(e.getPlayer());

        //Reset last logged time.
        if (u.afk) {
            u.last_time_log = u.last_movement = Time.currentTime();
            u.afk = false;
        }

        //Update statistics
        long time = Time.currentTime();
        Statistics.save(u, Time.getDate(time), time);

        //Remove user from list.
        instance.removeUser(u);

        //Get player uuid.
        UUID playerUUID = u.player.getUniqueId();

        //If they are currently in an inventory, remove them from the list of open inventories.
        Gui.openInventories.remove(playerUUID);

        //Delete any guis that may exist.
        if (u.mainGui != null) {
            u.mainGui.delete();
        }
        if (u.staffGui != null) {
            u.staffGui.delete();
        }
        if (u.lightsOut != null) {
            u.lightsOut.delete();
        }

        //If the player is not in the server_switch table they have disconnected from the network.
        if (!globalSQL.hasRow("SELECT uuid FROM server_switch WHERE uuid='" + e.getPlayer().getUniqueId()
                + "' AND from_server='" + instance.SERVER_NAME + "';")) {

            //Run leave network sequence.
            connect.leaveEvent(e.getPlayer());

        }

    }
}
