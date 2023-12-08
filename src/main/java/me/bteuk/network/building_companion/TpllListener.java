package me.bteuk.network.building_companion;

import me.bteuk.network.Network;
import me.bteuk.network.commands.navigation.Tpll;
import me.bteuk.network.utils.TpllFormat;
import me.bteuk.network.utils.regions.Region;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Arrays;

import static me.bteuk.network.utils.Constants.LOGGER;
import static me.bteuk.network.utils.NetworkConfig.CONFIG;

public class TpllListener implements Listener {

    private final BuildingCompanion companion;

    private final boolean regionsEnabled = CONFIG.getBoolean("regions_enabled");

    public TpllListener(BuildingCompanion companion) {
        this.companion = companion;
        Bukkit.getServer().getPluginManager().registerEvents(this, Network.getInstance());
    }

    @EventHandler
    public void onCommandPreProcess(PlayerCommandPreprocessEvent e) {
        if (!companion.playerEquals(e.getPlayer())) {
            return;
        }

        LOGGER.info(e.getMessage());

        if (e.getMessage().startsWith("/network:tpll")) {

            String[] command = e.getMessage().split(" ");

            if (command.length == 1) {
                //Command has no arguments, return.
                return;
            }

            //Convert the command to a usage format.
            TpllFormat format = Tpll.getUsableTpllFormat(Arrays.copyOfRange(command, 1, command.length));

            double[] proj;

            try {
                proj = Tpll.bteGeneratorSettings.projection().fromGeo(format.getCoordinates().getLng(), format.getCoordinates().getLat());
            } catch (Exception ex) {
                //No coordinates were parsed, return.
                return;
            }

            //Get location and region.
            Location l = new Location(e.getPlayer().getWorld(), proj[0], 1, proj[1]);
            Region region = null;
            if (regionsEnabled) {
                region = Network.getInstance().getRegionManager().getRegion(l);
            }

            l = Tpll.applyCoordinateTransformIfPlotSystem(region, l);

            //TODO: Verify that location is valid. (It must be in a buildable region/plot for the player)
            if (false) {
                //Notify the player!
            }

            // Add a new corner, or update an existing one.
            companion.addLocation(l);

        }
    }
}
