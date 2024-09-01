package net.bteuk.network.eventing.listeners.global_teleport;

import net.bteuk.network.Network;
import net.bteuk.network.eventing.events.EventManager;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.sql.PlotSQL;
import net.bteuk.network.utils.NetworkUser;
import net.bteuk.network.utils.SwitchServer;
import net.bteuk.network.utils.Time;
import net.bteuk.network.utils.enums.RegionStatus;
import net.bteuk.network.utils.regions.Region;
import net.bteuk.network.utils.regions.RegionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import static net.bteuk.network.commands.AFK.updateAfkStatus;
import static net.bteuk.network.utils.Constants.EARTH_WORLD;
import static net.bteuk.network.utils.Constants.LOGGER;
import static net.bteuk.network.utils.Constants.REGIONS_ENABLED;
import static net.bteuk.network.utils.NetworkConfig.CONFIG;

public class MoveListener implements Listener {

    private final boolean teleportEnabled;

    private final RegionManager regionManager;

    private boolean blocked;

    private final PlotSQL plotSQL;

    public MoveListener(Network instance) {

        Bukkit.getServer().getPluginManager().registerEvents(this, instance);

        teleportEnabled = CONFIG.getBoolean("global_teleport");

        regionManager = instance.getRegionManager();

        blocked = false;

        plotSQL = instance.getPlotSQL();

    }

