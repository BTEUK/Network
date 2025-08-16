package net.bteuk.network.regions;

import lombok.extern.java.Log;
import net.bteuk.network.api.ChatAPI;
import net.bteuk.network.api.CoordinateAPI;
import net.bteuk.network.api.EventAPI;
import net.bteuk.network.api.PlotAPI;
import net.bteuk.network.api.SQLAPI;
import net.bteuk.network.api.ServerAPI;
import net.bteuk.network.api.WorldGuardAPI;
import net.bteuk.network.core.Constants;
import net.bteuk.network.core.ServerType;
import net.bteuk.network.core.Time;
import net.bteuk.network.lib.dto.ChatMessage;
import net.bteuk.network.lib.dto.DirectMessage;
import net.bteuk.network.lib.enums.ChatChannels;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.papercore.LocationAdapter;
import net.bteuk.network.regions.listener.RegionMoveListener;
import net.bteuk.network.regions.sql.RegionSQL;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Log
public class RegionManager {

    private final Map<String, Region> regions;
    private final RegionSQL regionSQL;
    private final SQLAPI globalSQL;
    private final PlotAPI plotAPI;
    private final ChatAPI chat;
    private final CoordinateAPI coordinateAPI;
    private final EventAPI eventAPI;
    private final WorldGuardAPI worldGuard;
    private final Constants constants;

    private List<RegionUser> users = new ArrayList<>();

    public RegionManager(RegionSQL regionSQL, SQLAPI globalSQL, PlotAPI plotAPI, ChatAPI chat, CoordinateAPI coordinateAPI, EventAPI eventAPI, WorldGuardAPI worldGuard,
                         Constants constants, JavaPlugin plugin, ServerAPI serverAPI) {
        regions = new HashMap<>();

        this.regionSQL = regionSQL;
        this.globalSQL = globalSQL;
        this.plotAPI = plotAPI;
        this.chat = chat;
        this.coordinateAPI = coordinateAPI;
        this.eventAPI = eventAPI;
        this.worldGuard = worldGuard;
        this.constants = constants;

        new RegionMoveListener(plugin, this, plotAPI, constants, globalSQL, eventAPI, serverAPI);
    }

    public RegionUser getUserByPlayer(Player player) {
        return users.stream().filter(user -> user.getPlayer().equals(player)).findFirst().orElse(null);
    }

    /**
     * Get the region at a location.
     *
     * @param x region x
     * @param z region z
     * @return the {@link Region}
     */
    public Region getRegion(int x, int z) {
        return findRegion(x, z);
    }

    // Get region at location.
    public Region getRegion(double xCoordinate, double zCoordinate) {
        // Get x and z of the region as int rounded down.
        int x = (xCoordinate >= 0 ? (int) xCoordinate : ((int) xCoordinate) - 1) >> 9;
        int z = (zCoordinate >= 0 ? (int) zCoordinate : ((int) zCoordinate) - 1) >> 9;

        return getRegion(x, z);
    }

    // Get region at location with coordinate transform.
    public Region getRegion(double xCoordinate, double zCoordinate, int dx, int dz) {
        // Get x and z of the region as int rounded down with any necessary coordinate transforms.
        int x = ((xCoordinate >= 0 ? (int) xCoordinate : ((int) xCoordinate) - 1) + dx) >> 9;
        int z = ((zCoordinate >= 0 ? (int) zCoordinate : ((int) zCoordinate) - 1) + dz) >> 9;

        return getRegion(x, z);
    }

    public Region getRegion(String region) throws NumberFormatException {
        int x = Integer.parseInt(region.split(",")[0]);
        int z = Integer.parseInt(region.split(",")[1]);
        return findRegion(x, z);
    }

    // Check whether the region exists in the database.
    // This is mainly used to check whether guests can teleport there.
    public boolean exists(String regionName) {
        return (regionSQL.hasRow("SELECT region FROM regions WHERE region='" + regionName + "';"));
    }

