package net.bteuk.network;

import lombok.Getter;
import net.bteuk.network.eventing.events.EventManager;
import net.bteuk.network.eventing.listeners.Connect;
import net.bteuk.network.sql.GlobalSQL;
import net.bteuk.network.utils.NetworkUser;
import net.bteuk.network.utils.Statistics;
import net.bteuk.network.utils.Time;
import net.bteuk.network.utils.regions.Inactivity;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;

import static net.bteuk.network.utils.Constants.DISCORD_LINKING;
import static net.bteuk.network.utils.Constants.LOGGER;
import static net.bteuk.network.utils.Constants.REGIONS_ENABLED;
import static net.bteuk.network.utils.Constants.SERVER_NAME;
import static net.bteuk.network.utils.NetworkConfig.CONFIG;

public class Timers {

    //Plugin
    private final Network instance;

    //Users
    private final ArrayList<NetworkUser> users;

    //Timers
    private final ArrayList<Integer> timers;

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
    public final long inactivity;
    private ArrayList<Inactivity> inactive_owners;
    private String uuid;

    //Afk time
    private final long afk;

    //Discord roles
    @Getter
    private final HashMap<String, Long> roles;

    //Event manager
    @Getter
    private final EventManager eventManager;

    public Timers(Network instance, GlobalSQL globalSQL, Connect connect) {

        this.instance = instance;
        this.users = instance.getUsers();

        this.globalSQL = globalSQL;

        this.connect = connect;

        this.timers = new ArrayList<>();

        eventManager = new EventManager();
        events = new ArrayList<>();

        //days * 24 hours * 60 minutes * 60 seconds * 1000 milliseconds
        inactivity = CONFIG.getInt("region_inactivity") * 24L * 60L * 60L * 1000L;
        inactive_owners = new ArrayList<>();

        //Minutes * 60 seconds * 1000 milliseconds
        afk = CONFIG.getInt("afk") * 60L * 1000L;

        //Get roles from config if discord linking is enabled.
        roles = new HashMap<>();
        if (DISCORD_LINKING) {
            roles.put("reviewer", CONFIG.getLong("chat.global_chat.discord.linking.role_id.reviewer"));
            roles.put("architect", CONFIG.getLong("chat.global_chat.discord.linking.role_id.architect"));
            roles.put("builder", CONFIG.getLong("chat.global_chat.discord.linking.role_id.builder"));
            roles.put("jrbuilder", CONFIG.getLong("chat.global_chat.discord.linking.role_id.jrbuilder"));
            roles.put("apprentice", CONFIG.getLong("chat.global_chat.discord.linking.role_id.apprentice"));
            roles.put("applicant", CONFIG.getLong("chat.global_chat.discord.linking.role_id.applicant"));
        }

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
                    events = globalSQL.getEvents(SERVER_NAME, "network", events);

                    for (String[] event : events) {

                        //Deal with events here.
                        LOGGER.info("Event: " + event[1]);

                        //Split the event by word.
                        String[] aEvent = event[1].split(" ");

                        //Send the event to the event handler.
                        eventManager.event(event[0], aEvent, event[2]);

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

//                //Update the last_ping and display name.
//                //Check if display name has actually changed.
//                if ((PlaceholderAPI.setPlaceholders(user.player, "%luckperms_prefix%") + " " + user.player.getName()).equals(
//                        globalSQL.getString("SELECT display_name FROM online_users WHERE uuid='" + user.player.getUniqueId() + "' AND server='" + SERVER_NAME + "';")
//                )) {
//
//                    //Update last ping
//                    globalSQL.update("UPDATE online_users SET last_ping=" + time + " WHERE uuid='" + user.player.getUniqueId() + "' AND server='" + SERVER_NAME + "';");
//
//                } else {
//
//                    //Update display name, primary role and last ping
//                    globalSQL.update("UPDATE online_users SET last_ping=" + time + ",display_name='" +
//                            PlaceholderAPI.setPlaceholders(user.player, "%luckperms_prefix%") + " " + user.player.getName() +
//                            "',primary_role='" + Roles.getPrimaryRole(user.player) + "' WHERE uuid='" + user.player.getUniqueId() + "' AND server='" + SERVER_NAME + "';");
//
//                    if (TAB) {
//                        //Update tab for all players to update display name.
//                        //This is done with the tab chat channel.
//                        instance.getChat().broadcastMessage(Component.text("update " + user.player.getUniqueId()), "uknet:tab");
//                    }
//
//                }

                //If navigator is enabled check if they have it in slot 9.
                if (user.isNavigatorEnabled()) {
                    slot9 = user.player.getInventory().getItem(8);

                    if (slot9 == null) {
                        user.player.getInventory().setItem(8, instance.navigator);
                    } else if (!(slot9.equals(instance.navigator))) {
                        user.player.getInventory().setItem(8, instance.navigator);
                    }
                }

                //Check for messages that have been sent to this player.
                if (globalSQL.hasRow("SELECT message FROM messages WHERE recipient='" + user.player.getUniqueId() + "';")) {

                    //Get messages.
                    messages = globalSQL.getStringList("SELECT message FROM messages WHERE recipient='" + user.player.getUniqueId() + "';");

                    for (String message : messages) {

                        user.player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message));

                    }

                    //Delete messages.
                    globalSQL.update("DELETE FROM messages WHERE recipient='" + user.player.getUniqueId() + "'");
                }

