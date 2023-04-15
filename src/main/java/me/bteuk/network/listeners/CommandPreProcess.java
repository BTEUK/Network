package me.bteuk.network.listeners;

import me.bteuk.network.Network;
import me.bteuk.network.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import static me.bteuk.network.utils.Constants.LOGGER;
import static me.bteuk.network.utils.Constants.SERVER_NAME;

public class CommandPreProcess implements Listener {

    private final Network instance;

    public CommandPreProcess(Network instance) {
        this.instance = instance;
        instance.allow_shutdown = false;
        Bukkit.getServer().getPluginManager().registerEvents(this, instance);
    }

    @EventHandler
    public void onCommandPreProcess(PlayerCommandPreprocessEvent e) {
        //Replace /region with /network:region
        if (e.getMessage().startsWith("/region")) {
            if (Constants.REGIONS_ENABLED) {
                e.setMessage(e.getMessage().replace("/region", "/network:region"));
            } else {
                return;
            }
        } else if (e.getMessage().startsWith("/tpll")) {
            if (Constants.TPLL_ENABLED) {
                e.setMessage(e.getMessage().replace("/tpll", "/network:tpll"));
            } else {
                return;
            }
        }

        if (!e.getMessage().startsWith("/afk")) {

            //If player is afk, unset it.
            //Reset last logged time.
            NetworkUser u = instance.getUser(e.getPlayer());

            //If u is null, cancel.
            if (u == null) {
                LOGGER.severe("User " + e.getPlayer().getName() + " can not be found!");
                e.getPlayer().sendMessage(Utils.error("User can not be found, please relog!"));
                e.setCancelled(true);
                return;
            }

            u.last_movement = Time.currentTime();
            if (u.afk) {
                u.last_time_log = Time.currentTime();
                u.afk = false;
                Network.getInstance().chat.broadcastAFK(u.player, false);
            }

        }
    }

    @EventHandler
    public void serverCommand(ServerCommandEvent s) {
        if (s.getCommand().startsWith("stop")) {
            if (!instance.allow_shutdown) {
                instance.allow_shutdown = true;
                onServerClose(instance.getUsers());

                //Delay shutdown by 3 seconds to make sure players have switched server.
                s.setCancelled(true);
                Bukkit.getScheduler().scheduleSyncDelayedTask(instance, () -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "stop"), 60L);
            }
        }
    }

    //This class executes when the server closes, instead of a player server quit event since that will cause errors.
    //It for the most part copies the methods.
    public void onServerClose(ArrayList<NetworkUser> users) {

        //Disable server in server table.
        instance.globalSQL.update("UPDATE server_data SET online=0 WHERE name='" + SERVER_NAME + "';");

        //Block the LeaveServer listener so it doesn't trigger since it causes an error.
        //It needs to be active to prevent the leave message to show in chat.
        if (instance.leaveServer != null) {
            instance.leaveServer.block();
        }

        //Check if another server is online,
        //If true then switch all the players to this server.
        //Always check the lobby and earth first.
        //Remove all players from network.
        String server = null;

        //Try different servers.
        if (instance.globalSQL.hasRow("SELECT name FROM server_data WHERE type='LOBBY' AND online=1;")) {

            server = instance.globalSQL.getString("SELECT name FROM server_data WHERE type='LOBBY' AND online=1;");

        } else if (instance.globalSQL.hasRow("SELECT name FROM server_data WHERE type='EARTH' AND online=1;")) {

            server = instance.globalSQL.getString("SELECT name FROM server_data WHERE type='EARTH' AND online=1;");

        } else if (instance.globalSQL.hasRow("SELECT name FROM server_data WHERE online=1;")) {

            server = instance.globalSQL.getString("SELECT name FROM server_data WHERE online=1;");

        }

        for (NetworkUser u : users) {

            u.last_movement = Time.currentTime();
            if (server != null) {

                //Reset last logged time.
                if (u.afk) {
                    u.last_time_log = Time.currentTime();
                    u.afk = false;
                    Network.getInstance().chat.broadcastAFK(u.player, false);
                }


                //Switch the player to that server.
                try {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    DataOutputStream out = new DataOutputStream(stream);
                    out.writeUTF("Connect");
                    out.writeUTF(server);
                    u.player.sendPluginMessage(instance, "BungeeCord", stream.toByteArray());
                } catch (IOException e) {
                    LOGGER.severe("IOException when attempting to switch player to another server.");
                    return;
                }

            } else {

                //Reset last logged time.
                if (u.afk) {
                    u.last_time_log = Time.currentTime();
                    u.afk = false;
                }

                String uuid = u.player.getUniqueId().toString();

                //Remove any outstanding invites that this player has sent.
                instance.plotSQL.update("DELETE FROM plot_invites WHERE owner='" + uuid + "';");

                //Remove any outstanding invites that this player has received.
                instance.plotSQL.update("DELETE FROM plot_invites WHERE uuid='" + uuid + "';");

                //Set last_online time in playerdata.
                instance.globalSQL.update("UPDATE player_data SET last_online=" + Time.currentTime() + " WHERE UUID='" + uuid + "';");

                //Remove player from online_users.
                //Since this closes the server tab does not need to be updated for these players.
                instance.globalSQL.update("DELETE FROM online_users WHERE uuid='" + uuid + "';");

                //Log playercount in database
                instance.globalSQL.update("INSERT INTO player_count(log_time,players) VALUES(" + Time.currentTime() + "," +
                        instance.globalSQL.getInt("SELECT count(uuid) FROM online_users;") + ");");

            }

            //Update statistics
            long time = Time.currentTime();
            Statistics.save(u, Time.getDate(time), time);

        }

        //Block movement and teleport listeners.
        instance.moveListener.block();
        instance.teleportListener.block();

        //Remove users from list.
        users.clear();

    }
}