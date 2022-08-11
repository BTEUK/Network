package me.bteuk.network.listeners;

import me.bteuk.network.Network;
import me.bteuk.network.events.EventManager;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Time;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinServer implements Listener {

    Network instance;
    Connect connect;

    GlobalSQL globalSQL;

    public JoinServer(Network instance, GlobalSQL globalSQL, Connect connect) {

        this.instance = instance;
        this.connect = connect;
        this.globalSQL = globalSQL;
        Bukkit.getServer().getPluginManager().registerEvents(this, instance);

    }

    @EventHandler
    public void joinServerEvent(PlayerJoinEvent e) {

        //Cancel default join message to null.
        e.setJoinMessage(null);

        //If the player is not in the online_users table add them, and run a network connect.
        //If they are update the server.
        if (globalSQL.hasRow("SELECT uuid FROM online_users WHERE uuid='" + e.getPlayer().getUniqueId() + "';")) {

            //Update server.
            globalSQL.update("UPDATE online_users SET server='" + Network.SERVER_NAME + "' WHERE uuid='" + e.getPlayer().getUniqueId() + "';");

            //Remove their server_switch entry.
            globalSQL.update("DELETE FROM server_switch WHERE uuid='" + e.getPlayer().getUniqueId() + "';");

        } else {

            //Add user to table and run network connect.
            globalSQL.update("INSERT INTO online_users(uuid,join_time,last_ping,server) VALUES('" + e.getPlayer().getUniqueId() +
                    "'," + Time.currentTime() + "," + Time.currentTime() + ",'" + Network.SERVER_NAME + "');");
            connect.joinEvent(e.getPlayer());
        }

        //Add user to the list.
        NetworkUser u = new NetworkUser(e.getPlayer());
        instance.addUser(u);

        //Check if the player has any join events, if try run them.
        if (globalSQL.hasRow("SELECT uuid FROM join_events WHERE uuid='" + u.player.getUniqueId() + "' AND type='network';")) {

            //Get the event from the database.
            String event = globalSQL.getString("SELECT event FROM join_events WHERE uuid='" + u.player.getUniqueId() + "' AND type='network'");

            //Split the event by word.
            String[] aEvent = event.split(" ");

            //Send the event to the event handler.
            EventManager.event(u.player.getUniqueId().toString(), aEvent);

            //Clear the events.
            globalSQL.update("DELETE FROM join_events WHERE uuid='" + u.player.getUniqueId() + "' AND type='network';");

        }
    }
}
