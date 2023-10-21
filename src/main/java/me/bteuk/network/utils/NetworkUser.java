package me.bteuk.network.utils;

import lombok.Getter;
import lombok.Setter;
import me.bteuk.network.Network;
import me.bteuk.network.commands.Nightvision;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.utils.regions.Region;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static me.bteuk.network.utils.Constants.*;
import static me.bteuk.network.utils.enums.ServerType.EARTH;
import static me.bteuk.network.utils.enums.ServerType.PLOT;

public class NetworkUser {

    //Network instance.
    private final Network instance;

    //Player instance.
    public final Player player;

    //Main gui, includes everything that is part of the navigator.
    public Gui mainGui;

    //Lights out, a gui game.
    public LightsOut lightsOut;

    //Staff gui.
    public Gui staffGui;

    //Staff chat
    public boolean staffChat;

    //Region information.
    public boolean inRegion;
    public Region region;
    public int dx;
    public int dz;

    //Navigator in hotbar.
    public boolean navigator;

    //If the player is switching server.
    public boolean switching;

    //If the player is afk.
    public boolean afk;
    public long last_movement;

    //Information for online-time logging.
    //Records when the player online-time was last logged.
    public long last_time_log;
    //Total active time in current session.
    public long active_time;

    //If linked to discord.
    public boolean isLinked;
    public long discord_id;

    //If the player is currently in a portal,
    //This is to prevent continuous execution of portal events.
    public boolean inPortal;
    public boolean wasInPortal;

    //Should tips be displayed for the player.
    @Getter
    @Setter
    private boolean tips_enabled;

    public NetworkUser(Player player) {

        this.instance = Network.getInstance();

        this.player = player;

        switching = false;
        inPortal = false;
        wasInPortal = false;
        afk = false;
        last_movement = Time.currentTime();

        //Set tips based on database value.
        tips_enabled = instance.globalSQL.hasRow("SELECT tips_enabled FROM player_data WHERE uuid='" + player.getUniqueId() + "' AND tips_enabled=1;");

        //Update builder role in database.
        instance.globalSQL.update("UPDATE player_data SET builder_role='" + Roles.builderRole(player) + "' WHERE uuid='" + player.getUniqueId() + "';");

        //Load tab for the user.
        loadTab();

        //Get discord linked status.
        //If they're linked get discord id.
        isLinked = instance.globalSQL.hasRow("SELECT uuid FROM discord WHERE uuid='" + player.getUniqueId() + "';");
        if (isLinked) {
            discord_id = instance.globalSQL.getLong("SELECT discord_id FROM discord WHERE uuid='" + player.getUniqueId() + "';");
        }

        //Set navigator enabled/disabled.
        navigator = instance.globalSQL.hasRow("SELECT navigator FROM player_data WHERE uuid='" + player.getUniqueId() + "' AND navigator=1;");
        //If navigator is disabled, remove the navigator if in the inventory.
        if (!navigator) {

            ItemStack slot8 = player.getInventory().getItem(8);

            if (slot8 != null) {
                if (slot8.equals(instance.navigator)) {
                    player.getInventory().setItem(8, null);
                }
            }
        }

        //Set staff chat value, if user is no longer staff, auto-disable.
        if (instance.globalSQL.hasRow("SELECT uuid FROM player_data WHERE uuid='" + player.getUniqueId() + "' AND staff_chat=1;")) {
            if (player.hasPermission("uknet.staff")) {
                staffChat = true;
            } else {
                staffChat = false;
                //And remove staff from database.
                instance.globalSQL.update("UPDATE player_data SET staff_chat=0 WHERE uuid='" + player.getUniqueId() + "';");
            }
        } else {
            staffChat = false;
        }

        //Check if the player is in a region.
        if (REGIONS_ENABLED) {
            if (SERVER_TYPE == EARTH) {
                //Check if they are in the earth world.
                if (player.getWorld().getName().equals(EARTH_WORLD)) {
                    region = instance.getRegionManager().getRegion(player.getLocation());
                    //Add region to database if not exists.
                    region.addToDatabase();
                    inRegion = true;
                }
            } else if (SERVER_TYPE == PLOT) {
                //Check if the player is in a buildable plot world and apply coordinate transform if true.
                if (instance.plotSQL.hasRow("SELECT name FROM location_data WHERE name='" + player.getLocation().getWorld().getName() + "';")) {
                    dx = -instance.plotSQL.getInt("SELECT xTransform FROM location_data WHERE name='" + player.getLocation().getWorld().getName() + "';");
                    dz = -instance.plotSQL.getInt("SELECT zTransform FROM location_data WHERE name='" + player.getLocation().getWorld().getName() + "';");

                    region = instance.getRegionManager().getRegion(player.getLocation(), dx, dz);
                    inRegion = true;
                }
            }
        }

        runEvents();

        last_time_log = Time.currentTime();
        active_time = 0;

        //Give the player nightvision if enabled or remove it if disabled.
        if (instance.globalSQL.hasRow("SELECT nightvision_enabled FROM player_data WHERE nightvision_enabled=1 AND uuid='" + player.getUniqueId() + "';")) {

            Nightvision.giveNightvision(player);

        } else {

            Nightvision.removeNightvision(player);

        }
    }

