package me.bteuk.network.events;

import me.bteuk.network.Network;
import me.bteuk.network.utils.enums.RegionStatus;
import me.bteuk.network.utils.regions.Region;
import org.bukkit.Location;

public class RegionEvent extends AbstractEvent {

    @Override
    public void event(String uuid, String[] event, String eMessage) {

        Region region;

        switch (event[1]) {
            case "set" -> {

                //Get region.
                region = Network.getInstance().getRegionManager().getRegion(event[3]);

                //If the region is not in the database add it.
                region.addToPlotsystem();
                if (event[2].equals("plotsystem")) {

                    //If region is not already set to plotsystem.
                    if (!(region.status() == RegionStatus.PLOT)) {

                        //Set region to plotsystem.
                        //This will kick any members.
                        region.setPlot();

                    }
                } else if (event[2].equals("default")) {

                    //If region is not already set to default.
                    if (!(region.status() == RegionStatus.DEFAULT)) {

                        //Set region to default.
                        region.setDefault();

                    }

                }
            }
            case "request" -> {
                if (event[2].equals("accept")) {

                    //If length is 4 then no user is specified, this implies that it should accept all requests for the region, rather than a specific request.
                    region = Network.getInstance().getRegionManager().getRegion(event[3]);
                    if (event.length == 4) {

                        region.acceptRequests();

                    } else {

                        //The 5th argument specifies the uuid of the player who created the request.

                        region.acceptRequest(event[4]);

                        //Send feedback to user who accepted the request.
                        Network.getInstance().globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" + uuid + "','&aAccepted region request for &3" + Network.getInstance().globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + event[4] + "';") + " &ain the region &3 " + event[3] + ".');");

                    }
                } else if (event[2].equals("deny")) {

                    region = Network.getInstance().getRegionManager().getRegion(event[3]);

                    region.denyRequest(event[4]);

                    //Send feedback to user who accepted the request.
                    Network.getInstance().globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" + uuid + "','&aDenied region request for &3" + Network.getInstance().globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + event[4] + "';") + " &ain the region &3 " + event[3] + ".');");

                }
            }
            case "leave" -> {

                //Get region.
                region = Network.getInstance().getRegionManager().getRegion(event[2]);

                //Leave region.
                region.leaveRegion(uuid, eMessage);

                //If the region has members after you've left but no owner.
                //Find the most recent member and make them owner.
                if (region.hasMember() && !region.hasOwner()) {

                    String member = region.getRecentMember();

                    region.makeOwner(member);

                    //Send message to member that they are now the owner.
                    Network.getInstance().globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" + member + "','&aTransferred ownership of region "
                            + region.getTag(member) + " to you due to the previous owner leaving the region.');");

                } else if (!region.hasOwner() && !region.hasMember()) {

                    //The region is has no owner and members, set the status to default.
                    region.setDefault();

                }
            }
            case "join" -> {

                //Get the region.
                region = Network.getInstance().getRegionManager().getRegion(event[2]);

                //Add player to the region.
                //Create a copy of the coordinate id that the owner has.
                //The reason for a copy rather than using the same copy id is for if the user wants to set a new location.
                //This then allows us to update the existing coordinate rather than create a new coordinate each time this is done.
                Location l = Network.getInstance().globalSQL.getCoordinate(region.getCoordinateID(region.getOwner()));
                int coordinateID = Network.getInstance().globalSQL.addCoordinate(l);
                region.joinRegion(uuid, coordinateID);

                //Send message to plot owner.
                Network.getInstance().globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" + region.getOwner() + "','&3" +
                        Network.getInstance().globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + uuid + "';") + " &ahas joined region &3" + region.getTag(region.getOwner()) + "');");
            }
        }
    }
}
