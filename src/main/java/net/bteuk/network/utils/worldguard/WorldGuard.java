package net.bteuk.network.utils.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.bteuk.network.Network;
import org.bukkit.World;

import java.util.UUID;

import static net.bteuk.network.utils.Constants.LOGGER;

public class WorldGuard {

    public static boolean addMember(String region, String uuid, World world) {

        //Get instance of WorldGuard.
        com.sk89q.worldguard.WorldGuard wg = com.sk89q.worldguard.WorldGuard.getInstance();

        //Get regions.
        RegionContainer container = wg.getPlatform().getRegionContainer();
        RegionManager buildRegions = container.get(BukkitAdapter.adapt(world));

        if (buildRegions == null) {

            LOGGER.warning("RegionManager for world " + world.getName() + " is null!");
            return false;

        }

        ProtectedRegion buildRegion = buildRegions.getRegion(region);

        if (buildRegion == null) {

            LOGGER.warning("Region " + region + " does not exist!");
            return false;

        }

        //Add the member to the region.
        buildRegion.getMembers().addPlayer(UUID.fromString(uuid));

        //Save the changes
        try {
            buildRegions.saveChanges();
            return true;
        } catch (StorageException e1) {
            e1.printStackTrace();
            return false;
        }
    }

    public static boolean addGroup(String region, String group, World world) {

        //Get instance of WorldGuard.
        com.sk89q.worldguard.WorldGuard wg = com.sk89q.worldguard.WorldGuard.getInstance();

        //Get regions.
        RegionContainer container = wg.getPlatform().getRegionContainer();
        RegionManager buildRegions = container.get(BukkitAdapter.adapt(world));

        if (buildRegions == null) {

            LOGGER.warning("RegionManager for world " + world.getName() + " is null!");
            return false;

        }

        ProtectedRegion buildRegion = buildRegions.getRegion(region);

        if (buildRegion == null) {

            LOGGER.warning("Region " + region + " does not exist!");
            return false;

        }

        //Add the group to the region.
        buildRegion.getMembers().addGroup(group);
        Network.getInstance().getLogger().info("Added " + group + " to " + region);

        //Save the changes
        try {
            buildRegions.saveChanges();
            return true;
        } catch (StorageException e1) {
            e1.printStackTrace();
            return false;
        }
    }

    public static boolean removeMember(String region, String uuid, World world) {

        //Get instance of WorldGuard.
        com.sk89q.worldguard.WorldGuard wg = com.sk89q.worldguard.WorldGuard.getInstance();

        //Get regions.
        RegionContainer container = wg.getPlatform().getRegionContainer();
        RegionManager buildRegions = container.get(BukkitAdapter.adapt(world));

        if (buildRegions == null) {

            LOGGER.warning("RegionManager for world " + world.getName() + " is null!");
            return false;

        }

        ProtectedRegion buildRegion = buildRegions.getRegion(region);

        if (buildRegion == null) {

            LOGGER.warning("Region " + region + " does not exist!");
            return false;

        }

        //Check if the member is in the region.
        if (buildRegion.getMembers().contains(UUID.fromString(uuid))) {
            //Remove the member to the region.
            buildRegion.getMembers().removePlayer(UUID.fromString(uuid));
        } else {
            return false;
        }

        //Save the changes
        try {
            buildRegions.saveChanges();
            return true;
        } catch (StorageException e1) {
            e1.printStackTrace();
            return false;
        }
    }

    public static boolean removeGroup(String region, String group, World world) {

        //Get instance of WorldGuard.
        com.sk89q.worldguard.WorldGuard wg = com.sk89q.worldguard.WorldGuard.getInstance();

        //Get regions.
        RegionContainer container = wg.getPlatform().getRegionContainer();
        RegionManager buildRegions = container.get(BukkitAdapter.adapt(world));

        if (buildRegions == null) {

            LOGGER.warning("RegionManager for world " + world.getName() + " is null!");
            return false;

        }

        //Remove the group from the region.
        ProtectedRegion buildRegion = buildRegions.getRegion(region);

        if (buildRegion == null) {

            LOGGER.warning("Region " + region + " does not exist!");
            return false;

        }

        buildRegion.getMembers().removeGroup(group);

        //Save the changes
        try {
            buildRegions.saveChanges();
            return true;
        } catch (
                StorageException e1) {
            e1.printStackTrace();
            return false;
        }

    }

    public static boolean createRegion(String regionName, int xmin, int zmin, int xmax, int zmax, World world) {

        //Get instance of WorldGuard.
        com.sk89q.worldguard.WorldGuard wg = com.sk89q.worldguard.WorldGuard.getInstance();

        //Get regions.
        RegionContainer container = wg.getPlatform().getRegionContainer();
        RegionManager buildRegions = container.get(BukkitAdapter.adapt(world));

        if (buildRegions == null) {

            LOGGER.warning("RegionManager for world " + world.getName() + " is null!");
            return false;

        }

        ProtectedCuboidRegion region = new ProtectedCuboidRegion(regionName, BlockVector3.at(xmin, -512, zmin), BlockVector3.at(xmax, 1536, zmax));

        buildRegions.addRegion(region);

        //Save the changes
        try {
            buildRegions.saveChanges();
            return true;
        } catch (StorageException e1) {
            e1.printStackTrace();
            return false;
        }
    }
}