    private void loadTab() {

        //If this is the first player on the server, add all players from other servers to tab.
        if (instance.getServer().getOnlinePlayers().size() == 1 && TAB) {
            //Add all players from other servers to the fake players list, so they will show in tab when players connect.
            for (String uuid : instance.globalSQL.getStringList("SELECT uuid FROM online_users WHERE server<>'" + SERVER_NAME + "';")) {
                instance.tab.addFakePlayer(uuid);
            }
        }

        if (TAB) {
            //Add the player to the fake players list for other servers.
            instance.chat.broadcastMessage(Component.text("add " + player.getUniqueId()), "uknet:tab");

            //Remove the player from the fake players list, if they are currently in it.
            instance.tab.removeFakePlayer(player.getUniqueId().toString());

            //Load tab for the player, this will add the fake players.
            Bukkit.getScheduler().runTask(instance, () -> instance.tab.loadTab(player));
        }

    }

    private void runEvents() {

        //Check if the player has any join events, if try run them.
        //Delay by 1 second for all plugins to run their join events.
        Bukkit.getScheduler().scheduleSyncDelayedTask(instance, () -> {
            if (instance.globalSQL.hasRow("SELECT uuid FROM join_events WHERE uuid='" + player.getUniqueId() + "' AND type='network';")) {

                //Get the event from the database.
                String event = instance.globalSQL.getString("SELECT event FROM join_events WHERE uuid='" + player.getUniqueId() + "' AND type='network'");

                //Get message.
                String message = instance.globalSQL.getString("SELECT message FROM join_events WHERE uuid='" + player.getUniqueId() + "' AND type='network'");

                //Split the event by word.
                String[] aEvent = event.split(" ");

                //Clear the events.
                instance.globalSQL.update("DELETE FROM join_events WHERE uuid='" + player.getUniqueId() + "' AND type='network';");

                //Send the event to the event handler.
                instance.getTimers().getEventManager().event(player.getUniqueId().toString(), aEvent, message);

            }
        }, 20L);

    }

    /**
     * Check if the {@link NetworkUser} has the permission node.
     *
     * @param permission_node permission node.
     * @return whether the {@link NetworkUser} has the permission node.
     */
    public boolean hasPermission(String permission_node) {
        return player.hasPermission(permission_node);
    }

    /**
     * Check if the {@link NetworkUser} has any permission node in the array.
     *
     * @param permission_nodes array of permission nodes.
     * @return whether the {@link NetworkUser} has any of the permission nodes.
     */
    public boolean hasAnyPermission(String[] permission_nodes) {

        for (String permission_node : permission_nodes) {
            if (hasPermission(permission_node)) {
                return true;
            }
        }

        return false;

    }

    /**
     * Check if a user is online.
     *
     * @param uuid uuid of the user
     * @return true if the user is online
     */
    public static boolean isOnline(String uuid) {
        return Network.getInstance().globalSQL.hasRow("SELECT uuid FROM online_users WHERE uuid='" + uuid + "';");
    }

    /**
     * Get the name of a user.
     *
     * @param uuid uuid of the user
     * @return {@link String} name of the user
     */
    public static String getName(String uuid) {
        return Network.getInstance().globalSQL.getString("SELECT name FROM player_data WHERE uuid'" + uuid + "';");
    }

    /**
     * Send the user an offline message. This also works for online players.
     *
     * @param uuid uuid of the user
     * @param message the message to send using legacy ampersand format
     */
    public static void sendOfflineMessage(String uuid, String message) {
        Network.getInstance().globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" + uuid + "','" + message + "');");
    }
}
