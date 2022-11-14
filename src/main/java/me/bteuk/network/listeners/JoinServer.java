package me.bteuk.network.listeners;

import me.bteuk.network.Network;
import me.bteuk.network.commands.Nightvision;
import me.bteuk.network.events.EventManager;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Time;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.enums.ServerType;
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

            //Remove their server_switch entry. Delayed by 1 second to make sure the previous server has run their PlayerQuitEvent.
            Bukkit.getScheduler().scheduleSyncDelayedTask(instance, () -> globalSQL.update("DELETE FROM server_switch WHERE uuid='" + e.getPlayer().getUniqueId() + "';"), 20L);

            //Update the last_ping.
            globalSQL.update("UPDATE online_users SET last_ping=" + Time.currentTime() + " WHERE uuid='" + e.getPlayer().getUniqueId() + "' AND server='" + Network.SERVER_NAME + "';");

        } else {

            //Add user to table and run network connect.
            globalSQL.update("INSERT INTO online_users(uuid,join_time,last_ping,server) VALUES('" + e.getPlayer().getUniqueId() +
                    "'," + Time.currentTime() + "," + Time.currentTime() + ",'" + Network.SERVER_NAME + "');");
            connect.joinEvent(e.getPlayer());
        }

        //Add user to the list.
        NetworkUser u = new NetworkUser(e.getPlayer());
        instance.addUser(u);

        /*
        //Check version per server type.
        //Ignore lobby, only relevant for Earth and Plot.
        int version = ProtocolLibrary.getProtocolManager().getProtocolVersion(u.player);
        TODO: Get protocol version from database.

        if (Network.SERVER_TYPE == ServerType.EARTH && version != 340) {

            //Player is not on 1.12.2, give them a warning letting them know using 1.12.2 with the BTE modpack is the most stable version.
            u.player.sendMessage(Utils.chat(Network.getInstance().getConfig().getString("version.earth")));


        } else if (Network.SERVER_TYPE == ServerType.PLOT && version < 755) {

            //Player is in a version below 1.17.1, this means they do not have full access to the blocks available in the server thus limiting their ability to build and view builds.
            u.player.sendMessage(Utils.chat(Network.getInstance().getConfig().getString("version.plot")));

        }
        */

        //Check if the player has any join events, if try run them.
        if (globalSQL.hasRow("SELECT uuid FROM join_events WHERE uuid='" + u.player.getUniqueId() + "' AND type='network';")) {

            //Get the event from the database.
            String event = globalSQL.getString("SELECT event FROM join_events WHERE uuid='" + u.player.getUniqueId() + "' AND type='network'");

            //Split the event by word.
            String[] aEvent = event.split(" ");

            //Clear the events.
            globalSQL.update("DELETE FROM join_events WHERE uuid='" + u.player.getUniqueId() + "' AND type='network';");

            //Send the event to the event handler.
            EventManager.event(u.player.getUniqueId().toString(), aEvent);

        }

        //Give the player nightvision if enabled or remove it if disabled.
        if (globalSQL.hasRow("SELECT nightvision_enabled FROM player_data WHERE nightvision_enabled=1 AND uuid='" + u.player.getUniqueId() + "';")) {

            Nightvision.giveNightvision(u.player);

        } else {

            Nightvision.removeNightvision(u.player);

        }
    }
}
