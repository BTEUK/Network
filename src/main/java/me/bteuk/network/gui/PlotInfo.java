package me.bteuk.network.gui;

import me.bteuk.network.Network;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.sql.PlotSQL;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

public class PlotInfo {

    public static UniqueGui createPlotInfo(NetworkUser user, int plotID) {

        UniqueGui gui = new UniqueGui(45, Component.text("Plot Menu", NamedTextColor.AQUA, TextDecoration.BOLD));

        //Get plot sql.
        PlotSQL plotSQL = Network.getInstance().plotSQL;

        //Get global sql.
        GlobalSQL globalSQL = Network.getInstance().globalSQL;

        //If this plot has feedback, create button to go to plot feedback.
        if (plotSQL.hasRow("SELECT id FROM deny_data WHERE id=" + plotID + ";")) {

            gui.setItem(12, Utils.createItem(Material.SPRUCE_DOOR, 1,
                            Utils.chat("&b&lPlot Members"),
                            Utils.chat("&fManage the members of your plot.")),
                    u -> {

                        //Delete this inventory.
                        u.uniqueGui.delete();
                        u.player.closeInventory();

                        //Open the plot feedback menu.
                        u.uniqueGui = PlotFeedback.createPlotFeedback(u, plotID);
                        u.uniqueGui.open(u);

                    });
        }

        //If you the owner of this plot.
        if (plotSQL.hasRow("SELECT id FROM plot_members WHERE id=" + plotID + " AND is_owner=1;")) {

            //If plot is not submitted show submit button.
            if (plotSQL.hasRow("SELECT id FROM plot_data WHERE id=" + plotID + " AND status='claimed';")) {

                gui.setItem(12, Utils.createItem(Material.SPRUCE_DOOR, 1,
                                Utils.chat("&b&lPlot Members"),
                                Utils.chat("&fManage the members of your plot.")),
                        u -> {

                            //Delete this inventory.
                            u.uniqueGui.delete();
                            u.player.closeInventory();

                            //Add server event to delete plot.
                            globalSQL.insert("INSERT INTO server_events(uuid,server,event) VALUES(" + u.player.getUniqueId() + "," +
                                    Network.SERVER_NAME + ",'submit plot" + plotID + "');");

                        });

            }

            //If plot is submitted show retract submission button.
            if (plotSQL.hasRow("SELECT id FROM plot_data WHERE id=" + plotID + " AND status='submitted';")) {

                gui.setItem(12, Utils.createItem(Material.SPRUCE_DOOR, 1,
                                Utils.chat("&b&lRetract Submission"),
                                Utils.chat("&fYour plot will no longer be submitted.")),
                        u -> {

                            //Delete this inventory.
                            u.uniqueGui.delete();
                            u.player.closeInventory();

                            //Add server event to retract plot submission.
                            globalSQL.insert("INSERT INTO server_events(uuid,server,event) VALUES(" + u.player.getUniqueId() + "," +
                                    Network.SERVER_NAME + ",'retract plot" + plotID + "');");

                        });
            }

            //If plot is not under review allow it to be removed.
            if (plotSQL.hasRow("SELECT id FROM plot_data WHERE id=" + plotID + " AND (status='claimed' OR status='submitted');")) {

                //TODO: Add confirmation gui for confirming the removal of the plot.

                gui.setItem(12, Utils.createItem(Material.SPRUCE_DOOR, 1,
                                Utils.chat("&b&lPlot Members"),
                                Utils.chat("&fManage the members of your plot.")),
                        u -> {

                            //Delete this inventory.
                            u.uniqueGui.delete();
                            u.player.closeInventory();

                            //Add server event to delete plot.
                            globalSQL.insert("INSERT INTO server_events(uuid,server,event) VALUES(" + u.player.getUniqueId() + "," +
                                    plotSQL.getString("SELECT server FROM location_data WHERE name=" +
                                            plotSQL.getString("SELECT location FROM plot_data WHERE id=" + plotID + ";") + ";") +
                                    ",'delete plot" + plotID + "');");

                        });

            }

            //If plot has members, edit plot members.
            gui.setItem(12, Utils.createItem(Material.SPRUCE_DOOR, 1,
                            Utils.chat("&b&lPlot Members"),
                            Utils.chat("&fManage the members of your plot.")),
                    u -> {

                        //Delete this inventory.
                        u.uniqueGui.delete();
                        u.player.closeInventory();

                        //Open the plot members menu.
                        u.uniqueGui = PlotMembers.createPlotMembers(u, plotID);
                        u.uniqueGui.open(u);

                    });

            //Invite new members to your plot.
            gui.setItem(12, Utils.createItem(Material.SPRUCE_DOOR, 1,
                            Utils.chat("&b&lInvite Members"),
                            Utils.chat("&fInvite a new member to your plot.")),
                    u -> {

                        //Delete this inventory.
                        u.uniqueGui.delete();
                        u.player.closeInventory();

                        //Open the invite members menu.
                        u.uniqueGui = InviteMembers.createInviteMembers(u, plotID);
                        u.uniqueGui.open(u);

                    });


        } else {
            //You are a member of this plot.

            //Leave plot.
            gui.setItem(12, Utils.createItem(Material.SPRUCE_DOOR, 1,
                            Utils.chat("&b&lLeave Plot"),
                            Utils.chat("&fYou will not be able to build in the plot once you leave.")),
                    u -> {

                        //Delete this inventory.
                        u.uniqueGui.delete();
                        u.player.closeInventory();

                        //Add server event to leave plot.
                        globalSQL.insert("INSERT INTO server_events(uuid,server,event) VALUES(" + u.player.getUniqueId() + "," +
                                plotSQL.getString("SELECT server FROM location_data WHERE name=" +
                                        plotSQL.getString("SELECT location FROM plot_data WHERE id=" + plotID + ";") + ";") +
                                ",'leave plot" + plotID + "');");

                    });

        }

        //Return
        gui.setItem(12, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.chat("&b&lReturn"),
                        Utils.chat("&fOpen the building menu.")),
                u -> {

                    //Delete this inventory.
                    u.uniqueGui.delete();
                    u.player.closeInventory();

                    //Open the navigator.
                    u.uniqueGui = PlotMenu.createPlotMenu(u);
                    u.uniqueGui.open(u);

                });

        return gui;

    }
}
