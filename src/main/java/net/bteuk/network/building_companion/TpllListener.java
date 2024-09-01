package net.bteuk.network.building_companion;

import net.bteuk.network.Network;
import net.bteuk.network.commands.navigation.Tpll;
import net.bteuk.network.utils.Constants;
import net.bteuk.network.utils.TpllFormat;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Arrays;

import static net.bteuk.network.utils.Constants.LOGGER;

public class TpllListener implements Listener {

    private final BuildingCompanion companion;

    public TpllListener(BuildingCompanion companion) {
        this.companion = companion;
        Bukkit.getServer().getPluginManager().registerEvents(this, Network.getInstance());
    }

    @EventHandler
    public void onCommandPreProcess(PlayerCommandPreprocessEvent e) {
        if (companion.playerNotEquals(e.getPlayer())) {
            return;
        }

        LOGGER.info(e.getMessage());

        if (e.getMessage().startsWith("/network:tpll")) {

            String[] command = e.getMessage().split(" ");

            if (command.length == 1) {
                // Command has no arguments, return.
                return;
            }

            // Convert the command to a usage format.
            TpllFormat format = Tpll.getUsableTpllFormat(Arrays.copyOfRange(command, 1, command.length));

            double[] proj;

            try {
                proj = Tpll.bteGeneratorSettings.projection().fromGeo(format.getCoordinates().getLng(), format.getCoordinates().getLat());
            } catch (Exception ex) {
                // No coordinates were parsed, return.
                return;
            }

            // Apply coordinate transform if in the plotsystem.
            Location l = new Location(e.getPlayer().getWorld(), proj[0], 1, proj[1]);
            if (Constants.REGIONS_ENABLED) {
                l = Tpll.applyCoordinateTransformIfPlotSystem(Network.getInstance().getRegionManager().getRegion(l), l);
            }

            // Add a new corner, or update an existing one.
            companion.addLocation(l);
        }
    }
}
