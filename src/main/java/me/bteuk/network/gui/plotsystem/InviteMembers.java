package me.bteuk.network.gui.plotsystem;

import me.bteuk.network.Network;
import me.bteuk.network.gui.UniqueGui;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.sql.PlotSQL;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

import java.util.ArrayList;

public class InviteMembers {

    public static UniqueGui createInviteMembers(NetworkUser user, int plotID, int page) {

        UniqueGui gui = new UniqueGui(45, Component.text("Plot Menu", NamedTextColor.AQUA, TextDecoration.BOLD));

        GlobalSQL globalSQL = Network.getInstance().globalSQL;
        PlotSQL plotSQL = Network.getInstance().plotSQL;

        //Get all online players.
        ArrayList<String> online_users = globalSQL.getStringList("SELECT uuid FROM online_users;");

        //Slot count.
        int slot = 10;

        //Skip count.
        int skip = 28 * (page - 1);

        //Iterate through all online players.
        for (String uuid : online_users) {

            //If the slot is greater than the number that fit in a page, create a new page.
            if (slot > 34) {

                //TODO: Create additional page.

            }

            //Check whether the player is not already the owner or member of the plot, if true skip them.
            if (plotSQL.hasRow("SELECT uuid FROM plot_members WHERE id=" + plotID + " uuid=" + uuid + ";")) {
                continue;
            }

            //If skip is greater than 0, skip this iteration.
            if (skip > 0) {
                skip--;
                continue;
            }

            //Add player to gui.
            gui.setItem(slot, Utils.createItem(Material.DIAMOND_PICKAXE, 1,
                            Utils.chat("&b&lInvite " + globalSQL.getString("SELECT name FROM player_data WHERE uuid=" + uuid + ";" + " to your plot.")),
                            Utils.chat("&fThey will receive an invitation in chat."),
                            Utils.chat("&fSpamming invites may result in plot invites being blocked!")),
                    u ->

                    {

                        //Send invite via chat.
                        //If the player is on this server, send it directly.
                        //Else, add the event to database for the current server to handle it.
                        globalSQL.update("INSERT INTO server_events(uuid,type,server,event) VALUES(" + uuid + ",'network'," +
                                globalSQL.getString("SELECT server FROM online_users WHERE uuid=" + uuid + ";") + ",'invite plot " + plotID + "')");

                    });


            //Increase slot accordingly.
            if (slot % 9 == 7) {
                //Increase row, basically add 3.
                slot += 3;
            } else {
                //Increase value by 1.
                slot++;
            }
        }


        return gui;

    }
}
