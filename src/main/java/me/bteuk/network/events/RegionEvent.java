package me.bteuk.network.events;

import me.bteuk.network.Network;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.regions.Region;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.UUID;

public class RegionEvent {

    public static void event(String uuid, String[] event) {

        //Get player.
        Player p = Bukkit.getPlayer(UUID.fromString(uuid));

        if (event[1].equals("set")) {
            if (event[2].equals("plotsystem")) {

                //Get region.
                Region region = Network.getInstance().getRegionManager().getRegion(event[3]);

                //If region is not already set to plotsystem.
                if (!region.isPlot()) {

                    //Set region to plotsystem.
                    //This will kick any members.
                    region.setPlot();

                }
            }

        } else if (event[1].equals("request")) {

            if (event[2].equals("accept")) {

                //If length is 4 then no user is specified, this implies that it should accept all requests for the region, rather than a specific request.
                if (event.length == 4) {

                    Region region = Network.getInstance().getRegionManager().getRegion(event[3]);

                    region.acceptRequests();

                }
            }

        } else if (event[1].equals("leave")) {

            //Get region.
            Region region = Network.getInstance().getRegionManager().getRegion(event[2]);

            //Leave region.
            region.leaveRegion(uuid);

            //If the region has members after you've left.
            //Find the most recent member and make them owner.
            if (region.hasMember()) {

                String member = region.getRecentMember();

                region.makeOwner(member);

                //Send message to member that they are now the owner.
                Network.getInstance().globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" + member + "','&aTransferred ownership of region "
                        + region.getTag(member) + " to you due to the previous owner leaving the region.');");

            }
        } else if (event[1].equals("teleport")) {

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

        } else if (event[1].equals("join")) {

            //Get the region.
            Region region = Network.getInstance().getRegionManager().getRegion(event[2]);

            //Add player to the region.
            //Set the coordinate id the same as the location of the owner.
            region.joinRegion(uuid, region.getCoordinateID(region.getOwner()));

            String message = "&aYou have joined region " + region.getTag(uuid) + " as a member.";

            if (p != null) {

                p.sendMessage(Utils.chat(message));

            } else {

                //Send a cross-server message.
                Network.getInstance().globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" + uuid + "','" + message + "';");

            }
        }
    }
}
