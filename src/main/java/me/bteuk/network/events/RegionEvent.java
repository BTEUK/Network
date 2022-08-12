package me.bteuk.network.events;

import me.bteuk.network.Network;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.regions.Region;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public class RegionEvent {

    public static void event(String uuid, String[] event) {

        switch (event[1]) {
            case "set":
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

                break;
            case "request":

                if (event[2].equals("accept")) {

                    //If length is 4 then no user is specified, this implies that it should accept all requests for the region, rather than a specific request.
                    Region region = Network.getInstance().getRegionManager().getRegion(event[3]);
                    if (event.length == 4) {

                        region.acceptRequests();

                    } else {

                        //The 5th argument specifies the uuid of the player who created the request.

                        region.acceptRequest(event[4]);

                        //Send feedback to user who accepted the request.
                        Network.getInstance().globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" + uuid + "','&aAccepted region request for &3" + Network.getInstance().globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + event[4] + "';") + " &ain the region &3 " + event[3] + ".'");

                    }
                } else if (event[2].equals("deny")) {

                    Region region = Network.getInstance().getRegionManager().getRegion(event[3]);

                    region.denyRequest(event[4]);

                    //Send feedback to user who accepted the request.
                    Network.getInstance().globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" + uuid + "','&aDenied region request for &3" + Network.getInstance().globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + event[4] + "';") + " &ain the region &3 " + event[3] + ".'");

                }

                break;
            case "leave": {

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
                break;
            }
            case "teleport": {

                //Get player.
                Player p = Bukkit.getPlayer(UUID.fromString(uuid));

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

                break;
            }
            case "join": {

                //Get player.
                Player p = Bukkit.getPlayer(UUID.fromString(uuid));

                //Get the region.
                Region region = Network.getInstance().getRegionManager().getRegion(event[2]);

                //Add player to the region.
                //Create a copy of the coordinate id that the owner has.
                //The reason for a copy rather than using the same copy id is for if the user wants to set a new location.
                //This then allows us to update the existing coordinate rather than create a new coordinate each time this is done.
                Location l = Network.getInstance().globalSQL.getCoordinate(region.getCoordinateID(region.getOwner()));
                int coordinateID = Network.getInstance().globalSQL.addCoordinate(l);
                region.joinRegion(uuid, coordinateID);

                String message = "&aYou have joined region " + region.getTag(uuid) + " as a member.";

                if (p != null) {

                    p.sendMessage(Utils.chat(message));

                } else {

                    //Send a cross-server message.
                    Network.getInstance().globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" + uuid + "','" + message + "';");

                }
                break;
            }
        }
    }
}
