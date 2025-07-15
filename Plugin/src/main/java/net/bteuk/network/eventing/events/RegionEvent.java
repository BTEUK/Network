package net.bteuk.network.eventing.events;

import net.bteuk.network.Network;
import net.bteuk.network.lib.dto.DirectMessage;
import net.bteuk.network.lib.enums.ChatChannels;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.utils.enums.RegionStatus;
import net.bteuk.network.utils.regions.Region;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;

public class RegionEvent extends AbstractEvent {

    @Override
    public void event(String uuid, String[] event, String eMessage) {

        Region region;

        switch (event[1]) {
            case "set" -> {

                // Get region.
                region = Network.getInstance().getRegionManager().getRegion(event[3]);

                // If the region is not in the database add it.
                region.addToPlotsystem();
                if (event[2].equals("plotsystem")) {

                    // If region is not already set to plotsystem.
                    if (!(region.status() == RegionStatus.PLOT)) {

                        // Set region to plotsystem.
                        // This will kick any members.
                        region.setPlot();
                    }
                } else if (event[2].equals("default")) {

                    // If region is not already set to default.
                    if (!(region.status() == RegionStatus.DEFAULT)) {

                        // Set region to default.
                        region.setDefault();
                    }
                }
            }
            case "request" -> {
                if (event[2].equals("accept")) {

                    // If length is 4 then no user is specified, this implies that it should accept all requests for
                    // the region, rather than a specific request.
                    region = Network.getInstance().getRegionManager().getRegion(event[3]);
                    if (event.length == 4) {

                        region.acceptRequests();
                    } else {

                        // The 5th argument specifies the uuid of the player who created the request.

                        region.acceptRequest(event[4]);

                        // Send feedback to user who accepted the request.
                        DirectMessage directMessage = new DirectMessage(ChatChannels.GLOBAL.getChannelName(), uuid,
                                "server",
                                ChatUtils.success("Accepted region request for %s in the region %s.",
                                        Network.getInstance().getGlobalSQL().getString("SELECT name FROM player_data " +
                                                "WHERE uuid='" + event[4] + "';"), event[3]),
                                true);
                        Network.getInstance().getChat().sendSocketMessage(directMessage);
                    }
                } else if (event[2].equals("deny")) {

                    region = Network.getInstance().getRegionManager().getRegion(event[3]);

                    region.denyRequest(event[4]);

                    // Send feedback to user who denied the request.
                    DirectMessage directMessage = new DirectMessage(ChatChannels.GLOBAL.getChannelName(), uuid,
                            "server",
                            ChatUtils.success("Denied region request for %s in the region %s.",
                                    Network.getInstance().getGlobalSQL().getString("SELECT name FROM player_data " +
                                            "WHERE uuid='" + event[4] + "';"), event[3]),
                            true);
                    Network.getInstance().getChat().sendSocketMessage(directMessage);
                }
            }
            case "leave" -> {

                // Get region.
                region = Network.getInstance().getRegionManager().getRegion(event[2]);

                // Leave region.
                region.leaveRegion(uuid, LegacyComponentSerializer.legacyAmpersand().deserialize(eMessage));

                // If the region has members after you've left but no owner.
                // Find the most recent member and make them owner.
                if (region.hasMember() && !region.hasOwner()) {

                    String member = region.getRecentMember();

                    region.makeOwner(member);

                    // Send message to member that they are now the owner.
                    DirectMessage directMessage = new DirectMessage(ChatChannels.GLOBAL.getChannelName(), member,
                            "server",
                            ChatUtils.success("Transferred ownership of region %s to you due to the previous owner " +
                                    "leaving the region.", region.getTag(member)),
                            true);
                    Network.getInstance().getChat().sendSocketMessage(directMessage);
                } else if (!region.hasOwner() && !region.hasMember()) {

                    // The region is has no owner and members, set the status to default.
                    region.setDefault();
                }
            }
            case "join" -> {

                // Get the region.
                region = Network.getInstance().getRegionManager().getRegion(event[2]);

                // Add player to the region.
                // Create a copy of the coordinate id that the owner has.
                // The reason for a copy rather than using the same copy id is for if the user wants to set a new
                // location.
                // This then allows us to update the existing coordinate rather than create a new coordinate each
                // time this is done.
                Location l =
                        Network.getInstance().getGlobalSQL().getLocation(region.getCoordinateID(region.getOwner()));
                int coordinateID = Network.getInstance().getGlobalSQL().addCoordinate(l);
                region.joinRegion(uuid, coordinateID);

                // Send message to plot owner.
                DirectMessage directMessage = new DirectMessage(ChatChannels.GLOBAL.getChannelName(),
                        region.getOwner(), "server",
                        ChatUtils.success("%s has joined the region %s.",
                                Network.getInstance().getGlobalSQL().getString("SELECT name FROM player_data WHERE " +
                                        "uuid='" + uuid + "';"), region.getTag(region.getOwner())),
                        true);
                Network.getInstance().getChat().sendSocketMessage(directMessage);
            }
        }
    }
}