    private Region findRegion(int x, int z) {
        String regionName = x + "," + z;
        Region region = regions.get(regionName);
        if (region == null) {
            // Region does not exist, create it.
            region = new Region(regionName, x, z);
            regions.put(regionName, region);
        }
        return region;
    }

    // Get the tag of the region for a specific player, or name if no tag is set.
    public String getTag(Region region, String uuid) {
        if (hasTag(region, uuid)) {
            return regionSQL.getString(
                    "SELECT tag FROM region_members WHERE region='" + region.regionName() + "' AND uuid='" + uuid + "';");
        } else {
            return region.regionName();
        }
    }

    // Set the tag of the region for a specific player.
    public void setTag(Region region, String uuid, String tag) {
        regionSQL.update("UPDATE region_members SET tag='" + tag.replace("'", "\\'") + "' WHERE" +
                " region='" + region.regionName() + "' AND uuid='" + uuid + "';");
    }

    // Return whether the region has a tag for the specified uuid.
    private boolean hasTag(Region region, String uuid) {
        return (regionSQL.hasRow(
                "SELECT region FROM region_members WHERE region='" + region.regionName() + "' AND uuid='" + uuid + "' AND tag " +
                        "IS NOT NULL;"));
    }

    // Return whether the player has this region pinned.
    public boolean isPinned(Region region, String uuid) {
        return regionSQL.hasRow(
                "SELECT region FROM region_members WHERE region='" + region.regionName() + "' AND uuid='" + uuid + "' AND " +
                        "pinned=1;");
    }

    // Set the region to (un)pinned for a specific player.
    public void setPinned(Region region, String uuid, boolean pin) {
        regionSQL.update("UPDATE region_members SET pinned=" + (pin ? "1" : "0") + " WHERE " +
                "region='" + region.regionName() + "' AND uuid='" + uuid + "';");
    }

    // Get the server of the region.
    public String getServer(Region region) {
        if (regionSQL.hasRow("SELECT region FROM regions WHERE region='" + region.regionName() + "' AND " +
                "status='plot'")) {
            return (plotAPI.getRegionServer("SELECT server FROM regions WHERE region='" + region.regionName() + "';"));
        } else {
            return (globalSQL.getString("SELECT name FROM server_data WHERE type='EARTH';"));
        }
    }

    // Return whether the uuid is the uuid of the region owner.
    public boolean isOwner(Region region, String uuid) {
        return (regionSQL.hasRow(
                "SELECT region FROM region_members WHERE region='" + region.regionName() + "' AND uuid='" + uuid + "' AND " +
                        "is_owner=1;"));
    }

    // Return whether the uuid is the uuid of a region member.
    public boolean isMember(Region region, String uuid) {
        return (regionSQL.hasRow(
                "SELECT region FROM region_members WHERE region='" + region.regionName() + "' AND uuid='" + uuid + "' AND " +
                        "is_owner=0;"));
    }

    // Return the region status.
    public RegionStatus status(Region region) {
        String status =
                regionSQL.getString("SELECT status FROM regions WHERE region='" + region.regionName() +
                        "';");
        if (status == null) {
            return RegionStatus.DEFAULT;
        } else {
            return (RegionStatus.valueOf(status.toUpperCase(Locale.ROOT)));
        }
    }

    // Return whether the region is claimable.
    public boolean isClaimable(Region region) {
        return (regionSQL.hasRow("SELECT region FROM regions WHERE region='" + region.regionName() + "' " +
                "AND (status='default' OR status='public' OR status='inactive');"));
    }

    // Return whether the region has been claimed in the past.
    public boolean wasClaimed(Region region) {
        return (regionSQL.hasRow(
                "SELECT region FROM region_logs WHERE region='" + region.regionName() + "' AND is_owner=1;"));
    }

    // Set the region as inactive.
    public void setInactive(Region region) {
        regionSQL.update("UPDATE regions SET status='inactive' WHERE region='" + region.regionName() +
                "';");
    }

