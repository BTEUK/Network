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

        ArrayList<Integer> plots = plotSQL.getIntList("SELECT id FROM plot_members WHERE uuid=" + user.player.getUniqueId() + " SORT BY last_enter DESC;");

        //Make a button for each plot.
        for (int i = 0; i < plots.size(); i++) {

            int finalI = i;
            gui.setItem(9 + Math.floorDiv(i, 7) + i, Utils.createItem(Material.LIME_CONCRETE, 1,
                            Utils.chat("&b&lPlot " + plots.get(i)),
                            Utils.chat("&fClick to open the menu of this plot.")),
                    u -> {

                        //Delete this inventory.
                        u.uniqueGui.delete();
                        u.player.closeInventory();

                        //Open the menu for this plot.
                        u.uniqueGui = PlotInfo.createPlotInfo(plots.get(finalI));
                        u.uniqueGui.open(u);

                    });

        }

        //Return
        gui.setItem(44, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.chat("&b&lReturn"),
                        Utils.chat("&fOpen the building menu.")),
                u -> {

                    //Delete this inventory.
                    u.uniqueGui.delete();
                    u.player.closeInventory();

                    //Open the navigator.
                    u.uniqueGui = BuildGui.createBuildGui(u);
                    u.uniqueGui.open(u);

                });

        return gui;

    }
}
