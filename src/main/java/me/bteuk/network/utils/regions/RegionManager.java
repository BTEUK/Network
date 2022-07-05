package me.bteuk.network.utils.regions;

import me.bteuk.network.Network;
import me.bteuk.network.sql.RegionSQL;
import org.bukkit.Location;

import java.util.ArrayList;

public class RegionManager {

    private final ArrayList<Region> regions;
    private final RegionSQL regionSQL;

    public RegionManager(RegionSQL regionSQL) {
        regions = new ArrayList<>();

        this.regionSQL = regionSQL;
    }

    //Get region at location.
    public Region getRegion(Location l) {

        String regionName = getRegionName(l);

        return getRegion(regionName);

    }

    //Get region at location with coordinate transform.
    public Region getRegion(Location l, int dx, int dz) {

        l.setX(l.getX() + dx);
        l.setZ(l.getZ() + dz);

        String regionName = getRegionName(l);

        return getRegion(regionName);

    }

    private String getRegionName(Location l) {

        return ((int) Math.floor((l.getX()/512)) + "," + (int) Math.floor((l.getZ()/512)));

    }

    //Check whether the region exists in the database.
    //This is mainly used to check whether guests can teleport there.
    private boolean exists(String regionName) {
        return (regionSQL.hasRow("SELECT region FROM regions WHERE region='" + regionName + "';" ));
    }

    private Region getRegion(String regionName) {
        for (Region region : regions) {
            if (region.getName().equals(regionName)) {
                return region;
            }
        }

        //Region does not exist, create it.
        Region region = new Region(regionName);
        regions.add(region);
        return region;
    }
}
