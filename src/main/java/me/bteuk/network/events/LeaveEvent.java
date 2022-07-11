package me.bteuk.network.events;

import me.bteuk.network.Network;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.regions.Region;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class LeaveEvent {

    public static void event(String uuid, String[] event) {

        //Get player.
        Player p = Bukkit.getPlayer(UUID.fromString(uuid));

        //If second string is 'coordinate' then the player must be teleported to a coordinate id.
        if (event[1].equals("region")) {

            //Get region.
            Region region = Network.getInstance().getRegionManager().getRegion(event[2]);

            //Set message.
            String message = "&aYou have left region " + region.getTag(uuid);

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

            //If player exists send message directly, else use cross-server message.
            if (p == null) {

                Network.getInstance().globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" + uuid + "','" + message + "');");

            } else {
                p.sendMessage(Utils.chat(message));
            }
        }
    }
}
