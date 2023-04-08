package me.bteuk.network.events;

import me.bteuk.network.Network;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.sql.PlotSQL;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.regions.Region;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
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

                p.sendMessage(Utils.success("You have been invited to plot &3" + event[2] + " &aby &3" +
                        globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + plotSQL.getString("SELECT owner FROM plot_invites WHERE id=" + id + ";") + "';")));

                TextComponent message = new TextComponent(Utils.success("To join the plot click &3here&a!"));
                message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/plot join " + event[2]));
                p.spigot().sendMessage(message);

            }
        } else if (event[1].equals("zone")) {

            //Get player.
            Player p = Bukkit.getPlayer(UUID.fromString(uuid));

            //Send the player a message telling them the command to join the plot.
            if (p != null) {

                GlobalSQL globalSQL = Network.getInstance().globalSQL;
                PlotSQL plotSQL = Network.getInstance().plotSQL;

                int id = Integer.parseInt(event[2]);

                p.sendMessage(Utils.success("You have been invited to zone &3" + event[2] + " &aby &3" +
                        globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + plotSQL.getString("SELECT owner FROM zone_invites WHERE id=" + id + ";") + "';")));

                TextComponent message = new TextComponent(Utils.success("To join the zone click &3here&a!"));
                message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/zone join " + event[2]));
                p.spigot().sendMessage(message);

            }
        } else if (event[1].equals("region")) {

            //Get player.
            Player p = Bukkit.getPlayer(UUID.fromString(uuid));

            //Send the player a message telling them the command to join the plot.
            if (p != null) {

                GlobalSQL globalSQL = Network.getInstance().globalSQL;
                Region region = Network.getInstance().getRegionManager().getRegion(event[2]);

                p.sendMessage(Utils.success("You have been invited to region &3" + event[2] + " &aby &3" +
                        globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + region.getOwner() + "';")));

                TextComponent message = new TextComponent(Utils.success("To join the region click &3here&a!"));
                message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/region join " + event[2]));
                p.spigot().sendMessage(message);

            }
        }
    }
}
