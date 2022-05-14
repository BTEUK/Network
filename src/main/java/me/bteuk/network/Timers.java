package me.bteuk.network;

import me.bteuk.network.events.EventManager;
import me.bteuk.network.listeners.Connect;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Time;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Timers {

    //Plugin
    private final Network instance;

    //Users
    private final ArrayList<NetworkUser> users;

    //Server name
    private final String SERVER_NAME;

    //SQL
    private final GlobalSQL globalSQL;

    //Network connect.
    private final Connect connect;

    //Server events
    private HashMap<String, String> events;

    //Uuids
    private ArrayList<String> uuids;

    public Timers(Network instance, GlobalSQL globalSQL, Connect connect) {

        this.instance = instance;
        this.users = instance.getUsers();

        this.globalSQL = globalSQL;

        this.connect = connect;

        SERVER_NAME = Network.SERVER_NAME;

        events = new HashMap<>();

    }

    public void startTimers() {

        //1 tick timer.
        instance.getServer().getScheduler().scheduleSyncRepeatingTask(instance, () -> {

            //Check for new server_events.
            if (globalSQL.hasRow("SELECT uuid FROM server_events WHERE server=" + SERVER_NAME + ";")) {

                //Get events for this server.
                events.clear();
                events = globalSQL.getEvents(SERVER_NAME, events);

                for (Map.Entry<String, String> entry : events.entrySet()) {

                    //Deal with events here.

                    //Split the event by word.
                    String[] aEvent = entry.getValue().split(" ");

                    //Send the event to the event handler.
                    EventManager.event(entry.getKey(), aEvent);

                }
            }
        }, 0L, 1L);

        //1 second timer.
        instance.getServer().getScheduler().scheduleSyncRepeatingTask(instance, () -> {

            //Get current time.
            long time = Time.currentTime();

            //Ping all players on this server to check their online status.
            for (NetworkUser user : users) {

                //Update the last_ping.
                globalSQL.update("UPDATE online_users SET last_ping=" + time + " WHERE uuid=" + user.player.getUniqueId() + " AND server=" + SERVER_NAME + ";");

            }

            //Get any users with a last_ping greater than 2 seconds.
            uuids = globalSQL.getStringList("SELECT uuid FROM online_users WHERE last_ping<" + (time-2000) + " AND server=" + SERVER_NAME + ";");

            //Iterate through uuids and run a network disconnect.
            for (String uuid : uuids) {

                connect.leaveEvent(uuid);

            }

        }, 0L, 20L);

    }
}