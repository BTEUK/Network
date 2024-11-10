package net.bteuk.network.commands.navigation;

import io.papermc.lib.PaperLib;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.bteuk.network.Network;
import net.bteuk.network.commands.AbstractCommand;
import net.bteuk.network.eventing.events.EventManager;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.sql.PlotSQL;
import net.bteuk.network.utils.Statistics;
import net.bteuk.network.utils.SwitchServer;
import net.bteuk.network.utils.Time;
import net.bteuk.network.utils.TpllFormat;
import net.bteuk.network.utils.Utils;
import net.bteuk.network.utils.regions.Region;
import net.bteuk.network.utils.regions.RegionManager;
import net.buildtheearth.terraminusminus.dataset.IScalarDataset;
import net.buildtheearth.terraminusminus.generator.EarthGeneratorPipelines;
import net.buildtheearth.terraminusminus.generator.EarthGeneratorSettings;
import net.buildtheearth.terraminusminus.generator.GeneratorDatasets;
import net.buildtheearth.terraminusminus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraminusminus.util.geo.CoordinateParseUtils;
import net.buildtheearth.terraminusminus.util.geo.LatLng;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static net.bteuk.network.utils.Constants.EARTH_WORLD;
import static net.bteuk.network.utils.Constants.SERVER_NAME;
import static net.bteuk.network.utils.NetworkConfig.CONFIG;

public class Tpll extends AbstractCommand {

    private final boolean requires_permission;
    private static final DecimalFormat DECIMAL_FORMATTER = new DecimalFormat("##.#####");

    public static final EarthGeneratorSettings bteGeneratorSettings = EarthGeneratorSettings.parse(EarthGeneratorSettings.BTE_DEFAULT_SETTINGS);

    private final RegionManager regionManager;

    private final PlotSQL plotSQL;
    private static final boolean regionsEnabled = CONFIG.getBoolean("regions_enabled");

    private static final Component USAGE = ChatUtils.error("/tpll <latitude> <longitude> [altitude]");

