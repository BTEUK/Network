package me.bteuk.network.gui.plotsystem;

import me.bteuk.network.Network;
import me.bteuk.network.gui.BuildGui;
import me.bteuk.network.gui.UniqueGui;
import me.bteuk.network.sql.PlotSQL;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

import java.util.ArrayList;

public class PlotMenu {

    public static UniqueGui createPlotMenu(NetworkUser user) {

        UniqueGui gui = new UniqueGui(45, Component.text("Plot Menu", NamedTextColor.AQUA, TextDecoration.BOLD));

        PlotSQL plotSQL = Network.getInstance().plotSQL;

        ArrayList<Integer> plots = plotSQL.getIntList("SELECT id FROM plot_members WHERE uuid='" + user.player.getUniqueId() + "' SORT BY last_enter DESC;");

        //Slot count.
        int slot = 10;

        //Make a button for each plot.
        for (int i = 0; i < plots.size(); i++) {

            int finalI = i;
            gui.setItem(slot, Utils.createItem(Material.LIME_CONCRETE, 1,
                            Utils.chat("&b&lPlot " + plots.get(i)),
                            Utils.chat("&fClick to open the menu of this plot.")),
                    u -> {

                        //Switch to plot info.
                        u.uniqueGui.switchGui(u, PlotInfo.createPlotInfo(plots.get(finalI)));

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

        //Accepted plots menu, if you have any.
        if (plotSQL.hasRow("SELECT uuid FROM accept_data WHERE uuid='" + user.player.getUniqueId() + "';")) {

            gui.setItem(40, Utils.createItem(Material.CLOCK, 1,
                            Utils.chat("&b&lAccepted Plots"),
                            Utils.chat("&fClick to open the accepted plots menu.")),

                    u -> {

                        //Switch to accepted plot feedback.
                        u.uniqueGui.switchGui(u, AcceptedPlotFeedback.createAcceptedPlotFeedback(u, 1));

                    });
        }

        //Return
        gui.setItem(44, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.chat("&b&lReturn"),
                        Utils.chat("&fOpen the building menu.")),
                u -> {

                    //Switch back to build menu.
                    u.uniqueGui.switchGui(u, BuildGui.createBuildGui(u));

                });

        return gui;

    }
}
