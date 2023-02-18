package me.bteuk.network;

import me.bteuk.network.events.EventManager;
import me.bteuk.network.listeners.Connect;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.utils.*;
import me.bteuk.network.utils.regions.Inactivity;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Timers {

    //Plugin
    private final Network instance;

    //Users
    private final ArrayList<NetworkUser> users;

    //Timers
    private final ArrayList<Integer> timers;

    //Server name
    private final String SERVER_NAME;

    //SQL
    private final GlobalSQL globalSQL;

    //Network connect.
    private final Connect connect;

    //Server events
    private ArrayList<String[]> events;
    private boolean isBusy;

    //Uuids
    private ArrayList<String> uuids;

    //Messages
    private ArrayList<String> messages;

    //Navigator Check
    private ItemStack slot9;

    //Region Inactivity
    private final long inactivity;
    private ArrayList<Inactivity> inactive_owners;
    private String uuid;

    //Afk time
    private final long afk;

    //Discord roles
    private final HashMap<String, Long> roles;

    public Timers(Network instance, GlobalSQL globalSQL, Connect connect) {

        this.instance = instance;
        FileConfiguration config = instance.getConfig();
        this.users = instance.getUsers();

        this.globalSQL = globalSQL;

        this.connect = connect;

        this.timers = new ArrayList<>();

        SERVER_NAME = Network.SERVER_NAME;

        events = new ArrayList<>();

        //days * 24 hours * 60 minutes * 60 seconds * 1000 milliseconds
        inactivity = config.getInt("region_inactivity") * 24L * 60L * 60L * 1000L;
        inactive_owners = new ArrayList<>();

        //Minutes * 60 seconds * 1000 milliseconds
        afk = config.getInt("afk") * 60L * 1000L;

        //Get roles from config.
        roles = new HashMap<>();
        roles.put("architect", config.getLong("role_id.architect"));
        roles.put("builder", config.getLong("role_id.builder"));
        roles.put("jrbuilder", config.getLong("role_id.jrbuilder"));
        roles.put("apprentice", config.getLong("role_id.apprentice"));
        roles.put("applicant", config.getLong("role_id.applicant"));

    }

    public void startTimers() {

        //1 tick timer.
        timers.add(instance.getServer().getScheduler().scheduleSyncRepeatingTask(instance, () -> {

            //Check for new server_events.
            if (globalSQL.hasRow("SELECT uuid FROM server_events WHERE server='" + SERVER_NAME + "' AND type='network';")) {

                //If events is not empty, skip this iteration.
                //Additionally isBusy needs to be false, implying that the server is not still running a previous iteration.
                if (events.isEmpty() && !isBusy) {

                    isBusy = true;

                    //Get events for this server.
                    events = globalSQL.getEvents(SERVER_NAME, events);

                    for (String[] event : events) {

                        //Deal with events here.
                        Network.getInstance().getLogger().info("Event: " + event[1]);

                        //Split the event by word.
                        String[] aEvent = event[1].split(" ");

                        //Send the event to the event handler.
                        EventManager.event(event[0], aEvent, event[2]);

                    }

                    //Clear events when done.
                    events.clear();
                    isBusy = false;
                }
            }

        }, 0L, 1L));

        //1 second timer.
        timers.add(instance.getServer().getScheduler().scheduleSyncRepeatingTask(instance, () -> {

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

                //Check for messages that have been sent to this player.
                if (instance.globalSQL.hasRow("SELECT message FROM messages WHERE recipient='" + user.player.getUniqueId() + "';")) {

                    //Get messages.
                    messages = instance.globalSQL.getStringList("SELECT message FROM messages WHERE recipient='" + user.player.getUniqueId() + "';");

                    for (String message : messages) {

                        user.player.sendMessage(Utils.chat(message));

                    }

                    //Delete messages.
                    instance.globalSQL.update("DELETE FROM messages WHERE recipient='" + user.player.getUniqueId() + "'");
                }

                //Check if the player is afk.
                if (user.last_movement < (time - afk) && !user.afk) {

                    //Set player as AFK
                    user.afk = true;

                    //Save statistics.
                    Statistics.save(user, Time.getDate(time), time);

                    //Send message to chat and discord.
                    Network.getInstance().chat.broadcastMessage("&7" + user.player.getName() + " is now afk.", "uknet:globalchat");

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

        }, 0L, 20L));

        //1 minute timer.
        timers.add(instance.getServer().getScheduler().scheduleSyncRepeatingTask(instance, () -> {

            //Check for inactive owners.
            //If the region has members then make another member the new owner,
            //If the region has no members then set it inactive.
            inactive_owners.clear();
            inactive_owners = instance.regionSQL.getInactives("SELECT region,uuid FROM region_members WHERE is_owner=1 AND last_enter<" + (Time.currentTime() - inactivity) + ";");

            for (Inactivity inactive : inactive_owners) {

                if (inactive.region.hasMember()) {

                    //Get most recent member.
                    uuid = inactive.region.getRecentMember();

                    //Make the previous owner a member.
                    inactive.region.makeMember();

                    //Give the new player ownership.
                    inactive.region.makeOwner(uuid);

                    //Update any requests to take into account the new region owner.
                    inactive.region.updateRequests();

                } else {

                    //Set region as inactive.
                    inactive.region.setInactive();

                }
            }

            //For all online players sync the roles.
            for (NetworkUser u : instance.getUsers()) {

                if (u.isLinked) {
                    //Get the highest role for syncing and sync it, except for guest.
                    String role = Roles.builderRole(u.player);
                    discordSync(u.discord_id, role);
                }

                //Update role in online_players table.
                globalSQL.update("UPDATE online_users SET primary_role='" + Roles.getPrimaryRole(u.player) + "' WHERE uuid='" + u.player.getUniqueId() + "';");
            }

            //Update online time of all players.
            Statistics.saveAll();

        }, 0L, 1200L));
    }

    public void close() {

        //Cancel all timers.
        for (int timer : timers) {
            Bukkit.getScheduler().cancelTask(timer);
        }

    }

    public HashMap<String, Long> getRoles() {
        return roles;
    }

    public void discordSync(long discord_id, String role) {
        //Remove all roles except current role.
        for (Map.Entry<String, Long> entry : Network.getInstance().timers.getRoles().entrySet()) {

            if (role.equals(entry.getKey())) {
                instance.chat.broadcastMessage("addrole " + discord_id + " " + entry.getValue(), "uknet:discord");
            } else {
                instance.chat.broadcastMessage("removerole " + discord_id + " " + entry.getValue(), "uknet:discord");
            }
        }
    }
}