package me.bteuk.network.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import me.bteuk.network.exceptions.RegionManagerNotFoundException;
import org.bukkit.World;
import org.bukkit.entity.Player;

import static net.buildtheearth.terraminusminus.TerraMinusMinus.LOGGER;

public class WorldguardManager {

    private static WorldGuard instance;

    /**
     * Constructor, initialises the WorldGuard instance.
     */
    public static void setInstance() {
        instance = WorldGuard.getInstance();
        LOGGER.info("Set WorldGuard instance");
    }

    /**
     * Get the region manager for a {@link World}.
     *
     * @param world the world to get the region manager from
     * @return the {@link RegionManager}
     * @throws RegionManagerNotFoundException if no region manager exists
     */
    public static RegionManager getRegionManager(World world) throws RegionManagerNotFoundException {
        if (instance == null) {
            setInstance();
        }
        RegionContainer container = instance.getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(BukkitAdapter.adapt(world));

        if (regionManager == null) {
            throw new RegionManagerNotFoundException(String.format("No region manager found for World %s", world.getName()));
        }
        return regionManager;
    }

    public static LocalPlayer wrapPlayer(Player player) {
        return WorldGuardPlugin.inst().wrapPlayer(player);
    }
}
