package me.bteuk.network.gui.plotsystem;

import me.bteuk.network.Network;
import me.bteuk.network.events.EventManager;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.gui.InviteMembers;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.sql.PlotSQL;
import me.bteuk.network.utils.PlotValues;
import me.bteuk.network.utils.SwitchServer;
import me.bteuk.network.utils.Time;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.enums.RegionType;
import net.buildtheearth.terraminusminus.generator.EarthGeneratorSettings;
import net.buildtheearth.terraminusminus.projection.OutOfProjectionBoundsException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;

public class ZoneInfo extends Gui {

    private final int zoneID;
    private final String uuid;

    public ZoneInfo(int zoneID, String uuid) {

        super(27, Component.text("Zone " + zoneID, NamedTextColor.AQUA, TextDecoration.BOLD));

        this.zoneID = zoneID;
        this.uuid = uuid;

        createGui();

    }

    public void createGui() {

        //Get plot sql.
        PlotSQL plotSQL = Network.getInstance().plotSQL;

        //Get global sql.
        GlobalSQL globalSQL = Network.getInstance().globalSQL;

        setItem(4, Utils.createItem(Material.BOOK, 1,
                Utils.title("Zone &7" + zoneID),
                Utils.line("Zone Owner: &7" + globalSQL.getString("SELECT name FROM player_data WHERE uuid='" +
                        plotSQL.getString("SELECT uuid FROM zone_members WHERE id=" + zoneID + " AND is_owner=1;") + "';")),
                Utils.line("Plot Members: &7" + plotSQL.getInt("SELECT COUNT(uuid) FROM zone_members WHERE id=" + zoneID + " AND is_owner=0;")),
                Utils.line("Expiration: &7" + Time.getDateTime(plotSQL.getLong("SELECT expiration FROM zone_info WHERE id=" + zoneID + ";"))),
                Utils.line("Public: &7" + (plotSQL.hasRow("SELECT id FROM zones WHERE id=" + zoneID + " AND is_public=1") ? "True" : "False"))));

        setItem(24, Utils.createItem(Material.ENDER_PEARL, 1,
                        Utils.title("Teleport to Zone"),
                        Utils.line("Click to teleport to this zone.")),

                u -> {

                    u.player.closeInventory();

                    //Get the server of the plot.
                    String server = Network.getInstance().plotSQL.getString("SELECT server FROM location_data WHERE name='"
                            + Network.getInstance().plotSQL.getString("SELECT location FROM zones WHERE id=" + zoneID + ";")
                            + "';");

                    //If the plot is on the current server teleport them directly.
                    //Else teleport them to the correct server and them teleport them to the plot.
                    if (server.equals(Network.SERVER_NAME)) {

                        EventManager.createTeleportEvent(false, u.player.getUniqueId().toString(), "plotsystem", "teleport zone " + zoneID, u.player.getLocation());

                    } else {

                        //Set the server join event.
                        EventManager.createTeleportEvent(true, u.player.getUniqueId().toString(), "plotsystem", "teleport zone " + zoneID, u.player.getLocation());

                        //Teleport them to another server.
                        SwitchServer.switchServer(u.player, server);

                    }

                });

        //If you the owner of this plot.
        if (plotSQL.hasRow("SELECT id FROM zone_members WHERE id=" + zoneID + " AND uuid='" + uuid + "' AND is_owner=1;")) {

            //Delete zone button.
            //Beware, this will revert any progress made in the zone!
            setItem(6, Utils.createItem(Material.RED_CONCRETE, 1,
                            Utils.title("Delete Plot"),
                            Utils.line("Delete the plot and all its contents.")),
                    u -> {

                        //Delete this gui.
                        this.delete();
                        u.mainGui = null;

                        //Switch back to plot menu.
                        u.mainGui = new DeleteConfirm(plotID);
                        u.mainGui.open(u);

                    });

            //Close and save zone.
            //This will save the progress and then close the zone.
            setItem(6, Utils.createItem(Material.RED_CONCRETE, 1,
                            Utils.title("Delete Plot"),
                            Utils.line("Delete the plot and all its contents.")),
                    u -> {

                        //Delete this gui.
                        this.delete();
                        u.mainGui = null;

                        //Switch back to plot menu.
                        u.mainGui = new DeleteConfirm(plotID);
                        u.mainGui.open(u);

                    });

            //If zone has members, edit plot members.
            setItem(21, Utils.createItem(Material.PLAYER_HEAD, 1,
                            Utils.title("Zone Members"),
                            Utils.line("Manage the members of your zone.")),
                    u -> {

                        //Delete this gui.
                        this.delete();
                        u.mainGui = null;

                        //Switch back to plot menu.
                        u.mainGui = new PlotMembers(plotID);
                        u.mainGui.open(u);

                    });

            //Invite new members to your zone.
            setItem(20, Utils.createItem(Material.OAK_BOAT, 1,
                            Utils.title("Invite Members"),
                            Utils.line("Invite a new member to your zone."),
                            Utils.line("You can only invite online users.")),
                    u -> {

                        //Delete this gui.
                        this.delete();
                        u.mainGui = null;

                        //Switch back to plot menu.
                        u.mainGui = new InviteMembers(zoneID, RegionType.ZONE);
                        u.mainGui.open(u);

                    });

        } else {
            //You are a member of this plot.

            //Leave zone.
            setItem(20, Utils.createItem(Material.RED_CONCRETE, 1,
                            Utils.title("Leave Zone"),
                            Utils.line("You will not be able to build in the zone once you leave.")),
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
                        Utils.line("Open the zone menu.")),
                u ->

                {

                    //Delete this gui.
                    this.delete();
                    u.mainGui = null;

                    //Switch back to plot menu.
                    u.mainGui = new ZoneMenu(u);
                    u.mainGui.open(u);

                });
    }

    public void refresh() {

        this.clearGui();
        createGui();

    }
}
