package me.bteuk.network.commands;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.bteuk.network.Network;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.coords.CoordinateParseUtils;
import me.bteuk.network.utils.coords.LatLng;
import me.bteuk.network.utils.coords.LocationUtil;
import me.bteuk.network.utils.projection.BTEDymaxionProjection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Tpll implements CommandExecutor {

    private final boolean requires_permission;
    private static final DecimalFormat DECIMAL_FORMATTER = new DecimalFormat("##.#####");

    private final BTEDymaxionProjection projection = new BTEDymaxionProjection();

    public Tpll(boolean requires_permission) {
        this.requires_permission = requires_permission;
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
        p.sendMessage(Utils.chat("&c/tpll <latitude> <longitude> [altitude]"));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Check if the sender is a player.
        //Only players can use /tpll.
        if (!(sender instanceof Player)) {

            sender.sendMessage(Utils.chat("&cThis command can only be used by players."));
            return true;

        }

        //Get player.
        Player p = (Player) sender;

        //Check if permission is required.
        if (requires_permission) {

            if (!p.hasPermission("uknet.network.tpll")) {

                p.sendMessage(Utils.chat("&cYou do not have permission to use this command."));
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
            proj = projection.fromGeo(defaultCoords.getLng(), defaultCoords.getLat());
        } catch (Exception e) {
            this.usage(p);
            return true;
        }

        if (Double.isNaN(altitude)) {

            //Get elevation here.
            altitude = p.getWorld().getHighestBlockYAt((int) proj[0], (int) proj[1]);

        }

        //Check if the region is on a plotserver.
        NetworkUser u = Network.getInstance().getUser(p);
        Location l = new Location(p.getWorld(), proj[0], altitude, proj[1]);
        if (Network.getInstance().regionSQL.hasRow("SELECT region FROM regions WHERE region=" + u.region.getRegion(l) + " AND status='plot';")) {

            //Get server and location of region.
            String server = Network.getInstance().plotSQL.getString("SELECT server FROM regions WHERE region=" + u.region.getRegion(l) + ";");
            String location = Network.getInstance().plotSQL.getString("SELECT location FROM regions WHERE region=" + u.region.getRegion(l) + ";");

            //Get the coordinate transformations.
            int xTransform = Network.getInstance().plotSQL.getInt("SELECT xTransform FROM location_data WHERE location=" + location + ";");
            int zTransform = Network.getInstance().plotSQL.getInt("SELECT zTransform FROM location_data WHERE location=" + location + ";");

            //If they are on the correct server, teleport them directly, else switch their server.
            if (server.equals(Network.SERVER_NAME)) {

                Location loc = new Location(Bukkit.getWorld(location), (proj[0] + xTransform), altitude, (proj[1] + zTransform), p.getLocation().getYaw(), p.getLocation().getPitch());
                Location loc2 = LocationUtil.getSafeDestination(loc);
                loc.setY(loc2.getY());

                LatLng finalDefaultCoords = defaultCoords;
                p.sendMessage(Utils.chat("&7Teleporting to &9" + DECIMAL_FORMATTER.format(finalDefaultCoords.getLat()) + "&7, &9" + DECIMAL_FORMATTER.format(finalDefaultCoords.getLng())));
                p.teleport(loc);

            } else {

                //Set join event to teleport there.
                Network.getInstance().globalSQL.update("INSERT INTO join_events(uuid,type,event) VALUES(" + p.getUniqueId() + ",'network'," + "tpll "
                        + (proj[0] + xTransform) + " " + (proj[1] + zTransform) + " " + p.getLocation().getYaw() + " " + p.getLocation().getPitch());

                //Switch server.
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("Connect");
                out.writeUTF(server);

            }
        }

        return true;
    }
}
