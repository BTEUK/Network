package me.bteuk.network.gui.plotsystem;

import me.bteuk.network.Network;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.gui.UniqueGui;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.sql.PlotSQL;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

public class DeleteConfirm {

    public static UniqueGui createDeleteConfirm(int plotID) {

        UniqueGui gui = new UniqueGui(27, Component.text("Delete Plot " + plotID, NamedTextColor.AQUA, TextDecoration.BOLD));

        //Get plot sql.
        PlotSQL plotSQL = Network.getInstance().plotSQL;

        //Get global sql.
        GlobalSQL globalSQL = Network.getInstance().globalSQL;

        //Delete plot
        gui.setItem(13, Utils.createItem(Material.TNT, 1,
                        Utils.chat("&b&lDelete Plot &3" + plotID),
                        Utils.chat("&fDelete the plot and its contents.")),
                u -> {

                    //Delete this inventory.
                    u.uniqueGui.delete(u);

                    //Add server event to delete plot.
                    globalSQL.update("INSERT INTO server_events(uuid,type,server,event) VALUES('" + u.player.getUniqueId() + "','plotsystem'," +
                            plotSQL.getString("SELECT server FROM location_data WHERE name='" +
                                    plotSQL.getString("SELECT location FROM plot_data WHERE id=" + plotID + ";") + "';") +
                            ",'delete plot " + plotID + "');");

                });

        //Return to plot info menu.
        gui.setItem(26, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.chat("&b&lReturn"),
                        Utils.chat("&fReturn to the menu of plot " + plotID + ".")),
                u ->

                {

                    //Switch back to plot info.
                    u.uniqueGui.switchGui(u, PlotInfo.createPlotInfo(plotID));

                });

        return gui;

    }
}
