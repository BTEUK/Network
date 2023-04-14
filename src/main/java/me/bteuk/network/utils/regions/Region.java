package me.bteuk.network.utils.regions;

import me.bteuk.network.Network;
import me.bteuk.network.events.EventManager;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Time;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.WorldGuard;
import me.bteuk.network.utils.enums.RegionStatus;
import me.bteuk.network.utils.enums.ServerType;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Objects;

import static me.bteuk.network.utils.Constants.EARTH_WORLD;
import static me.bteuk.network.utils.Constants.SERVER_TYPE;
import static me.bteuk.network.utils.NetworkConfig.CONFIG;

public record Region(String regionName) {

    //Get the tag of the region for a specific player, or name if no tag is set.
    public String getTag(String uuid) {
        if (hasTag(uuid)) {
            return Network.getInstance().regionSQL.getString("SELECT tag FROM region_members WHERE region='" + regionName + "' AND uuid='" + uuid + "';");
        } else {
            return regionName;
        }
    }

    //Set the tag of the region for a specific player.
    public void setTag(String uuid, String tag) {
        Network.getInstance().regionSQL.update("UPDATE region_members SET tag='" + tag.replace("'", "\\'")+ "' WHERE region='" + regionName + "' AND uuid='" + uuid + "';");
    }

    //Return whether the region has a tag for the specified uuid.
    private boolean hasTag(String uuid) {
        return (Network.getInstance().regionSQL.hasRow("SELECT region FROM region_members WHERE region='" + regionName + "' AND uuid='" + uuid + "' AND tag IS NOT NULL;"));
    }

    //Get the server of the region.
    public String getServer() {
        if (Network.getInstance().regionSQL.hasRow("SELECT region FROM regions WHERE region='" + regionName + "' AND status='plot'")) {
            //Region is on a plot server.
            return (Network.getInstance().plotSQL.getString("SELECT server FROM regions WHERE region='" + regionName + "';"));
        } else {
            //Region is on earth server.
            return (Network.getInstance().globalSQL.getString("SELECT name FROM server_data WHERE type='EARTH';"));
        }
    }

    //Return whether the uuid is the uuid of the region owner.
    public boolean isOwner(String uuid) {
        return (Network.getInstance().regionSQL.hasRow("SELECT region FROM region_members WHERE region='" + regionName + "' AND uuid='" + uuid + "' AND is_owner=1;"));
    }

    //Return whether the uuid is the uuid of a region member.
    public boolean isMember(String uuid) {
        return (Network.getInstance().regionSQL.hasRow("SELECT region FROM region_members WHERE region='" + regionName + "' AND uuid='" + uuid + "' AND is_owner=0;"));
    }

    //Return the region status.
    public RegionStatus status() {
        return (RegionStatus.valueOf(Network.getInstance().regionSQL.getString("SELECT status FROM regions WHERE region='" + regionName + "';")));
    }

    //Return whether the region is claimable.
    public boolean isClaimable() {
        return (Network.getInstance().regionSQL.hasRow("SELECT region FROM regions WHERE region='" + regionName + "' AND (status='default' OR status='public' OR status='inactive');"));
    }

    //Return whether the region has been claimed in the past.
    public boolean wasClaimed() {
        return (Network.getInstance().regionSQL.hasRow("SELECT region FROM region_logs WHERE region='" + regionName + "' AND is_owner=1;"));
    }

    //Set the region as inactive.
    public void setInactive() {
        Network.getInstance().regionSQL.update("UPDATE regions SET status='inactive' WHERE region='" + regionName + "';");
    }

    //Set the region to be for plots.
    public void setPlot() {

        //Kick members if exist.
        for (String uuid : getMembers()) {

            //Send message to user.
            //Is sent before actual removal so we can read the region tag.
            Network.getInstance().globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" + uuid + "','&aYou have been kicked from region &3" + getTag(uuid) + ", it has been moved to the plot system.')");

            //Leave region in database.
            Network.getInstance().regionSQL.update("DELETE FROM region_members WHERE region='" + regionName + "' AND uuid='" + uuid + "';");

            //Close log of player in region.
            Network.getInstance().regionSQL.update("UPDATE region_logs SET end_time=" + Time.currentTime()
                    + " WHERE region='" + regionName + "' AND uuid='" + uuid + "';");

            //Leave region in WorldGuard.
            WorldGuard.addMember(regionName, uuid, Bukkit.getWorld(Objects.requireNonNull(EARTH_WORLD)));

        }

        //Set region to plot.
        Network.getInstance().regionSQL.update("UPDATE regions SET status='plot' WHERE region='" + regionName + "';");
        Network.getInstance().getLogger().info("Region " + regionName + " set to plot status.");
    }

