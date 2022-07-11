package me.bteuk.network.utils.regions;

import me.bteuk.network.Network;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Time;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.WorldGuard;
import org.bukkit.Bukkit;

public record Region(String regionName) {

    public String getName() {
        return regionName;
    }

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

    //Check whether the region equals another region.
    public boolean equals(Region region) {
        return (getName().equals(region.getName()));
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
            u.player.sendMessage(Utils.chat("&aRequested to join region " + regionName + ", awaiting staff review."));

            //TODO send message to reviewers if online.

        } else {
            //Owner request

            //Create request.
            Network.getInstance().regionSQL.update("INSERT INTO region_requests(region,uuid,owner_accept,coordinate_id) VALUES ('" + regionName + "','" +
                    u.player.getUniqueId() + "',0," + coordinate + ");");

            //Send message to player.
            u.player.sendMessage(Utils.chat("&aRequested to join region " + regionName + ", awaiting owner review."));

            //TODO send message to owner if online.

        }
    }

    //Join region with no request.
    public void joinRegion(NetworkUser u) {

        //If region is public then it must already have an owner.
        if (isPublic()) {

            //Join region as member.
            Network.getInstance().regionSQL.update("INSERT INTO region_members(region,uuid,last_enter) VALUES('" + regionName + "','" +
                    u.player.getUniqueId() + "'," + Time.currentTime() + ");");

            //Start log of player in region.
            Network.getInstance().regionSQL.update("INSERT INTO region_logs(region,uuid,start_time) VALUES('" + regionName + "','" +
                    u.player.getUniqueId() + "'," + Time.currentTime() + ");");

            //Join region in WorldGuard.
            WorldGuard.addMember(regionName, u.player.getUniqueId().toString(), u.player.getWorld());

            u.player.sendMessage(Utils.chat("&aYou have joined the region " + regionName + " as a member."));

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

                Network.getInstance().globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" + owner + "','&aYou have been demoted to a member in region "
                        + getTag(owner) + " due to inactivity.');");

            }

            //Join region as owner.
            Network.getInstance().regionSQL.update("INSERT INTO region_members(region,uuid,is_owner,last_enter) VALUES('" + regionName + "','" +
                    u.player.getUniqueId() + "',1," + Time.currentTime() + ");");

            //Start log of player in region.
            Network.getInstance().regionSQL.update("INSERT INTO region_logs(region,uuid,is_owner,start_time) VALUES('" + regionName + "','" +
                    u.player.getUniqueId() + "',1," + Time.currentTime() + ");");

            //Join region in WorldGuard.
            WorldGuard.addMember(regionName, u.player.getUniqueId().toString(), u.player.getWorld());

            u.player.sendMessage(Utils.chat("&aYou have joined the region " + regionName + " as the owner."));

        }
    }

    //Leave region.
    public void leaveRegion(String uuid) {

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