    // Check if the region is in the plot system.
    public boolean isPlot(Region region) {
        return regionSQL.hasRow("SELECT region FROM regions WHERE region='" + region.regionName() + "' " +
                "AND status='plot';");
    }

    // Set the region to be for plots.
    public void setPlot(Region region, ChatAPI chat) {

        // Kick members if exist.
        for (String uuid : getMembers(region)) {

            // Send message to user.
            // Is sent before actual removal so we can read the region tag.
            DirectMessage directMessage = new DirectMessage(ChatChannels.GLOBAL.getChannelName(), uuid, "server",
                    ChatUtils.error("You have been kicked from region %s, it has been moved to the plot system.",
                            getTag(region, uuid)),
                    true);
            chat.sendSocketMessage(directMessage);

            // Leave region in database.
            regionSQL.update("DELETE FROM region_members WHERE region='" + region.regionName() + "' AND " +
                    "uuid='" + uuid + "';");

            // Close log of player in region.
            regionSQL.update("UPDATE region_logs SET end_time=" + Time.currentTime()
                    + " WHERE region='" + region.regionName() + "' AND uuid='" + uuid + "';");

            // Leave region in worldGuard.
            worldGuard.addMember(region.regionName(), uuid, constants.earthWorld());
        }

        // Set region to plot.
        regionSQL.update("UPDATE regions SET status='plot' WHERE region='" + region.regionName() + "';");
        log.info("Region " + region.regionName() + " set to plot status.");
    }

    // Return whether the region has an owner.
    public boolean hasOwner(Region region) {
        return (regionSQL.hasRow(
                "SELECT region FROM region_members WHERE region='" + region.regionName() + "' AND is_owner=1;"));
    }

    // Return whether the region has an owner.
    public boolean hasActiveOwner(Region region) {
        return (regionSQL.hasRow(
                "SELECT region FROM region_members WHERE region='" + region.regionName() + "' AND is_owner=1 AND last_enter>=" + (Time.currentTime() - constants.regionInactivity()) + ";"));
    }

    // Return whether the region has a member.
    public boolean hasMember(Region region) {
        return (regionSQL.hasRow(
                "SELECT region FROM region_members WHERE region='" + region.regionName() + "' AND is_owner=0;"));
    }

    // Return whether the region has an active member.
    public boolean hasActiveMember(Region region, long time) {
        return (regionSQL.hasRow(
                "SELECT region FROM region_members WHERE region='" + region.regionName() + "' AND is_owner=0 AND last_enter>=" + time + ";"));
    }

    // Return the number of members, if any.
    public int memberCount(Region region) {
        return (regionSQL.getInt(
                "SELECT COUNT(uuid) FROM region_members WHERE region='" + region.regionName() + "' AND is_owner=0;"));
    }

    // Return the uuid of the region owner, if exists.
    public String getOwner(Region region) {
        if (hasOwner(region)) {
            return (regionSQL.getString(
                    "SELECT uuid FROM region_members WHERE region='" + region.regionName() + "' AND is_owner=1;"));
        } else {
            return "null";
        }
    }

