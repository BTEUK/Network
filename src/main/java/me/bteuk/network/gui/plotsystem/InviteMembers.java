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

import java.util.ArrayList;

public class InviteMembers extends Gui {

    private int page;

    private final int plotID;

    private final GlobalSQL globalSQL;
    private final PlotSQL plotSQL;

    public InviteMembers(int plotID) {

        super(45, Component.text("Invite Members", NamedTextColor.AQUA, TextDecoration.BOLD));

        this.plotID = plotID;

        page = 1;

        globalSQL = Network.getInstance().globalSQL;
        plotSQL = Network.getInstance().plotSQL;

        createGui();

    }

    private void createGui() {

        //Get all online players in the network.
        ArrayList<String> online_users = globalSQL.getStringList("SELECT uuid FROM online_users;");

        //Slot count.
        int slot = 10;

        //Skip count.
        int skip = 21 * (page - 1);

        //If page is greater than 1 add a previous page button.
        if (page > 1) {
            setItem(18, Utils.createItem(Material.ARROW, 1,
                            Utils.chat("&b&lPrevious Page"),
                            Utils.chat("&fOpen the previous page of online users.")),
                    u ->

                    {

                        //Update the gui.
                        page--;
                        this.refresh();
                        u.player.getOpenInventory().getTopInventory().setContents(this.getInventory().getContents());

                    });
        }

        //Iterate through all online players.
        for (String uuid : online_users) {

            //If the slot is greater than the number that fit in a page, create a new page.
            if (slot > 34) {

                setItem(26, Utils.createItem(Material.ARROW, 1,
                                Utils.chat("&b&lNext Page"),
                                Utils.chat("&fOpen the next page of online users.")),
                        u ->

                        {

                            //Update the gui.
                            page++;
                            this.refresh();
                            u.player.getOpenInventory().getTopInventory().setContents(this.getInventory().getContents());

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
            setItem(slot, Utils.createPlayerSkull(uuid, 1,
                            Utils.chat("&b&lInvite " + globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + uuid + "';") + " to your plot."),
                            Utils.chat("&fThey will receive an invitation in chat.")),
                    u ->

                    {

                        //Check if the player is still online.
                        if (globalSQL.hasRow("SELECT uuid FROM online_users WHERE uuid='" + uuid + "';")) {

                            //Check if the player is not already a member of the plot.
                            if (!plotSQL.hasRow("SELECT id FROM plot_members WHERE id=" + plotID + " AND uuid='" + uuid + "';")) {

                                //Check if the player has not already been invited.
                                if (!plotSQL.hasRow("SELECT id FROM plot_invites WHERE id=" + plotID + " AND uuid='" + uuid + "';")) {

                                    //Send invite via chat.
                                    //The invite will be active until they disconnect from the network.
                                    //They will need to run a command to actually join the plot.
                                    globalSQL.update("INSERT INTO server_events(uuid,type,server,event) VALUES('" + uuid + "','network','" +
                                            globalSQL.getString("SELECT server FROM online_users WHERE uuid='" + uuid + "';") + "','invite plot " + plotID + "')");

                                    u.player.sendMessage(Utils.chat("&aInvited " + globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + uuid + "'") + " to your plot."));

                                } else {
                                    u.player.sendMessage(Utils.chat("&cYou've already invited this player to your plot."));
                                }

                            } else {
                                u.player.sendMessage(Utils.chat("&cThis player is already a member of your plot."));
                            }

                        } else {
                            u.player.sendMessage(Utils.chat("&cThis player is no longer online."));
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

        //Return to plot info menu.
        setItem(44, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.chat("&b&lReturn"),
                        Utils.chat("&fReturn to the menu of plot " + plotID + ".")),
                u ->

                {

                    //Delete this gui.
                    this.delete();
                    u.inviteMembers = null;

                    //Switch back to plot info.
                    u.plotInfo = new PlotInfo(plotID);
                    u.plotInfo.open(u);

                });
    }

    public void refresh() {

        this.clearGui();
        createGui();

    }
}
