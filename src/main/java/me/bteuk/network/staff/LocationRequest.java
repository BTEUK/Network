package me.bteuk.network.staff;

import me.bteuk.network.Network;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;

public class LocationRequest extends Gui {

    private String name;
    private String category;
    private String subcategory;
    private final int coordinate_id;

    private final GlobalSQL globalSQL;

    public LocationRequest(String name) {

        super(27, Component.text(name, NamedTextColor.AQUA, TextDecoration.BOLD));

        globalSQL = Network.getInstance().globalSQL;

        //Get data from request.
        this.name = name;
        category = globalSQL.getString("SELECT category FROM location_requests WHERE location='" + name + "';");
        subcategory = globalSQL.getString("SELECT subcategory FROM location_requests WHERE location='" + name + "';");
        coordinate_id = globalSQL.getInt("SELECT coordinate FROM location_requests WHERE location='" + name + "';");

        createGui();

    }

    private void createGui() {

        //Teleport to location request.
        setItem(10, Utils.createItem(Material.ENDER_PEARL, 1,
                        Utils.title("Teleport to Location Request"),
                        Utils.line("Click to teleport to the location"),
                        Utils.line("of the request.")),
                u -> {

                    //Get location from coordinate id.
                    //TODO: If the location is on a plot server, get the location transformation and convert the coordinate to take that into account.
                    Location l = globalSQL.getCoordinate(coordinate_id);

                    //Close inventory.
                    u.player.closeInventory();

                    //Teleport to location.
                    u.player.teleport(l);

                });

        //Accept request.
        setItem(12, Utils.createItem(Material.LIME_CONCRETE, 1,
                        Utils.title("Accepted Location Request"),
                        Utils.line("Location will be added to"),
                        Utils.line("the exploration menu as well as"),
                        Utils.line("the list of warps.")),
                u -> {

                    //Delete request.
                    globalSQL.update("DELETE FROM location_requests WHERE location='" + name + "';");

                    //Add location.
                    if (subcategory == null) {
                        globalSQL.update("INSERT INTO location_data(location,category,coordinate) " +
                                "VALUES('" + name + "','" + category + "'," + coordinate_id + ");");
                    } else {
                        globalSQL.update("INSERT INTO location_data(location,category,subcategory,coordinate) " +
                                "VALUES('" + name + "','" + category + "','" + subcategory + "'," + coordinate_id + ");");
                    }

                    //Notify player.
                    u.player.sendMessage(Utils.success("Accepted location request &3" + name + " &a."));

                    previousGui(u);

                });

        //Deny request.
        setItem(14, Utils.createItem(Material.RED_CONCRETE, 1,
                        Utils.title("Deny Location Request"),
                        Utils.line("Location request will be denied.")),
                u -> {

                    //Delete request.
                    globalSQL.update("DELETE FROM location_requests WHERE location='" + name + "';");

                    //Notify player.
                    u.player.sendMessage(Utils.error("Denied location request &4" + name + "&c."));

                    previousGui(u);

                });

        //Return
        setItem(26, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.chat("&b&lReturn"),
                        Utils.chat("&fReturn to location requests.")),
                this::previousGui);

    }

    public void refresh() {

        this.clearGui();
        createGui();

    }

    public void previousGui(NetworkUser u) {
        //Delete gui and return to previous menu.
        this.delete();
        u.staffGui = null;

        u.staffGui = new LocationRequests();
        u.staffGui.open(u);
    }
}
