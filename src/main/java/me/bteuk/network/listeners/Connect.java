package me.bteuk.network.listeners;

import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.sql.PlotSQL;
import org.bukkit.entity.Player;

//This class deals with players joining and leaving the network.
public class Connect {

    private final GlobalSQL globalSQL;
    private final PlotSQL plotSQL;

    public Connect(GlobalSQL globalSQL, PlotSQL plotSQL) {

        this.globalSQL = globalSQL;
        this.plotSQL = plotSQL;

    }

    /*
    A player has officially connected to the network if they have
    join the server but are not in the online_users table in the database.
     */
    public void joinEvent(Player p) {

    }

    /*
    A player has officially disconnected from the network after two
    unsuccessful pings by any network-connected server.
    A ping will occur on a one-second interval.
     */
    public void leaveEvent(String uuid) {

        //Remove any outstanding invites that this player has sent.
        plotSQL.update("DELETE FROM plot_invites WHERE owner=" + uuid + ";");

        //Remove any outstanding invites that this player has received.
        plotSQL.update("DELETE FROM plot_invites WHERE uuid=" + uuid + ";");

    }

}
