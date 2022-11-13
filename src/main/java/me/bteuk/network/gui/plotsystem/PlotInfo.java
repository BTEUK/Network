package me.bteuk.network.gui.plotsystem;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.bteuk.network.Network;
import me.bteuk.network.events.EventManager;
import me.bteuk.network.gui.*;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.sql.PlotSQL;
import me.bteuk.network.utils.PlotValues;
import me.bteuk.network.utils.SwitchServer;
import me.bteuk.network.utils.Utils;
import net.buildtheearth.terraminusminus.generator.EarthGeneratorSettings;
import net.buildtheearth.terraminusminus.projection.OutOfProjectionBoundsException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;

public class PlotInfo extends Gui {

    private final int plotID;
    private final String uuid;

    public PlotInfo(int plotID, String uuid) {

        super(27, Component.text("Plot " + plotID, NamedTextColor.AQUA, TextDecoration.BOLD));

        this.plotID = plotID;
        this.uuid = uuid;

        createGui();

    }

    public void createGui() {

        //Get plot sql.
        PlotSQL plotSQL = Network.getInstance().plotSQL;

        //Get global sql.
        GlobalSQL globalSQL = Network.getInstance().globalSQL;

        setItem(4, Utils.createItem(Material.BOOK, 1,
                Utils.chat("&b&lPlot &7" + plotID),
                Utils.chat("&fPlot Owner: &7" + globalSQL.getString("SELECT name FROM player_data WHERE uuid='" +
                        plotSQL.getString("SELECT uuid FROM plot_members WHERE id=" + plotID + " AND is_owner=1;") + "';")),
                Utils.chat("&fPlot Members: &7" + plotSQL.getInt("SELECT COUNT(uuid) FROM plot_members WHERE id=" + plotID + " AND is_owner=0;")),
                Utils.chat("&fDifficulty: &7" + PlotValues.difficultyName(plotSQL.getInt("SELECT difficulty FROM plot_data WHERE id=" + plotID + ";"))),
                Utils.chat("&fSize: &7" + PlotValues.sizeName(plotSQL.getInt("SELECT size FROM plot_data WHERE id=" + plotID + ";")))));

        setItem(24, Utils.createItem(Material.ENDER_PEARL, 1,
                        Utils.chat("&b&lTeleport to Plot"),
                        Utils.chat("&fClick to teleport to this plot.")),

                u -> {

                    //Get the server of the plot.
                    String server = Network.getInstance().plotSQL.getString("SELECT server FROM location_data WHERE name='"
                            + Network.getInstance().plotSQL.getString("SELECT location FROM plot_data WHERE id=" + plotID + ";")
                            + "';");

                    //If the plot is on the current server teleport them directly.
                    //Else teleport them to the correct server and them teleport them to the plot.
                    if (server.equals(Network.SERVER_NAME)) {

                        u.player.closeInventory();
                        Network.getInstance().globalSQL.update("INSERT INTO server_events(uuid,type,server,event) VALUES('"
                                + u.player.getUniqueId()
                                + "','plotsystem','" + Network.SERVER_NAME
                                + "','teleport plot " + plotID + "');");

                    } else {

                        //Set the server join event.
                        EventManager.createJoinEvent(u.player.getUniqueId().toString(), "plotsystem", "teleport plot " + plotID + " " + Network.SERVER_NAME);

                        //Teleport them to another server.
                        u.player.closeInventory();
                        SwitchServer.switchServer(u.player, server);

                    }

                });

        setItem(23, Utils.createItem(Material.ENDER_EYE, 1,
                        Utils.chat("&b&lView plot in Google Maps"),
                        Utils.chat("&fClick to be linked to the plot in Google Maps.")),

                u -> {

                    //Get corners of the plot.
                    int[][] corners = plotSQL.getPlotCorners(plotID);

                    int sumX = 0;
                    int sumZ = 0;

                    //Find the centre.
                    for (int[] corner : corners) {

                        sumX += corner[0];
                        sumZ += corner[1];

                    }

                    double x = sumX / (double) corners.length;
                    double z = sumZ / (double) corners.length;

                    //Subtract the coordinate transform to make the coordinates in the real location.
                    x -= plotSQL.getInt("SELECT xTransform FROM location_data WHERE name='" +
                            plotSQL.getString("SELECT location FROM plot_data WHERE id=" + plotID + ";") + "';");
                    z -= plotSQL.getInt("SELECT zTransform FROM location_data WHERE name='" +
                            plotSQL.getString("SELECT location FROM plot_data WHERE id=" + plotID + ";") + "';");

                    //Convert to irl coordinates.
                    try {

                        final EarthGeneratorSettings bteGeneratorSettings = EarthGeneratorSettings.parse(EarthGeneratorSettings.BTE_DEFAULT_SETTINGS);
                        double[] coords = bteGeneratorSettings.projection().toGeo(x, z);

                        //Generate link to google maps.
                        u.player.closeInventory();

                        TextComponent message = new TextComponent("Click here to open the plot in Google Maps");
                        message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.google.com/maps/@?api=1&map_action=map&basemap=satellite&zoom=21&center=" + coords[1] + "," + coords[0]));

                        u.player.sendMessage(message);

                    } catch (OutOfProjectionBoundsException e) {
                        e.printStackTrace();
                    }

                });

        //If this plot has feedback, create button to go to plot feedback.
        if (plotSQL.hasRow("SELECT id FROM deny_data WHERE id=" + plotID + ";")) {

            setItem(22, Utils.createItem(Material.WRITABLE_BOOK, 1,
                            Utils.chat("&b&lPlot Feedback"),
                            Utils.chat("&fClick to show feedback for this plot.")),
                    u -> {

                        //Delete this gui.
                        this.delete();
                        u.mainGui = null;

                        //Switch back to plot menu.
                        u.mainGui = new DeniedPlotFeedback(plotID);
                        u.mainGui.open(u);

                    });
        }

        //If you the owner of this plot.
        if (plotSQL.hasRow("SELECT id FROM plot_members WHERE id=" + plotID + " AND uuid='" + uuid + "' AND is_owner=1;")) {

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
                            u.mainGui = null;

                            //Switch back to plot menu.
                            u.mainGui = new DeleteConfirm(plotID);
                            u.mainGui.open(u);

                        });

            }

            //If plot has members, edit plot members.
            setItem(21, Utils.createItem(Material.PLAYER_HEAD, 1,
                            Utils.chat("&b&lPlot Members"),
                            Utils.chat("&fManage the members of your plot.")),
                    u -> {

                        //Delete this gui.
                        this.delete();
                        u.mainGui = null;

                        //Switch back to plot menu.
                        u.mainGui = new PlotMembers(plotID);
                        u.mainGui.open(u);

                    });

            //Invite new members to your plot.
            setItem(20, Utils.createItem(Material.OAK_BOAT, 1,
                            Utils.chat("&b&lInvite Members"),
                            Utils.chat("&fInvite a new member to your plot."),
                            Utils.chat("&fYou can only invite online users.")),
                    u -> {

                        //Delete this gui.
                        this.delete();
                        u.mainGui = null;

                        //Switch back to plot menu.
                        u.mainGui = new InvitePlotMembers(plotID);
                        u.mainGui.open(u);

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
                        u.mainGui = null;

                        //Switch back to plot menu.
                        Bukkit.getScheduler().scheduleSyncDelayedTask(Network.getInstance(), () -> {
                            u.mainGui = new PlotMenu(u);
                            u.mainGui.open(u);
                        }, 20L);


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
                    u.mainGui = null;

                    //Switch back to plot menu.
                    u.mainGui = new PlotMenu(u);
                    u.mainGui.open(u);

                });
    }

    public void refresh() {

        this.clearGui();
        createGui();

    }
}
