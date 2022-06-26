package me.bteuk.network;

import me.bteuk.network.events.EventManager;
import me.bteuk.network.listeners.Connect;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Time;
import org.bukkit.inventory.ItemStack;

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

    //Navigator Check
    private ItemStack slot9;

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
            if (globalSQL.hasRow("SELECT uuid FROM server_events WHERE server='" + SERVER_NAME + "' AND type='network';")) {

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
                globalSQL.update("UPDATE online_users SET last_ping=" + time + " WHERE uuid='" + user.player.getUniqueId() + "' AND server='" + SERVER_NAME + "';");

                //If navigator is enabled check if they have it in slot 9.
                if (user.navigator) {
                    slot9 = user.player.getInventory().getItem(8);

                    if (slot9 == null) {
                        user.player.getInventory().setItem(8, instance.navigator);
                    } else if (!(slot9.equals(instance.navigator))) {
                        user.player.getInventory().setItem(8, instance.navigator);
                    }
                }

            }

            //Check for users switching to this server.
            //If their switch time was greater than 10 seconds ago, disconnect them from the network, if not already.
            uuids = globalSQL.getStringList("SELECT uuid FROM server_switch WHERE to_server='" + SERVER_NAME + "';");

            //Iterate through uuids and check time.
            for (String uuid : uuids) {

                //If it's more than 10 seconds ago.
                if (globalSQL.getLong("SELECT switch_time FROM server_switch WHERE uuid='" + uuid + "';") < time - (1000 * 10)) {

                    //Run network disconnect and remove their entry.
                    globalSQL.update("DELETE FROM server_switch WHERE uuid='" + uuid + "';");
                    connect.leaveEvent(uuid);

                }
            }

            //Check for users with a last ping greater than 10 seconds ago, disconnect them from the network.
            uuids = globalSQL.getStringList("SELECT uuid FROM online_users WHERE last_ping<" + (time - (1000 * 10)) + ";");

            //Iterate through uuids and check time.
            for (String uuid : uuids) {

                //Run network disconnect and remove their entry.
                connect.leaveEvent(uuid);

            }

        }, 0L, 20L);

    }
}