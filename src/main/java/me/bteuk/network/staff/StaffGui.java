package me.bteuk.network.staff;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.bteuk.network.Network;
import me.bteuk.network.gui.UniqueGui;
import me.bteuk.network.sql.PlotSQL;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

import java.util.ArrayList;

public class StaffGui {

    public static UniqueGui createBuildGui(NetworkUser user) {

        UniqueGui gui = new UniqueGui(27, Component.text("Staff Menu", NamedTextColor.AQUA, TextDecoration.BOLD));

        /*TODO:

        Staff menu for regions.
        Staff menu for region join requests.

        Staff menu for plot reviewing.

        Staff menu for navigation menu requests.

        Staff menu for moderation.

         */

        /*
        Click to open menu to edit region details.

        Event team:
            Make any region open or public.

        Moderators:
            Remove people from regions, or transfer ownership.
            Lock regions.

         */
        gui.setItem(12, Utils.createItem(Material.ENDER_PEARL, 1,
                        Utils.chat("&b&lManage Region " + user.getRegion()),
                        Utils.chat("&fOpens a menu to manage details of the region you are currently in.")),
                u -> {

                    //Manage Region Menu.

                });

        //Click to open menu to deal with region join requests.
        if (true/*request exists*/) {
            gui.setItem(12, Utils.createItem(Material.ENDER_PEARL, 1,
                            Utils.chat("&b&lReview Region Requests"),
                            Utils.chat("&fOpens a menu to review active region join requests by Jr.Builders.")),
                    u -> {

                    });
        }

        //Click to review plot.
        //Check if there is a plot available to review,
        //that you are not already the owner or member of.
        PlotSQL plotSQL = Network.getInstance().plotSQL;
        if (plotSQL.hasRow("SELECT id FROM plot_data WHERE status='submitted';")) {

            //Get arraylist of submitted plots.
            ArrayList<Integer> plots = plotSQL.getIntList("SELECT id FROM plot_data WHERE status='submitted';");

            //Iterate through all plots.
            for (int plot : plots) {

                //If you are not owner or member of the plot select it for the next review.
                if (plotSQL.hasRow("SELECT id FROM plot_members WHERE uuid=" + user.player.getUniqueId() + " AND id=" + plot + ";")) {

                    //Show review plot button in gui.
                    gui.setItem(12, Utils.createItem(Material.ENDER_PEARL, 1,
                                    Utils.chat("&b&lReview Plot"),
                                    Utils.chat("&fClick to review a submitted plot.")),
                            u -> {

                                //Check if there is a plot available to review,
                                //that you are not already the owner or member of.
                                if (plotSQL.hasRow("SELECT id FROM plot_data WHERE status='submitted';")) {

                                    //Get arraylist of submitted plots.
                                    ArrayList<Integer> nPlots = plotSQL.getIntList("SELECT id FROM plot_data WHERE status='submitted';");

                                    //Iterate through all plots.
                                    for (int nPlot : nPlots) {

                                        //If you are not owner or member of the plot select it for the next review.
                                        if (plotSQL.hasRow("SELECT id FROM plot_members WHERE uuid=" + u.player.getUniqueId() + " AND id=" + nPlot + ";")) {

                                            //Set plot as under review.
                                            plotSQL.update("UPDATE plot_data SET status='reviewing' WHERE id=" + nPlot + ";");

                                            //Get server of plot.
                                            String server = plotSQL.getString("SELECT server FROM location_data WHERE name=" +
                                                    plotSQL.getString("SELECT location FROM plot_data WHERE id=" + nPlot + ";") + ";");

                                            //If they are not in the same server as the plot teleport them to that server and start the reviewing process.
                                            if (server.equals(Network.SERVER_NAME)) {

                                                Network.getInstance().globalSQL.update("INSERT INTO server_events(uuid,server,event) VALUES("
                                                        + u.player.getUniqueId() + "," + Network.SERVER_NAME + ",'review plot " + nPlot + "');");

                                            } else {

                                                //Player is not on the current server.
                                                //Set the server join event.
                                                Network.getInstance().globalSQL.update("INSERT INTO join_events(uuid,event) VALUES("
                                                        + u.player.getUniqueId()
                                                        + "," + "review plot " + nPlot + ");");

                                                //Teleport them to another server.
                                                u.player.closeInventory();
                                                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                                                out.writeUTF("Connect");
                                                out.writeUTF(server);

                                            }
                                        }
                                    }
                                }
                            });

                    //Stop iterating.
                    break;
                }
            }
        }

        //Click to open menu of navigation menu requests.
        if (true/*request exists*/) {
            gui.setItem(12, Utils.createItem(Material.ENDER_PEARL, 1,
                            Utils.chat("&b&lReview Navigation Menu Requests"),
                            Utils.chat("&fOpens a menu to review navigation menu requests.")),
                    u -> {

                    });
        }

        //Click to open moderation menu.
        gui.setItem(12, Utils.createItem(Material.ENDER_PEARL, 1,
                        Utils.chat("&b&lModeration Menu"),
                        Utils.chat("&fOpens the moderation menu to deal with wrongdoers.")),
                u -> {

                });

        return gui;
    }
}