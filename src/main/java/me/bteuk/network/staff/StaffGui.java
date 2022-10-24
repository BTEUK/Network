package me.bteuk.network.staff;

import me.bteuk.network.Network;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.gui.regions.RegionRequests;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.SwitchServer;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

import java.util.ArrayList;

public class StaffGui extends Gui {

    private final NetworkUser user;

    public StaffGui(NetworkUser user) {

        super(27, Component.text("Staff Menu", NamedTextColor.AQUA, TextDecoration.BOLD));

        this.user = user;

        createGui();

    }

    private void createGui() {

        /*TODO:

        Staff menu for regions.
        Staff menu for region join requests.

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
        //If player is in a region show manage region, else show no region.
        if (user.inRegion) {

            setItem(10, Utils.createItem(Material.ANVIL, 1,
                            Utils.chat("&b&lManage Region " + user.region.regionName()),
                            Utils.chat("&fOpens a menu to manage details of the region you are currently in.")),
                    u ->

                    {

                        //Check if user has the relevant permissions.
                        if (u.player.hasPermission("uknet.regions.manage")) {

                            if (u.inRegion) {

                                //Open manage region menu

                            }

                        }

                        //Check if the user is in a region.

                        //Manage Region Menu.

                    });
        } else {

            setItem(10, Utils.createItem(Material.ANVIL, 1,
                    Utils.chat("&b&lNo Region"),
                    Utils.chat("&fYou are currently not in a region.")));

        }

        //Click to open menu to deal with region join requests.
        //Can only click on this if requests exist and player is a reviewer.
        setItem(11, Utils.createItem(Material.CHEST_MINECART, 1,
                        Utils.chat("&b&lReview Region Requests"),
                        Utils.chat("&fOpens a menu to review active region join requests by Jr.Builders."),
                        Utils.chat("&fThere are currently &7" +
                                Network.getInstance().regionSQL.getInt("SELECT COUNT(*) FROM region_requests WHERE staff_accept=0;") + " &fregion requests.")),
                u -> {

                    if (Network.getInstance().regionSQL.hasRow("SELECT region FROM region_requests WHERE staff_accept=0;")) {
                        if (u.player.hasPermission("uknet.regions.request")) {

                            //Open region request menu.
                            this.delete();
                            u.staffUser.staffGui = null;

                            u.staffUser.regionRequests = new RegionRequests(true);
                            u.staffUser.regionRequests.open(u);

                        } else {
                            u.player.sendMessage(Utils.chat("&cYou must be a reviewer to review region requests."));
                        }
                    } else {
                        u.player.sendMessage(Utils.chat("&cThere are currently no region requests."));
                    }

                });

        //Click to review plot.
        //Show review plot button in gui.
        setItem(13, Utils.createItem(Material.WRITABLE_BOOK, 1,
                        Utils.chat("&b&lReview Plot"),
                        Utils.chat("&fClick to review a submitted plot."),
                        Utils.chat("&fThere are currently &7" +
                                Network.getInstance().plotSQL.getInt("SELECT COUNT(id) FROM plot_data WHERE status='submitted';") +
                                " &fsubmitted plots.")),
                u -> {

                    //Check if there is a plot available to review,
                    //that you are not already the owner or member of.
                    if (Network.getInstance().plotSQL.hasRow("SELECT id FROM plot_data WHERE status='submitted';")) {

                        //Get arraylist of submitted plots.
                        ArrayList<Integer> nPlots = Network.getInstance().plotSQL.getIntList("SELECT id FROM plot_data WHERE status='submitted';");

                        int counter = 0;

                        //Iterate through all plots.
                        for (int nPlot : nPlots) {

                            //Counter
                            counter++;

                            //If you are not owner or member of the plot select it for the next review.
                            if (!Network.getInstance().plotSQL.hasRow("SELECT id FROM plot_members WHERE uuid='" + u.player.getUniqueId() + "' AND id=" + nPlot + ";")) {

                                //Check if the player has permission to review a plot.
                                if (u.player.hasPermission("uknet.plots.review")) {

                                    //Get server of plot.
                                    String server = Network.getInstance().plotSQL.getString("SELECT server FROM location_data WHERE name='" +
                                            Network.getInstance().plotSQL.getString("SELECT location FROM plot_data WHERE id=" + nPlot + ";") + "';");

                                    //If they are not in the same server as the plot teleport them to that server and start the reviewing process.
                                    if (server.equals(Network.SERVER_NAME)) {

                                        u.player.closeInventory();
                                        Network.getInstance().globalSQL.update("INSERT INTO server_events(uuid,type,server,event) VALUES('"
                                                + u.player.getUniqueId() + "','plotsystem','" + Network.SERVER_NAME + "','review plot " + nPlot + "');");

                                    } else {

                                        //Player is not on the current server.
                                        //Set the server join event.
                                        Network.getInstance().globalSQL.update("INSERT INTO join_events(uuid,type,event) VALUES('"
                                                + u.player.getUniqueId() + "','plotsystem',"
                                                + "'review plot " + nPlot + "');");

                                        //Teleport them to another server.
                                        u.player.closeInventory();
                                        SwitchServer.switchServer(u.player, server);

                                    }
                                } else {
                                    u.player.sendMessage(Utils.chat("&cYou must be a reviewer to review plots."));
                                }

                                //Stop iterating.
                                break;
                            }

                            //If counter is equal to the size of the array then all plots have been cycled through without result.
                            if (counter == nPlots.size()) {
                                u.player.sendMessage(Utils.chat("&cYou are the owner or a member of all remaining submitted plots."));
                            }
                        }
                    } else {
                        u.player.sendMessage(Utils.chat("&cThere are currently no submitted plots."));
                    }
                });

        //Click to open menu of navigation menu requests.
        if (true/*request exists*/) {
            setItem(15, Utils.createItem(Material.ENDER_EYE, 1,
                            Utils.chat("&b&lReview Navigation Menu Requests"),
                            Utils.chat("&fOpens a menu to review navigation menu requests.")),
                    u -> {

                    });
        }

        //Click to open moderation menu.
        setItem(16, Utils.createItem(Material.REDSTONE_BLOCK, 1,
                        Utils.chat("&b&lModeration Menu"),
                        Utils.chat("&fOpens the moderation menu to deal with wrongdoers.")),
                u ->

                {

                });
    }

    public void refresh() {

        this.clearGui();
        createGui();

    }
}