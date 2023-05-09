package me.bteuk.network.listeners.global_teleport;

import me.bteuk.network.Network;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.enums.RegionStatus;
import me.bteuk.network.utils.enums.ServerType;
import me.bteuk.network.utils.regions.Region;
import me.bteuk.network.utils.regions.RegionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import static me.bteuk.network.utils.Constants.*;

public class TeleportListener implements Listener {

    private final RegionManager regionManager;

    private boolean blocked;

    public TeleportListener(Network instance) {

        Bukkit.getServer().getPluginManager().registerEvents(this, instance);

        regionManager = instance.getRegionManager();

        blocked = false;

    }

    public void block() {
        blocked = true;
    }

    @Deprecated
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent e) {

        if (blocked) {
            e.setCancelled(true);
            return;
        }

        Player p = e.getPlayer();
        NetworkUser u = Network.getInstance().getUser(p);

        //If u is null, cancel.
        if (u == null) {
            LOGGER.severe("User " + p.getName() + " can not be found!");
            p.sendMessage("User can not be found, please relog!");
            e.setCancelled(true);
            return;
        }


        //Cancel event if player is switching server.
        if (u.switching) {
            e.setCancelled(true);
            return;
        }

        //If regions are enabled, check for movement between regions.
        if (REGIONS_ENABLED) {

            //Check whether the player is teleporting to a server and world that uses regions.
            //If the player is on the earth server check if they are teleporting to the earth world.
            if (SERVER_NAME.equals(Network.getInstance().globalSQL.getString("SELECT name FROM server_data WHERE type='EARTH';"))) {
                if (e.getTo().getWorld().getName().equals(EARTH_WORLD)) {

                    //Get region.
                    Region region = regionManager.getRegion(e.getTo());

                    //Player is teleport to earth world on earth server.
                    //Check if are teleporting to a new region, if currently in one.
                    if (u.inRegion) {

                        //If the regions are not equal
                        if (!u.region.equals(region)) {

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
                                                .append(Utils.success(", you can build in this region.")));

                            } else {

                                //Send default enter message.
                                p.sendActionBar(
                                        Utils.success("You have entered ")
                                                .append(Component.text(region.getTag(p.getUniqueId().toString()), NamedTextColor.DARK_AQUA)));

                            }

                            //Update the region the player is in.
                            u.region = region;

                        } else {

                            //You can't enter this region.
                            p.sendMessage(Utils.error("The terrain for this region has not been generated, you must be at least Jr.Builder to load new terrain."));
                            e.setCancelled(true);
                        }

                    }

                } else if (u.inRegion) {

                    //Send default leave message.
                    p.sendActionBar(
                            Utils.success("You have left ")
                                    .append(Component.text(u.region.regionName(), NamedTextColor.DARK_AQUA)));

                    //Set inRegion to false.
                    u.inRegion = false;

                }
            } else if (SERVER_TYPE == ServerType.PLOT) {

                //Check if the player is teleporting to a buildable world in the plot system.
                if (Network.getInstance().plotSQL.hasRow("SELECT name FROM location_data WHERE name='" + e.getTo().getWorld().getName() + "';")) {

                    //Get negative coordinate transform of new location.
                    u.dx = -Network.getInstance().plotSQL.getInt("SELECT xTransform FROM location_data WHERE name='" + e.getTo().getWorld().getName() + "';");
                    u.dz = -Network.getInstance().plotSQL.getInt("SELECT zTransform FROM location_data WHERE name='" + e.getTo().getWorld().getName() + "';");

                    Location l = e.getTo().clone();

                    //Alter location to add necessary coordinate transformation.
                    l.setX(l.getX() + u.dx);
                    l.setZ(l.getZ() + u.dz);

                    //Get region.
                    Region region = regionManager.getRegion(l);

                    //Check if you are teleporting to a new region, if currently in one.
                    if (u.inRegion) {

                        //If the regions are not equal
                        if (!u.region.equals(region)) {

                            //Send default enter message.
                            p.sendActionBar(
                                    Utils.success("You have entered ")
                                            .append(Component.text(region.regionName(), NamedTextColor.DARK_AQUA))
                                            .append(Utils.success(" and left "))
                                            .append(Component.text(u.region.regionName(), NamedTextColor.DARK_AQUA)));

                            //Update the region the player is in.
                            u.region = region;
                        }

                    } else {

                        //Send default enter message.
                        p.sendActionBar(
                                Utils.success("You have entered ")
                                        .append(Component.text(region.regionName(), NamedTextColor.DARK_AQUA)));

                        //Update the region the player is in.
                        u.region = region;

                    }

                } else if (u.inRegion) {

                    //Send default leave message.
                    p.sendActionBar(
                            Utils.success("You have left ")
                                    .append(Component.text(u.region.regionName(), NamedTextColor.DARK_AQUA)));

                    //Set inRegion to false.
                    u.inRegion = false;

                }

            }
        }

        //Network.getInstance().getLogger().info("Teleport: " + e.getTo().getX() + "," + e.getTo().getZ());

    }
}
