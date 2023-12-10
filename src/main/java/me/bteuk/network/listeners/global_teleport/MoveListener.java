package me.bteuk.network.listeners.global_teleport;

import me.bteuk.network.Network;
import me.bteuk.network.events.EventManager;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.SwitchServer;
import me.bteuk.network.utils.Time;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.enums.RegionStatus;
import me.bteuk.network.utils.regions.Region;
import me.bteuk.network.utils.regions.RegionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import static me.bteuk.network.utils.Constants.*;
import static me.bteuk.network.utils.NetworkConfig.CONFIG;

public class MoveListener implements Listener {

    private final boolean teleportEnabled;

    private final RegionManager regionManager;

    private boolean blocked;

    public MoveListener(Network instance) {

        Bukkit.getServer().getPluginManager().registerEvents(this, instance);

        teleportEnabled = CONFIG.getBoolean("global_teleport");

        regionManager = instance.getRegionManager();

        blocked = false;

    }

    public void block() {
        blocked = true;
    }

    @Deprecated
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {

        if (blocked) {
            e.setCancelled(true);
            return;
        }

        Player p = e.getPlayer();
        NetworkUser u = Network.getInstance().getUser(p);

        //If u is null, cancel.
        if (u == null) {
            LOGGER.severe("User " + p.getName() + " can not be found!");
            p.sendMessage(Utils.error("User can not be found, please relog!"));
            e.setCancelled(true);
            return;
        }

        //Cancel event if player is switching server.
        if (u.switching) {
            e.setCancelled(true);
            return;
        }

        //Reset last movement of player, if they're afk unset that.
        u.last_movement = Time.currentTime();

        if (u.afk) {
            u.last_time_log = u.last_movement;
            u.afk = false;
            Network.getInstance().chat.broadcastAFK(u.player, false);
        }

        // If regions are enabled, check for movement between regions.
        // If the player is currently not in a region then that implies they are in a world without regions, so movement will not effect this.
        // Not being in a region also means that region is null.
        if (REGIONS_ENABLED && u.inRegion) {

            // Get x and z of the region as int rounded down with any necessary coordinate transforms.
            int x = ((e.getTo().getX() >= 0 ? (int) e.getTo().getX() : ((int) e.getTo().getX()) - 1) + u.dx) >> 9;
            int z = ((e.getTo().getZ() >= 0 ? (int) e.getTo().getZ() : ((int) e.getTo().getZ()) - 1) + u.dz) >> 9;

            // Check if the player has moved to another region.
            if (!u.region.equals(x, z)) {

                //Get new region.
                Region region = regionManager.getRegion(x, z);

                //Check if the new region is on this server or not.
                if (!u.region.getServer().equals(region.getServer())) {

                    //If cross-server teleport is enabled teleport them to the correct server and location.
                    if (teleportEnabled) {

                        //Check if the player can enter the region.
                        if (region.inDatabase() || p.hasPermission("group.jrbuilder")) {

                            //If the server is offline, notify the player.
                            if (Network.getInstance().globalSQL.getBoolean("SELECT online FROM server_data WHERE name='" + region.getServer() + "';")) {

                                //Add region to database if not exists.
                                region.addToDatabase();

                                //Region is on another server, teleport them accordingly.
                                //If the new region is on a plot server, check for coordinate transform.
                                if (region.status() == RegionStatus.PLOT) {

                                    //Get server and world of region.
                                    String location = Network.getInstance().plotSQL.getString("SELECT location FROM regions WHERE region='" + region.regionName() + "';");

                                    int xTransform = Network.getInstance().plotSQL.getInt("SELECT xTransform FROM location_data WHERE name='" + location + "';");
                                    int zTransform = Network.getInstance().plotSQL.getInt("SELECT zTransform FROM location_data WHERE name='" + location + "';");

                                    //Set join event to teleport there.
                                    EventManager.createJoinEvent(u.player.getUniqueId().toString(), "network", "teleport " +
                                            location + " " + (e.getTo().getX() + xTransform) + " " + (e.getTo().getZ() + zTransform) + " " + e.getTo().getYaw() + " " + e.getTo().getPitch());

                                } else {

                                    //Set join event to teleport there.
                                    EventManager.createJoinEvent(u.player.getUniqueId().toString(), "network", "teleport " +
                                            EARTH_WORLD + " " + e.getTo().getX() + " " + e.getTo().getZ() + " " + e.getTo().getYaw() + " " + e.getTo().getPitch());

                                }

                                //Switch server.
                                e.setCancelled(true);
                                SwitchServer.switchServer(u.player, region.getServer());

                            } else {

                                p.sendMessage(Utils.error("This region is on another server, however the server is currently offline."));

                            }

                        } else {

                            //You can't enter this region.
                            p.sendMessage(Utils.error("The terrain for this region has not been generated, you must be at least Jr.Builder to load new terrain."));
                        }

                    } else {

                        //Cancel movement as the location is on another server.
                        p.sendMessage(Utils.error("The terrain for this location is on another server, you may not enter."));
                    }
                    e.setCancelled(true);
                } else {

                    //Check if the player can enter the region.
                    if (region.inDatabase() || p.hasPermission("group.jrbuilder")) {

                        //Add region to database if not exists.
                        region.addToDatabase();

                        //If the player is the region owner update last enter and tell set the message.
                        if (region.isOwner(p.getUniqueId().toString())) {

                            p.sendActionBar(
                                    Utils.success("You have entered ")
                                            .append(Component.text(region.getTag(p.getUniqueId().toString()), NamedTextColor.DARK_AQUA))
                                            .append(Utils.success(" and left "))
                                            .append(Component.text(u.region.getTag(p.getUniqueId().toString()), NamedTextColor.DARK_AQUA))
                                            .append(Utils.success(", you are the owner of this region.")));
                            region.setLastEnter(p.getUniqueId().toString());

                            //If the region is inactive, set it to active.
                            if (region.status() == RegionStatus.INACTIVE) {
                                region.setDefault();
                                p.sendMessage(Utils.success("This region is no longer \"Inactive\", it has been set back to default settings."));
                            }

                            //Check if the player is a region members.
                        } else if (region.isMember(p.getUniqueId().toString())) {

                            p.sendActionBar(
                                    Utils.success("You have entered ")
                                            .append(Component.text(region.getTag(p.getUniqueId().toString()), NamedTextColor.DARK_AQUA))
                                            .append(Utils.success(" and left "))
                                            .append(Component.text(u.region.getTag(p.getUniqueId().toString()), NamedTextColor.DARK_AQUA))
                                            .append(Utils.success(", you are a member of this region.")));
                            region.setLastEnter(p.getUniqueId().toString());

                            //If the region is inactive, make this member to owner.
                            if (region.status() == RegionStatus.INACTIVE) {
                                //Make the previous owner a member.
                                region.makeMember();

                                //Give the new player ownership.
                                region.makeOwner(p.getUniqueId().toString());

                                //Update any requests to take into account the new region owner.
                                region.updateRequests();

                                p.sendMessage(Utils.success("This region is no longer \"Inactive\", it has been set back to default settings."));
                                p.sendMessage(Utils.success("You have been made the new region owner."));

                            }

                            //Check if the region is open and the player is at least jr.builder.
                        } else if (region.status() == RegionStatus.OPEN && p.hasPermission("group.jrbuilder")) {

                            p.sendActionBar(
                                    Utils.success("You have entered ")
                                            .append(Component.text(region.getTag(p.getUniqueId().toString()), NamedTextColor.DARK_AQUA))
                                            .append(Utils.success(" and left "))
                                            .append(Component.text(u.region.getTag(p.getUniqueId().toString()), NamedTextColor.DARK_AQUA))
                                            .append(Utils.success(", you can build in this region.")));

                        } else {

                            //Send default enter message.
                            p.sendActionBar(
                                    Utils.success("You have entered ")
                                            .append(Component.text(region.getTag(p.getUniqueId().toString()), NamedTextColor.DARK_AQUA))
                                            .append(Utils.success(" and left "))
                                            .append(Component.text(u.region.getTag(p.getUniqueId().toString()), NamedTextColor.DARK_AQUA)));

                        }

                        //Update the region the player is in.
                        u.region = region;

                    } else {

                        //You can't enter this region.
                        p.sendMessage(Utils.error("The terrain for this region has not been generated, you must be at least Jr.Builder to load new terrain."));
                        e.setCancelled(true);
                    }
                }
            }
        }
    }
}
