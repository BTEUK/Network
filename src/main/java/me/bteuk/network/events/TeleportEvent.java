package me.bteuk.network.events;

import me.bteuk.network.Network;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.regions.Region;
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

        //If second string is 'coordinate' then the player must be teleported to a coordinate id.
        if (event[1].equals("region")) {

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
