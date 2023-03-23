package me.bteuk.network.server_conversion.regions;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import me.bteuk.network.Network;
import me.bteuk.network.utils.regions.Region;
import me.bteuk.network.utils.regions.RegionManager;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class WGRegions {

    //Adds all members of region in the database to the correct ingame regions.
    public static void convertWGRegions() {

        //Get instance of WorldGuard.
        com.sk89q.worldguard.WorldGuard wg = com.sk89q.worldguard.WorldGuard.getInstance();

        //Get regions.
        RegionContainer container = wg.getPlatform().getRegionContainer();
        com.sk89q.worldguard.protection.managers.RegionManager regions = container.get(BukkitAdapter.adapt(Bukkit.getWorld(Network.getInstance().getConfig().getString("earth_world"))));

        if (regions == null) {
            return;
        }

        //Iterate through all regions.
        for (Map.Entry<String, ProtectedRegion> region : regions.getRegions().entrySet()) {

            //Get members from database and add them to the region.
            for (String uuid: Network.getInstance().regionSQL.getStringList("SELECT uuid FROM region_members WHERE region='" + region.getKey() + "';")) {

                DefaultDomain members = region.getValue().getMembers();
                members.addPlayer(UUID.fromString(uuid));

                region.getValue().setMembers(members);

            }
        }

        //Save the regions.
        try {
            regions.saveChanges();
        } catch (StorageException e) {
            e.printStackTrace();
        }
    }
}