                //Check if the player is afk.
                if (user.last_movement < (time - afk) && !user.afk) {

                    //Set player as AFK
                    user.afk = true;

                    //Save statistics.
                    Statistics.save(user, Time.getDate(time), time);

                    //Send message to chat and discord.
                    Network.getInstance().getChat().broadcastAFK(user.player, true);

                }
            }

//            //Check for users switching to this server.
//            //If their switch time was greater than 10 seconds ago, disconnect them from the network, if not already.
//            uuids = globalSQL.getStringList("SELECT uuid FROM server_switch WHERE to_server='" + SERVER_NAME + "';");
//
//            //Iterate through uuids and check time.
//            for (String uuid : uuids) {
//
//                //If it's more than 10 seconds ago.
//                if (globalSQL.getLong("SELECT switch_time FROM server_switch WHERE uuid='" + uuid + "';") < time - (1000 * 10)) {
//
//                    //Run network disconnect and remove their entry.
//                    globalSQL.update("DELETE FROM server_switch WHERE uuid='" + uuid + "';");
//
//                    connect.networkLeaveEvent(Bukkit.getOfflinePlayer(UUID.fromString(uuid)));
//
//                }
//            }

        }, 0L, 20L));

        //1 minute timer.
        timers.add(instance.getServer().getScheduler().scheduleSyncRepeatingTask(instance, () -> {

            //Check for inactive owners.
            //If the region has members then make another member the new owner,
            //If the region has no members then set it inactive.
            if (REGIONS_ENABLED) {
                inactive_owners.clear();
                inactive_owners = instance.regionSQL.getInactives("SELECT region,uuid FROM region_members WHERE is_owner=1 AND last_enter<" + (Time.currentTime() - inactivity) + ";");

                for (Inactivity inactive : inactive_owners) {

                    //Check if there is another member in this region, they must be active.
                    if (inactive.region.hasActiveMember(Time.currentTime() - inactivity)) {

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
            }

//            //For all online players sync the roles.
//            for (NetworkUser u : instance.getUsers()) {
//
//                if (u.isLinked && DISCORD_LINKING) {
//                    //Get the highest role for syncing and sync it, except for guest.
//                    String role = Roles.builderRole(u.player);
//                    discordSync(u.discord_id, role);
//                }
//
//                //Update role in online_players table.
//                globalSQL.update("UPDATE online_users SET primary_role='" + Roles.getPrimaryRole(u.player) + "' WHERE uuid='" + u.player.getUniqueId() + "';");
//            }

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

//    //TODO: Only run this on role add/remove as well as discord link/unlink.
//    public void discordSync(long discord_id, String role) {
//        //Remove all roles except current role.
//        for (Map.Entry<String, Long> entry : Network.getInstance().getTimers().getRoles().entrySet()) {
//
//            if (role.equals(entry.getKey())) {
//                instance.getChat().broadcastMessage(Component.text("addrole " + discord_id + " " + entry.getValue()), "uknet:discord_linking");
//            } else {
//                instance.getChat().broadcastMessage(Component.text("removerole " + discord_id + " " + entry.getValue()), "uknet:discord_linking");
//            }
//        }
//    }
}