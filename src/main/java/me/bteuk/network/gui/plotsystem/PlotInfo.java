package me.bteuk.network.gui.plotsystem;

import me.bteuk.network.Network;
import me.bteuk.network.events.EventManager;
import me.bteuk.network.gui.*;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.sql.PlotSQL;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.PlotValues;
import me.bteuk.network.utils.SwitchServer;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.enums.RegionType;
import net.buildtheearth.terraminusminus.generator.EarthGeneratorSettings;
import net.buildtheearth.terraminusminus.projection.OutOfProjectionBoundsException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import static me.bteuk.network.utils.Constants.SERVER_NAME;

public class PlotInfo extends Gui {

    private final int plotID;
    private final String uuid;

    private final NetworkUser user;

    public PlotInfo(NetworkUser user, int plotID, String uuid) {

        super(27, Component.text("Plot " + plotID, NamedTextColor.AQUA, TextDecoration.BOLD));

        this.user = user;
        this.plotID = plotID;
        this.uuid = uuid;

        createGui();

    }

    public void createGui() {

        //Get plot sql.
        PlotSQL plotSQL = Network.getInstance().getPlotSQL();

        //Get global sql.
        GlobalSQL globalSQL = Network.getInstance().globalSQL;

        setItem(4, Utils.createItem(Material.BOOK, 1,
                Utils.title("Plot " + plotID),
                Utils.line("Plot Owner: ")
                        .append(Component.text(globalSQL.getString("SELECT name FROM player_data WHERE uuid='" +
                                plotSQL.getString("SELECT uuid FROM plot_members WHERE id=" + plotID + " AND is_owner=1;") + "';"), NamedTextColor.GRAY)),
                Utils.line("Plot Members: ")
                        .append(Component.text(plotSQL.getInt("SELECT COUNT(uuid) FROM plot_members WHERE id=" + plotID + " AND is_owner=0;"), NamedTextColor.GRAY)),
                Utils.line("Difficulty: ")
                        .append(Component.text(PlotValues.difficultyName(plotSQL.getInt("SELECT difficulty FROM plot_data WHERE id=" + plotID + ";")), NamedTextColor.GRAY)),
                Utils.line("Size: ")
                        .append(Component.text(PlotValues.sizeName(plotSQL.getInt("SELECT size FROM plot_data WHERE id=" + plotID + ";")), NamedTextColor.GRAY))));

        setItem(24, Utils.createItem(Material.ENDER_PEARL, 1,
                        Utils.title("Teleport to Plot"),
                        Utils.line("Click to teleport to this plot.")),

                u -> {

                    u.player.closeInventory();

                    //Get the server of the plot.
                    String server = plotSQL.getString("SELECT server FROM location_data WHERE name='"
                            + plotSQL.getString("SELECT location FROM plot_data WHERE id=" + plotID + ";")
                            + "';");

                    //If the plot is on the current server teleport them directly.
                    //Else teleport them to the correct server and them teleport them to the plot.
                    if (server.equals(SERVER_NAME)) {

                        EventManager.createTeleportEvent(false, u.player.getUniqueId().toString(), "plotsystem", "teleport plot " + plotID, u.player.getLocation());

                    } else {

                        //Set the server join event.
                        EventManager.createTeleportEvent(true, u.player.getUniqueId().toString(), "plotsystem", "teleport plot " + plotID, u.player.getLocation());

                        //Teleport them to another server.
                        SwitchServer.switchServer(u.player, server);

                    }

                });

        setItem(23, Utils.createItem(Material.ENDER_EYE, 1,
                        Utils.title("View plot in Google Maps"),
                        Utils.line("Click to be linked to the plot in Google Maps.")),

                u -> {

                    u.player.closeInventory();

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
                        Component message = Component.text("Click here to open the plot in Google Maps", NamedTextColor.GREEN);
                        message = message.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, "https://www.google.com/maps/@?api=1&map_action=map&basemap=satellite&zoom=21&center=" + coords[1] + "," + coords[0]));

                        u.player.sendMessage(message);

                    } catch (OutOfProjectionBoundsException e) {
                        e.printStackTrace();
                    }

                });

        //If this plot has feedback, create button to go to plot feedback.
        if (plotSQL.hasRow("SELECT id FROM deny_data WHERE id=" + plotID + ";")) {

            setItem(22, Utils.createItem(Material.WRITABLE_BOOK, 1,
                            Utils.title("Plot Feedback"),
                            Utils.line("Click to show feedback for this plot.")),
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
                                Utils.title("Submit Plot"),
                                Utils.line("Submit your plot to be reviewed."),
                                Utils.line("Reviewing may take over 24 hours.")),
                        u -> {

                            u.player.closeInventory();

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
                                Utils.title("Retract Submission"),
                                Utils.line("Your plot will no longer be submitted.")),
                        u -> {

                            u.player.closeInventory();

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
                                Utils.title("Delete Plot"),
                                Utils.line("Delete the plot and all its contents.")),
                        u -> {

                            //Delete this gui.
                            this.delete();
                            u.mainGui = null;

                            //Switch back to plot menu.
                            u.mainGui = new DeleteConfirm(plotID, RegionType.PLOT);
                            u.mainGui.open(u);

                        });

            }

            //If plot has members, edit plot members.
            setItem(21, Utils.createItem(Material.PLAYER_HEAD, 1,
                            Utils.title("Plot Members"),
                            Utils.line("Manage the members of your plot.")),
                    u -> {

                        //Delete this gui.
                        this.delete();
                        u.mainGui = null;

                        //Switch back to plot menu.
                        u.mainGui = new PlotsystemMembers(plotID, RegionType.PLOT);
                        u.mainGui.open(u);

                    });

            //Invite new members to your plot.
            setItem(20, Utils.createItem(Material.OAK_BOAT, 1,
                            Utils.title("Invite Members"),
                            Utils.line("Invite a new member to your plot."),
                            Utils.line("You can only invite online users.")),
                    u -> {

                        //Delete this gui.
                        this.delete();
                        u.mainGui = null;

                        //Switch back to plot menu.
                        u.mainGui = new InviteMembers(plotID, RegionType.PLOT);
                        u.mainGui.open(u);

                    });

        } else {
            //You are a member of this plot.

            //Leave plot.
            setItem(20, Utils.createItem(Material.RED_CONCRETE, 1,
                            Utils.title("Leave Plot"),
                            Utils.line("You will not be able to build in the plot once you leave.")),
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
                        Utils.title("Return"),
                        Utils.line("Open the plot menu.")),
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

        // If the plot no longer exists, return to the plot menu.
        if (Network.getInstance().getPlotSQL().hasRow("SELECT id FROM plot_data WHERE id=" + plotID + ";")) {
            createGui();
        } else {
            //Delete this gui.
            this.delete();
            user.mainGui = null;

            //Switch back to plot menu.
            user.mainGui = new PlotMenu(user);
            user.mainGui.open(user);
        }
    }
}
