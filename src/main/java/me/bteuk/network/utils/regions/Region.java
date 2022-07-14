package me.bteuk.network.utils.regions;

import me.bteuk.network.Network;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Time;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.WorldGuard;
import org.bukkit.Bukkit;

import java.util.ArrayList;

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
        Network.getInstance().regionSQL.update("UPDATE region_members SET tag='" + tag + "' WHERE region='" + regionName + "' AND uuid='" + uuid + "';");
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
            return (Network.getInstance().globalSQL.getString("SELECT name FROM server_data WHERE type='PLOT';"));
        }
    }

    //Return whether the uuid is the uuid of the region owner.
    public boolean isOwner(String uuid) {
        return (Network.getInstance().regionSQL.hasRow("SELECT region FROM region_members WHERE region='" + regionName + "' AND 'uuid='" + uuid + "' AND is_owner=1;"));
    }

    //Return whether the uuid is the uuid of a region member.
    public boolean isMember(String uuid) {
        return (Network.getInstance().regionSQL.hasRow("SELECT region FROM region_members WHERE region='" + regionName + "' AND uuid='" + uuid + "' AND is_owner=0;"));
    }

    //Return whether the region is open or not.
    public boolean isOpen() {
        return (Network.getInstance().regionSQL.hasRow("SELECT region FROM regions WHERE region='" + regionName + "' AND status='open';"));
    }

    //Return whether the region is public or not.
    public boolean isPublic() {
        return (Network.getInstance().regionSQL.hasRow("SELECT region FROM regions WHERE region='" + regionName + "' AND status='public';"));
    }

    //Return whether the region is claimable.
    public boolean isClaimable() {
        return (Network.getInstance().regionSQL.hasRow("SELECT region FROM regions WHERE region='" + regionName + "' AND (status='default' OR status='public' OR status='inactive');"));
    }

    //Return whether the region is inactive.
    public boolean isInactive() {
        return (Network.getInstance().regionSQL.hasRow("SELECT region FROM regions WHERE region='" + regionName + "' AND status='inactive';"));
    }

    //Return whether the region is on the plot server.
    public boolean isPlot() {
        return (Network.getInstance().regionSQL.hasRow("SELECT region FROM regions WHERE region='" + regionName + "' AND status='plot';"));
    }

    //Return whether the region has an owner.
    public boolean hasOwner() {
        return (Network.getInstance().regionSQL.hasRow("SELECT region FROM region_members WHERE region='" + regionName + "' AND is_owner=1;"));
    }

    //Return whether the region has a member.
    public boolean hasMember() {
        return (Network.getInstance().regionSQL.hasRow("SELECT region FROM region_members WHERE region='" + regionName + "' AND is_owner=0;"));
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
            return (Network.getInstance().globalSQL.getString("SELECT name FROM player_Data WHERE uuid='" + getOwner() + "';"));
        } else {
            return "nobody";
        }
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

    //Set the region to private (default).
    public void setPrivate() {
        Network.getInstance().regionSQL.update("UPDATE regions SET status='default' WHERE region='" + regionName + "';");
    }

    //Set the region to public.
    public void setPublic() {
        Network.getInstance().regionSQL.update("UPDATE regions SET status='public' WHERE region='" + regionName + "';");
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
        }
    }

    //Check if this region has any requests.
    public boolean hasRequests() {
        return Network.getInstance().regionSQL.hasRow("SELECT region FROM region_requests WHERE region='" + regionName + "';");
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

    //Join region with owner or staff request.
    //If staff request is true then it requires a staff request else it's an owner request.
    public void requestRegion(NetworkUser u, boolean staffRequest) {

        //Get coordinate of player.
        int coordinate = Network.getInstance().globalSQL.addCoordinate(u.player.getLocation());
        if (staffRequest) {
            //Staff request

            //Create request.
            Network.getInstance().regionSQL.update("INSERT INTO region_requests(region,uuid,staff_accept,coordinate_id) VALUES ('" + regionName + "','" +
                    u.player.getUniqueId() + "',0," + coordinate + ");");

            //Send message to player.
            u.player.sendMessage(Utils.chat("&aRequested to join region &3" + regionName + ", &aawaiting staff review."));

            //TODO send message to reviewers if online.

        } else {
            //Owner request

            //Create request.
            Network.getInstance().regionSQL.update("INSERT INTO region_requests(region,uuid,owner_accept,coordinate_id) VALUES ('" + regionName + "','" +
                    u.player.getUniqueId() + "',0," + coordinate + ");");

            //Send message to player.
            u.player.sendMessage(Utils.chat("&aRequested to join region &3" + regionName + ", &aawaiting owner review."));

            //TODO send message to owner if online.

        }
    }

    //Join region with no request.
    public void joinRegion(NetworkUser u) {

        //If region is public then it must already have an owner.
        if (isPublic()) {

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

            u.player.sendMessage(Utils.chat("&aYou have joined the region &3" + regionName + " &aas a member."));

        } else {

            //If the region is inactive, demote the previous owner to a member.
            if (isInactive()) {

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

            u.player.sendMessage(Utils.chat("&aYou have joined the region &3" + regionName + " &aas the owner."));

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
            WorldGuard.addMember(regionName, uuid, Bukkit.getWorld(Network.getInstance().getConfig().getString("earth_world")));

            //Send message to user.
            Network.getInstance().globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" + uuid + "','&aYou have joined the region &3" + regionName + " &aas a member.'");

        } else {

            //If the region is inactive, demote the previous owner to a member.
            if (isInactive()) {

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
            Network.getInstance().regionSQL.update("INSERT INTO region_members(region,uuid,is_owner,last_enter) VALUES('" + regionName + "','" +
                    uuid + "',1," + Time.currentTime() + ");");

            //Start log of player in region.
            Network.getInstance().regionSQL.update("INSERT INTO region_logs(region,uuid,is_owner,start_time) VALUES('" + regionName + "','" +
                    uuid + "',1," + Time.currentTime() + ");");

            //Join region in WorldGuard.
            WorldGuard.addMember(regionName, uuid, Bukkit.getWorld(Network.getInstance().getConfig().getString("earth_world")));

            Network.getInstance().globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" + uuid + "','&aYou have joined the region &3" + regionName + " &aas the owner.'");

        }
    }

    //Leave region.
    public void leaveRegion(String uuid) {

        //Send message to user.
        //Is sent before actual removal so we can read the region tag.
        Network.getInstance().globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" + uuid + "','&aYou have left region &3" + getTag(uuid) + "')");

        //Leave region in database.
        Network.getInstance().regionSQL.update("DELETE FROM region_members WHERE region='" + regionName + "' AND uuid='" + uuid + "';");

        //Close log of player in region.
        Network.getInstance().regionSQL.update("UPDATE region_logs SET end_time=" + Time.currentTime()
                + " WHERE region='" + regionName + "' AND uuid='" + uuid + "';");

        //Leave region in WorldGuard.
        WorldGuard.addMember(regionName, uuid, Bukkit.getWorld(Network.getInstance().getConfig().getString("earth_world")));

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

        }
    }
}
