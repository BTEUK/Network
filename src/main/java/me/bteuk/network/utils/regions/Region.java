package me.bteuk.network.utils.regions;

import me.bteuk.network.Network;

public class Region {

    private String regionName;

    private int regionX;
    private int regionZ;

    public Region(String regionName) {
        this.regionName = regionName;
        regionX = Integer.parseInt(regionName.split(",")[0]);
        regionZ = Integer.parseInt(regionName.split(",")[1]);
    }

    public String getName() {
        return regionName;
    }

    public int getRegionX() {
        return regionX;
    }

    public int getRegionZ() {
        return regionZ;
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
        return (Network.getInstance().regionSQL.hasRow("SELECT region FROM region_members WHERE region='" + regionName + " AND 'uuid='" + uuid + "' AND is_owner=1;"));
    }

    //Return whether the uuid is the uuid of a region member.
    public boolean isMember(String uuid) {
        return (Network.getInstance().regionSQL.hasRow("SELECT region FROM region_members WHERE region='" + regionName + " AND uuid='" + uuid + "' AND is_owner=0;"));
    }

    //Return whether the region is open or not.
    public boolean isOpen() {
        return (Network.getInstance().regionSQL.hasRow("SELECT region FROM regions WHERE region='" + regionName + "' AND status='open';"));
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
}
