package me.bteuk.network.listeners;

import me.bteuk.network.Network;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.sql.PlotSQL;
import me.bteuk.network.utils.Time;
import org.bukkit.entity.Player;

//This class deals with players joining and leaving the network.
public class Connect {

    Network instance;

    private final GlobalSQL globalSQL;
    private final PlotSQL plotSQL;

    private final String joinMessage;
    private final String leaveMessage;

    public Connect(Network instance, GlobalSQL globalSQL, PlotSQL plotSQL) {

        this.instance = instance;

        this.globalSQL = globalSQL;
        this.plotSQL = plotSQL;

        //Get join and leave message from config.
        joinMessage = instance.getConfig().getString("chat.join");
        leaveMessage = instance.getConfig().getString("chat.leave");

    }

    /*
    A player has officially connected to the network if they have
    join the server but are not in the online_users table in the database.
     */
    public void joinEvent(Player p) {

        //If the user is not yet in the player_data table add them.
        if (!globalSQL.hasRow("SELECT uuid FROM player_data WHERE uuid='" + p.getUniqueId() + "';")) {

            globalSQL.update("INSERT INTO player_data(uuid,name,last_online,last_submit) VALUES('" +
                    p.getUniqueId() + "','" + p.getName() + "'," + Time.currentTime() + "," + 0 + ");");

        } else {

            //Update the online time and name.
            globalSQL.update("UPDATE player_data SET name='" + p.getName() + "',last_online=" + Time.currentTime() + " WHERE uuid='" + p.getUniqueId() + "';");

        }

        //Send global connect message.
        instance.chat.broadcastMessage(joinMessage.replace("%player%", p.getName()));

    }

    /*
    A player has officially disconnected from the network after two
    unsuccessful pings by any network-connected server.
    A ping will occur on a one-second interval.
     */
    public void leaveEvent(String uuid) {

        //Remove any outstanding invites that this player has sent.
        plotSQL.update("DELETE FROM plot_invites WHERE owner='" + uuid + "';");

        //Remove any outstanding invites that this player has received.
        plotSQL.update("DELETE FROM plot_invites WHERE uuid='" + uuid + "';");

        //Set last_online time in playerdata.
        globalSQL.update("UPDATE player_data SET last_online=" + Time.currentTime() + " WHERE UUID='" + uuid + "';");

        //Get the player name and send global disconnect message.
        String name = globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + uuid + "';");
        instance.chat.broadcastMessage(leaveMessage.replace("%player%", name));

        //Remove player from online_users.
        globalSQL.update("DELETE FROM online_users WHERE uuid='" + uuid + "';");

    }

}