    //Return whether the region has an owner.
    public boolean hasOwner() {
        return (Network.getInstance().regionSQL.hasRow("SELECT region FROM region_members WHERE region='" + regionName + "' AND is_owner=1;"));
    }

    //Return whether the region has an owner.
    public boolean hasActiveOwner() {
        return (Network.getInstance().regionSQL.hasRow("SELECT region FROM region_members WHERE region='" + regionName + "' AND is_owner=1 AND last_enter>=" + (Time.currentTime()-Network.getInstance().timers.inactivity) + ";"));
    }

    //Return whether the region has a member.
    public boolean hasMember() {
        return (Network.getInstance().regionSQL.hasRow("SELECT region FROM region_members WHERE region='" + regionName + "' AND is_owner=0;"));
    }

    //Return whether the region has an active member.
    public boolean hasActiveMember(long time) {
        return (Network.getInstance().regionSQL.hasRow("SELECT region FROM region_members WHERE region='" + regionName + "' AND is_owner=0 AND last_enter>=" + time + ";"));
    }

    //Return the number of members, if any.
    public int memberCount() {
        return (Network.getInstance().regionSQL.getInt("SELECT COUNT(uuid) FROM region_members WHERE region='" + regionName + "' AND is_owner=0;"));
    }

    //Return the uuid of the region owner, if exists.
    public String getOwner() {
        if (hasOwner()) {
            return (Network.getInstance().regionSQL.getString("SELECT uuid FROM region_members WHERE region='" + regionName + "' AND is_owner=1;"));
        } else {
            return "null";
        }
    }

