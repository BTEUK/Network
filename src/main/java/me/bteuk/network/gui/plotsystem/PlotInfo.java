package me.bteuk.network.gui.plotsystem;

import me.bteuk.network.Network;
import me.bteuk.network.gui.*;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.sql.PlotSQL;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;

public class PlotInfo extends Gui {

    private final int plotID;

    public PlotInfo(int plotID) {

        super(27, Component.text("Plot " + plotID, NamedTextColor.AQUA, TextDecoration.BOLD));

        this.plotID = plotID;

        createGui();

    }

    public void createGui() {

        //Get plot sql.
        PlotSQL plotSQL = Network.getInstance().plotSQL;

        //Get global sql.
        GlobalSQL globalSQL = Network.getInstance().globalSQL;

        //TODO PLOT INFO

        //TODO TELEPORT TO PLOT

        //If this plot has feedback, create button to go to plot feedback.
        if (plotSQL.hasRow("SELECT id FROM deny_data WHERE id=" + plotID + ";")) {

            setItem(24, Utils.createItem(Material.WRITABLE_BOOK, 1,
                            Utils.chat("&b&lPlot Feedback"),
                            Utils.chat("&fClick to show feedback for this plot.")),
                    u -> {

                        //Delete this gui.
                        this.delete();
                        u.plotInfo = null;

                        //Switch back to plot menu.
                        u.deniedPlotFeedback = new DeniedPlotFeedback(plotID);
                        u.deniedPlotFeedback.open(u);

                    });
        }

        //If you the owner of this plot.
        if (plotSQL.hasRow("SELECT id FROM plot_members WHERE id=" + plotID + " AND is_owner=1;")) {

            //If plot is not submitted show submit button.
            if (plotSQL.hasRow("SELECT id FROM plot_data WHERE id=" + plotID + " AND status='claimed';")) {

                setItem(2, Utils.createItem(Material.LIGHT_BLUE_CONCRETE, 1,
                                Utils.chat("&b&lSubmit Plot"),
                                Utils.chat("&fSubmit your plot to be reviewed."),
                                Utils.chat("&fReviewing may take over 24 hours.")),
                        u -> {

                            //Refresh the gui page after a second.
                            //The delay is so the plotsystem has time to submit the plot.
                            Bukkit.getScheduler().scheduleSyncDelayedTask(Network.getInstance(), () -> {

                                //Update the gui.
                                this.refresh();
                                u.player.getOpenInventory().getTopInventory().setContents(this.getInventory().getContents());

                            }, 20L);

                            //Add server event to submit plot.
                            globalSQL.update("INSERT INTO server_events(uuid,type,server,event) VALUES('" + u.player.getUniqueId() + "','plotsystem','"
                                    + plotSQL.getString("SELECT server FROM location_data WHERE name='" +
                                    plotSQL.getString("SELECT location FROM plot_data WHERE id=" + plotID + ";") + "';")
                                    + "','submit plot " + plotID + "');");

                        });

            }

            //If plot is submitted show retract submission button.
            if (plotSQL.hasRow("SELECT id FROM plot_data WHERE id=" + plotID + " AND status='submitted';")) {

                setItem(2, Utils.createItem(Material.ORANGE_CONCRETE, 1,
                                Utils.chat("&b&lRetract Submission"),
                                Utils.chat("&fYour plot will no longer be submitted.")),
                        u -> {

                            //Refresh the gui page after a second.
                            //The delay is so the plotsystem has time to retract the submission.
                            Bukkit.getScheduler().scheduleSyncDelayedTask(Network.getInstance(), () -> {

                                //Update the gui.
                                this.refresh();
                                u.player.getOpenInventory().getTopInventory().setContents(this.getInventory().getContents());

                            }, 20L);

                            //Add server event to retract plot submission.
                            globalSQL.update("INSERT INTO server_events(uuid,type,server,event) VALUES('" + u.player.getUniqueId() + "','plotsystem','"
                                    + plotSQL.getString("SELECT server FROM location_data WHERE name='" +
                                    plotSQL.getString("SELECT location FROM plot_data WHERE id=" + plotID + ";") + "';")
                                    + "','retract plot " + plotID + "');");

                        });
            }

            //If plot is not under review allow it to be removed.
            if (plotSQL.hasRow("SELECT id FROM plot_data WHERE id=" + plotID + " AND (status='claimed' OR status='submitted');")) {

                setItem(6, Utils.createItem(Material.RED_CONCRETE, 1,
                                Utils.chat("&b&lDelete Plot"),
                                Utils.chat("&fDelete the plot and all its contents.")),
                        u -> {

                            //Delete this gui.
                            this.delete();
                            u.plotInfo = null;

                            //Switch back to plot menu.
                            u.deleteConfirm = new DeleteConfirm(plotID);
                            u.deleteConfirm.open(u);

                        });

            }

            //If plot has members, edit plot members.
            setItem(21, Utils.createItem(Material.PLAYER_HEAD, 1,
                            Utils.chat("&b&lPlot Members"),
                            Utils.chat("&fManage the members of your plot.")),
                    u -> {

                        //Delete this gui.
                        this.delete();
                        u.plotInfo = null;

                        //Switch back to plot menu.
                        u.plotMembers = new PlotMembers(plotID);
                        u.plotMembers.open(u);

                    });

            //Invite new members to your plot.
            setItem(20, Utils.createItem(Material.OAK_BOAT, 1,
                            Utils.chat("&b&lInvite Members"),
                            Utils.chat("&fInvite a new member to your plot."),
                            Utils.chat("&fYou can only invite online users.")),
                    u -> {

                        //Delete this gui.
                        this.delete();
                        u.plotInfo = null;

                        //Switch back to plot menu.
                        u.inviteMembers = new InviteMembers(plotID);
                        u.inviteMembers.open(u);

                    });

        } else {
            //You are a member of this plot.

            //Leave plot.
            setItem(20, Utils.createItem(Material.RED_CONCRETE, 1,
                            Utils.chat("&b&lLeave Plot"),
                            Utils.chat("&fYou will not be able to build in the plot once you leave.")),
                    u -> {

                        //Delete this gui.
                        this.delete();
                        u.plotInfo = null;

                        //Switch back to plot menu.
                        u.plotMenu = new PlotMenu(u);
                        u.plotMenu.open(u);


                        //Add server event to leave plot.
                        globalSQL.update("INSERT INTO server_events(uuid,type,server,event) VALUES('" + u.player.getUniqueId() + "','plotsystem','" +
                                plotSQL.getString("SELECT server FROM location_data WHERE name='" +
                                        plotSQL.getString("SELECT location FROM plot_data WHERE id=" + plotID + ";") + "';") +
                                "','leave plot " + plotID + "');");

                    });

        }

        //Return
        setItem(26, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.chat("&b&lReturn"),
                        Utils.chat("&fOpen the plot menu.")),
                u -> {

                    //Delete this gui.
                    this.delete();
                    u.plotInfo = null;

                    //Switch back to plot menu.
                    u.plotMenu = new PlotMenu(u);
                    u.plotMenu.open(u);

                });
    }

    public void refresh() {

        this.clearGui();
        createGui();

    }
}
