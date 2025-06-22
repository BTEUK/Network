package net.bteuk.network.gui.plotsystem;

import net.bteuk.network.Network;
import net.bteuk.network.eventing.events.EventManager;
import net.bteuk.network.gui.Gui;
import net.bteuk.network.gui.InviteMembers;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.sql.GlobalSQL;
import net.bteuk.network.sql.PlotSQL;
import net.bteuk.network.utils.NetworkUser;
import net.bteuk.network.utils.SwitchServer;
import net.bteuk.network.utils.Time;
import net.bteuk.network.utils.Utils;
import net.bteuk.network.utils.enums.RegionType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import static net.bteuk.network.utils.Constants.SERVER_NAME;

public class ZoneInfo extends Gui {

    private final int zoneID;
    private final String uuid;

    private final PlotSQL plotSQL;

    private final NetworkUser user;

    public ZoneInfo(NetworkUser user, int zoneID, String uuid) {

        super(27, Component.text("Zone " + zoneID, NamedTextColor.AQUA, TextDecoration.BOLD));

        this.user = user;
        this.zoneID = zoneID;
        this.uuid = uuid;

        // Get plot sql.
        plotSQL = Network.getInstance().getPlotSQL();

        createGui();
    }

    public void createGui() {

        // Get global sql.
        GlobalSQL globalSQL = Network.getInstance().getGlobalSQL();

        setItem(4, Utils.createItem(Material.BOOK, 1,
                Utils.title("Zone " + zoneID),
                Utils.line("Zone Owner: ")
                        .append(Component.text(globalSQL.getString("SELECT name FROM player_data WHERE uuid='" +
                                plotSQL.getString("SELECT uuid FROM zone_members WHERE id=" + zoneID + " AND " +
                                        "is_owner=1;") + "';"), NamedTextColor.GRAY)),
                Utils.line("Zone Members: ")
                        .append(Component.text(plotSQL.getInt(
                                        "SELECT COUNT(uuid) FROM zone_members WHERE id=" + zoneID + " AND is_owner=0;"),
                                NamedTextColor.GRAY)),
                Utils.line("Expiration: ")
                        .append(Component.text(Time.getDateTime(plotSQL.getLong("SELECT expiration FROM zones WHERE " +
                                "id=" + zoneID + ";")), NamedTextColor.GRAY)),
                Utils.line("Public: ")
                        .append(Component.text((plotSQL.hasRow("SELECT id FROM zones WHERE id=" + zoneID + " AND " +
                                "is_public=1") ? "True" : "False"), NamedTextColor.GRAY))));

        setItem(8, Utils.createItem(Material.ENDER_PEARL, 1,
                        Utils.title("Teleport to Zone"),
                        Utils.line("Click to teleport to this zone.")),

                u -> {

                    u.player.closeInventory();

                    // Get the server of the plot.
                    String server = Network.getInstance().getPlotSQL().getString("SELECT server FROM location_data " +
                            "WHERE name='"
                            + Network.getInstance().getPlotSQL()
                            .getString("SELECT location FROM zones WHERE id=" + zoneID + ";")
                            + "';");

                    // If the plot is on the current server teleport them directly.
                    // Else teleport them to the correct server and them teleport them to the plot.
                    if (server.equals(SERVER_NAME)) {

                        EventManager.createTeleportEvent(false, u.player.getUniqueId().toString(), "plotsystem",
                                "teleport zone " + zoneID, u.player.getLocation());
                    } else {

                        // Set the server join event.
                        EventManager.createTeleportEvent(true, u.player.getUniqueId().toString(), "plotsystem",
                                "teleport zone " + zoneID, u.player.getLocation());

                        // Teleport them to another server.
                        SwitchServer.switchServer(u.player, server);
                    }
                });

        // If you the owner of this zone.
        if (plotSQL.hasRow("SELECT id FROM zone_members WHERE id=" + zoneID + " AND uuid='" + uuid + "' AND " +
                "is_owner=1;")) {

            // Delete zone button.
            // Beware, this will revert any progress made in the zone!
            setItem(6, Utils.createItem(Material.RED_CONCRETE, 1,
                            Utils.title("Delete Zone"),
                            Utils.line("Delete the zone and all its contents.")),
                    u -> {

                        // Delete this gui.
                        this.delete();
                        u.mainGui = null;

                        // Switch back to plot menu.
                        u.mainGui = new DeleteConfirm(zoneID, RegionType.ZONE);
                        u.mainGui.open(u);
                    });

            // Close and save zone.
            // This will save the progress and then close the zone.
            setItem(2, Utils.createItem(Material.LIME_CONCRETE, 1,
                            Utils.title("Save and close Zone"),
                            Utils.line("Close the Zone and save its contents.")),
                    u -> {

                        // Delete this gui.
                        this.delete();
                        u.mainGui = null;

                        // Open close confirm menu.
                        u.mainGui = new CloseConfirm(zoneID);
                        u.mainGui.open(u);
                    });

            // If zone has members, edit plot members.
            setItem(9, Utils.createItem(Material.PLAYER_HEAD, 1,
                            Utils.title("Zone Members"),
                            Utils.line("Manage the members of your zone.")),
                    u -> {

                        // Delete this gui.
                        this.delete();
                        u.mainGui = null;

                        // Open the members menu.
                        u.mainGui = new PlotsystemMembers(zoneID, RegionType.ZONE);
                        u.mainGui.open(u);
                    });

            // Invite new members to your zone.
            setItem(0, Utils.createItem(Material.OAK_BOAT, 1,
                            Utils.title("Invite Members"),
                            Utils.line("Invite a new member to your zone."),
                            Utils.line("You can only invite online users.")),
                    u -> {

                        // Delete this gui.
                        this.delete();
                        u.mainGui = null;

                        // Switch back to plot menu.
                        u.mainGui = new InviteMembers(zoneID, RegionType.ZONE);
                        u.mainGui.open(u);
                    });

            // Set public/private
            if (plotSQL.hasRow("SELECT id FROM zones WHERE id=" + zoneID + " AND is_public=1;")) {

                setItem(18, Utils.createItem(Material.OAK_DOOR, 1,
                                Utils.title("Set Private"),
                                Utils.line("Private zones require you to"),
                                Utils.line("invite people if they want to build.")),
                        u -> {

                            // Set zone to private and refresh this gui.
                            plotSQL.update("UPDATE zones SET is_public=0 WHERE id=" + zoneID + ";");
                            u.player.sendMessage(ChatUtils.success("Set zone to private."));

                            this.refresh();
                            u.player.getOpenInventory().getTopInventory()
                                    .setContents(this.getInventory().getContents());
                        });
            } else {

                setItem(18, Utils.createItem(Material.IRON_DOOR, 1,
                                Utils.title("Set Public"),
                                Utils.line("Public zones allow Jr.Builder+"),
                                Utils.line("to join the zone without invitation.")),
                        u -> {

                            // Set zone to private and refresh this gui.
                            plotSQL.update("UPDATE zones SET is_public=1 WHERE id=" + zoneID + ";");
                            u.player.sendMessage(ChatUtils.success("Set zone to public."));

                            this.refresh();
                            u.player.getOpenInventory().getTopInventory()
                                    .setContents(this.getInventory().getContents());
                        });
            }

            // Extend zone duration (can't exceed maximum of 48 hours).
            setExtendZoneDurationItem(21, ZoneExtensionTime.HOUR_2);
            setExtendZoneDurationItem(22, ZoneExtensionTime.HOUR_6);
            setExtendZoneDurationItem(23, ZoneExtensionTime.HOUR_24);
        } else {

            // You are a member of this zone.
            setItem(4, Utils.createItem(Material.RED_CONCRETE, 1,
                            Utils.title("Leave Zone"),
                            Utils.line("You will not be able to build in the zone once you leave.")),
                    u -> {

                        // Delete this gui.
                        this.delete();
                        u.mainGui = null;

                        // Switch back to zone menu,
                        Bukkit.getScheduler().scheduleSyncDelayedTask(Network.getInstance(), () -> {
                            u.mainGui = new ZoneMenu(u);
                            u.mainGui.open(u);
                        }, 20L);

                        // Add server event to leave plot.
                        globalSQL.update(
                                "INSERT INTO server_events(uuid,type,server,event) VALUES('" + u.player.getUniqueId() + "','plotsystem','" +
                                        plotSQL.getString("SELECT server FROM location_data WHERE name='" +
                                                plotSQL.getString(
                                                        "SELECT location FROM zones WHERE id=" + zoneID + ";") +
                                                "';") +
                                        "','leave zone " + zoneID + "');");
                    });
        }

        // Return
        setItem(26, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.title("Return"),
                        Utils.line("Open the zone menu.")),
                u ->

                {

                    // Delete this gui.
                    this.delete();
                    u.mainGui = null;

                    // Switch back to plot menu.
                    u.mainGui = new ZoneMenu(u);
                    u.mainGui.open(u);
                });
    }

    public void refresh() {

        this.clearGui();

        // If the plot no longer exists, return to the plot menu.
        if (Network.getInstance().getPlotSQL().hasRow("SELECT id FROM zones WHERE id=" + zoneID + ";")) {
            createGui();
        } else {
            // Delete this gui.
            this.delete();
            user.mainGui = null;

            // Switch back to plot menu.
            user.mainGui = new ZoneMenu(user);
            user.mainGui.open(user);
        }
    }

    private void setExtendZoneDurationItem(int slot, ZoneExtensionTime extension) {
        setItem(slot, Utils.createItem(Material.CLOCK, extension.hours,
                        Utils.title("Extend Zone Duration by " + extension.hours + " Hours"),
                        Utils.line("Increases the expiration time"),
                        Utils.line("of the zone by " + extension.hours + " hours,"),
                        Utils.line("can't exceed the maximum of " + ZoneExtensionTime.HOUR_48.hours + " hours.")),
                u -> {
                    // Get expiration time.
                    long expiration =
                            plotSQL.getLong("SELECT expiration FROM zones WHERE id=" + zoneID + ";") + extension.time;

                    // Get maximum expiration time.
                    long max_time = Time.currentTime() + ZoneExtensionTime.HOUR_48.time;

                    plotSQL.update(
                            "UPDATE zones SET expiration=" + Math.min(expiration, max_time) + " WHERE id=" + zoneID);
                    u.player.sendMessage(ChatUtils.success("Set Zone expiration time to ")
                            .append(Component.text(Time.getDateTime(Math.min(expiration, max_time)),
                                    NamedTextColor.DARK_AQUA)));

                    u.player.closeInventory();
                });
    }

    private enum ZoneExtensionTime {
        HOUR_2(2, 1000L * 60L * 60L * 2L),
        HOUR_6(6, 1000L * 60L * 60L * 6L),
        HOUR_24(24, 1000L * 60L * 60L * 24L),
        HOUR_48(48, 1000L * 60L * 60L * 48L);

        private final int hours;
        private final long time;

        ZoneExtensionTime(int hours, long time) {
            this.hours = hours;
            this.time = time;
        }
    }
}
