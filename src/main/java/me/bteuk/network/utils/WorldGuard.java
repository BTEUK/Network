package me.bteuk.network.utils;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.World;

import java.util.UUID;

public class WorldGuard {

    public static boolean addMember(String region, String uuid, World world) {

        //Get instance of WorldGuard.
        com.sk89q.worldguard.WorldGuard wg = com.sk89q.worldguard.WorldGuard.getInstance();

        //Get regions.
        RegionContainer container = wg.getPlatform().getRegionContainer();
        RegionManager buildRegions = container.get(BukkitAdapter.adapt(world));

        //Add the member to the region.
        buildRegions.getRegion(region).getMembers().addPlayer(UUID.fromString(uuid));

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

        //Check if the member is in the region.
        if (buildRegions.getRegion(region).getMembers().contains(UUID.fromString(uuid))) {
            //Remove the member to the region.
            buildRegions.getRegion(region).getMembers().removePlayer(UUID.fromString(uuid));
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

    public static boolean createRegion(String regionName, int xmin, int zmin, int xmax, int zmax, World world) {

        //Get instance of WorldGuard.
        com.sk89q.worldguard.WorldGuard wg = com.sk89q.worldguard.WorldGuard.getInstance();

        //Get regions.
        RegionContainer container = wg.getPlatform().getRegionContainer();
        RegionManager buildRegions = container.get(BukkitAdapter.adapt(world));

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