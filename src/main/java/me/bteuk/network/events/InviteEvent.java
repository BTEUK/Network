package me.bteuk.network.events;

import me.bteuk.network.Network;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.sql.PlotSQL;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.regions.Region;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class InviteEvent {

    @Deprecated
    public static void event(String uuid, String[] event) {

        switch (event[1]) {
            case "plot" -> {

                //Get player.
                Player p = Bukkit.getPlayer(UUID.fromString(uuid));

                //Send the player a message telling them the command to join the plot.
                if (p != null) {

                    GlobalSQL globalSQL = Network.getInstance().globalSQL;
                    PlotSQL plotSQL = Network.getInstance().plotSQL;

                    int id = Integer.parseInt(event[2]);

                    p.sendMessage(Utils.success("You have been invited to plot &3" + event[2] + " &aby &3" +
                            globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + plotSQL.getString("SELECT owner FROM plot_invites WHERE id=" + id + ";") + "';")));

                    Component message = Utils.success("To join the plot click here!");
                    message = message.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/plot join " + event[2]));
                    p.sendMessage(message);

                }
            }
            case "zone" -> {

                //Get player.
                Player p = Bukkit.getPlayer(UUID.fromString(uuid));

                //Send the player a message telling them the command to join the plot.
                if (p != null) {

                    GlobalSQL globalSQL = Network.getInstance().globalSQL;
                    PlotSQL plotSQL = Network.getInstance().plotSQL;

                    int id = Integer.parseInt(event[2]);

                    p.sendMessage(Utils.success("You have been invited to zone &3" + event[2] + " &aby &3" +
                            globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + plotSQL.getString("SELECT owner FROM zone_invites WHERE id=" + id + ";") + "';")));


                    Component message = Utils.success("To join the zone click here!");
                    message = message.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/zone join " + event[2]));
                    p.sendMessage(message);

                }
            }
            case "region" -> {

                //Get player.
                Player p = Bukkit.getPlayer(UUID.fromString(uuid));

                //Send the player a message telling them the command to join the plot.
                if (p != null) {

                    GlobalSQL globalSQL = Network.getInstance().globalSQL;
                    Region region = Network.getInstance().getRegionManager().getRegion(event[2]);

                    p.sendMessage(Utils.success("You have been invited to region &3" + event[2] + " &aby &3" +
                            globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + region.getOwner() + "';")));

                    Component message = Utils.success("To join the region click here!");
                    message = message.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/region join " + event[2]));
                    p.sendMessage(message);

                }
            }
        }
    }
}