    public Tpll(Network instance, boolean requires_permission) {
        this.requires_permission = requires_permission;

        regionManager = instance.getRegionManager();
        plotSQL = instance.getPlotSQL();
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {

        //Check if the sender is a player.
        //Only players can use /tpll.
        Player player = getPlayer(stack);
        if (player == null) {
            return;
        }

        //Check if permission is required.
        if (requires_permission) {
            if (!player.hasPermission("uknet.navigation.tpll")) {
                player.sendMessage(NO_PERMISSION);
                return;
            }
        }

        tpll(player, args, false);
    }

    public void tpll(Player p, String[] args, boolean fromEvent) {

        //Check if there is at least 1 argument.
        if (args.length == 0) {
            p.sendMessage(USAGE);
            return;
        }

        //Convert the input to a usable format.
        TpllFormat format = getUsableTpllFormat(args);

        if (format.getCoordinates() == null) {
            p.sendMessage(USAGE);
            return;
        }

        double[] proj;

        try {
            proj = bteGeneratorSettings.projection().fromGeo(format.getCoordinates().getLng(), format.getCoordinates().getLat());
        } catch (Exception e) {
            p.sendMessage(USAGE);
            return;
        }
        //Get location and region.
        Location l = new Location(p.getWorld(), proj[0], 1, proj[1], p.getLocation().getYaw(), p.getLocation().getPitch());
        Region region = null;
        if (regionsEnabled) {
            region = regionManager.getRegion(l);
        }

        //Check if the player is allowed to teleport here.
        if (!canTeleportHere(p, region)) {
            p.sendMessage(ChatUtils.error("The terrain for this region has not been generated, you must be at least Jr.Builder to load new terrain."));
        }

        //Check the server of the location.
        //Switch if necessary.
        if (switchServerIfNecessary(p, region, args)) {
            p.sendMessage(ChatUtils.success("The location is on another server, switching servers..."));
            return;
        }

        //If the region is in the plot system, apply the coordinate transform.
        l = applyCoordinateTransformIfPlotSystem(region, l);

        //Set the correct world.
        setWorldOfRegion(region, l);

        //Check if the chunk has already been generated.
        //If not warn the player that it needs to be generated.
        CompletableFuture<Double> altFuture = getAltitude(p, format, l);
        if (altFuture == null) {
            return;
        }

        teleport(p, altFuture, format, l, fromEvent);

    }

    /**
     * Convert the input arguments to a usable format
     *
     * @param args the command arguments
     * @return {@link TpllFormat} that includes the coordinate information that could be read from the command
     */
    public static TpllFormat getUsableTpllFormat(String[] args) {
        TpllFormat format = new TpllFormat();

        format.setCoordinates(CoordinateParseUtils.parseVerbatimCoordinates(getRawArguments(args).trim()));

        if (format.getCoordinates() == null) {
            LatLng possiblePlayerCoords = CoordinateParseUtils.parseVerbatimCoordinates(getRawArguments(selectArray(args)));
            if (possiblePlayerCoords != null) {
                format.setCoordinates(possiblePlayerCoords);
            }
        }

        LatLng possibleHeightCoords = CoordinateParseUtils.parseVerbatimCoordinates(getRawArguments(inverseSelectArray(args, args.length - 1)));
        if (possibleHeightCoords != null) {
            format.setCoordinates(possibleHeightCoords);
            try {
                format.setAltitude(Double.parseDouble(args[args.length - 1]));
            } catch (Exception ignored) {
            }
        }

        LatLng possibleHeightNameCoords = CoordinateParseUtils.parseVerbatimCoordinates(getRawArguments(inverseSelectArray(selectArray(args), selectArray(args).length - 1)));
        if (possibleHeightNameCoords != null) {
            format.setCoordinates(possibleHeightNameCoords);
            try {
                format.setAltitude(Double.parseDouble(selectArray(args)[selectArray(args).length - 1]));
            } catch (Exception ignored) {
            }
        }

        return format;
    }

    /**
     * Check if the player is allowed to teleport here.
     *
     * @param p      the player to check
     * @param region the region to check
     * @return whether the player can teleport here
     */
    private boolean canTeleportHere(Player p, Region region) {
        return !regionsEnabled || region.inDatabase() || p.hasPermission("group.jrbuilder");
    }

    /**
     * Check of the region is on the current server, else switch server.
     *
     * @param region the region to check
     * @return whether the player is switching server
     */
    private boolean switchServerIfNecessary(Player p, Region region, String[] args) {
        if (regionsEnabled) {

            //Check if the server of the region equals the current server, else teleport them with a teleport event for tpll.
            if (!region.getServer().equals(SERVER_NAME)) {

                //Create teleport event.
                EventManager.createTeleportEvent(true, p.getUniqueId().toString(), "network", "teleport tpll "
                        + String.join(" ", args), p.getLocation());

                //Switch server.
                SwitchServer.switchServer(p, region.getServer());
                return true;

            }
        }
        return false;
    }

    /**
     * Apply a coordinate transformation if the region is in the plot system.
     *
     * @param region the region to check
     * @param l      the location of the tpll
     * @return {@link Location} the location with potential coordinate transform
     */
    public static Location applyCoordinateTransformIfPlotSystem(Region region, Location l) {

        //Regions must be enabled to use the plot system.
        if (regionsEnabled) {

            //Check if the region is on a plot server.
            if (region.isPlot()) {
                String location = Network.getInstance().getPlotSQL().getString("SELECT location FROM regions WHERE region='" + region.regionName() + "';");

                //Get the coordinate transformations.
                int xTransform = Network.getInstance().getPlotSQL().getInt("SELECT xTransform FROM location_data WHERE name='" + location + "';");
                int zTransform = Network.getInstance().getPlotSQL().getInt("SELECT zTransform FROM location_data WHERE name='" + location + "';");

                Location newLocation = l.clone();
                newLocation.setX(l.getX() + xTransform);
                newLocation.setZ((l.getZ() + zTransform));
                return newLocation;
            }
        }
        return l;
    }

    /**
     * Set the world to the location.
     *
     * @param region the region to get the world for
     * @param l      the location of the tpll
     */
    private void setWorldOfRegion(Region region, Location l) {

        //Regions must be enabled to get the world,else return the current world.
        if (regionsEnabled) {

            //Check if the region is on the plot server.
            if (region.isPlot()) {
                String location = plotSQL.getString("SELECT location FROM regions WHERE region='" + region.regionName() + "';");
                l.setWorld(Bukkit.getWorld(location));
            } else {
                l.setWorld(Bukkit.getWorld(EARTH_WORLD));
            }
        }
    }

    /**
     * Get the altitude of the location
     *
     * @param p      the player
     * @param format the tpll format
     * @param l      the location of the tpll
     * @return {@link CompletableFuture<Double>} the completableFuture that will give the altitude
     */
    private CompletableFuture<Double> getAltitude(Player p, TpllFormat format, Location l) {
        CompletableFuture<Double> altFuture;
        if (!PaperLib.isChunkGenerated(l)) {
            p.sendMessage(ChatUtils.success("Location is generating, please wait a moment..."));

            //If the altitude was not specified, get it from the data.
            if (Double.isNaN(format.getAltitude())) {
                try {
                    altFuture = new GeneratorDatasets(bteGeneratorSettings)
                            .<IScalarDataset>getCustom(EarthGeneratorPipelines.KEY_DATASET_HEIGHTS)
                            .getAsync(format.getCoordinates().getLng(), format.getCoordinates().getLat())
                            .thenApply(a -> a + 1.0d);
                } catch (OutOfProjectionBoundsException e) { //out of bounds, notify user
                    p.sendMessage(ChatUtils.error("These coordinates are out of the projection bounds."));
                    return null;
                }
            } else {
                altFuture = CompletableFuture.completedFuture(format.getAltitude());
            }
        } else {

            //If the altitude was not specified, get it from the data.
            if (Double.isNaN(format.getAltitude())) {
                altFuture = CompletableFuture.completedFuture((double) Utils.getHighestYAt(l.getWorld(), l.getBlockX(), l.getBlockZ()));
            } else {
                altFuture = CompletableFuture.completedFuture(format.getAltitude());
            }
        }
        return altFuture;
    }

    /**
     * Teleport to the coordinates
     * @param p the player to teleport
     * @param altFuture the altitude future to get the altitude from
     * @param format the format
     * @param l the location to teleport to
     * @param fromEvent whether the command was executed from an event
     */
    private void teleport(Player p, CompletableFuture<Double> altFuture, TpllFormat format, Location l, boolean fromEvent) {
        altFuture.thenAccept(s -> Bukkit.getScheduler().runTask(Network.getInstance(), () -> {

            //If the tpll is from an event, don't save the previous coordinate, since that was already done when creating the event.
            if (!fromEvent) {

                //Set current location for /back
                Back.setPreviousCoordinate(p.getUniqueId().toString(), p.getLocation());
            }

            //Set the altitude
            l.setY(s);


            //Add tpll to statistics.
            Statistics.addTpll(p.getUniqueId().toString(), Time.getDate(Time.currentTime()));

            //Teleport player.
            PaperLib.teleportAsync(p, l);

            p.sendMessage(
                    ChatUtils.success("Teleported to ")
                            .append(Component.text(DECIMAL_FORMATTER.format(format.getCoordinates().getLat()), NamedTextColor.DARK_AQUA))
                            .append(ChatUtils.success(", "))
                            .append(Component.text(DECIMAL_FORMATTER.format(format.getCoordinates().getLng()), NamedTextColor.DARK_AQUA)));

        }));
    }

    /**
     * Gets all objects in a string array above a given index
     *
     * @param args Initial array
     * @return Selected array
     */
    private static String[] selectArray(String[] args) {
        List<String> array = new ArrayList<>();

        if (args.length > 1) {
            array.addAll(Arrays.asList(args).subList(1, args.length));
        }

        return array.toArray(array.toArray(new String[0]));
    }

    private static String[] inverseSelectArray(String[] args, int index) {
        List<String> array = new ArrayList<>();

        if (index > 0) {
            array.addAll(Arrays.asList(args).subList(0, index));
        }

        return array.toArray(array.toArray(new String[0]));
    }

    private static String getRawArguments(String[] args) {
        if (args.length == 0) {
            return "";
        }
        if (args.length == 1) {
            return args[0];
        }

        StringBuilder arguments = new StringBuilder(args[0].replace((char) 176, (char) 32).trim());

        for (int x = 1; x < args.length; x++) {
            arguments.append(" ").append(args[x].replace((char) 176, (char) 32).trim());
        }

        return arguments.toString();
    }
}
