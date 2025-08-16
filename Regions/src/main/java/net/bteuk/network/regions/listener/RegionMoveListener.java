package net.bteuk.network.regions.listener;

import lombok.extern.java.Log;
import net.bteuk.network.api.EventAPI;
import net.bteuk.network.api.PlotAPI;
import net.bteuk.network.api.SQLAPI;
import net.bteuk.network.api.ServerAPI;
import net.bteuk.network.core.Constants;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.papercore.PlayerAdapter;
import net.bteuk.network.regions.Region;
import net.bteuk.network.regions.RegionManager;
import net.bteuk.network.regions.RegionStatus;
import net.bteuk.network.regions.RegionUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

@Log
public class RegionMoveListener implements Listener {

    private final RegionManager regionManager;
    private final PlotAPI plotAPI;

    private final Constants constants;

    private final SQLAPI globalSQL;

    private final EventAPI eventAPI;

    private final ServerAPI serverAPI;

    private boolean blocked;

    public RegionMoveListener(JavaPlugin plugin, RegionManager regionManager, PlotAPI plotAPI, Constants constants, SQLAPI globalSQL, EventAPI eventAPI, ServerAPI serverAPI) {

        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);

        this.regionManager = regionManager;
        this.plotAPI = plotAPI;
        this.constants = constants;
        this.globalSQL = globalSQL;
        this.eventAPI = eventAPI;
        this.serverAPI = serverAPI;

