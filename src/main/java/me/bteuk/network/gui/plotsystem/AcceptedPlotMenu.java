package me.bteuk.network.gui.plotsystem;

import lombok.Setter;
import me.bteuk.network.Network;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.sql.PlotSQL;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

import java.util.HashMap;

/**
 * This Gui class allows the player to view accepted plots.
 * Default is filtered to view the completed plots by the user.
 * However, the filter can be altered to view all accepted plots,
 * or those by a specific user, granted they have completed at least one plot.
 */
public class AcceptedPlotMenu extends Gui {

    private final PlotSQL plotSQL;
    private final GlobalSQL globalSQL;

    /**
     * Filter that is applied when fetching all plots.
     * The uuid is the value.
     * A filter of null implies all users.
     */
    @Setter
    private String filter;

    private int page = 1;

    public AcceptedPlotMenu(NetworkUser user) {

        super(45, Component.text("Plot Menu", NamedTextColor.AQUA, TextDecoration.BOLD));

        plotSQL = Network.getInstance().getPlotSQL();
        globalSQL = Network.getInstance().getGlobalSQL();

        filter = user.player.getUniqueId().toString();
        createGui();

    }

    private void createGui() {

        // Fetch accepted plots.
        HashMap<Integer, String> plots;
        if (filter == null) {
            plots = plotSQL.getIntStringMap("SELECT id,uuid FROM accept_data ORDER BY accept_time DESC;");
        } else {
            plots = plotSQL.getIntStringMap("SELECT id,uuid FROM accept_data WHERE uuid='" + filter + "' ORDER BY accept_time DESC;");
        }

        // Set the filter.
        setItem(4, Utils.createItem(
                        Material.SPRUCE_SIGN, 1, Utils.title("Alter filter"),
                        Utils.line("The current filter is set to: ").append(Component.text(
                                filter == null ? "All Players" : globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + filter + "';"), NamedTextColor.GRAY
                        )),
                        Utils.line("Click to select a different filter.")),
                u -> {
                    // Open the filter menu.
                    this.delete();
                    u.mainGui = new FilterMenu(u);
                    u.mainGui.open(u);
                });

        // Slot count.
        int slot = 10;

        // If page > 1 set number of iterations that must be skipped.
        int skip = (page - 1) * 21;

        //If page is greater than 1 add a previous page button.
        if (page > 1) {
            setItem(18, Utils.createItem(Material.ARROW, 1,
                            Utils.title("Previous Page"),
                            Utils.line("Open the previous page of accepted plots.")),
                    u ->

                    {
                        //Update the gui.
                        page--;
                        this.refresh();
                        u.player.getOpenInventory().getTopInventory().setContents(this.getInventory().getContents());
                    });
        }

        //Make a button for each plot.
        for (int plotID : plots.keySet()) {

            //Skip iterations if skip > 0.
            if (skip > 0) {
                skip--;
                continue;
            }

            //If the slot is greater than the number that fit in a page, create a new page.
            if (slot > 34) {

                setItem(26, Utils.createItem(Material.ARROW, 1,
                                Utils.title("Next Page"),
                                Utils.line("Open the next page of accepted plots.")),
                        u ->

                        {
                            //Update the gui.
                            page++;
                            this.refresh();
                            u.player.getOpenInventory().getTopInventory().setContents(this.getInventory().getContents());
                        });

                //Stop iterating.
                break;
            }

            // The icon is the player head of the plot builder.
            setItem(slot, Utils.createPlayerSkull(plots.get(plotID), 1,
                            Utils.title("Plot " + plotID),
                            Utils.line("Completed by: ").append(Component.text(globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + plots.get(plotID) + "';"), NamedTextColor.GRAY)),
                            Utils.line("Click to open the menu of this plot.")),
                    u -> {
                        //Delete this gui.
                        this.delete();
                        u.mainGui = null;

                        //Switch to plot info.
                        u.mainGui = new PlotInfo(u, plotID);
                        u.mainGui.open(u);
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

        //Return
        setItem(44, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.title("Return"),
                        Utils.line("Open the plot menu.")),
                u -> {
                    //Delete this gui.
                    this.delete();
                    u.mainGui = null;

                    //Return to the plot menu.
                    u.mainGui = new PlotMenu(u);
                    u.mainGui.open(u);
                });
    }

    public void refresh() {

        this.clearGui();
        createGui();

    }
}
