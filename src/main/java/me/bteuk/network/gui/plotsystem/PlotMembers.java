package me.bteuk.network.gui.plotsystem;

import me.bteuk.network.Network;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.sql.PlotSQL;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.ArrayList;

public class PlotMembers extends Gui {

    private int page;

    private final int plotID;

    private final GlobalSQL globalSQL;
    private final PlotSQL plotSQL;

    public PlotMembers(int plotID) {

        super(45, Component.text("Manage Members", NamedTextColor.AQUA, TextDecoration.BOLD));

        this.plotID = plotID;

        page = 1;

        globalSQL = Network.getInstance().globalSQL;
        plotSQL = Network.getInstance().plotSQL;

        createGui();

    }

    private void createGui() {

        //Get members of the plot.
        ArrayList<String> plot_members = plotSQL.getStringList("SELECT uuid FROM plot_members WHERE id=" + plotID + " AND is_owner=0;");

        //Slot count.
        int slot = 10;

        //Skip count.
        int skip = 21 * (page - 1);

        //If page is greater than 1 add a previous page button.
        if (page > 1) {
            setItem(18, Utils.createItem(Material.ARROW, 1,
                            Utils.title("Previous Page"),
                            Utils.line("Open the previous page of online users.")),
                    u ->

                    {

                        //Update the gui.
                        page--;
                        this.refresh();
                        u.player.getOpenInventory().getTopInventory().setContents(this.getInventory().getContents());

                    });
        }

        //Iterate through all online players.
        if (plot_members != null) {
            for (String uuid : plot_members) {

                //If the slot is greater than the number that fit in a page, create a new page.
                if (slot > 34) {

                    setItem(26, Utils.createItem(Material.ARROW, 1,
                                    Utils.title("Next Page"),
                                    Utils.line("Open the next page of online users.")),
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

                //If skip is greater than 0, skip this iteration.
                if (skip > 0) {
                    skip--;
                    continue;
                }

                //Add player to gui.
                setItem(slot, Utils.createPlayerSkull(uuid, 1,
                                Utils.title("Kick " + globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + uuid + "';") + " from your plot."),
                                Utils.line("Click to remove them as member of your plot."),
                                Utils.line("They will no longer be able to build in it.")),
                        u ->

                        {
                            //Check if the player is not a member of the plot.
                            if (plotSQL.hasRow("SELECT id FROM plot_members WHERE id=" + plotID + " AND uuid='" + uuid + "';")) {

                                //Send invite via chat.
                                //The invite will be active until they disconnect from the network.
                                //They will need to run a command to actually join the plot.
                                globalSQL.update("INSERT INTO server_events(uuid,type,server,event) VALUES('" + uuid + "','plotsystem','" +
                                        plotSQL.getString("SELECT server FROM location_data WHERE name='" +
                                                plotSQL.getString("SELECT location FROM plot_data WHERE id=" + plotID + ";") + "';") + "','kick plot " + plotID + "')");

                                //Return to the previous menu, since otherwise the gui won't have updated.
                                this.delete();
                                u.mainGui = null;

                                //Switch back to plot info.
                                u.mainGui = new PlotInfo(plotID, u.player.getUniqueId().toString());
                                u.mainGui.open(u);

                            } else {
                                u.player.sendMessage(Utils.error("This player is not a member of your plot."));
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
        setItem(44, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.title("Return"),
                        Utils.line("Open the plot info for this plot.")),
                u -> {

                    //Delete this gui.
                    this.delete();
                    u.mainGui = null;

                    //Switch back to plot info.
                    u.mainGui = new PlotInfo(plotID, u.player.getUniqueId().toString());
                    u.mainGui.open(u);

                });
    }

    public void refresh() {

        this.clearGui();
        createGui();

    }
}