        blocked = false;
    }

    public void block() {
        blocked = true;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {

        // TODO: Move to other listener instance.
        if (blocked) {
            e.setCancelled(true);
            return;
        }

        // If u is null, cancel.
        // TODO: Move to other listener instance.
        if (user == null) {
            LOGGER.severe("User " + p.getName() + " can not be found!");
            p.sendMessage(ChatUtils.error("User can not be found, please relog!"));
            e.setCancelled(true);
            return;
        }

        // Cancel event if player is switching server.
        // TODO: Move to other listener instance.
        if (user.switching) {
            e.setCancelled(true);
            return;
        }

        // Reset last movement of player, if they're afk unset that.
        // TODO: Move ot other listener instance.
        user.last_movement = Time.currentTime();
        if (user.afk) {
            user.afk = false;
            Afk.updateAfkStatus(user, false);
        }

        // If the movement event is already cancelled, skipt his entirely.
        if (e.isCancelled()) {
            return;
        }

        RegionUser regionUser = regionManager.getUserByPlayer(e.getPlayer());
        if (regionUser == null) {
            log.severe("Region user is null for player " + e.getPlayer().getName());
            return;
        }

        // If regions are enabled, check for movement between regions.
        // If the player is currently not in a region then that implies they are in a world without regions, so
        // movement will not effect this.
        // Not being in a region also means that region is null.
        if (constants.regionsEnabled() && regionUser.hasTrackedRegion()) {

            // Get x and z of the region as int rounded down with any necessary coordinate transforms.
            int x = ((e.getTo().getX() >= 0 ? (int) e.getTo().getX() : ((int) e.getTo().getX()) - 1) + regionUser.getDeltaX()) >> 9;
            int z = ((e.getTo().getZ() >= 0 ? (int) e.getTo().getZ() : ((int) e.getTo().getZ()) - 1) + regionUser.getDeltaZ()) >> 9;

            // Check if the player has moved to another region.
            if (!regionUser.getTrackedRegion().equals(x, z)) {

                // Get the new region.
                Region region = regionManager.getRegion(x, z);

                // Check if the player can enter the region.
                if (regionManager.inDatabase(region) || regionUser.getPlayer().hasPermission("uknet.regions.generate")) {
                    e.setCancelled(!switchRegion(regionUser, region, e.getTo()));
                } else {
                    // You can't enter this region.
                    e.setCancelled(true);
                    regionUser.getPlayer().sendMessage(ChatUtils.error("The terrain for this region has not been generated, you " +
                            "do not have permission load new terrain."));
                }
            }
        }
    }

    private boolean switchRegion(RegionUser regionUser, Region newRegion, Location newLocation) {

        // Check if the new region is on this server or not.
        if (!regionManager.getServer(regionUser.getTrackedRegion()).equals(regionManager.getServer(newRegion))) {

            // If cross-server teleport is enabled teleport them to the correct server and location.
            if (constants.standalone()) {
                // Cancel movement as the location is on another server, but the server doesn't support multiple servers.
                regionUser.getPlayer().sendMessage(ChatUtils.error("The terrain for this location is on another server, you may " +
                        "not enter."));
                return false;
            } else {
                // If the server is offline, notify the player.
                if (globalSQL.hasRow("SELECT 1 FROM server_data WHERE online=1 AND name='" + regionManager.getServer(newRegion) + "';")) {

                    // Add the region to the database if not exists.
                    regionManager.addToDatabase(newRegion);

                    // Region is on another server, teleport them accordingly.
                    // If the new region is on a plot server, check for coordinate transform.
                    String world = constants.earthWorld();
                    int xTransform = regionUser.getDeltaX();
                    int zTransform = regionUser.getDeltaZ();
                    if (regionManager.status(newRegion) == RegionStatus.PLOT) {
                        // Get server and world of the region.
                        String location = plotAPI.getRegionLocation(newRegion.regionName());

                        xTransform = plotAPI.getXTransform(location);
                        zTransform = plotAPI.getZTransform(location);

                        world = location;
                    }

                    // Set join event to teleport there.
                    eventAPI.createJoinEvent(regionUser.getPlayer().getUniqueId().toString(), "network",
                            "teleport " + world + " " + " " + (newLocation.getX() + xTransform) + " " + (newLocation.getZ() + zTransform) + " " + newLocation.getYaw() + " " + newLocation.getPitch());

                    // Switch server.
                    serverAPI.switchServer(PlayerAdapter.adapt(regionUser.getPlayer()), regionManager.getServer(newRegion));
                    return false;
                } else {
                    regionUser.getPlayer().sendMessage(ChatUtils.error("This region is on another server, however the server is currently offline."));
                }
            }
        } else {

            // Add the region to the database if not exists.
            regionManager.addToDatabase(newRegion);

            // If the player is the region owner update last enter and tell set the message.
            if (region.isOwner(p.getUniqueId().toString())) {

                p.sendActionBar(
                        ChatUtils.success("You have entered ")
                                .append(Component.text(region.getTag(p.getUniqueId().toString()),
                                        NamedTextColor.DARK_AQUA))
                                .append(ChatUtils.success(" and left "))
                                .append(Component.text(user.region.getTag(p.getUniqueId().toString()),
                                        NamedTextColor.DARK_AQUA))
                                .append(ChatUtils.success(", you are the owner of this region.")));
                region.setLastEnter(p.getUniqueId().toString());

                // If the region is inactive, set it to active.
                if (region.status() == RegionStatus.INACTIVE) {
                    region.setDefault();
                    p.sendMessage(ChatUtils.success("This region is no longer \"Inactive\", it has been " +
                            "set back to default settings."));
                }

                // Check if the player is a region members.
            } else if (region.isMember(p.getUniqueId().toString())) {

                p.sendActionBar(
                        ChatUtils.success("You have entered ")
                                .append(Component.text(region.getTag(p.getUniqueId().toString()),
                                        NamedTextColor.DARK_AQUA))
                                .append(ChatUtils.success(" and left "))
                                .append(Component.text(user.region.getTag(p.getUniqueId().toString()),
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

                    p.sendMessage(ChatUtils.success("This region is no longer \"Inactive\", it has been " +
                            "set back to default settings."));
                    p.sendMessage(ChatUtils.success("You have been made the new region owner."));
                }

                // Check if the region is open and the player is at least jr.builder.
            } else if (region.status() == RegionStatus.OPEN && p.hasPermission("group.jrbuilder")) {

                p.sendActionBar(
                        ChatUtils.success("You have entered ")
                                .append(Component.text(region.getTag(p.getUniqueId().toString()),
                                        NamedTextColor.DARK_AQUA))
                                .append(ChatUtils.success(" and left "))
                                .append(Component.text(user.region.getTag(p.getUniqueId().toString()),
                                        NamedTextColor.DARK_AQUA))
                                .append(ChatUtils.success(", you can build in this region.")));
            } else {

                // Send default enter message.
                p.sendActionBar(
                        ChatUtils.success("You have entered ")
                                .append(Component.text(region.getTag(p.getUniqueId().toString()),
                                        NamedTextColor.DARK_AQUA))
                                .append(ChatUtils.success(" and left "))
                                .append(Component.text(user.region.getTag(p.getUniqueId().toString()),
                                        NamedTextColor.DARK_AQUA)));
            }

            // Update the region the player is in.
            user.region = region;
            return true;
        }
    }
}
