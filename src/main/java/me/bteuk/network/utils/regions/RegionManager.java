package me.bteuk.network.utils.regions;

import me.bteuk.network.sql.RegionSQL;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

public class RegionManager {

    private final Map<String, Region> regions;
    private final RegionSQL regionSQL;

    public RegionManager(RegionSQL regionSQL) {
        regions = new HashMap<>();

        this.regionSQL = regionSQL;
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

    //Get region at location.
    public Region getRegion(Location l) {
        // Get x and z of the region as int rounded down.
        int x = (l.getX() >= 0 ? (int) l.getX() : ((int) l.getX()) - 1) >> 9;
        int z = (l.getZ() >= 0 ? (int) l.getZ() : ((int) l.getZ()) - 1) >> 9;

        return getRegion(x, z);
    }

    //Get region at location with coordinate transform.
    public Region getRegion(Location l, int dx, int dz) {
        // Get x and z of the region as int rounded down with any necessary coordinate transforms.
        int x = ((l.getX() >= 0 ? (int) l.getX() : ((int) l.getX()) - 1) + dx) >> 9;
        int z = ((l.getZ() >= 0 ? (int) l.getZ() : ((int) l.getZ()) - 1) + dz) >> 9;

        return getRegion(x, z);
    }

    public Region getRegion(String region) throws NumberFormatException {
        int x = Integer.parseInt(region.split(",")[0]);
        int z = Integer.parseInt(region.split(",")[1]);
        return findRegion(x, z);
    }

    //Check whether the region exists in the database.
    //This is mainly used to check whether guests can teleport there.
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
}
