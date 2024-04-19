package net.bteuk.network.gui.plotsystem;

import lombok.Setter;
import net.bteuk.network.Network;
import net.bteuk.network.eventing.events.EventManager;
import net.bteuk.network.gui.Gui;
import net.bteuk.network.gui.InviteMembers;
import net.bteuk.network.sql.GlobalSQL;
import net.bteuk.network.sql.PlotSQL;
import net.bteuk.network.utils.NetworkUser;
import net.bteuk.network.utils.PlotValues;
import net.bteuk.network.utils.SwitchServer;
import net.bteuk.network.utils.Utils;
import net.bteuk.network.utils.enums.PlotStatus;
import net.bteuk.network.utils.enums.RegionType;
import net.buildtheearth.terraminusminus.generator.EarthGeneratorSettings;
import net.buildtheearth.terraminusminus.projection.OutOfProjectionBoundsException;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static net.bteuk.network.utils.Constants.SERVER_NAME;

public class PlotInfo extends Gui {

    private final int plotID;
    private final NetworkUser user;

    private final PlotSQL plotSQL;
    private final GlobalSQL globalSQL;

    private String plot_owner;

    @Setter
    private AcceptedPlotMenu acceptedPlotMenu;

    public PlotInfo(NetworkUser user, int plotID) {

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

        // Get the plot status.
        PlotStatus status = PlotStatus.fromDatabaseValue(plotSQL.getString("SELECT status FROM plot_data WHERE id=" + plotID + ";"));
        if (status == null) {
            user.player.sendMessage(Utils.error("This plot has an invalid status, can't open the info menu."));
            return;
        }
        // Get the plot owner.
        if (status == PlotStatus.CLAIMED || status == PlotStatus.SUBMITTED || status == PlotStatus.REVIEWING) {
            plot_owner = plotSQL.getString("SELECT uuid FROM plot_members WHERE id=" + plotID + " AND is_owner=1;");
        } else if (status == PlotStatus.COMPLETED) {
            plot_owner = plotSQL.getString("SELECT uuid FROM accept_data WHERE id=" + plotID + ";");
        }
        // Determine the type of menu to create.
        PLOT_INFO_TYPE plotInfoType = determineMenuType(status);
        if (plotInfoType == null || plotInfoType == PLOT_INFO_TYPE.DELETED) {
            user.player.sendMessage(Utils.error("This plot not longer exists, can't open the info menu."));
            return;
        }

        // Return
        setItem(26, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.title("Return"),
                        Utils.line("Open the plot menu.")),
                u -> {

                    // Switch back to plot menu, or accepted plot menu.
                    if (status == PlotStatus.COMPLETED && acceptedPlotMenu != null) {
                        this.deleteThis();
                        acceptedPlotMenu.setPlotInfo(null);
                        acceptedPlotMenu.open(u);
                    } else {
                        // Delete this gui.
                        this.delete();
                        u.mainGui = new PlotMenu(u);
                        u.mainGui.open(u);
                    }
                });

