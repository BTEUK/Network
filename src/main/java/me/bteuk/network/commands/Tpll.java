package me.bteuk.network.commands;

import me.bteuk.network.Network;
import me.bteuk.network.events.EventManager;
import me.bteuk.network.utils.Statistics;
import me.bteuk.network.utils.SwitchServer;
import me.bteuk.network.utils.Time;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.regions.Region;
import me.bteuk.network.utils.regions.RegionManager;
import net.buildtheearth.terraminusminus.dataset.IScalarDataset;
import net.buildtheearth.terraminusminus.generator.EarthGeneratorPipelines;
import net.buildtheearth.terraminusminus.generator.EarthGeneratorSettings;
import net.buildtheearth.terraminusminus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraminusminus.util.geo.CoordinateParseUtils;
import net.buildtheearth.terraminusminus.util.geo.LatLng;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Tpll implements CommandExecutor {

    private final boolean requires_permission;
    private static final DecimalFormat DECIMAL_FORMATTER = new DecimalFormat("##.#####");

    private final EarthGeneratorSettings bteGeneratorSettings = EarthGeneratorSettings.parse(EarthGeneratorSettings.BTE_DEFAULT_SETTINGS);

    private final boolean isEarth;
    private final String earthServer;
    private final String earthWorld;

    private final RegionManager regionManager;
    private final boolean regionsEnabled;

    public Tpll(boolean requires_permission) {
        this.requires_permission = requires_permission;
        this.isEarth = Network.getInstance().globalSQL.hasRow("SELECT name FROM server_data WHERE name='" + Network.SERVER_NAME + "' AND type='earth'");
        earthServer = Network.getInstance().globalSQL.getString("SELECT name FROM server_data WHERE type='earth';");
        earthWorld = Network.getInstance().getConfig().getString("earth_world");

        regionManager = Network.getInstance().getRegionManager();

        FileConfiguration config = Network.getInstance().getConfig();
        regionsEnabled = config.getBoolean("regions_enabled");

    }

    /**
     * Gets all objects in a string array above a given index
     *
     * @param args  Initial array
     * @param index Starting index
     * @return Selected array
     */
    private String[] selectArray(String[] args, int index) {
        List<String> array = new ArrayList<>();
        for (int i = index; i < args.length; i++) {
            array.add(args[i]);
        }

        return array.toArray(array.toArray(new String[array.size()]));
    }

    private String[] inverseSelectArray(String[] args, int index) {
        List<String> array = new ArrayList<>();
        for (int i = 0; i < index; i++) {
            array.add(args[i]);
        }

        return array.toArray(array.toArray(new String[array.size()]));

    }

    /**
     * Gets a space seperated string from an array
     *
     * @param args A string array
     * @return The space seperated String
     */
    private String getRawArguments(String[] args) {
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

    private void usage(Player p) {
        p.sendMessage(Utils.error("/tpll <latitude> <longitude> [altitude]"));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Check if the sender is a player.
        //Only players can use /tpll.
        if (!(sender instanceof Player p)) {

            sender.sendMessage(Utils.error("This command can only be used by players."));
            return true;

        }

        //Check if permission is required.
        if (requires_permission) {

            if (!p.hasPermission("uknet.tpll")) {

                p.sendMessage(Utils.error("You do not have permission to use this command."));
                return true;

            }
        }

        if (args.length == 0) {
            usage(p);
            return true;
        }

        double altitude = Double.NaN;
        LatLng defaultCoords = CoordinateParseUtils.parseVerbatimCoordinates(this.getRawArguments(args).trim());

        if (defaultCoords == null) {
            LatLng possiblePlayerCoords = CoordinateParseUtils.parseVerbatimCoordinates(this.getRawArguments(this.selectArray(args, 1)));
            if (possiblePlayerCoords != null) {
                defaultCoords = possiblePlayerCoords;
            }
        }

        LatLng possibleHeightCoords = CoordinateParseUtils.parseVerbatimCoordinates(this.getRawArguments(this.inverseSelectArray(args, args.length - 1)));
        if (possibleHeightCoords != null) {
            defaultCoords = possibleHeightCoords;
            try {
                altitude = Double.parseDouble(args[args.length - 1]);
            } catch (Exception ignored) {
            }
        }

        LatLng possibleHeightNameCoords = CoordinateParseUtils.parseVerbatimCoordinates(this.getRawArguments(this.inverseSelectArray(this.selectArray(args, 1), this.selectArray(args, 1).length - 1)));
        if (possibleHeightNameCoords != null) {
            defaultCoords = possibleHeightNameCoords;
            try {
                altitude = Double.parseDouble(this.selectArray(args, 1)[this.selectArray(args, 1).length - 1]);
            } catch (Exception ignored) {
            }
        }

        if (defaultCoords == null) {
            this.usage(p);
            return true;
        }

        double[] proj;

        try {
            proj = bteGeneratorSettings.projection().fromGeo(defaultCoords.getLng(), defaultCoords.getLat());
        } catch (Exception e) {
            this.usage(p);
            return true;
        }

        Location l = new Location(p.getWorld(), proj[0], 1, proj[1]);

        //Get region.
        Region region = regionManager.getRegion(l);

        //If altitude wasn't specified in the command.
        CompletableFuture<Double> altFuture;
        if (Double.isNaN(altitude)) {

            int alt;

            //Check if the region is on a plot server.
            if (Network.getInstance().regionSQL.hasRow("SELECT region FROM regions WHERE region='" + region.regionName() + "' AND status='plot';")) {

                //Get server and location of region.
                String location = Network.getInstance().plotSQL.getString("SELECT location FROM regions WHERE region='" + region.regionName() + "';");

                //Get the coordinate transformations.
                int xTransform = Network.getInstance().plotSQL.getInt("SELECT xTransform FROM location_data WHERE name='" + location + "';");
                int zTransform = Network.getInstance().plotSQL.getInt("SELECT zTransform FROM location_data WHERE name='" + location + "';");
                alt = Utils.getHighestYAt(p.getWorld(), (int) proj[0] + xTransform, (int) proj[1] + zTransform);

            } else {

                alt = Utils.getHighestYAt(p.getWorld(), (int) proj[0], (int) proj[1]);

            }

            if (alt == Integer.MIN_VALUE) {
                try {
                    altFuture = bteGeneratorSettings.datasets()
                            .<IScalarDataset>getCustom(EarthGeneratorPipelines.KEY_DATASET_HEIGHTS)
                            .getAsync(defaultCoords.getLng(), defaultCoords.getLat())
                            .thenApply(a -> a + 1.0d);
                } catch (OutOfProjectionBoundsException e) { //out of bounds, notify user
                    sender.sendMessage(Utils.error("These coordinates are out of the projection bounds."));
                    return true;
                }
            } else {
                altFuture = CompletableFuture.supplyAsync(() -> (double) alt);
            }
        } else {
            altFuture = CompletableFuture.completedFuture(altitude);
        }

        LatLng finalDefaultCoords = defaultCoords;
        altFuture.thenAccept(s -> Bukkit.getScheduler().runTask(Network.getInstance(), () -> {

            if (regionsEnabled) {

                //Check if the region is on a plot server.
                if (Network.getInstance().regionSQL.hasRow("SELECT region FROM regions WHERE region='" + region.regionName() + "' AND status='plot';")) {

                    //Get server and location of region.
                    String server = Network.getInstance().plotSQL.getString("SELECT server FROM regions WHERE region='" + region.regionName() + "';");
                    String location = Network.getInstance().plotSQL.getString("SELECT location FROM regions WHERE region='" + region.regionName() + "';");

                    //Get the coordinate transformations.
                    int xTransform = Network.getInstance().plotSQL.getInt("SELECT xTransform FROM location_data WHERE name='" + location + "';");
                    int zTransform = Network.getInstance().plotSQL.getInt("SELECT zTransform FROM location_data WHERE name='" + location + "';");

                    //If they are on the correct server, teleport them directly, else switch their server.
                    if (server.equals(Network.SERVER_NAME)) {

                        //Set current location for /back
                        Back.setPreviousCoordinate(p.getUniqueId().toString(), p.getLocation());

                        Location loc = new Location(Bukkit.getWorld(location), (proj[0] + xTransform), s, (proj[1] + zTransform), p.getLocation().getYaw(), p.getLocation().getPitch());

                        //Add tpll to statistics.
                        Statistics.addTpll(p.getUniqueId().toString(), Time.getDate(Time.currentTime()));

                        p.sendMessage(Utils.chat("&7Teleporting to &9" + DECIMAL_FORMATTER.format(finalDefaultCoords.getLat()) + "&7, &9" + DECIMAL_FORMATTER.format(finalDefaultCoords.getLng())));
                        p.teleport(loc);

                    } else {

                        //Set join event to teleport there.
                        EventManager.createTeleportEvent(true, p.getUniqueId().toString(), "network", "teleport "
                                + location + " " + (proj[0] + xTransform) + " " + (proj[1] + zTransform) + " "
                                + p.getLocation().getYaw() + " " + p.getLocation().getPitch(), p.getLocation());

                        //Add tpll to statistics.
                        Statistics.addTpll(p.getUniqueId().toString(), Time.getDate(Time.currentTime()));

                        //Switch server.
                        SwitchServer.switchServer(p, server);

                    }

                } else {

                    //Region is on earth server.
                    //Check if the player can enter this region.
                    if (region.inDatabase() || p.hasPermission("group.jrbuilder")) {

                        //If the player is already on the Earth server teleport them directly.
                        if (isEarth) {

                            //Set current location for /back
                            Back.setPreviousCoordinate(p.getUniqueId().toString(), p.getLocation());

                            Location loc = new Location(Bukkit.getWorld(earthWorld), (proj[0]), s, (proj[1]), p.getLocation().getYaw(), p.getLocation().getPitch());

                            //Add tpll to statistics.
                            Statistics.addTpll(p.getUniqueId().toString(), Time.getDate(Time.currentTime()));

                            p.sendMessage(Utils.chat("&7Teleporting to &9" + DECIMAL_FORMATTER.format(finalDefaultCoords.getLat()) + "&7, &9" + DECIMAL_FORMATTER.format(finalDefaultCoords.getLng())));
                            p.teleport(loc);


                        } else {

                            //Set join event to teleport there.
                            EventManager.createTeleportEvent(true, p.getUniqueId().toString(), "network", "teleport "
                                    + earthWorld + " " + proj[0] + " " + proj[1] + " "
                                    + p.getLocation().getYaw() + " " + p.getLocation().getPitch(), p.getLocation());

                            //Add tpll to statistics.
                            Statistics.addTpll(p.getUniqueId().toString(), Time.getDate(Time.currentTime()));

                            //Switch server.
                            SwitchServer.switchServer(p, earthServer);

                        }

                    } else {

                        //You can't enter this region.
                        p.sendMessage(Utils.error("The terrain for this region has not been generated, you must be Jr.Builder or higher to load new terrain."));

                    }
                }
            } else {
                Location loc = new Location(p.getWorld(), (proj[0]), s, (proj[1]), p.getLocation().getYaw(), p.getLocation().getPitch());

                //Add tpll to statistics.
                Statistics.addTpll(p.getUniqueId().toString(), Time.getDate(Time.currentTime()));

                p.sendMessage(Utils.chat("&7Teleporting to &9" + DECIMAL_FORMATTER.format(finalDefaultCoords.getLat()) + "&7, &9" + DECIMAL_FORMATTER.format(finalDefaultCoords.getLng())));
                p.teleport(loc);
            }
        }));

        return true;
    }
}