    //Return the name of the region owner, if exists.
    public String ownerName() {
        if (hasOwner()) {
            return (Network.getInstance().globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + getOwner() + "';"));
        } else {
            return "nobody";
        }
    }

    //Return string array of member uuids.
    public ArrayList<String> getMembers() {
        return Network.getInstance().regionSQL.getStringList("SELECT uuid FROM region_members WHERE region='" + regionName + "';");
    }

    //Get the coordinate id for the location the player has set.
    public int getCoordinateID(String uuid) {
        return Network.getInstance().regionSQL.getInt("SELECT coordinate_id FROM region_members WHERE region='" + regionName + "' AND uuid='" + uuid + "';");
    }

    //Set the coordinate id for the location of the specified player.
    public void setCoordinateID(String uuid, int id) {
        Network.getInstance().regionSQL.update("UPDATE region_members SET coordinate_id=" + id + " WHERE region='" + regionName + "' AND uuid='" + uuid + "';");
    }

    //Set the last enter time for the region.
    public void setLastEnter(String uuid) {
        Network.getInstance().regionSQL.update("UPDATE region_members SET last_enter=" + Time.currentTime() + " WHERE region='" + regionName + "' AND uuid='" + uuid + "';");
    }

    //Get the most recent member to enter the region.
    public String getRecentMember() {
        if (hasMember()) {
            return Network.getInstance().regionSQL.getString("SELECT uuid FROM region_members WHERE region='" + regionName + "' AND is_owner=0 ORDER BY last_enter DESC;");
        } else {
            return null;
        }
    }

    //Set the region to default.
    public void setDefault() {
        Network.getInstance().regionSQL.update("UPDATE regions SET status='default' WHERE region='" + regionName + "';");
    }

    //Set the region to default.
    public void setDefault(String removeRole) {
        WorldGuard.removeGroup(regionName, removeRole, Bukkit.getWorld(Objects.requireNonNull(EARTH_WORLD)));
        Network.getInstance().regionSQL.update("UPDATE regions SET status='default' WHERE region='" + regionName + "';");
    }

    //Set the region to public.
    public void setPublic() {
        Network.getInstance().regionSQL.update("UPDATE regions SET status='public' WHERE region='" + regionName + "';");
    }

    //Set region to locked.
    public void setLocked() {

        //Remove all members and the owner.
        removeMembers("&cThe region &4%tag% &chas been locked, you can no longer build here.");

        //Set locked.
        Network.getInstance().regionSQL.update("UPDATE regions SET status='locked' WHERE region='" + regionName + "';");

    }

    //Set region to open.
    public void setOpen() {

        //Remove all members and the owner.
        removeMembers("&aThe region &3%tag% &ais now open, you no longer need to claimed it to build here.");

        //Set open.
        WorldGuard.addGroup(regionName, "jrbuilder", Bukkit.getWorld(Objects.requireNonNull(EARTH_WORLD)));
        Network.getInstance().regionSQL.update("UPDATE regions SET status='open' WHERE region='" + regionName + "';");

    }

    //Check whether the region equals another region.
    public boolean equals(Region region) {
        return (regionName.equals(region.regionName()));
    }

    //Check whether the region is in the database.
    public boolean inDatabase() {
        return (Network.getInstance().regionSQL.hasRow("SELECT region FROM regions WHERE region='" + regionName + "';"));
    }

    //Add the region to the database.

    public void addToDatabase() {
        //Check if it's not already in the database.
        if (!inDatabase()) {
            Network.getInstance().regionSQL.update("INSERT INTO regions(region,status) VALUES('" + regionName + "','default');");

            //Create region in worldguard.
            WorldGuard.createRegion(regionName, Integer.parseInt(regionName.split(",")[0]) * 512, Integer.parseInt(regionName.split(",")[1]) * 512,
                    Integer.parseInt(regionName.split(",")[0]) * 512 + 511, Integer.parseInt(regionName.split(",")[1]) * 512 + 511,
                    Bukkit.getWorld(Objects.requireNonNull(EARTH_WORLD)));
        }
    }

    //Add the region to the database for plotsystem.
    public void addToPlotsystem() {
        //Check if it's not already in the database.
        if (!inDatabase()) {
            Network.getInstance().regionSQL.update("INSERT INTO regions(region,status) VALUES('" + regionName + "','plot');");

            //Create region in worldguard.
            WorldGuard.createRegion(regionName, Integer.parseInt(regionName.split(",")[0]) * 512, Integer.parseInt(regionName.split(",")[1]) * 512,
                    Integer.parseInt(regionName.split(",")[0]) * 512 + 511, Integer.parseInt(regionName.split(",")[1]) * 512 + 511,
                    Bukkit.getWorld(Objects.requireNonNull(EARTH_WORLD)));
        }
    }

    //Check if this region has any requests.
    public boolean hasRequests() {
        return Network.getInstance().regionSQL.hasRow("SELECT region FROM region_requests WHERE region='" + regionName + "';");
    }

    //Check if the player has already requested to join this region.
    public boolean hasRequest(NetworkUser u) {
        return Network.getInstance().regionSQL.hasRow("SELECT region FROM region_requests WHERE region='" + regionName + "' AND uuid='" + u.player.getUniqueId() + "';");
    }

    //Check if the specified player has been invited to this region.
    public boolean hasInvite(String uuid) {
        return Network.getInstance().regionSQL.hasRow("SELECT region FROM region_invites WHERE region='" + regionName + "' AND uuid='" + uuid + "';");
    }

    //Remove a region invite.
    public void removeInvite(String uuid) {
        Network.getInstance().regionSQL.update("DELETE FROM region_invites WHERE region='" + regionName + "' AND uuid='" + uuid + "';");
    }

    //Accept any requests for this region.
    public void acceptRequests() {

        //Get all requests for this region by uuid.
        ArrayList<String> uuids = Network.getInstance().regionSQL.getStringList("SELECT uuid FROM region_requests WHERE region='" + regionName + "';");
        int coordinate_id;

        //Add all users to the region.
        for (String uuid : uuids) {

            //Get coordinate id from request.
            coordinate_id = Network.getInstance().regionSQL.getInt("SELECT coordinate_id FROM region_requests WHERE region='" + regionName + "' AND uuid='" + uuid + "';");

            //Join region.
            joinRegion(uuid, coordinate_id);

        }

        //Clear all requests for the region.
        Network.getInstance().regionSQL.update("DELETE FROM region_requests WHERE region='" + regionName + "';");

    }

    //Accept a request for a specific user.
    public void acceptRequest(String uuid) {

        //Get the coordinate id for the request.
        int coordinate_id = Network.getInstance().regionSQL.getInt("SELECT coordinate_id FROM region_requests WHERE region='" + regionName + "' AND uuid='" + uuid + "';");

        //Add them to the region.
        joinRegion(uuid, coordinate_id);

        //Delete request.
        Network.getInstance().regionSQL.update("DELETE FROM region_requests WHERE region='" + regionName + "' AND uuid='" + uuid + "'; ");

    }

    //Deny a request for a specific user.
    public void denyRequest(String uuid) {

        //Delete the request.
        Network.getInstance().regionSQL.update("DELETE FROM region_requests WHERE region='" + regionName + "' AND uuid='" + uuid + "';");

        //Send message to user.
        Network.getInstance().globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" + uuid + "','&aYour request to join region &3" + regionName + " &ahas been denied.');");

    }

    //Cancel a request for a specific user.
    public void cancelRequest(NetworkUser u) {

        //Delete request.
        Network.getInstance().regionSQL.update("DELETE FROM region_requests WHERE region='" + regionName + "' AND uuid='" + u.player.getUniqueId() + "'; ");

        //Send message to user.
        u.player.sendMessage(Utils.success("&cCancelled region join request."));
    }

    //Join region with owner or staff request.
    //If staff request is true then it requires a staff request else it's an owner request.
    public void requestRegion(NetworkUser u, boolean staffRequest) {

        //Check if you don't already have a request for this region.
        if (hasRequest(u)) {

            u.player.sendMessage(Utils.error("You have already requested to join this region."));
            u.player.sendMessage(Utils.error("Check the request status in the region menu."));
            return;
        }

        //Get coordinate of player.
        int coordinate = Network.getInstance().globalSQL.addCoordinate(u.player.getLocation());
        if (staffRequest) {
            //Staff request

            //Create request.
            Network.getInstance().regionSQL.update("INSERT INTO region_requests(region,uuid,owner,staff_accept,coordinate_id) VALUES ('" + regionName + "','" +
                    u.player.getUniqueId() + "','" + getOwner() + "',0," + coordinate + ");");

            //Send message to player.
            u.player.sendMessage(Utils.success("Requested to join region &3" + regionName + "&a, awaiting staff review."));

            Network.getInstance().chat.broadcastMessage("&aA region join request has been submitted by &3" + u.player.getName() + "&a for region &3" + regionName + "&a.", "uknet:reviewer");

        } else {
            //Owner request

            //Create request.
            Network.getInstance().regionSQL.update("INSERT INTO region_requests(region,uuid,owner,owner_accept,coordinate_id) VALUES ('" + regionName + "','" +
                    u.player.getUniqueId() + "','" + getOwner() + "',0," + coordinate + ");");

            //Send message to player.
            u.player.sendMessage(Utils.success("Requested to join region &3" + regionName + "&a, awaiting owner review."));

            //If owner is in the online users list send a message.
            if (Network.getInstance().globalSQL.hasRow("SELECT uuid FROM online_users WHERE uuid='" + getOwner() + "';")) {
                Network.getInstance().globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" + getOwner() + "','&3" + u.player.getName() + " &ahas requested to join region &3" + getTag(getOwner()) + "');");
            }
        }
    }

    //Join region with no request.
    public void joinRegion(NetworkUser u) {

        //If region is public then it must already have an owner.
        if (status() == RegionStatus.PUBLIC) {

            //Add new coordinate at the location of the player.
            int coordinateID = Network.getInstance().globalSQL.addCoordinate(u.player.getLocation());

            //Join region as member.
            Network.getInstance().regionSQL.update("INSERT INTO region_members(region,uuid,last_enter,coordinate_id) VALUES('" + regionName + "','" +
                    u.player.getUniqueId() + "'," + Time.currentTime() + "," + coordinateID + ");");

            //Start log of player in region.
            Network.getInstance().regionSQL.update("INSERT INTO region_logs(region,uuid,start_time) VALUES('" + regionName + "','" +
                    u.player.getUniqueId() + "'," + Time.currentTime() + ");");

            //Join region in WorldGuard.
            WorldGuard.addMember(regionName, u.player.getUniqueId().toString(), u.player.getWorld());

            u.player.sendMessage(Utils.success("You have joined the region &3" + regionName + " &aas a member."));

        } else {

            //If the region is inactive, demote the previous owner to a member.
            if (status() == RegionStatus.INACTIVE) {

                String owner = getOwner();

                //Demote owner in database.
                Network.getInstance().regionSQL.update("UPDATE region_members SET is_owner=0 WHERE region='" + regionName + "' AND uuid='" + owner + "';");

                //Close log for owner.
                Network.getInstance().regionSQL.update("UPDATE region_logs SET end_time=" + Time.currentTime() + " WHERE region='" + regionName + "' AND uuid='" + owner + "';");

                //Open log for previous owner as member.
                Network.getInstance().regionSQL.update("INSERT INTO region_logs(region,uuid,start_time) VALUES('" + regionName + "','" +
                        owner + "'," + Time.currentTime() + ");");

                Network.getInstance().globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" + owner + "','&aYou have been demoted to a member in region &3"
                        + getTag(owner) + " &adue to inactivity.');");

                //Set region to default, since it would've been set to inactive previously.
                setDefault();

            }

            //Add new coordinate at the location of the player.
            int coordinateID = Network.getInstance().globalSQL.addCoordinate(u.player.getLocation());

            //Join region as owner.
            Network.getInstance().regionSQL.update("INSERT INTO region_members(region,uuid,is_owner,last_enter,coordinate_id) VALUES('" + regionName + "','" +
                    u.player.getUniqueId() + "',1," + Time.currentTime() + "," + coordinateID + ");");

            //Start log of player in region.
            Network.getInstance().regionSQL.update("INSERT INTO region_logs(region,uuid,is_owner,start_time) VALUES('" + regionName + "','" +
                    u.player.getUniqueId() + "',1," + Time.currentTime() + ");");

            //Join region in WorldGuard.
            WorldGuard.addMember(regionName, u.player.getUniqueId().toString(), u.player.getWorld());

            u.player.sendMessage(Utils.success("You have joined the region &3" + regionName + " &aas the owner."));

        }
    }

    //Join region if we don't know whether the user is online.
    public void joinRegion(String uuid, int coordinateID) {

        //If region has an owner join as member.
        if (hasOwner()) {

            //Join region as member.
            Network.getInstance().regionSQL.update("INSERT INTO region_members(region,uuid,last_enter,coordinate_id) VALUES('" + regionName + "','" +
                    uuid + "'," + Time.currentTime() + "," + coordinateID + ");");

            //Start log of player in region.
            Network.getInstance().regionSQL.update("INSERT INTO region_logs(region,uuid,start_time) VALUES('" + regionName + "','" +
                    uuid + "'," + Time.currentTime() + ");");

            //Join region in WorldGuard.
            WorldGuard.addMember(regionName, uuid, Bukkit.getWorld(Objects.requireNonNull(EARTH_WORLD)));

            //Send message to user.
            Network.getInstance().globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" + uuid + "','&aYou have joined the region &3" + regionName + " &aas a member.');");

        } else {

            //If the region is inactive, demote the previous owner to a member.
            if (status() == RegionStatus.INACTIVE) {

                String owner = getOwner();

                //Demote owner in database.
                Network.getInstance().regionSQL.update("UPDATE region_members SET is_owner=0 WHERE region='" + regionName + "' AND uuid='" + owner + "';");

                //Close log for owner.
                Network.getInstance().regionSQL.update("UPDATE region_logs SET end_time=" + Time.currentTime() + " WHERE region='" + regionName + "' AND uuid='" + owner + "';");

                //Open log for previous owner as member.
                Network.getInstance().regionSQL.update("INSERT INTO region_logs(region,uuid,start_time) VALUES('" + regionName + "','" +
                        owner + "'," + Time.currentTime() + ");");

                Network.getInstance().globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" + owner + "','&aYou have been demoted to a member in region &3"
                        + getTag(owner) + " &adue to inactivity.');");

            }

            //Join region as owner.
            Network.getInstance().regionSQL.update("INSERT INTO region_members(region,uuid,is_owner,last_enter,coordinate_id) VALUES('" + regionName + "','" +
                    uuid + "',1," + Time.currentTime() + "," + coordinateID + ");");

            //Start log of player in region.
            Network.getInstance().regionSQL.update("INSERT INTO region_logs(region,uuid,is_owner,start_time) VALUES('" + regionName + "','" +
                    uuid + "',1," + Time.currentTime() + ");");

            //Join region in WorldGuard.
            WorldGuard.addMember(regionName, uuid, Bukkit.getWorld(Objects.requireNonNull(EARTH_WORLD)));

            Network.getInstance().globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" + uuid + "','&aYou have joined the region &3" + regionName + " &aas the owner.');");

        }
    }

    //Leave region.
    public void leaveRegion(String uuid, String message) {

        //Check if this is the correct server.
        //If this is not the earth server then create a server-event.
        if (SERVER_TYPE == ServerType.EARTH) {

            //Send message to user.
            //Is sent before actual removal so we can read the region tag.
            Network.getInstance().globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" + uuid + "','" + message + "')");

            //Leave region in database.
            Network.getInstance().regionSQL.update("DELETE FROM region_members WHERE region='" + regionName + "' AND uuid='" + uuid + "';");

            //Close log of player in region.
            Network.getInstance().regionSQL.update("UPDATE region_logs SET end_time=" + Time.currentTime()
                    + " WHERE region='" + regionName + "' AND uuid='" + uuid + "';");

            //Leave region in WorldGuard.
            WorldGuard.removeMember(regionName, uuid, Bukkit.getWorld(Objects.requireNonNull(EARTH_WORLD)));

        } else {

            EventManager.createEvent(uuid, "network", Network.getInstance().globalSQL.getString("SELECT name FROM server_data WHERE type='EARTH';"),
                    "region leave " + regionName, message);

        }
    }

    //Make the owner a member of the region.
    public void makeMember() {

        //Get the owner.
        String uuid = getOwner();

        //Close log of player as owner.
        Network.getInstance().regionSQL.update("UPDATE region_logs SET end_time=" + Time.currentTime()
                + " WHERE region='" + regionName + "' AND uuid='" + uuid + "';");

        //Open log of player as member.
        Network.getInstance().regionSQL.update("INSERT INTO region_logs(region,uuid,start_time) VALUES('" + regionName + "','" +
                uuid + "'," + Time.currentTime() + ");");

        //Update region member to set as member.
        Network.getInstance().regionSQL.update("UPDATE region_members SET is_owner=0 WHERE region='" + regionName + "' AND uuid='" + uuid + "';");

    }

    //Make a member the owner of the region.
    public void makeOwner(String uuid) {

        //Check if they are a member.
        if (isMember(uuid)) {

            //Close log of player as member.
            Network.getInstance().regionSQL.update("UPDATE region_logs SET end_time=" + Time.currentTime()
                    + " WHERE region='" + regionName + "' AND uuid='" + uuid + "';");

            //Open log of player as owner.
            Network.getInstance().regionSQL.update("INSERT INTO region_logs(region,uuid,is_owner,start_time) VALUES('" + regionName + "','" +
                    uuid + "',1," + Time.currentTime() + ");");

            //Update region member to set as owner.
            Network.getInstance().regionSQL.update("UPDATE region_members SET is_owner=1 WHERE region='" + regionName + "' AND uuid='" + uuid + "';");

            //If the region is currently set as inactive and the new owner isn't, set it to default.
            if (status() == RegionStatus.INACTIVE && hasActiveOwner()) {
                setDefault();
            }
        }
    }

    //Update any region requests.
    public void updateRequests() {

        //If the region has no owner, accept all requests.
        //If there is an owner, update the owner row in the request.
        if (hasOwner()) {

            Network.getInstance().regionSQL.update("UPDATE region_requests SET owner='" + getOwner() + "' WHERE region='" + regionName + "';");

        } else {

            acceptRequests();

        }
    }

    //Remove all members and owner of the region.
    //The placeholder %tag% is used in the message to show the region tag.
    public void removeMembers(String message) {

        if (hasOwner() || hasMember()) {

            //Get all members.
            ArrayList<String> uuids = Network.getInstance().regionSQL.getStringList("SELECT uuid FROM region_members WHERE region='" + regionName + "';");

            for (String uuid : uuids) {

                leaveRegion(uuid, message.replace("%tag%", getTag(uuid)));

            }
        }
    }

    //Get time that a region member was last in this region.
    public long lastActive(String uuid) {
        return (Network.getInstance().regionSQL.getLong("SELECT last_enter FROM region_members WHERE region='" + regionName + "' AND uuid='" + uuid + "';"));
    }
}
