package me.bteuk.network.gui.plotsystem;

import me.bteuk.network.Network;
import me.bteuk.network.gui.UniqueGui;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.sql.PlotSQL;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.ArrayList;

public class PlotMembers {

    public static UniqueGui createPlotMembers(int plotID, int page) {

        UniqueGui gui = new UniqueGui(45, Component.text("Manage Members", NamedTextColor.AQUA, TextDecoration.BOLD));

        GlobalSQL globalSQL = Network.getInstance().globalSQL;
        PlotSQL plotSQL = Network.getInstance().plotSQL;

        //Get all online players in the network.
        ArrayList<String> plot_members = plotSQL.getStringList("SELECT uuid FROM plot_members WHERE id=" + plotID + ";");

        //Slot count.
        int slot = 10;

        //Skip count.
        int skip = 21 * (page - 1);

        //If page is greater than 1 add a previous page button.
        if (page > 1) {
            gui.setItem(18, Utils.createItem(Material.ARROW, 1,
                            Utils.chat("&b&lPrevious Page"),
                            Utils.chat("&fOpen the previous page of online users.")),
                    u ->

                    {

                        //Update the gui.
                        u.uniqueGui.update(u, PlotMembers.createPlotMembers(plotID, page - 1));

                    });
        }

        //Iterate through all online players.
        if (plot_members != null) {
            for (String uuid : plot_members) {

                //If the slot is greater than the number that fit in a page, create a new page.
                if (slot > 34) {

                    gui.setItem(26, Utils.createItem(Material.ARROW, 1,
                                    Utils.chat("&b&lNext Page"),
                                    Utils.chat("&fOpen the next page of online users.")),
                            u ->

                            {

                                //Update the gui.
                                u.uniqueGui.update(u, PlotMembers.createPlotMembers(plotID, page + 1));

                            });

                }

                //Check whether the player is not already the owner or member of the plot, if true skip them.
                if (plotSQL.hasRow("SELECT uuid FROM plot_members WHERE id=" + plotID + " AND uuid='" + uuid + "';")) {
                    continue;
                }

                //If skip is greater than 0, skip this iteration.
                if (skip > 0) {
                    skip--;
                    continue;
                }

                //Add player to gui.
                gui.setItem(slot, Utils.createPlayerSkull(uuid, 1,
                                Utils.chat("&b&lKick " + globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + uuid + "';" + " from your plot.")),
                                Utils.chat("&fClick to remove them as member of your plot."),
                                Utils.chat("&fThey will no longer be able to build in it.")),
                        u ->

                        {
                            //Check if the player is not a member of the plot.
                            if (!plotSQL.hasRow("SELECT id FROM plot_members WHERE id=" + plotID + " AND uuid='" + uuid + "';")) {

                                //Send invite via chat.
                                //The invite will be active until they disconnect from the network.
                                //They will need to run a command to actually join the plot.
                                globalSQL.update("INSERT INTO server_events(uuid,type,server,event) VALUES('" + uuid + "','network','" +
                                        globalSQL.getString("SELECT server FROM online_users WHERE uuid=" + uuid + ";") + "','kick plot " + plotID + "')");

                                //Refresh the gui page after a second.
                                //The delay is so the plotsystem has time to update the members database.
                                Bukkit.getScheduler().scheduleSyncDelayedTask(Network.getInstance(), () -> {

                                    //Update the gui.
                                    u.uniqueGui.update(u, PlotMembers.createPlotMembers(plotID, page));

                                }, 20L);

                            } else {
                                u.player.sendMessage(Utils.chat("&cThis player is not a member of your plot."));
                            }
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
        }

        //Return
        gui.setItem(44, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.chat("&b&lReturn"),
                        Utils.chat("&fOpen the plot info for this plot.")),
                u -> {

                    //Switch back to plot info.
                    u.uniqueGui.switchGui(u, PlotInfo.createPlotInfo(plotID));

                });

        return gui;

    }
}
