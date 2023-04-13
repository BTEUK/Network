package me.bteuk.network.utils;

import me.bteuk.network.Network;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.utils.regions.Region;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static me.bteuk.network.utils.Constants.SERVER_NAME;
import static me.bteuk.network.utils.NetworkConfig.CONFIG;

public class NetworkUser {

    //Player instance.
    public Player player;

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

    public NetworkUser(Player player) {

        this.player = player;

        switching = false;
        inPortal = false;
        wasInPortal = false;
        afk = false;
        last_movement = Time.currentTime();

        //Get discord linked status.
        //If they're linked get discord id.
        isLinked = Network.getInstance().globalSQL.hasRow("SELECT uuid FROM discord WHERE uuid='" + player.getUniqueId() + "';");
        if (isLinked) {
            discord_id = Network.getInstance().globalSQL.getLong("SELECT discord_id FROM discord WHERE uuid='" + player.getUniqueId() + "';");
        }

        //Set navigator enabled/disabled.
        navigator = Network.getInstance().globalSQL.hasRow("SELECT navigator FROM player_data WHERE uuid='" + player.getUniqueId() + "' AND navigator=1;");
        //If navigator is disabled, remove the navigator if in the inventory.
        if (!navigator) {

            ItemStack slot8 = player.getInventory().getItem(8);

            if (slot8 != null) {
                if (slot8.equals(Network.getInstance().navigator)) {
                    player.getInventory().setItem(8, null);
                }
            }
        }

        //Set staff chat value, if user is no longer staff, auto-disable.
        if (Network.getInstance().globalSQL.hasRow("SELECT uuid FROM player_data WHERE uuid='" + player.getUniqueId() + "' AND staff_chat=1;")) {
            if (player.hasPermission("uknet.staff")) {
                staffChat = true;
            } else {
                staffChat = false;
                //And remove staff from database.
                Network.getInstance().globalSQL.update("UPDATE player_data SET staff_chat=0 WHERE uuid='" + player.getUniqueId() + "';");
            }
        } else {
            staffChat = false;
        }

        //Update builder role in database.
        Network.getInstance().globalSQL.update("UPDATE player_data SET builder_role='" + Roles.builderRole(player) + "' WHERE uuid='" + player.getUniqueId() + "';");

        //Check if the player is in a region.
        if (SERVER_NAME.equals(Network.getInstance().globalSQL.getString("SELECT name FROM server_data WHERE type='EARTH'"))) {
            //Check if they are in the earth world.
            if (player.getLocation().getWorld().getName().equals(CONFIG.getString("earth_world"))) {
                region = Network.getInstance().getRegionManager().getRegion(player.getLocation());
                //Add region to database if not exists.
                region.addToDatabase();
                inRegion = true;
            }
        } else if (SERVER_NAME.equals(Network.getInstance().globalSQL.getString("SELECT name FROM server_data WHERE type='PLOT';"))) {
            //Check if the player is in a buildable plot world and apply coordinate transform if true.
            if (Network.getInstance().plotSQL.hasRow("SELECT name FROM location_data WHERE name='" + player.getLocation().getWorld().getName() + "';")) {
                dx = -Network.getInstance().plotSQL.getInt("SELECT xTransform FROM location_data WHERE name='" + player.getLocation().getWorld().getName() + "';");
                dz = -Network.getInstance().plotSQL.getInt("SELECT zTransform FROM location_data WHERE name='" + player.getLocation().getWorld().getName() + "';");

                region = Network.getInstance().getRegionManager().getRegion(player.getLocation(), dx, dz);
                inRegion = true;
            }
        }

        last_time_log = Time.currentTime();
        active_time = 0;
    }
}
