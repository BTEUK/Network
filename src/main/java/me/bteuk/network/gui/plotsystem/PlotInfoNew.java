package me.bteuk.network.gui.plotsystem;

import me.bteuk.network.Network;
import me.bteuk.network.events.EventManager;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.gui.InviteMembers;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.sql.PlotSQL;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.PlotValues;
import me.bteuk.network.utils.SwitchServer;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.enums.PlotStatus;
import me.bteuk.network.utils.enums.RegionType;
import net.buildtheearth.terraminusminus.generator.EarthGeneratorSettings;
import net.buildtheearth.terraminusminus.projection.OutOfProjectionBoundsException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static me.bteuk.network.utils.Constants.SERVER_NAME;

public class PlotInfoNew extends Gui {

    private final int plotID;
    private final NetworkUser user;

    private final PlotSQL plotSQL;
    private final GlobalSQL globalSQL;

    public PlotInfoNew(NetworkUser user, int plotID) {

        // Create the menu.
        super(27, Component.text("Plot " + plotID, NamedTextColor.AQUA, TextDecoration.BOLD));

        this.user = user;
        this.plotID = plotID;

        // Get plot sql.
        plotSQL = Network.getInstance().getPlotSQL();

        // Get global sql.
        globalSQL = Network.getInstance().getGlobalSQL();

        createGui();

    }

    public void createGui() {

        // Return
        setItem(26, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.title("Return"),
                        Utils.line("Open the plot menu.")),
                u -> {
                    // Delete this gui.
                    this.delete();
                    u.mainGui = null;

                    // Switch back to plot menu.
                    u.mainGui = new PlotMenu(u);
                    u.mainGui.open(u);
                });

        // Determine the type of menu to create.
        PlotStatus status = PlotStatus.fromDatabaseValue(plotSQL.getString("SELECT status FROM plot_data WHERE id=" + plotID + ";"));
        if (status == null) {
            user.player.sendMessage(Utils.error("This plot has an invalid status, can't open the info menu."));
            return;
        }
        PLOT_INFO_TYPE plotInfoType = determineMenuType(status);
        if (plotInfoType == null || plotInfoType == PLOT_INFO_TYPE.UNCLAIMED || plotInfoType == PLOT_INFO_TYPE.DELETED) {
            user.player.sendMessage(Utils.error("This plot has an invalid status, can't open the info menu."));
            return;
        }

        // Plot Info
        setItem(4, Utils.createItem(Material.BOOK, 1,
                Utils.title("Plot " + plotID),
                createPlotInfo(plotInfoType)));

        // Plot Teleport (Always in slot 24).
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

        // Plot in Google Maps (In slot 20 or 23 depending on the situation).
        setItem(getSlotForGoogleMapsLink(plotInfoType), Utils.createItem(Material.ENDER_EYE, 1,
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
        //TODO: This does not check for the uuid of the plot owner, so if the plot was previously given feedback for someone else this wills still show!!!!
        //TODO: Plot members should be able to view feedback???
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

        // Enable/disable outlines for the plot.
        setItem(18, Utils.createItem(Material.ORANGE_STAINED_GLASS, 1,
                        Utils.title("Toggle Outlines"),
                        Utils.line("Enable/disable the outlines"),
                        Utils.line("for this plot."),
                        Utils.line("Rejoining the server"),
                        Utils.line("will reset this to enabled.")),
                u -> {
                    EventManager.createEvent(u.player.getUniqueId().toString(), "plotsystem", SERVER_NAME, "outlines toggle " + plotID);
                    u.player.closeInventory();
                });
    }

    public void refresh() {

        this.clearGui();
        createGui();
    }

    private PLOT_INFO_TYPE determineMenuType(PlotStatus status) {
        return switch (status) {
            case UNCLAIMED -> PLOT_INFO_TYPE.UNCLAIMED;
            case CLAIMED -> claimedType();
            case SUBMITTED -> {
                if (user.hasPermission("group.reviewer")) {
                    yield PLOT_INFO_TYPE.SUBMITTED_REVIEWER;
                } else {
                    yield claimedType();
                }
            }
            case REVIEWING -> {
                if (user.hasPermission("group.reviewer")) {
                    yield PLOT_INFO_TYPE.REVIEWING_REVIEWER;
                } else {
                    yield claimedType();
                }
            }
            case COMPLETED -> {
                if (plotSQL.hasRow("SELECT id FROM accept_data WHERE id=" + plotID + " AND uuid='" + user.player.getUniqueId() + "';")) {
                    yield PLOT_INFO_TYPE.ACCEPTED_OWNER;
                } else {
                    yield PLOT_INFO_TYPE.ACCEPTED;
                }
            }
            case DELETED -> PLOT_INFO_TYPE.DELETED;
        };
    }

    private PLOT_INFO_TYPE claimedType() {
        if (plotSQL.hasRow("SELECT id FROM plot_members WHERE id=" + plotID + " AND uuid='" + user.player.getUniqueId() + "' AND is_owner=1;")) {
            return PLOT_INFO_TYPE.CLAIMED_OWNER;
        } else if (plotSQL.hasRow("SELECT id FROM plot_members WHERE id=" + plotID + " AND uuid='" + user.player.getUniqueId() + "' AND is_owner=0;")) {
            return PLOT_INFO_TYPE.CLAIMED_MEMBER;
        } else {
            return PLOT_INFO_TYPE.CLAIMED;
        }
    }

    private Component[] createPlotInfo(PLOT_INFO_TYPE plotInfoType) {
        List<Component> info = new ArrayList<>();
        return info.toArray(Component[]::new);
    }

    private int getSlotForGoogleMapsLink(PLOT_INFO_TYPE plotInfoType) {
        if (plotInfoType == PLOT_INFO_TYPE.CLAIMED_OWNER || plotInfoType == PLOT_INFO_TYPE.SUBMITTED_REVIEWER) {
            return 23;
        } else {
            return 20;
        }
    }

    private enum PLOT_INFO_TYPE {
        CLAIMED_OWNER,
        CLAIMED_MEMBER,
        CLAIMED,

        SUBMITTED_REVIEWER,

        REVIEWING_REVIEWER,

        ACCEPTED_OWNER,
        ACCEPTED,

        UNCLAIMED,
        DELETED
    }
}
