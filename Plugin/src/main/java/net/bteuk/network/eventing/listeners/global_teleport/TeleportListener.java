package net.bteuk.network.eventing.listeners.global_teleport;

import net.bteuk.network.Network;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.sql.PlotSQL;
import net.bteuk.network.utils.NetworkUser;
import net.bteuk.network.utils.enums.RegionStatus;
import net.bteuk.network.utils.enums.ServerType;
import net.bteuk.network.utils.regions.Region;
import net.bteuk.network.utils.regions.RegionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import static net.bteuk.network.utils.Constants.EARTH_WORLD;
import static net.bteuk.network.utils.Constants.LOGGER;
import static net.bteuk.network.utils.Constants.REGIONS_ENABLED;
import static net.bteuk.network.utils.Constants.SERVER_TYPE;
import static net.bteuk.network.utils.enums.ServerType.EARTH;

public class TeleportListener implements Listener {

    private final RegionManager regionManager;
    private final PlotSQL plotSQL;
    private boolean blocked;

    public TeleportListener(Network instance) {

        Bukkit.getServer().getPluginManager().registerEvents(this, instance);

        regionManager = instance.getRegionManager();

        blocked = false;

        plotSQL = instance.getPlotSQL();
    }

    public void block() {
        blocked = true;
    }

    @Deprecated
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent e) {

        if (blocked || e.getCause() == PlayerTeleportEvent.TeleportCause.SPECTATE) {
            e.setCancelled(true);
            if (e.getCause() == PlayerTeleportEvent.TeleportCause.SPECTATE) {
                e.getPlayer().sendMessage(ChatUtils.error("Teleporting via the spectator menu is disabled, please use" +
                        " /tp <player>"));
            }
            return;
        }

        Player p = e.getPlayer();
        NetworkUser u = Network.getInstance().getUser(p);

        // If u is null, cancel.
        if (u == null) {
            LOGGER.severe("User " + p.getName() + " can not be found!");
            p.sendMessage(ChatUtils.error("User can not be found, please relog!"));
            e.setCancelled(true);
            return;
        }

        // Cancel event if player is switching server.
        if (u.switching) {
            e.setCancelled(true);
            return;
        }

        // If building companion is enabled, check if the player changed world.
        if (u.getCompanion() != null) {
            u.getCompanion().checkChangeWorld(e.getTo().getWorld());
        }

        // If regions are enabled, check for movement between regions.
        if (REGIONS_ENABLED) {

            // Check whether the player is teleporting to a server and world that uses regions.
            // If the player is on the earth server check if they are teleporting to the earth world.
            if (SERVER_TYPE == EARTH) {
                if (e.getTo().getWorld().getName().equals(EARTH_WORLD)) {

                    // Get region.
                    Region region = regionManager.getRegion(e.getTo());

                    // Player is teleport to earth world on earth server.
                    // Check if are teleporting to a new region, if currently in one.
                    if (u.inRegion) {

                        // If the regions are not equal
                        if (!u.region.equals(region)) {

                            // Check if the player can enter the region.
                            if (region.inDatabase() || p.hasPermission("group.jrbuilder")) {

                                // Add region to database if not exists.
                                region.addToDatabase();

                                // If the player is the region owner update last enter and tell set the message.
                                if (region.isOwner(p.getUniqueId().toString())) {

                                    p.sendActionBar(
                                            ChatUtils.success("You have entered ")
                                                    .append(Component.text(region.getTag(p.getUniqueId().toString()),
                                                            NamedTextColor.DARK_AQUA))
                                                    .append(ChatUtils.success(" and left "))
                                                    .append(Component.text(u.region.getTag(p.getUniqueId().toString()),
                                                            NamedTextColor.DARK_AQUA))
                                                    .append(ChatUtils.success(", you are the owner of this region.")));
                                    region.setLastEnter(p.getUniqueId().toString());

                                    // If the region is inactive, set it to active.
                                    if (region.status() == RegionStatus.INACTIVE) {
                                        region.setDefault();
                                        p.sendMessage(ChatUtils.success("This region is no longer \"Inactive\", it " +
                                                "has been set back to default settings."));
                                    }

                                    // Check if the player is a region members.
                                } else if (region.isMember(p.getUniqueId().toString())) {

                                    p.sendActionBar(
                                            ChatUtils.success("You have entered ")
                                                    .append(Component.text(region.getTag(p.getUniqueId().toString()),
                                                            NamedTextColor.DARK_AQUA))
                                                    .append(ChatUtils.success(" and left "))
                                                    .append(Component.text(u.region.getTag(p.getUniqueId().toString()),
                                                            NamedTextColor.DARK_AQUA))
                                                    .append(ChatUtils.success(", you are a member of this region.")));
                                    region.setLastEnter(p.getUniqueId().toString());

                                    // If the region is inactive, make this member to owner.
                                    if (region.status() == RegionStatus.INACTIVE) {
                                        // Make the previous owner a member.
                                        region.makeMember();

                                        // Give the new player ownership.
                                        region.makeOwner(p.getUniqueId().toString());

                                        // Update any requests to take into account the new region owner.
                                        region.updateRequests();

                                        p.sendMessage(ChatUtils.success("This region is no longer \"Inactive\", it " +
                                                "has been set back to default settings."));
                                        p.sendMessage(ChatUtils.success("You have been made the new region owner."));
                                    }

                                    // Check if the region is open and the player is at least jr.builder.
                                } else if (region.status() == RegionStatus.OPEN && p.hasPermission("group.jrbuilder")) {

                                    p.sendActionBar(
                                            ChatUtils.success("You have entered ")
                                                    .append(Component.text(region.getTag(p.getUniqueId().toString()),
                                                            NamedTextColor.DARK_AQUA))
                                                    .append(ChatUtils.success(" and left "))
                                                    .append(Component.text(u.region.getTag(p.getUniqueId().toString()),
                                                            NamedTextColor.DARK_AQUA))
                                                    .append(ChatUtils.success(", you can build in this region.")));
                                } else {

                                    // Send default enter message.
                                    p.sendActionBar(
                                            ChatUtils.success("You have entered ")
                                                    .append(Component.text(region.getTag(p.getUniqueId().toString()),
                                                            NamedTextColor.DARK_AQUA))
                                                    .append(ChatUtils.success(" and left "))
                                                    .append(Component.text(u.region.getTag(p.getUniqueId().toString()),
                                                            NamedTextColor.DARK_AQUA)));
                                }

                                // Update the region the player is in.
                                u.region = region;
                            } else {

                                // You can't enter this region.
                                p.sendMessage(ChatUtils.error("The terrain for this region has not been generated, " +
                                        "you must be at least Jr.Builder to load new terrain."));
                                e.setCancelled(true);
                            }
                        }
                    } else {

                        // Check if the player can enter the region.
                        if (region.inDatabase() || p.hasPermission("group.jrbuilder")) {

                            // Add region to database if not exists.
                            region.addToDatabase();

                            // If the player is the region owner update last enter and tell set the message.
                            if (region.isOwner(p.getUniqueId().toString())) {

                                p.sendActionBar(
                                        ChatUtils.success("You have entered ")
                                                .append(Component.text(region.getTag(p.getUniqueId().toString()),
                                                        NamedTextColor.DARK_AQUA))
                                                .append(ChatUtils.success(", you are the owner of this region.")));
                                region.setLastEnter(p.getUniqueId().toString());

                                // If the region is inactive, set it to active.
                                if (region.status() == RegionStatus.INACTIVE) {
                                    region.setDefault();
                                    p.sendMessage(ChatUtils.success("This region is no longer \"Inactive\", it has " +
                                            "been set back to default settings."));
                                }

                                // Check if the player is a region members.
                            } else if (region.isMember(p.getUniqueId().toString())) {

                                p.sendActionBar(
                                        ChatUtils.success("You have entered ")
                                                .append(Component.text(region.getTag(p.getUniqueId().toString()),
                                                        NamedTextColor.DARK_AQUA))
                                                .append(ChatUtils.success(", you are a member of this region.")));
                                region.setLastEnter(p.getUniqueId().toString());

                                // If the region is inactive, make this member to owner.
                                if (region.status() == RegionStatus.INACTIVE) {
                                    // Make the previous owner a member.
                                    region.makeMember();

                                    // Give the new player ownership.
                                    region.makeOwner(p.getUniqueId().toString());

                                    // Update any requests to take into account the new region owner.
                                    region.updateRequests();

                                    p.sendMessage(ChatUtils.success("This region is no longer \"Inactive\", it has " +
                                            "been set back to default settings."));
                                    p.sendMessage(ChatUtils.success("You have been made the new region owner."));
                                }

                                // Check if the region is open and the player is at least jr.builder.
                            } else if (region.status() == RegionStatus.OPEN && p.hasPermission("group.jrbuilder")) {

                                p.sendActionBar(
                                        ChatUtils.success("You have entered ")
                                                .append(Component.text(region.getTag(p.getUniqueId().toString()),
                                                        NamedTextColor.DARK_AQUA))
                                                .append(ChatUtils.success(", you can build in this region.")));
                            } else {

                                // Send default enter message.
                                p.sendActionBar(
                                        ChatUtils.success("You have entered ")
                                                .append(Component.text(region.getTag(p.getUniqueId().toString()),
                                                        NamedTextColor.DARK_AQUA)));
                            }

                            // Update the region the player is in.
                            u.region = region;
                            u.inRegion = true;
                        } else {

                            // You can't enter this region.
                            p.sendMessage(ChatUtils.error("The terrain for this region has not been generated, you " +
                                    "must be at least Jr.Builder to load new terrain."));
                            e.setCancelled(true);
                        }
                    }
                } else if (u.inRegion) {

                    // Send default leave message.
                    p.sendActionBar(
                            ChatUtils.success("You have left ")
                                    .append(Component.text(u.region.regionName(), NamedTextColor.DARK_AQUA)));

                    // Set inRegion to false.
                    u.inRegion = false;
                }
            } else if (SERVER_TYPE == ServerType.PLOT) {

                // Check if the player is teleporting to a buildable world in the plot system.
                if (plotSQL.hasRow("SELECT name FROM location_data WHERE name='" + e.getTo().getWorld().getName() +
                        "';")) {

                    // Get negative coordinate transform of new location.
                    Location l = e.getTo().clone();
                    u.updateCoordinateTransform(plotSQL, l);

                    // Alter location to add necessary coordinate transformation.
                    l.setX(l.getX() + u.dx);
                    l.setZ(l.getZ() + u.dz);

                    // Get region.
                    Region region = regionManager.getRegion(l);

                    // Check if you are teleporting to a new region, if currently in one.
                    if (u.inRegion) {

                        // If the regions are not equal
                        if (!u.region.equals(region)) {

                            // Send default enter message.
                            p.sendActionBar(
                                    ChatUtils.success("You have entered ")
                                            .append(Component.text(region.regionName(), NamedTextColor.DARK_AQUA))
                                            .append(ChatUtils.success(" and left "))
                                            .append(Component.text(u.region.regionName(), NamedTextColor.DARK_AQUA)));

                            // Update the region the player is in.
                            u.region = region;
                        }
                    } else {

                        // Send default enter message.
                        p.sendActionBar(
                                ChatUtils.success("You have entered ")
                                        .append(Component.text(region.regionName(), NamedTextColor.DARK_AQUA)));

                        // Update the region the player is in.
                        u.region = region;
                        u.inRegion = true;
                    }
                } else if (u.inRegion) {

                    // Send default leave message.
                    p.sendActionBar(
                            ChatUtils.success("You have left ")
                                    .append(Component.text(u.region.regionName(), NamedTextColor.DARK_AQUA)));

                    // Set inRegion to false.
                    u.inRegion = false;
                }
            }
        }

        // Network.getInstance().getLogger().info("Teleport: " + e.getTo().getX() + "," + e.getTo().getZ());

    }
}