        // Plot Info
        setItem(4, Utils.createItem(Material.BOOK, 1,
                Utils.title("Plot " + plotID),
                createPlotInfo(status, plotInfoType)));

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
                        u.player.closeInventory();

                    } catch (OutOfProjectionBoundsException e) {
                        u.player.sendMessage(Utils.error("Can't find the location of this plot."));
                        u.player.closeInventory();
                    }
                });

        // Enable/disable outlines for the plot. (Slot 18 if owner or member)
        if (plotInfoType == PLOT_INFO_TYPE.CLAIMED_OWNER || plotInfoType == PLOT_INFO_TYPE.CLAIMED_MEMBER) {
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

        // For the plot owner, add the manage and invite members options. (Slot 20 and 21)
        // As well as the submit/retract button. (Slot 2)
        // If the plot is not under review allow it to be removed. (Slot 6)
        if (plotInfoType == PLOT_INFO_TYPE.CLAIMED_OWNER) {
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

            if (status == PlotStatus.CLAIMED) {
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

            if (status == PlotStatus.SUBMITTED) {
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

            if (status != PlotStatus.REVIEWING) {
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
        }

        // Members have the option to leave the plot (Slot 20)
        if (plotInfoType == PLOT_INFO_TYPE.CLAIMED_MEMBER) {
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

        // If this plot has feedback, add feedback for the plot owner and members (Slot 22)
        // As well as for reviewers (Slot 22 while reviewing, slot 21 while submitted)
        if ((plotInfoType == PLOT_INFO_TYPE.CLAIMED_OWNER || plotInfoType == PLOT_INFO_TYPE.CLAIMED_MEMBER || plotInfoType == PLOT_INFO_TYPE.REVIEWING_REVIEWER || plotInfoType == PLOT_INFO_TYPE.SUBMITTED_REVIEWER)
                && plotSQL.hasRow("SELECT id FROM deny_data WHERE id=" + plotID + " AND uuid='" + plot_owner + "';")) {
            setItem(getFeedbackSlot(plotInfoType), Utils.createItem(Material.WRITABLE_BOOK, 1,
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
            // If the plot is accepted and has feedback show for the owner (Slot 22)
        } else if (plotInfoType == PLOT_INFO_TYPE.ACCEPTED_OWNER && plotSQL.hasRow("SELECT id FROM accept_data WHERE id=" + plotID + ";")) {
            setItem(getFeedbackSlot(plotInfoType), Utils.createItem(Material.WRITABLE_BOOK, 1,
                            Utils.title("Plot Feedback"),
                            Utils.line("Click to show feedback for this plot.")),
                    u -> {
                        //Create book.
                        Component title = Component.text("Plot " + plotID, NamedTextColor.AQUA, TextDecoration.BOLD);
                        Component author = Component.text(globalSQL.getString("SELECT name FROM player_data WHERE uuid='" +
                                plotSQL.getString("SELECT reviewer FROM accept_data WHERE id=" + plotID + ";") + "';"));

                        //Get pages of the book.
                        ArrayList<String> sPages = plotSQL.getStringList("SELECT contents FROM book_data WHERE id="
                                + plotSQL.getInt("SELECT book_id FROM accept_data WHERE id=" + plotID + ";") + ";");

                        //Create a list of components from the list of strings.
                        ArrayList<Component> pages = new ArrayList<>();
                        for (String page : sPages) {
                            pages.add(Component.text(page));
                        }

                        Book book = Book.book(title, author, pages);

                        //Open the book.
                        u.player.openBook(book);
                    });
        }

        // If the plot is submitted add the start review option for reviewers. (Slot 20)
        if (plotInfoType == PLOT_INFO_TYPE.SUBMITTED_REVIEWER) {
            setItem(20, Utils.createItem(Material.EMERALD, 1,
                            Utils.title("Review Plot"),
                            Utils.line("Click to start reviewing this plot.")),
                    u -> {
                        // If you are not owner or member of the plot, start the review.
                        if (!Network.getInstance().getPlotSQL().hasRow("SELECT id FROM plot_members WHERE uuid='" + u.player.getUniqueId() + "' AND id=" + plotID + ";")) {
                            //Get server of plot.
                            String server = Network.getInstance().getPlotSQL().getString("SELECT server FROM location_data WHERE name='" +
                                    Network.getInstance().getPlotSQL().getString("SELECT location FROM plot_data WHERE id=" + plotID + ";") + "';");

                            //If they are not in the same server as the plot teleport them to that server and start the reviewing process.
                            if (server.equals(SERVER_NAME)) {
                                u.player.closeInventory();
                                Network.getInstance().getGlobalSQL().update("INSERT INTO server_events(uuid,type,server,event) VALUES('"
                                        + u.player.getUniqueId() + "','plotsystem','" + SERVER_NAME + "','review plot " + plotID + "');");
                            } else {
                                // Player is not on the current server.
                                // Set the server join event.
                                Network.getInstance().getGlobalSQL().update("INSERT INTO join_events(uuid,type,event) VALUES('"
                                        + u.player.getUniqueId() + "','plotsystem',"
                                        + "'review plot " + plotID + "');");

                                //Teleport them to the server.
                                u.player.closeInventory();
                                SwitchServer.switchServer(u.player, server);
                            }
                        } else {
                            user.player.sendMessage(Utils.error("You are not allowed to review this plot since you have contributed to it."));
                        }
                    });
        }
    }

    public void refresh() {

        this.clearGui();
        createGui();
    }

    @Override
    public void delete() {
        if (acceptedPlotMenu != null) {
            acceptedPlotMenu.delete();
        } else {
            deleteThis();
        }
    }

    public void deleteThis() {
        super.delete();
    }

    private PLOT_INFO_TYPE determineMenuType(PlotStatus status) {
        return switch (status) {
            case UNCLAIMED -> PLOT_INFO_TYPE.UNCLAIMED;
            case CLAIMED -> claimedType();
            case SUBMITTED -> {
                if (user.hasPermission("uknet.plots.review") &&
                        !plotSQL.hasRow("SELECT id FROM plot_members WHERE id=" + plotID + " AND uuid='" + user.player.getUniqueId() + "';")) {
                    yield PLOT_INFO_TYPE.SUBMITTED_REVIEWER;
                } else {
                    yield claimedType();
                }
            }
            case REVIEWING -> {
                if (user.hasPermission("uknet.plots.review") &&
                        !plotSQL.hasRow("SELECT id FROM plot_members WHERE id=" + plotID + " AND uuid='" + user.player.getUniqueId() + "';")) {
                    yield PLOT_INFO_TYPE.REVIEWING_REVIEWER;
                } else {
                    yield claimedType();
                }
            }
            case COMPLETED -> {
                if (Objects.equals(plot_owner, user.player.getUniqueId().toString())) {
                    yield PLOT_INFO_TYPE.ACCEPTED_OWNER;
                } else {
                    yield PLOT_INFO_TYPE.ACCEPTED;
                }
            }
            case DELETED -> PLOT_INFO_TYPE.DELETED;
        };
    }

    private PLOT_INFO_TYPE claimedType() {
        if (Objects.equals(plot_owner, user.player.getUniqueId().toString())) {
            return PLOT_INFO_TYPE.CLAIMED_OWNER;
        } else if (plotSQL.hasRow("SELECT id FROM plot_members WHERE id=" + plotID + " AND uuid='" + user.player.getUniqueId() + "' AND is_owner=0;")) {
            return PLOT_INFO_TYPE.CLAIMED_MEMBER;
        } else {
            return PLOT_INFO_TYPE.CLAIMED;
        }
    }

    private Component[] createPlotInfo(PlotStatus status, PLOT_INFO_TYPE plotInfoType) {
        List<Component> info = new ArrayList<>();
        if (status == PlotStatus.CLAIMED || status == PlotStatus.SUBMITTED || status == PlotStatus.REVIEWING) {
            info.add(Utils.line("Plot Owner: ")
                    .append(Component.text(globalSQL.getString("SELECT name FROM player_data WHERE uuid='" +
                            plotSQL.getString("SELECT uuid FROM plot_members WHERE id=" + plotID + " AND is_owner=1;") + "';"), NamedTextColor.GRAY)));
            info.add(Utils.line("Plot Members: ")
                    .append(Component.text(plotSQL.getInt("SELECT COUNT(uuid) FROM plot_members WHERE id=" + plotID + " AND is_owner=0;"), NamedTextColor.GRAY)));
        } else if (status == PlotStatus.COMPLETED) {
            info.add(Utils.line("Completed by: ").append(Component.text(globalSQL.getString("SELECT name FROM player_data WHERE uuid='"
                    + plotSQL.getString("SELECT uuid FROM accept_data WHERE id=" + plotID + ";") + "';"), NamedTextColor.GRAY)));
            info.add(Utils.line("Accepted by: ")
                    .append(Component.text(globalSQL.getString("SELECT name FROM player_data WHERE uuid='"
                            + plotSQL.getString("SELECT reviewer FROM accept_data WHERE id=" + plotID + ";") + "';"), NamedTextColor.GRAY)));
        } else if (status == PlotStatus.UNCLAIMED) {
            info.add(Utils.line("This plot is unclaimed!"));
        }

        // Add size and difficulty stats.
        info.add(Utils.line("Difficulty: ")
                .append(Component.text(PlotValues.difficultyName(plotSQL.getInt("SELECT difficulty FROM plot_data WHERE id=" + plotID + ";")), NamedTextColor.GRAY)));
        info.add(Utils.line("Size: ")
                .append(Component.text(PlotValues.sizeName(plotSQL.getInt("SELECT size FROM plot_data WHERE id=" + plotID + ";")), NamedTextColor.GRAY)));

        // If accepted, and the builder opens de menu, show the accuracy and quality ratings.
        if (plotInfoType == PLOT_INFO_TYPE.ACCEPTED_OWNER) {
            info.add(Utils.line("Accuracy: ")
                    .append(Utils.greyText(String.valueOf(plotSQL.getInt("SELECT accuracy FROM accept_data WHERE id=" + plotID + ";"))))
                    .append(Utils.line("/"))
                    .append(Utils.greyText("5")));
            info.add(Utils.line("Quality: ")
                    .append(Utils.greyText(String.valueOf(plotSQL.getInt("SELECT quality FROM accept_data WHERE id=" + plotID + ";"))))
                    .append(Utils.line("/"))
                    .append(Utils.greyText("5")));
        }

        // If accepted, add a disclaimer that the actual plot may have changed since it was accepted.
        if (status == PlotStatus.COMPLETED) {
            info.add(Component.text("Disclaimer: ", NamedTextColor.WHITE, TextDecoration.BOLD)
                    .append(Utils.line("the content of")));
            info.add(Utils.line("the plot may have changed"));
            info.add(Utils.line("since it was completed!"));
        }
        return info.toArray(Component[]::new);
    }

    private int getSlotForGoogleMapsLink(PLOT_INFO_TYPE plotInfoType) {
        if (plotInfoType == PLOT_INFO_TYPE.CLAIMED_OWNER || plotInfoType == PLOT_INFO_TYPE.CLAIMED_MEMBER || plotInfoType == PLOT_INFO_TYPE.SUBMITTED_REVIEWER) {
            return 23;
        } else {
            return 20;
        }
    }

    private int getFeedbackSlot(PLOT_INFO_TYPE plotInfoType) {
        if (plotInfoType == PLOT_INFO_TYPE.CLAIMED_OWNER || plotInfoType == PLOT_INFO_TYPE.CLAIMED_MEMBER ||
                plotInfoType == PLOT_INFO_TYPE.REVIEWING_REVIEWER || plotInfoType == PLOT_INFO_TYPE.ACCEPTED_OWNER) {
            return 22;
        } else if (plotInfoType == PLOT_INFO_TYPE.SUBMITTED_REVIEWER) {
            return 21;
        } else {
            return -1;
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

