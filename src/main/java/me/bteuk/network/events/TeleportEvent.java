package me.bteuk.network.events;

import me.bteuk.network.Network;
import me.bteuk.network.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

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
                p.sendMessage(Utils.chat("&aTeleported to " +
                        Network.getInstance().globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + event[2] + "';")));

            } else {
                p.sendMessage(Utils.chat("&c" +
                        Network.getInstance().globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + event[2] + "';") +
                        " is not online."));
            }
            return;

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

        //Get pitch and yaw.
        float yaw = Float.parseFloat(event[4]);
        float pitch = Float.parseFloat(event[5]);

        //Create location.
        Location l = new Location(world, x, y, z, yaw, pitch);

        //Teleport player.
        p.teleport(l);
        p.sendMessage(Utils.chat("&cTeleport to " + x + ", " + y + ", " + z));

    }
}
