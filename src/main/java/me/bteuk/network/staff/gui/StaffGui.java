package me.bteuk.network.staff.gui;

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

import static me.bteuk.network.utils.Constants.SERVER_NAME;

public class StaffGui extends Gui {

    private final NetworkUser user;

    public StaffGui(NetworkUser user) {

        super(27, Component.text("Staff Menu", NamedTextColor.AQUA, TextDecoration.BOLD));

        this.user = user;

        createGui();

    }

    private void createGui() {

        //Check if any location requests exist.
        //To make sure the string makes grammatical sense we check if the number is 1, in this case we change 'are' to 'is'.
        int lRequestCount = Network.getInstance().globalSQL.getInt("SELECT COUNT(location) FROM location_requests");
        Component lRequestString;
        if (lRequestCount == 1) {
            lRequestString = Utils.line("There is currently ")
                    .append(Component.text(1, NamedTextColor.GRAY))
                    .append(Utils.line(" location request."));
        } else {
            lRequestString = Utils.line("There are currently ")
                    .append(Component.text(lRequestCount, NamedTextColor.GRAY))
                    .append(Utils.line(" location requests."));
        }

        //Create item.
        setItem(14, Utils.createItem(Material.ENDER_CHEST, 1,
                        Utils.title("Location Requests"),
                        Utils.line("Opens a menu to view all location requests for navigation."),
                        lRequestString),
                u -> {

                    //Check if the user has the relevant permissions.
                    if (Network.getInstance().globalSQL.getInt("SELECT COUNT(location) FROM location_requests") > 0) {
                        if (u.player.hasPermission("uknet.navigation.review")) {

                            //Open the LocationRequest gui.
                            this.delete();
                            u.staffGui = null;

                            u.staffGui = new LocationRequests();
                            u.staffGui.open(u);

                        } else {
                            u.player.sendMessage(Utils.error("You must be a reviewer to review location requests."));
                        }
                    } else {
                        u.player.sendMessage(Utils.error("There are currently no location requests."));
                    }
                });

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
                            Utils.title("Manage Region " + user.region.regionName()),
                            Utils.line("Opens a menu to manage details of the region you are currently in.")),
                    u ->

                    {

                        //Check if the user has the relevant permissions.
                        if (u.player.hasPermission("uknet.regions.manage")) {

                            if (u.inRegion) {
                                //Open manage region menu
                                this.delete();
                                u.staffGui = new ManageRegion(u, u.region);
                                u.staffGui.open(u);

                            }

                        }

                        //Check if the user is in a region.

                        //Manage Region Menu.

                    });
        } else {

            setItem(10, Utils.createItem(Material.STRUCTURE_VOID, 1,
                    Utils.title("No Region"),
                    Utils.line("You are currently not standing in a valid region."),
                    Utils.line("This is likely due to being in a lobby.")));

        }

        //Click to open menu to deal with region join requests.
        //Can only click on this if requests exist and player is a reviewer.
        //Check if any location requests exist.
        //To make sure the string makes grammatical sense we check if the number is 1, in this case we change 'are' to 'is'.
        int rRequestCount = Network.getInstance().regionSQL.getInt("SELECT COUNT(region) FROM region_requests WHERE staff_accept=0");
        Component rRequestString;
        if (rRequestCount == 1) {
            rRequestString = Utils.line("There is currently ")
                    .append(Component.text(1, NamedTextColor.GRAY))
                    .append(Utils.line(" region join request by Jr.Builders."));
        } else {
            rRequestString = Utils.line("There are currently ")
                    .append(Component.text(lRequestCount, NamedTextColor.GRAY))
                    .append(Utils.line(" region join requests by Jr.Builders."));
        }

        setItem(11, Utils.createItem(Material.CHEST_MINECART, 1,
                        Utils.title("Review Region Requests"),
                        Utils.line("Opens a menu to review active region join requests by Jr.Builders."),
                        rRequestString),
                u -> {

                    if (Network.getInstance().regionSQL.hasRow("SELECT region FROM region_requests WHERE staff_accept=0;")) {
                        if (u.player.hasPermission("uknet.regions.request")) {

                            //Open region request menu.
                            this.delete();
                            u.staffGui = null;

                            u.staffGui = new RegionRequests(true);
                            u.staffGui.open(u);

                        } else {
                            u.player.sendMessage(Utils.error("You must be a reviewer to review region requests."));
                        }
                    } else {
                        u.player.sendMessage(Utils.error("There are currently no region requests."));
                    }

                });

        //Click to review plot.
        //Show review plot button in gui.
        int plot_count = Network.getInstance().plotSQL.getInt("SELECT count(id) FROM plot_data WHERE status='submitted';");
        Component message;

        if (plot_count == 1) {
            message = Utils.line("There is currently ")
                    .append(Component.text("1", NamedTextColor.GRAY))
                    .append(Utils.line(" submitted plot."));
        } else {
            message = Utils.line("There are currently ")
                    .append(Component.text(plot_count, NamedTextColor.GRAY))
                    .append(Utils.line(" submitted plots."));
        }

        setItem(13, Utils.createItem(Material.WRITABLE_BOOK, 1,
                        Utils.title("Review Plot"),
                        Utils.line("Click to review a submitted plot."),
                        message),
                u -> {

                    //Check if there is a plot available to review,
                    //that you are not already the owner or member of.
                    if (Network.getInstance().plotSQL.hasRow("SELECT id FROM plot_data WHERE status='submitted';")) {

                        //Get arraylist of submitted plots.
                        //Order them by submit time, so the oldest submissions are reviewed first.
                        ArrayList<Integer> nPlots = Network.getInstance().plotSQL.getIntList("SELECT pd.id FROM plot_data AS pd INNER JOIN plot_submissions AS ps ON pd.id=ps.id WHERE pd.status='submitted' ORDER BY ps.submit_time ASC;");

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
                                    if (server.equals(SERVER_NAME)) {

                                        u.player.closeInventory();
                                        Network.getInstance().globalSQL.update("INSERT INTO server_events(uuid,type,server,event) VALUES('"
                                                + u.player.getUniqueId() + "','plotsystem','" + SERVER_NAME + "','review plot " + nPlot + "');");

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
                                    u.player.sendMessage(Utils.error("You must be a reviewer to review plots."));
                                }

                                //Stop iterating.
                                break;
                            }

                            //If counter is equal to the size of the array then all plots have been cycled through without result.
                            if (counter == nPlots.size()) {
                                u.player.sendMessage(Utils.error("You are the owner or a member of all remaining submitted plots."));
                            }
                        }
                    } else {
                        u.player.sendMessage(Utils.error("There are currently no submitted plots."));
                    }
                });

        //Click to open moderation menu.
        setItem(16, Utils.createItem(Material.REDSTONE_BLOCK, 1,
                        Utils.title("Moderation Menu"),
                        Utils.line("Opens the moderation menu to deal with wrongdoers.")),
                u ->

                {

                });
    }

    public void refresh() {

        this.clearGui();
        createGui();

    }
}