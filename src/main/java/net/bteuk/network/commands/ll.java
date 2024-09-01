package net.bteuk.network.commands;

import net.bteuk.network.Network;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.sql.PlotSQL;
import net.bteuk.network.utils.NetworkUser;
import net.buildtheearth.terraminusminus.generator.EarthGeneratorSettings;
import net.buildtheearth.terraminusminus.projection.OutOfProjectionBoundsException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;

import static net.bteuk.network.utils.Constants.LL;
import static net.bteuk.network.utils.Constants.LOGGER;
import static net.bteuk.network.utils.Constants.REGIONS_ENABLED;
import static net.bteuk.network.utils.Constants.SERVER_TYPE;
import static net.bteuk.network.utils.enums.ServerType.PLOT;

public class ll extends AbstractCommand {

    private final EarthGeneratorSettings bteGeneratorSettings = EarthGeneratorSettings.parse(EarthGeneratorSettings.BTE_DEFAULT_SETTINGS);
    private static final DecimalFormat DECIMAL_FORMATTER = new DecimalFormat("##.#####");

    private final PlotSQL plotSQL;

    public ll(Network instance) {
        super(instance, "ll");
        plotSQL = instance.getPlotSQL();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Check if the sender is a player.
        Player p = getPlayer(sender);
        if (p == null) {
            return true;
        }

        //Get coordinates at the location of the player if they're in a region.
        NetworkUser u = Network.getInstance().getUser(p);

        //If u is null, cancel.
        if (u == null) {
            LOGGER.severe("User " + p.getName() + " can not be found!");
            p.sendMessage(ChatUtils.error("User can not be found, please relog!"));
            return true;
        }

        //Check is regions are enabled
        if (REGIONS_ENABLED) {
            //Check if in a valid region.
            if (!u.inRegion) {

                p.sendMessage(ChatUtils.error("You must be standing in a region to get the coordinates."));
                return true;

            }
        }

        //Send coordinates with a link to Google Maps to the player if /ll is enabled.
        if (LL) {

            try {

                //TEMP: The following if statement is only necessary because the server does not update the dx,dz
                // on teleport when regions are disabled, this will be fixed in BTEUK-315!
                // If the player is in the plotsystem update their dx,dz.
                if (SERVER_TYPE == PLOT && plotSQL.hasRow("SELECT name FROM location_data WHERE name='" + p.getWorld().getName() + "';")) {

                        //Get negative coordinate transform of new location.
                        u.dx = -plotSQL.getInt("SELECT xTransform FROM location_data WHERE name='" + p.getWorld().getName() + "';");
                        u.dz = -plotSQL.getInt("SELECT zTransform FROM location_data WHERE name='" + p.getWorld().getName() + "';");
                }

                double[] coords = bteGeneratorSettings.projection().toGeo(p.getLocation().getX() + u.dx, p.getLocation().getZ() + u.dz);

                p.sendMessage(ChatUtils.success("Your coordinates are ")
                        .append(Component.text(DECIMAL_FORMATTER.format(coords[1]), NamedTextColor.DARK_AQUA))
                        .append(ChatUtils.success(","))
                        .append(Component.text(DECIMAL_FORMATTER.format(coords[0]), NamedTextColor.DARK_AQUA)));
                Component message = ChatUtils.success("Click here to view the coordinates in Google Maps.");
                message = message.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, "https://www.google.com/maps/@?api=1&map_action=map&basemap=satellite&zoom=21&center=" + coords[1] + "," + coords[0]));
                p.sendMessage(message);
                return true;

            } catch (OutOfProjectionBoundsException e) {

                p.sendMessage(ChatUtils.error("You are not standing in a location where coordinates can be retrieved."));
                return true;

            }

        } else {

            p.sendMessage(ChatUtils.error("This command can not be used here."));
            return true;

        }
    }
}
