package me.bteuk.network.events;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.bteuk.network.Network;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Statistics;
import me.bteuk.network.utils.Time;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.util.ArrayList;

public class CommandPreProcess implements Listener {

    Network instance;

    public CommandPreProcess(Network instance) {
        this.instance = instance;
        instance.allow_shutdown = false;
        Bukkit.getServer().getPluginManager().registerEvents(this, instance);
    }

    @EventHandler
    public void onCommandPreProcess(PlayerCommandPreprocessEvent e) {
        //Replace /region with /network:region
        if (e.getMessage().startsWith("/region")) {
            e.setMessage(e.getMessage().replace("/region", "/network:region"));
        }

        //If player is afk, unset it.
        //Reset last logged time.
        NetworkUser u = instance.getUser(e.getPlayer());
        if (u.afk) {
            u.last_time_log = u.last_movement = Time.currentTime();
            u.afk = false;
            Network.getInstance().chat.broadcastMessage("&7" + u.player.getName() + " is no longer afk.", "uknet:globalchat");
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
                Bukkit.getScheduler().scheduleSyncDelayedTask(instance, () -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "stop"),60L);
            }
        }
    }

    //This class executes when the server closes, instead of a player server quit event since that will cause errors.
    //It for the most part copies the methods.
    public void onServerClose(ArrayList<NetworkUser> users) {

        //Disable server in server table.
        instance.globalSQL.update("UPDATE server_data SET online=0 WHERE name='" + Network.SERVER_NAME + "';");

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

            if (server != null) {

                //Reset last logged time.
                if (u.afk) {
                    u.last_time_log = u.last_movement = Time.currentTime();
                    u.afk = false;
                    Network.getInstance().chat.broadcastMessage("&7" + u.player.getName() + " is no longer afk.", "uknet:globalchat");
                }

                //Switch the player to that server.
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("Connect");
                out.writeUTF(server);
                u.player.sendPluginMessage(instance, "BungeeCord", out.toByteArray());

            } else {

                //Reset last logged time.
                if (u.afk) {
                    u.last_time_log = u.last_movement = Time.currentTime();
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
