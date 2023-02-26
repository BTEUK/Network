package me.bteuk.network.gui.regions;

import me.bteuk.network.Network;
import me.bteuk.network.commands.Back;
import me.bteuk.network.events.EventManager;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.sql.RegionSQL;
import me.bteuk.network.utils.SwitchServer;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.regions.Request;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

public class RegionRequest extends Gui {

    private final RegionSQL regionSQL;
    private final Request request;
    private final boolean staff;

    public RegionRequest(Request request, boolean staff) {

        super(27, Component.text("Region Request", NamedTextColor.AQUA, TextDecoration.BOLD));

        regionSQL = Network.getInstance().regionSQL;

        this.request = request;
        this.staff = staff;

        createGui();

    }

    private void createGui() {

        setItem(4, Utils.createItem(Material.BOOK, 1,
                Utils.title("Region " + request.region),
                Utils.line("Requested by &7" +
                        Network.getInstance().globalSQL.getString("SELECT name FROM player_data WHERE uuid='" +
                                request.uuid + "';"))));

        setItem(11, Utils.createItem(Material.LIME_CONCRETE, 1,
                        Utils.title("Accept Request"),
                        Utils.line("The user will be able to build in this region.")),
                u ->

                {

                    //Create event to accept request.
                    EventManager.createEvent(u.player.getUniqueId().toString(), "network", Network.getInstance().globalSQL.getString("SELECT name FROM server_data WHERE type='EARTH';"),
                            "region request accept " + request.region + " " + request.uuid);

                    //Return to request menu.
                    this.delete();

                    if (staff) {

                        u.staffGui = null;

                        //Delay opening to make sure request was dealt with.
                        Bukkit.getScheduler().runTaskLater(Network.getInstance(), () -> {
                            u.staffGui = new RegionRequests(true);
                            u.staffGui.open(u);
                        }, 20L);
                    } else {

                        u.mainGui = null;

                        //Delay opening to make sure request was dealt with.
                        Bukkit.getScheduler().runTaskLater(Network.getInstance(), () -> {
                            u.mainGui = new RegionRequests(false);
                            u.mainGui.open(u);
                        }, 20L);
                    }


                });

        setItem(15, Utils.createItem(Material.RED_CONCRETE, 1,
                        Utils.title("Deny Request"),
                        Utils.line("The user will not be able to build in this region.")),
                u ->

                {

                    //Create event to deny request.
                    EventManager.createEvent(u.player.getUniqueId().toString(), "network", Network.getInstance().globalSQL.getString("SELECT name FROM server_data WHERE type='EARTH';"),
                            "region request deny " + request.region + " " + request.uuid);

                    //Return to request menu.
                    this.delete();

                    if (staff) {

                        u.staffGui = null;

                        //Delay opening to make sure request was dealt with.
                        Bukkit.getScheduler().runTaskLater(Network.getInstance(), () -> {
                            u.staffGui = new RegionRequests(true);
                            u.staffGui.open(u);
                        }, 20L);
                    } else {

                        u.mainGui = null;

                        //Delay opening to make sure request was dealt with.
                        Bukkit.getScheduler().runTaskLater(Network.getInstance(), () -> {
                            u.mainGui = new RegionRequests(false);
                            u.mainGui.open(u);
                        }, 20L);
                    }

                });

        setItem(22, Utils.createItem(Material.ENDER_PEARL, 1,
                        Utils.title("Teleport to Region"),
                        Utils.line("Teleport to the location where the request was made.")),
                u ->

                {

                    GlobalSQL globalSQL = Network.getInstance().globalSQL;

                    //Get coordinate.
                    Location l = globalSQL.getCoordinate(regionSQL.getInt("SELECT coordinate_id FROM region_requests WHERE region='" + request.region + "' AND uuid='" + request.uuid + "';"));

                    //If the player is on the earth server get the coordinate.
                    if (Network.SERVER_NAME.equals(globalSQL.getString("SELECT name FROM server_data WHERE type='EARTH';"))) {

                        //Close inventory.
                        u.player.closeInventory();

                        //Set current location for /back
                        Back.setPreviousCoordinate(u.player.getUniqueId().toString(), u.player.getLocation());

                        //Teleport player.
                        u.player.teleport(l);
                        u.player.sendMessage(Utils.success("Teleported to region &3" + request.region));

                    } else {

                        //Create teleport event.
                        EventManager.createTeleportEvent(true, u.player.getUniqueId().toString(), "network", "teleport " +
                                Network.getInstance().getConfig().getString("earth_world") + " " + l.getX() + " " + l.getZ() + " " +
                                l.getYaw() + " " + l.getPitch(), u.player.getLocation());

                        //Switch server.
                        SwitchServer.switchServer(u.player, globalSQL.getString("SELECT name FROM server_data WHERE type='EARTH'"));

                    }

                });

        setItem(26, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.title("Previous Page"),
                        Utils.line("Open the region request menu.")),
                u ->

                {

                    //Return to request menu.
                    this.delete();

                    if (staff) {

                        u.staffGui = null;

                        //Delay opening to make sure request was dealt with.
                        Bukkit.getScheduler().runTaskLater(Network.getInstance(), () -> {
                            u.staffGui = new RegionRequests(true);
                            u.staffGui.open(u);
                        }, 20L);
                    } else {

                        u.mainGui = null;

                        //Delay opening to make sure request was dealt with.
                        Bukkit.getScheduler().runTaskLater(Network.getInstance(), () -> {
                            u.mainGui = new RegionRequests(false);
                            u.mainGui.open(u);
                        }, 20L);
                    }

                });
    }

    public void refresh() {

        this.clearGui();
        createGui();

    }
}
