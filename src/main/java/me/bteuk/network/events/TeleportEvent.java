package me.bteuk.network.events;

import me.bteuk.network.Network;
import me.bteuk.network.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.UUID;

public class TeleportEvent {

    public static void event(String uuid, String[] event) {

        //Get player.
        Player p = Bukkit.getPlayer(UUID.fromString(uuid));

        if (p == null) {
            Network.getInstance().getLogger().warning("Player is null in teleport event.");
            return;
        }

        //Check if the teleport is to a specific player.
        if (event[1].equals("player")) {

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
            return;

            //Check if the teleport is to a specific coordinate ID.
        } else if (event[1].equals("coordinateID")) {

            p.teleport(Network.getInstance().globalSQL.getCoordinate(Integer.parseInt(event[2])));
            p.sendMessage(Utils.chat("&aTeleported to previous location."));

        } else if (event[1].equals("location") || event[1].equals("location_request")) {

            //Get location name from all remaining args.
            String location = String.join(" ", Arrays.copyOfRange(event, 2, event.length));

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

        //Get world.
        World world = Bukkit.getWorld(event[1]);

        if (world == null) {
            p.sendMessage(Utils.chat("&cWorld can not be found."));
            return;
        }

        //Get x and z.
        double x = Double.parseDouble(event[2]);
        double z = Double.parseDouble(event[3]);

        //Get y elevation for teleport.
        int y = world.getHighestBlockYAt((int) x, (int) z);
        y++;

        //Get pitch and yaw.
        float yaw = Float.parseFloat(event[4]);
        float pitch = Float.parseFloat(event[5]);

        //Create location.
        Location l = new Location(world, x, y, z, yaw, pitch);

        //Teleport player.
        p.teleport(l);
        p.sendMessage(Utils.chat("&aTeleport to &3" + x + ", " + y + ", " + z));

    }
}