    // Return the name of the region owner, if exists.
    public String ownerName(Region region) {
        if (hasOwner(region)) {
            return (globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + getOwner(region) + "';"));
        } else {
            return "nobody";
        }
    }

    // Return string array of member uuids.
    public ArrayList<String> getMembers(Region region) {
        return regionSQL.getStringList(
                "SELECT uuid FROM region_members WHERE region='" + region.regionName() + "';");
    }

    // Get the coordinate id for the location the player has set.
    public int getCoordinateID(Region region, String uuid) {
        return regionSQL.getInt(
                "SELECT coordinate_id FROM region_members WHERE region='" + region.regionName() + "' AND uuid='" + uuid + "';");
    }

    // Set the coordinate id for the location of the specified player.
    public void setCoordinateID(Region region, String uuid, int id) {
        regionSQL.update(
                "UPDATE region_members SET coordinate_id=" + id + " WHERE region='" + region.regionName() + "' AND uuid='" + uuid + "';");
    }

    // Set the last enter time for the region.
    public void setLastEnter(Region region, String uuid) {
        regionSQL.update("UPDATE region_members SET last_enter=" + Time.currentTime() + " WHERE" +
                " region='" + region.regionName() + "' AND uuid='" + uuid + "';");
    }

    // Get the most recent member to enter the region.
    public String getRecentMember(Region region) {
        if (hasMember(region)) {
            return regionSQL.getString(
                    "SELECT uuid FROM region_members WHERE region='" + region.regionName() + "' AND is_owner=0 ORDER BY " +
                            "last_enter DESC;");
        } else {
            return null;
        }
    }

    // Set the region to default.
    public void setDefault(Region region) {
        regionSQL.update("UPDATE regions SET status='default' WHERE region='" + region.regionName() +
                "';");
    }

    // Set the region to default.
    public void setDefault(Region region, String removeRole) {
        worldGuard.removeGroup(region.regionName(), removeRole, constants.earthWorld());
        regionSQL.update("UPDATE regions SET status='default' WHERE region='" + region.regionName() +
                "';");
    }

    // Set the region to public.
    public void setPublic(Region region) {
        regionSQL.update("UPDATE regions SET status='public' WHERE region='" + region.regionName() + "';");
    }

    // Set region to locked.
    public void setLocked(Region region) {

        // Remove all members and the owner.
        removeMembers(region, "The region %s has been locked, you can no longer build here.", false);

        // Set locked.
        regionSQL.update("UPDATE regions SET status='locked' WHERE region='" + region.regionName() + "';");
    }

    // Set region to open.
    public void setOpen(Region region) {

        // Remove all members and the owner.
        removeMembers(region, "The region %s is now open, you no longer need to claim it to build here.", true);

        // Set open.
        worldGuard.addGroup(region.regionName(), "jrbuilder", constants.earthWorld());
        regionSQL.update("UPDATE regions SET status='open' WHERE region='" + region.regionName() + "';");
    }

    // Check whether the region is in the database.
    public boolean inDatabase(Region region) {
        return (regionSQL.hasRow("SELECT region FROM regions WHERE region='" + region.regionName() + "';"));
    }

    // Add the region to the database.
    public void addToDatabase(Region region) {
        // Check if it's not already in the database.
        if (!inDatabase(region)) {
            regionSQL.update("INSERT INTO regions(region,status) VALUES('" + region.regionName() + "'," +
                    "'default');");

            createWorldGuardRegion(region);
        }
    }

    // Add the region to the database for plotsystem.
    public void addToPlotsystem(Region region) {
        // Check if it's not already in the database.
        if (!inDatabase(region)) {
            regionSQL.update("INSERT INTO regions(region,status) VALUES('" + region.regionName() + "'," +
                    "'plot');");

            createWorldGuardRegion(region);
        }
    }

    private void createWorldGuardRegion(Region region) {
        // Create region in worldguard.
        worldGuard.createRegion(region.regionName(), Integer.parseInt(region.regionName().split(",")[0]) * 512,
                Integer.parseInt(region.regionName().split(",")[1]) * 512,
                Integer.parseInt(region.regionName().split(",")[0]) * 512 + 511,
                Integer.parseInt(region.regionName().split(",")[1]) * 512 + 511,
                constants.earthWorld());
    }

    // Check if this region has any requests.
    public boolean hasRequests(Region region) {
        return regionSQL.hasRow(
                "SELECT region FROM region_requests WHERE region='" + region.regionName() + "';");
    }

    // Check if the player has already requested to join this region.
    public boolean hasRequest(Region region, String uuid) {
        return regionSQL.hasRow(
                "SELECT region FROM region_requests WHERE region='" + region.regionName() + "' AND uuid='" + uuid + "';");
    }

    // Check if the specified player has been invited to this region.
    public boolean hasInvite(Region region, String uuid) {
        return regionSQL.hasRow(
                "SELECT region FROM region_invites WHERE region='" + region.regionName() + "' AND uuid='" + uuid + "';");
    }

    // Remove a region invite.
    public void removeInvite(Region region, String uuid) {
        regionSQL.update("DELETE FROM region_invites WHERE region='" + region.regionName() + "' AND " +
                "uuid='" + uuid + "';");
    }

    // Accept any requests for this region.
    public void acceptRequests(Region region) {

        // Get all requests for this region by uuid.
        ArrayList<String> uuids = regionSQL.getStringList("SELECT uuid FROM region_requests " +
                "WHERE region='" + region.regionName() + "';");
        int coordinate_id;

        // Add all users to the region.
        for (String uuid : uuids) {

            // Get coordinate id from request.
            coordinate_id = regionSQL.getInt("SELECT coordinate_id FROM region_requests WHERE " +
                    "region='" + region.regionName() + "' AND uuid='" + uuid + "';");

            // Join region.
            joinRegion(region, uuid, coordinate_id);
        }

        // Clear all requests for the region.
        regionSQL.update("DELETE FROM region_requests WHERE region='" + region.regionName() + "';");
    }

    // Accept a request for a specific user.
    public void acceptRequest(Region region, String uuid) {

        // Get the coordinate id for the request.
        int coordinate_id = regionSQL.getInt("SELECT coordinate_id FROM region_requests WHERE " +
                "region='" + region.regionName() + "' AND uuid='" + uuid + "';");

        // Add them to the region.
        joinRegion(region, uuid, coordinate_id);

        // Delete request.
        regionSQL.update("DELETE FROM region_requests WHERE region='" + region.regionName() + "' AND " +
                "uuid='" + uuid + "'; ");
    }

    // Deny a request for a specific user.
    public void denyRequest(Region region, String uuid) {

        // Delete the request.
        regionSQL.update("DELETE FROM region_requests WHERE region='" + region.regionName() + "' AND " +
                "uuid='" + uuid + "';");

        // Send message to user.
        DirectMessage directMessage = new DirectMessage(ChatChannels.GLOBAL.getChannelName(), uuid, "server",
                ChatUtils.success("Your request to join region %s has been denied.", region.regionName()),
                true);
        chat.sendSocketMessage(directMessage);
    }

    // Cancel a request for a specific player.
    public void cancelRequest(Region region, Player player) {

        // Delete request.
        regionSQL.update("DELETE FROM region_requests WHERE region='" + region.regionName() + "' AND " +
                "uuid='" + player.getUniqueId() + "'; ");

        // Send message to player.
        player.sendMessage(ChatUtils.success("Cancelled region join request."));
    }

    // Join region with owner or staff request.
    // If staff request is true then it requires a staff request else it's an owner request.
    public void requestRegion(Region region, Player player, boolean staffRequest) {

        // Check if you don't already have a request for this region.
        if (hasRequest(region, player.getUniqueId().toString())) {

            player.sendMessage(ChatUtils.error("You have already requested to join this region."));
            player.sendMessage(ChatUtils.error("Check the request status in the region menu."));
            return;
        }

        // Get coordinate of player.
        int coordinate = coordinateAPI.addCoordinate(LocationAdapter.adapt(player.getLocation()));
        if (staffRequest) {
            // Staff request

            // Create request.
            regionSQL.update("INSERT INTO region_requests(region,uuid,owner,staff_accept," +
                    "coordinate_id) VALUES ('" + region.regionName() + "','" +
                    player.getUniqueId() + "','" + getOwner(region) + "',0," + coordinate + ");");

            // Send message to player.
            player.sendMessage(ChatUtils.success("Requested to join region ")
                    .append(Component.text(region.regionName(), NamedTextColor.DARK_AQUA))
                    .append(ChatUtils.success(", awaiting staff review.")));

            ChatMessage chatMessage = new ChatMessage(ChatChannels.REVIEWER.getChannelName(), "server",
                    ChatUtils.success("A region join request has been submitted by %s for region %s",
                            player.getName(), region.regionName()));
            chat.sendSocketMessage(chatMessage);
        } else {
            // Owner request

            // Create request.
            regionSQL.update("INSERT INTO region_requests(region,uuid,owner,owner_accept," +
                    "coordinate_id) VALUES ('" + region.regionName() + "','" +
                    player.getUniqueId() + "','" + getOwner(region) + "',0," + coordinate + ");");

            // Send message to player.
            player.sendMessage(ChatUtils.success("Requested to join region ")
                    .append(Component.text(region.regionName(), NamedTextColor.DARK_AQUA))
                    .append(ChatUtils.success(", awaiting owner review.")));

            // Send the owner a message.
            DirectMessage directMessage = new DirectMessage(ChatChannels.GLOBAL.getChannelName(), getOwner(region), "server",
                    ChatUtils.success("%s has requested to join region %s.", player.getName(), getTag(region, getOwner(region))),
                    false);
            chat.sendSocketMessage(directMessage);
        }
    }

    // Join region with no request.
    public void joinRegion(Region region, Player player) {

        // If region is public then it must already have an owner.
        if (status(region) == RegionStatus.PUBLIC) {

            // Add new coordinate at the location of the player.
            int coordinateID = coordinateAPI.addCoordinate(LocationAdapter.adapt(player.getLocation()));

            // Join region as member.
            regionSQL.update("INSERT INTO region_members(region,uuid,last_enter,coordinate_id) " +
                    "VALUES('" + region.regionName() + "','" +
                    player.getUniqueId() + "'," + Time.currentTime() + "," + coordinateID + ");");

            // Start log of player in region.
            regionSQL.update(
                    "INSERT INTO region_logs(region,uuid,start_time) VALUES('" + region.regionName() + "','" +
                            player.getUniqueId() + "'," + Time.currentTime() + ");");

            // Join region in worldGuard.
            worldGuard.addMember(region.regionName(), player.getUniqueId().toString(), player.getWorld().getName());

            player.sendMessage(ChatUtils.success("You have joined the region ")
                    .append(Component.text(region.regionName(), NamedTextColor.DARK_AQUA))
                    .append(ChatUtils.success(" as a member.")));
        } else {

            // If the region is inactive, demote the previous owner to a member.
            if (status(region) == RegionStatus.INACTIVE) {

                String owner = getOwner(region);

                // Demote owner in database.
                regionSQL.update(
                        "UPDATE region_members SET is_owner=0 WHERE region='" + region.regionName() + "' AND uuid='" + owner + "';");

                // Close log for owner.
                regionSQL.update("UPDATE region_logs SET end_time=" + Time.currentTime() + " " +
                        "WHERE region='" + region.regionName() + "' AND uuid='" + owner + "';");

                // Open log for previous owner as member.
                regionSQL.update(
                        "INSERT INTO region_logs(region,uuid,start_time) VALUES('" + region.regionName() + "','" +
                                owner + "'," + Time.currentTime() + ");");

                DirectMessage directMessage = new DirectMessage(ChatChannels.GLOBAL.getChannelName(), owner, "server",
                        ChatUtils.success("You have been demoted to a member in region %s due to inactivity.",
                                getTag(region, owner)),
                        true);
                chat.sendSocketMessage(directMessage);

                // Set region to default, since it would've been set to inactive previously.
                setDefault(region);
            }

            // Add new coordinate at the location of the player.
            int coordinateID = coordinateAPI.addCoordinate(LocationAdapter.adapt(player.getLocation()));

            // Join region as owner.
            regionSQL.update("INSERT INTO region_members(region,uuid,is_owner,last_enter," +
                    "coordinate_id) VALUES('" + region.regionName() + "','" +
                    player.getUniqueId() + "',1," + Time.currentTime() + "," + coordinateID + ");");

            // Start log of player in region.
            regionSQL.update("INSERT INTO region_logs(region,uuid,is_owner,start_time) VALUES" +
                    "('" + region.regionName() + "','" +
                    player.getUniqueId() + "',1," + Time.currentTime() + ");");

            // Join region in worldGuard.
            worldGuard.addMember(region.regionName(), player.getUniqueId().toString(), player.getWorld().getName());

            player.sendMessage(ChatUtils.success("You have joined the region ")
                    .append(Component.text(region.regionName(), NamedTextColor.DARK_AQUA))
                    .append(ChatUtils.success(" as the owner.")));
        }
    }

    // Join region if we don't know whether the user is online.
    public void joinRegion(Region region, String uuid, int coordinateID) {

        // If region has an owner join as member.
        if (hasOwner(region)) {

            // Join region as member.
            regionSQL.update("INSERT INTO region_members(region,uuid,last_enter,coordinate_id) " +
                    "VALUES('" + region.regionName() + "','" +
                    uuid + "'," + Time.currentTime() + "," + coordinateID + ");");

            // Start log of player in region.
            regionSQL.update(
                    "INSERT INTO region_logs(region,uuid,start_time) VALUES('" + region.regionName() + "','" +
                            uuid + "'," + Time.currentTime() + ");");

            // Join region in worldGuard.
            worldGuard.addMember(region.regionName(), uuid, constants.earthWorld());

            // Send message to user.
            DirectMessage directMessage = new DirectMessage(ChatChannels.GLOBAL.getChannelName(), uuid, "server",
                    ChatUtils.success("You have joined the region %s as a member.", region.regionName()),
                    true);
            chat.sendSocketMessage(directMessage);
        } else {

            // If the region is inactive, demote the previous owner to a member.
            if (status(region) == RegionStatus.INACTIVE) {

                String owner = getOwner(region);

                // Demote owner in database.
                regionSQL.update(
                        "UPDATE region_members SET is_owner=0 WHERE region='" + region.regionName() + "' AND uuid='" + owner + "';");

                // Close log for owner.
                regionSQL.update("UPDATE region_logs SET end_time=" + Time.currentTime() + " " +
                        "WHERE region='" + region.regionName() + "' AND uuid='" + owner + "';");

                // Open log for previous owner as member.
                regionSQL.update(
                        "INSERT INTO region_logs(region,uuid,start_time) VALUES('" + region.regionName() + "','" +
                                owner + "'," + Time.currentTime() + ");");

                DirectMessage directMessage = new DirectMessage(ChatChannels.GLOBAL.getChannelName(), owner, "server",
                        ChatUtils.success("You have been demoted to a member in region %s due to inactivity.",
                                getTag(region, owner)),
                        true);
                chat.sendSocketMessage(directMessage);
            }

            // Join region as owner.
            regionSQL.update("INSERT INTO region_members(region,uuid,is_owner,last_enter," +
                    "coordinate_id) VALUES('" + region.regionName() + "','" +
                    uuid + "',1," + Time.currentTime() + "," + coordinateID + ");");

            // Start log of player in region.
            regionSQL.update("INSERT INTO region_logs(region,uuid,is_owner,start_time) VALUES" +
                    "('" + region.regionName() + "','" +
                    uuid + "',1," + Time.currentTime() + ");");

            // Join region in worldGuard.
            worldGuard.addMember(region.regionName(), uuid, constants.earthWorld());

            DirectMessage directMessage = new DirectMessage(ChatChannels.GLOBAL.getChannelName(), uuid, "server",
                    ChatUtils.success("You have joined the region %s as the owner.", region.regionName()),
                    true);
            chat.sendSocketMessage(directMessage);
        }
    }

    // Leave region.
    public void leaveRegion(Region region, String uuid, Component message) {

        // Check if this is the correct server.
        // If this is not the earth server then create a server-event.
        if (constants.serverType() == ServerType.EARTH) {

            // Send message to user.
            // Is sent before actual removal so we can read the region tag.
            DirectMessage directMessage = new DirectMessage(ChatChannels.GLOBAL.getChannelName(), uuid, "server",
                    message, true);
            chat.sendSocketMessage(directMessage);

            // Leave region in database.
            regionSQL.update("DELETE FROM region_members WHERE region='" + region.regionName() + "' AND " +
                    "uuid='" + uuid + "';");

            // Close log of player in region.
            regionSQL.update("UPDATE region_logs SET end_time=" + Time.currentTime()
                    + " WHERE region='" + region.regionName() + "' AND uuid='" + uuid + "';");

            // Leave region in worldGuard.
            worldGuard.removeMember(region.regionName(), uuid, constants.earthWorld());
        } else {

            eventAPI.createEvent(uuid, "network", globalSQL.getString("SELECT name " +
                            "FROM server_data WHERE type='EARTH';"),
                    "region leave " + region.regionName(), message);
        }
    }

    // Make the owner a member of the region.
    public void makeMember(Region region) {

        // Get the owner.
        String uuid = getOwner(region);

        // Close log of player as owner.
        regionSQL.update("UPDATE region_logs SET end_time=" + Time.currentTime()
                + " WHERE region='" + region.regionName() + "' AND uuid='" + uuid + "';");

        // Open log of player as member.
        regionSQL.update(
                "INSERT INTO region_logs(region,uuid,start_time) VALUES('" + region.regionName() + "','" +
                        uuid + "'," + Time.currentTime() + ");");

        // Update region member to set as member.
        regionSQL.update("UPDATE region_members SET is_owner=0 WHERE region='" + region.regionName() +
                "' AND uuid='" + uuid + "';");
    }

    // Make a member the owner of the region.
    public void makeOwner(Region region, String uuid) {

        // Check if they are a member.
        if (isMember(region, uuid)) {

            // Close log of player as member.
            regionSQL.update("UPDATE region_logs SET end_time=" + Time.currentTime()
                    + " WHERE region='" + region.regionName() + "' AND uuid='" + uuid + "';");

            // Open log of player as owner.
            regionSQL.update("INSERT INTO region_logs(region,uuid,is_owner,start_time) VALUES" +
                    "('" + region.regionName() + "','" +
                    uuid + "',1," + Time.currentTime() + ");");

            // Update region member to set as owner.
            regionSQL.update(
                    "UPDATE region_members SET is_owner=1 WHERE region='" + region.regionName() + "' AND uuid='" + uuid + "';");

            // If the region is currently set as inactive and the new owner isn't, set it to default.
            if (status(region) == RegionStatus.INACTIVE && hasActiveOwner(region)) {
                setDefault(region);
            }
        }
    }

    // Update any region requests.
    public void updateRequests(Region region) {

        // If the region has no owner, accept all requests.
        // If there is an owner, update the owner row in the request.
        if (hasOwner(region)) {

            regionSQL.update("UPDATE region_requests SET owner='" + getOwner(region) + "' WHERE " +
                    "region='" + region.regionName() + "';");
        } else {

            acceptRequests(region);
        }
    }

    // Remove all members and owner of the region.
    // The placeholder %tag% is used in the message to show the region tag.
    public void removeMembers(Region region, String message, boolean success) {

        if (hasOwner(region) || hasMember(region)) {

            // Get all members.
            ArrayList<String> uuids = regionSQL.getStringList("SELECT uuid FROM region_members " +
                    "WHERE region='" + region.regionName() + "';");

            for (String uuid : uuids) {
                leaveRegion(region, uuid, success ? ChatUtils.success(message, getTag(region, uuid)) : ChatUtils.error(message,
                        getTag(region, uuid)));
            }
        }
    }

    // Get time that a region member was last in this region.
    public long lastActive(Region region, String uuid) {
        return (regionSQL.getLong(
                "SELECT last_enter FROM region_members WHERE region='" + region.regionName() + "' AND uuid='" + uuid + "';"));
    }

    // Check if the player can build in this region.
    public boolean canBuild(Region region, Player p) {
        return ((status(region) == RegionStatus.OPEN && p.hasPermission("group.jrbuilder")) || isOwner(region, p.getUniqueId().toString()) || isMember(region,
                p.getUniqueId().toString()));
    }
}
