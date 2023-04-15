package me.bteuk.network.utils;

import me.bteuk.network.Network;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static me.bteuk.network.utils.Constants.LOGGER;
import static me.bteuk.network.utils.Constants.SERVER_NAME;

public class SwitchServer {

    public static void switchServer(Player p, String server) {

        NetworkUser u = Network.getInstance().getUser(p);

        //If u is null, cancel.
        if (u == null) {
            LOGGER.severe("User " + p.getName() + " can not be found!");
            p.sendMessage(Utils.error("User can not be found, please relog!"));
            return;
        }

        //If server is null, cancel and notify player.
        if (server == null) {
            p.sendMessage(Utils.error("An error occured, server does not exist."));
            Network.getInstance().getLogger().warning("Player attempting to switch to non-existing server.");

            //Remove any join events that the player may have.
            Network.getInstance().globalSQL.update("DELETE FROM join_events WHERE uuid='" + p.getUniqueId() + "';");
            return;
        }

        //Check if server exists and is online.
        if (!Network.getInstance().globalSQL.hasRow("SELECT name FROM server_data WHERE name='" + server + "';")) {
            p.sendMessage(Utils.error("The server " + server + " does not exist."));

            //Remove any join events that the player may have.
            Network.getInstance().globalSQL.update("DELETE FROM join_events WHERE uuid='" + p.getUniqueId() + "';");
            return;
        } else if (Network.getInstance().globalSQL.hasRow("SELECT online FROM server_data WHERE name='" + server + "' AND online=0;")) {
            p.sendMessage(Utils.error("The server " + server + " is currently offline."));

            //Remove any join events that the player may have.
            Network.getInstance().globalSQL.update("DELETE FROM join_events WHERE uuid='" + p.getUniqueId() + "';");
            return;
        }

        //Set switching to true in user.
        u.switching = true;

        //Add switch server instance in database.
        Network.getInstance().globalSQL.update("INSERT INTO server_switch(uuid,from_server,to_server,switch_time) VALUES('" +
                p.getUniqueId() + "','" + SERVER_NAME + "','" + server + "'," + Time.currentTime() + ");");

        //Switch Server
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(stream);
            out.writeUTF("Connect");
            out.writeUTF(server);
            u.player.sendPluginMessage(Network.getInstance(), "BungeeCord", stream.toByteArray());
        } catch (IOException e) {
            LOGGER.severe("IOException when attempting to switch player to another server.");
            return;
        }

        //Set delayed check to see whether the player is still on this server or not.
        //If they are cancel the server switch.
        //Delay is 3 seconds.
        Bukkit.getScheduler().scheduleSyncDelayedTask(Network.getInstance(), () -> {

            //Check for ping within the last 2 seconds and with a connection to this server.
            if (Network.getInstance().globalSQL.hasRow("SELECT uuid FROM online_users WHERE last_ping > " + (Time.currentTime() - 2000) + " AND server='" + SERVER_NAME + "';")) {

                //Delete server switch.
                Network.getInstance().globalSQL.update("DELETE FROM server_switch WHERE uuid='" + p.getUniqueId() + "';");

            }
        }, 60L);
    }
}
