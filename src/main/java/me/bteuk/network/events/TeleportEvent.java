package me.bteuk.network.events;

import me.bteuk.network.Network;
import me.bteuk.network.utils.SwitchServer;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.regions.Region;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.UUID;

public class TeleportEvent {

    public static void event(String uuid, String[] event, String message) {

        //Get player.
        Player p = Bukkit.getPlayer(UUID.fromString(uuid));

        if (p == null) {
            Network.getInstance().getLogger().warning("Player is null in teleport event.");
            return;
        }

        //Check if the teleport is to a specific player.
        switch (event[1]) {
            case "player" -> {

                //Get player if they're online and teleport the player there.
                Player player = Bukkit.getPlayer(UUID.fromString(event[2]));
                if (player != null) {

                    p.teleport(player.getLocation());
                    p.sendMessage(Utils.chat("&aTeleported to &3" +
                            Network.getInstance().globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + event[2] + "';")));

                } else {
                    p.sendMessage(Utils.chat("&c" +
                            Network.getInstance().globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + event[2] + "';") +
                            " is not online."));
                }
            }

            //Check if the teleport is to a specific coordinate ID.
            case "coordinateID" -> {
                p.teleport(Network.getInstance().globalSQL.getCoordinate(Integer.parseInt(event[2])));
                p.sendMessage(Utils.chat("&aTeleported to previous location."));
            }
            case "location", "location_request" -> {

                //Get location name from all remaining args.
                String location = String.join(" ", Arrays.copyOfRange(event, 2, event.length - 1));

                //Get the coordinate id.
                int coordinate_id;
                if (event[1].equals("location")) {
                    coordinate_id = Network.getInstance().globalSQL.getInt("SELECT coordinate FROM location_data WHERE location='" + location + "';");
                } else {
                    coordinate_id = Network.getInstance().globalSQL.getInt("SELECT coordinate FROM location_requests WHERE location='" + location + "';");
                }

                Location l = Network.getInstance().globalSQL.getCoordinate(coordinate_id);

                //If world is in plot system add coordinate transform.
                String world = Network.getInstance().globalSQL.getString("SELECT world FROM coordinates WHERE id=" + coordinate_id + ";");
                if (Network.getInstance().plotSQL.hasRow("SELECT name FROM location_data WHERE name='" + world + "';")) {
                    l.setX(l.getX() + Network.getInstance().plotSQL.getInt("SELECT xTransform FROM location_data WHERE name='" + world + "';"));
                    l.setZ(l.getZ() + Network.getInstance().plotSQL.getInt("SELECT zTransform FROM location_data WHERE name='" + world + "';"));
                }

                p.teleport(l);
                p.sendMessage(Utils.chat("&aTeleported to &3" + location));

            }
            case "region" -> {

                //Get the region.
                Region region = Network.getInstance().getRegionManager().getRegion(event[2]);
                Location l = Network.getInstance().globalSQL.getCoordinate(region.getCoordinateID(uuid));

                if (l == null) {
                    p.sendMessage(Utils.chat("&cAn error occurred while fetching the location to teleport."));
                    Network.getInstance().getLogger().warning("Location is null for coodinate id " + region.getCoordinateID(uuid));
                    return;
                }

                //Teleport player.
                p.teleport(l);
                p.sendMessage(Utils.chat("&aTeleported to region &3" + region.getTag(uuid)));

            }
            case "server" -> //Switch to server.
                    SwitchServer.switchServer(p, event[2]);

            default -> {

                //Get world.
                World world = Bukkit.getWorld(event[1]);

                if (world == null) {
                    p.sendMessage(Utils.chat("&cWorld can not be found."));
                    return;
                }

                double x;
                double y;
                double z;
                float yaw;
                float pitch;

                if (event.length == 6) {
                    //Length 6 means no y is specified.

                    //Get x and z.
                    x = Double.parseDouble(event[2]);
                    z = Double.parseDouble(event[3]);

                    //Get y elevation for teleport.
                    y = world.getHighestBlockYAt((int) x, (int) z);
                    y++;

                    //Get pitch and yaw.
                    yaw = Float.parseFloat(event[4]);
                    pitch = Float.parseFloat(event[5]);
                } else {
                    //Length 7 means y is specific.

                    //Get x, y and z.
                    x = Double.parseDouble(event[2]);
                    y = Double.parseDouble(event[3]);
                    z = Double.parseDouble(event[4]);

                    //Get pitch and yaw.
                    yaw = Float.parseFloat(event[5]);
                    pitch = Float.parseFloat(event[6]);
                }

                //Create location.
                Location l = new Location(world, x, y, z, yaw, pitch);

                //Teleport player.
                p.teleport(l);

                //If custom message is set, send that to player, else send default message.
                if (message == null) {
                    p.sendMessage(Utils.chat("&aTeleported to &3" + x + ", " + y + ", " + z));
                } else {
                    p.sendMessage(Utils.chat(message));
                }
            }
        }
    }
}