    public void block() {
        blocked = true;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {

        if (blocked) {
            e.setCancelled(true);
            return;
        }

        Player p = e.getPlayer();
        NetworkUser user = Network.getInstance().getUser(p);

        //If u is null, cancel.
        if (user == null) {
            LOGGER.severe("User " + p.getName() + " can not be found!");
            p.sendMessage(ChatUtils.error("User can not be found, please relog!"));
            e.setCancelled(true);
            return;
        }

        //Cancel event if player is switching server.
        if (user.switching) {
            e.setCancelled(true);
            return;
        }

        //Reset last movement of player, if they're afk unset that.
        user.last_movement = Time.currentTime();

        if (user.afk) {
            user.afk = false;
            updateAfkStatus(user, false);
        }

        // If regions are enabled, check for movement between regions.
        // If the player is currently not in a region then that implies they are in a world without regions, so movement will not effect this.
        // Not being in a region also means that region is null.
        if (REGIONS_ENABLED && user.inRegion) {

            // Get x and z of the region as int rounded down with any necessary coordinate transforms.
            int x = ((e.getTo().getX() >= 0 ? (int) e.getTo().getX() : ((int) e.getTo().getX()) - 1) + user.dx) >> 9;
            int z = ((e.getTo().getZ() >= 0 ? (int) e.getTo().getZ() : ((int) e.getTo().getZ()) - 1) + user.dz) >> 9;

            // Check if the player has moved to another region.
            if (!user.region.equals(x, z)) {

                //Get new region.
                Region region = regionManager.getRegion(x, z);

                //Check if the new region is on this server or not.
                if (!user.region.getServer().equals(region.getServer())) {

                    //If cross-server teleport is enabled teleport them to the correct server and location.
                    if (teleportEnabled) {

                        //Check if the player can enter the region.
                        if (region.inDatabase() || p.hasPermission("group.jrbuilder")) {

                            //If the server is offline, notify the player.
                            if (Network.getInstance().getGlobalSQL().getBoolean("SELECT online FROM server_data WHERE name='" + region.getServer() + "';")) {

                                //Add region to database if not exists.
                                region.addToDatabase();

                                //Region is on another server, teleport them accordingly.
                                //If the new region is on a plot server, check for coordinate transform.
                                if (region.status() == RegionStatus.PLOT) {

                                    //Get server and world of region.
                                    String location = plotSQL.getString("SELECT location FROM regions WHERE region='" + region.regionName() + "';");

                                    int xTransform = plotSQL.getInt("SELECT xTransform FROM location_data WHERE name='" + location + "';");
                                    int zTransform = plotSQL.getInt("SELECT zTransform FROM location_data WHERE name='" + location + "';");

                                    //Set join event to teleport there.
                                    EventManager.createJoinEvent(user.player.getUniqueId().toString(), "network", "teleport " +
                                            location + " " + (e.getTo().getX() + xTransform) + " " + (e.getTo().getZ() + zTransform) + " " + e.getTo().getYaw() + " " + e.getTo().getPitch());

                                } else {

                                    //Set join event to teleport there.
                                    EventManager.createJoinEvent(user.player.getUniqueId().toString(), "network", "teleport " +
                                            EARTH_WORLD + " " + (e.getTo().getX() + user.dx) + " " + (e.getTo().getZ()  + user.dz) + " " + e.getTo().getYaw() + " " + e.getTo().getPitch());

                                }

                                //Switch server.
                                e.setCancelled(true);
                                SwitchServer.switchServer(user.player, region.getServer());

                            } else {

                                p.sendMessage(ChatUtils.error("This region is on another server, however the server is currently offline."));

                            }

                        } else {

                            //You can't enter this region.
                            p.sendMessage(ChatUtils.error("The terrain for this region has not been generated, you must be at least Jr.Builder to load new terrain."));
                        }

                    } else {

                        //Cancel movement as the location is on another server.
                        p.sendMessage(ChatUtils.error("The terrain for this location is on another server, you may not enter."));
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
                                    ChatUtils.success("You have entered ")
                                            .append(Component.text(region.getTag(p.getUniqueId().toString()), NamedTextColor.DARK_AQUA))
                                            .append(ChatUtils.success(" and left "))
                                            .append(Component.text(user.region.getTag(p.getUniqueId().toString()), NamedTextColor.DARK_AQUA))
                                            .append(ChatUtils.success(", you are the owner of this region.")));
                            region.setLastEnter(p.getUniqueId().toString());

                            //If the region is inactive, set it to active.
                            if (region.status() == RegionStatus.INACTIVE) {
                                region.setDefault();
                                p.sendMessage(ChatUtils.success("This region is no longer \"Inactive\", it has been set back to default settings."));
                            }

                            //Check if the player is a region members.
                        } else if (region.isMember(p.getUniqueId().toString())) {

                            p.sendActionBar(
                                    ChatUtils.success("You have entered ")
                                            .append(Component.text(region.getTag(p.getUniqueId().toString()), NamedTextColor.DARK_AQUA))
                                            .append(ChatUtils.success(" and left "))
                                            .append(Component.text(user.region.getTag(p.getUniqueId().toString()), NamedTextColor.DARK_AQUA))
                                            .append(ChatUtils.success(", you are a member of this region.")));
                            region.setLastEnter(p.getUniqueId().toString());

                            //If the region is inactive, make this member to owner.
                            if (region.status() == RegionStatus.INACTIVE) {
                                //Make the previous owner a member.
                                region.makeMember();

                                //Give the new player ownership.
                                region.makeOwner(p.getUniqueId().toString());

                                //Update any requests to take into account the new region owner.
                                region.updateRequests();

                                p.sendMessage(ChatUtils.success("This region is no longer \"Inactive\", it has been set back to default settings."));
                                p.sendMessage(ChatUtils.success("You have been made the new region owner."));

                            }

                            //Check if the region is open and the player is at least jr.builder.
                        } else if (region.status() == RegionStatus.OPEN && p.hasPermission("group.jrbuilder")) {

                            p.sendActionBar(
                                    ChatUtils.success("You have entered ")
                                            .append(Component.text(region.getTag(p.getUniqueId().toString()), NamedTextColor.DARK_AQUA))
                                            .append(ChatUtils.success(" and left "))
                                            .append(Component.text(user.region.getTag(p.getUniqueId().toString()), NamedTextColor.DARK_AQUA))
                                            .append(ChatUtils.success(", you can build in this region.")));

                        } else {

                            //Send default enter message.
                            p.sendActionBar(
                                    ChatUtils.success("You have entered ")
                                            .append(Component.text(region.getTag(p.getUniqueId().toString()), NamedTextColor.DARK_AQUA))
                                            .append(ChatUtils.success(" and left "))
                                            .append(Component.text(user.region.getTag(p.getUniqueId().toString()), NamedTextColor.DARK_AQUA)));

                        }

                        //Update the region the player is in.
                        user.region = region;

                    } else {

                        //You can't enter this region.
                        p.sendMessage(ChatUtils.error("The terrain for this region has not been generated, you must be at least Jr.Builder to load new terrain."));
                        e.setCancelled(true);
                    }
                }
            }
        }
    }
}
