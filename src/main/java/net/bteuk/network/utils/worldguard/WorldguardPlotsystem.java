package net.bteuk.network.utils.worldguard;

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import net.bteuk.network.Network;
import net.bteuk.network.exceptions.RegionManagerNotFoundException;
import net.bteuk.network.exceptions.RegionNotFoundException;
import net.bteuk.network.sql.PlotSQL;
import net.bteuk.network.utils.Utils;
import net.bteuk.network.utils.math.Point;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Worldguard functions specific to the plot system.
 */
public class WorldguardPlotsystem {

    /**
     * Get the points of a specific plot or zone as if it was located in the save world.
     * This is done by getting the points in the world where the plot or zone is and then applying the negative transform from its original location.
     *
     * @param regionName
     * the name of the plot or zone
     * @param world
     * the name of the world where the plot or zone exists, NOT the world of the save world
     */
    public static List<BlockVector2> getPointsTransformedToSaveWorld(String regionName, World world) throws RegionNotFoundException, RegionManagerNotFoundException {

        List<BlockVector2> vector = WorldguardUtils.getPoints(regionName, world);
        List<BlockVector2> newVector = new ArrayList<>();

        //Get the negative coordinate transform.
        PlotSQL plotSQL = Network.getInstance().getPlotSQL();

        int xTransform = -plotSQL.getInt("SELECT xTransform FROM location_data WHERE name='" + world.getName() + "';");
        int zTransform = -plotSQL.getInt("SELECT zTransform FROM location_data WHERE name='" + world.getName() + "';");

        //Apply to transform to each coordinate.
        vector.forEach(bv -> newVector.add(BlockVector2.at(bv.x() + xTransform, bv.z() + zTransform)));

        return newVector;

    }

    /**
     * Get the location of the centre of a region.
     * @param regionName the region to get the location of
     * @param world the world in which the region is
     * @return the {@link Location} of the centre of the region
     * @throws RegionNotFoundException if the region can not be found
     * @throws RegionManagerNotFoundException if no region manager exists for this world
     */
    public static Location getCurrentLocation(String regionName, World world) throws RegionNotFoundException, RegionManagerNotFoundException {

        //Get the region manager.
        RegionManager regionManager = WorldguardManager.getRegionManager(world);

        //Get the worldguard region and teleport to player to one of the corners.
        ProtectedPolygonalRegion region = (ProtectedPolygonalRegion) regionManager.getRegion(regionName);

        if (region == null) {
            throw new RegionNotFoundException("Region " + regionName + " does not exist!");
        }

        BlockVector2 bv = Point.getAveragePoint(region.getPoints());

        return (new Location(world, bv.x(), Utils.getHighestYAt(world, bv.x(), bv.z()), bv.z()));

    }

//    /**
//     * Get the location of the centre of a region in the save world.
//     * @param regionName the region to get the location of
//     * @param world the world in which the region is
//     * @return the {@link Location} of the centre of the region
//     * @throws RegionNotFoundException if the region can not be found
//     * @throws RegionManagerNotFoundException if no region manager exists for this world
//     */
//    public static Location getBeforeLocation(String regionName, World world) throws WorldNotFoundException, RegionNotFoundException, RegionManagerNotFoundException {
//
//        //Get instance of plugin and config
//        PlotSystem instance = PlotSystem.getInstance();
//        FileConfiguration config = instance.getConfig();
//
//        //Get worlds from config
//        String save_world = config.getString("save_world");
//        if (save_world == null) {
//
//            throw new WorldNotFoundException("Save World is not defined in config, plot delete event has therefore failed!");
//
//        }
//
//        World saveWorld = Bukkit.getServer().getWorld(save_world);
//
//        //Get worldguard instance
//        WorldGuard wg = WorldGuard.getInstance();
//
//        //Get worldguard region data
//        RegionContainer container = wg.getPlatform().getRegionContainer();
//        RegionManager buildRegions = container.get(BukkitAdapter.adapt(buildWorld));
//
//        if (buildRegions == null) {
//
//            throw new RegionManagerNotFoundException("RegionManager for world " + buildWorld.getName() + " is null!");
//
//        }
//
//        ProtectedPolygonalRegion region = (ProtectedPolygonalRegion) buildRegions.getRegion(regionName);
//
//        if (region == null) {
//
//            throw new RegionNotFoundException("Region " + regionName + " does not exist!");
//
//        }
//
//        BlockVector2 bv = Point.getAveragePoint(region.getPoints());
//
//        //To get the actual location we need to take the negative coordinate transform of the plot.
//        PlotSQL plotSQL = PlotSystem.getInstance().plotSQL;
//
//        int xTransform = -plotSQL.getInt("SELECT xTransform FROM location_data WHERE name='" + buildWorld.getName() + "';");
//        int zTransform = -plotSQL.getInt("SELECT zTransform FROM location_data WHERE name='" + buildWorld.getName() + "';");
//
//        BlockVector2 bv2 = BlockVector2.at(bv.getX() + xTransform, bv.getZ() + zTransform);
//
//        return (new Location(saveWorld, bv2.getX(), Utils.getHighestYAt(saveWorld, bv2.getX(), bv2.getZ()), bv2.getZ()));
//
//    }
}
