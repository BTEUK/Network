package me.bteuk.network.server_conversion.regions;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import me.bteuk.network.Network;
import me.bteuk.network.utils.regions.Region;
import me.bteuk.network.utils.regions.RegionManager;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.UUID;

public class WGRegions {

    /*

    This class is purely for the initial server update, to convert existing regions into the new system.

    This will run after the regions have been converted to the new database,
    this class will then read the new database to create the regions.

    The region manager will be referenced to access regions, to they must all be there for it to work.

     */

    public static void convertWGRegions() {

        //Get regions.
        RegionManager manager = Network.getInstance().getRegionManager();

        ProtectedCuboidRegion wgRegion;
        int xmin, zmin, xmax, zmax;
        String regionName;

        //Get instance of WorldGuard.
        com.sk89q.worldguard.WorldGuard wg = com.sk89q.worldguard.WorldGuard.getInstance();

        //Get regions.
        RegionContainer container = wg.getPlatform().getRegionContainer();
        com.sk89q.worldguard.protection.managers.RegionManager regions = container.get(BukkitAdapter.adapt(Bukkit.getWorld(Network.getInstance().getConfig().getString("earth_world"))));

        //Iterate through the regions and only save it once they've all be added.
        for (Region region: manager.getRegions()) {

            regionName = region.regionName();
            xmin = Integer.parseInt(regionName.split(",")[0]) * 512;
            zmin = Integer.parseInt(regionName.split(",")[1]) * 512;
            xmax = Integer.parseInt(regionName.split(",")[0]) * 512 + 511;
            zmax = Integer.parseInt(regionName.split(",")[1]) * 512 + 511;

            wgRegion = new ProtectedCuboidRegion(region.regionName(), BlockVector3.at(xmin, -512, zmin), BlockVector3.at(xmax, 1536, zmax));

            DefaultDomain wgMembers = wgRegion.getMembers();

            //If the region has an owner, add them.
            if (region.hasOwner()) {
                wgMembers.addPlayer(UUID.fromString(region.getOwner()));
            }

            //If the region should have members, add them.
            ArrayList<String> members = region.getMembers();

            for (String uuid: members) {
                wgMembers.addPlayer(UUID.fromString(uuid));
            }

            //Set the members.
            wgRegion.setMembers(wgMembers);

            //Add the region.
            regions.addRegion(wgRegion);

        }

        //Save the regions.
        try {
            regions.saveChanges();
        } catch (StorageException e) {
            e.printStackTrace();
        }
    }
}
