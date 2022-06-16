package me.bteuk.network.gui.plotsystem;

import me.bteuk.network.Network;
import me.bteuk.network.gui.*;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.sql.PlotSQL;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

public class PlotInfo {

    public static UniqueGui createPlotInfo(int plotID) {

        UniqueGui gui = new UniqueGui(45, Component.text("Plot Menu", NamedTextColor.AQUA, TextDecoration.BOLD));

        //Get plot sql.
        PlotSQL plotSQL = Network.getInstance().plotSQL;

        //Get global sql.
        GlobalSQL globalSQL = Network.getInstance().globalSQL;

        //If this plot has feedback, create button to go to plot feedback.
        if (plotSQL.hasRow("SELECT id FROM deny_data WHERE id=" + plotID + ";")) {

            gui.setItem(12, Utils.createItem(Material.BOOK, 1,
                            Utils.chat("&b&lPlot Feedback"),
                            Utils.chat("&fClick to show feedback for this plot.")),
                    u -> {

                        //Delete this inventory.
                        u.uniqueGui.delete();
                        u.player.closeInventory();

                        //Open the plot feedback menu.
                        u.uniqueGui = DeniedPlotFeedback.createDeniedPlotFeedback(plotID);
                        u.uniqueGui.open(u);

                    });
        }

        //If you the owner of this plot.
        if (plotSQL.hasRow("SELECT id FROM plot_members WHERE id=" + plotID + " AND is_owner=1;")) {

            //If plot is not submitted show submit button.
            if (plotSQL.hasRow("SELECT id FROM plot_data WHERE id=" + plotID + " AND status='claimed';")) {

                gui.setItem(12, Utils.createItem(Material.LIGHT_BLUE_CONCRETE, 1,
                                Utils.chat("&b&lPlot Members"),
                                Utils.chat("&fManage the members of your plot.")),
                        u -> {

                            //Delete this inventory.
                            u.uniqueGui.delete();
                            u.player.closeInventory();

                            //Add server event to delete plot.
                            globalSQL.update("INSERT INTO server_events(uuid,type,server,event) VALUES('" + u.player.getUniqueId() + "','plotsystem',"
                                    + plotSQL.getString("SELECT server FROM location_data WHERE name='" +
                                    plotSQL.getString("SELECT location FROM plot_data WHERE id=" + plotID + ";") + "';")
                                    + ",'submit plot" + plotID + "');");

                        });

            }

            //If plot is submitted show retract submission button.
            if (plotSQL.hasRow("SELECT id FROM plot_data WHERE id=" + plotID + " AND status='submitted';")) {

                gui.setItem(12, Utils.createItem(Material.ORANGE_CONCRETE, 1,
                                Utils.chat("&b&lRetract Submission"),
                                Utils.chat("&fYour plot will no longer be submitted.")),
                        u -> {

                            //Delete this inventory.
                            u.uniqueGui.delete();
                            u.player.closeInventory();

                            //Add server event to retract plot submission.
                            globalSQL.update("INSERT INTO server_events(uuid,type,server,event) VALUES('" + u.player.getUniqueId() + "','plotsystem','"
                                    + plotSQL.getString("SELECT server FROM location_data WHERE name='" +
                                    plotSQL.getString("SELECT location FROM plot_data WHERE id=" + plotID + ";") + "';")
                                    + "','retract plot" + plotID + "');");

                        });
            }

            //If plot is not under review allow it to be removed.
            if (plotSQL.hasRow("SELECT id FROM plot_data WHERE id=" + plotID + " AND (status='claimed' OR status='submitted');")) {

                gui.setItem(12, Utils.createItem(Material.SPRUCE_DOOR, 1,
                                Utils.chat("&b&lDelete Plot"),
                                Utils.chat("&fDelete the plot and all its contents.")),
                        u -> {

                            //Copy reference of gui.
                            UniqueGui previousGui = u.uniqueGui;

                            //Delete this inventory.
                            u.uniqueGui.delete();
                            u.player.closeInventory();

                            //Open the delete confirm menu.
                            u.uniqueGui = DeleteConfirm.createDeleteConfirm(plotID, previousGui);
                            u.uniqueGui.open(u);

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
                        u.uniqueGui = PlotMembers.createPlotMembers(plotID,1);
                        u.uniqueGui.open(u);

                    });

            //Invite new members to your plot.
            gui.setItem(12, Utils.createItem(Material.SPRUCE_DOOR, 1,
                            Utils.chat("&b&lInvite Members"),
                            Utils.chat("&fInvite a new member to your plot."),
                            Utils.chat("&fYou can only invite online users.")),
                    u -> {

                        //Delete this inventory.
                        u.uniqueGui.delete();
                        u.player.closeInventory();

                        //Open the invite members menu.
                        u.uniqueGui = InviteMembers.createInviteMembers(plotID, 1);
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
                        globalSQL.update("INSERT INTO server_events(uuid,type,server,event) VALUES('" + u.player.getUniqueId() + "','plotsystem','" +
                                plotSQL.getString("SELECT server FROM location_data WHERE name='" +
                                        plotSQL.getString("SELECT location FROM plot_data WHERE id=" + plotID + ";") + "';") +
                                "','leave plot" + plotID + "');");

                    });

        }

        //Return
        gui.setItem(12, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.chat("&b&lReturn"),
                        Utils.chat("&fOpen the plot menu.")),
                u -> {

                    //Delete this inventory.
                    u.uniqueGui.delete();
                    u.player.closeInventory();

                    //Open the plot menu.
                    u.uniqueGui = PlotMenu.createPlotMenu(u);
                    u.uniqueGui.open(u);

                });

        return gui;

    }
}
