package me.bteuk.network.gui.plotsystem;

import me.bteuk.network.Network;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.sql.PlotSQL;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

public class DeleteConfirm extends Gui {

    private final int plotID;

    public DeleteConfirm(int plotID) {

        super(27, Component.text("Delete Plot " + plotID, NamedTextColor.AQUA, TextDecoration.BOLD));

        this.plotID = plotID;

        createGui();

    }

    private void createGui() {

        //Get plot sql.
        PlotSQL plotSQL = Network.getInstance().plotSQL;

        //Get global sql.
        GlobalSQL globalSQL = Network.getInstance().globalSQL;

        //Delete plot
        setItem(13, Utils.createItem(Material.TNT, 1,
                        Utils.chat("&b&lDelete Plot " + plotID),
                        Utils.chat("&fDelete the plot and its contents.")),
                u -> {

                    //Delete this inventory.
                    this.delete();
                    u.deleteConfirm = null;

                    //Create plot menu, so the next time you open the navigator you return to that.
                    u.plotMenu = new PlotMenu(u);

                    //Add server event to delete plot.
                    globalSQL.update("INSERT INTO server_events(uuid,type,server,event) VALUES('" + u.player.getUniqueId() + "','plotsystem','" +
                            plotSQL.getString("SELECT server FROM location_data WHERE name='" +
                                    plotSQL.getString("SELECT location FROM plot_data WHERE id=" + plotID + ";") + "';") +
                            "','delete plot " + plotID + "');");

                });

        //Return to plot info menu.
        setItem(26, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.chat("&b&lReturn"),
                        Utils.chat("&fReturn to the menu of plot " + plotID + ".")),
                u ->

                {

                    //Delete this inventory.
                    this.delete();
                    u.deleteConfirm = null;

                    //Switch back to plot info.
                    u.plotInfo = new PlotInfo(plotID, u.player.getUniqueId().toString());
                    u.plotInfo.open(u);

                });
    }

    public void refresh() {

        this.clearGui();
        createGui();

    }
}
