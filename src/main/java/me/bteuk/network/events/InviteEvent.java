package me.bteuk.network.events;

import me.bteuk.network.Network;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.sql.PlotSQL;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class InviteEvent {

    public static void event(String uuid, String[] event) {

        if (event[1].equals("plot")) {

            //Get player.
            Player p = Bukkit.getPlayer(UUID.fromString(uuid));

            //Send the player a message telling them the command to join the plot.
            if (p != null) {

                GlobalSQL globalSQL = Network.getInstance().globalSQL;
                PlotSQL plotSQL = Network.getInstance().plotSQL;

                int id = Integer.parseInt(event[2]);

                p.sendMessage(Utils.chat("&aYou have been invited to plot &3" + event[2] + " &aby &3" +
                        globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + plotSQL.getString("SELECT owner FROM plot_invites WHERE id=" + id + ";") + "';")));
                p.sendMessage(Utils.chat("&aTo join the plot run the command &7/plot join " + event[2]));

            }
        }
    }
}
