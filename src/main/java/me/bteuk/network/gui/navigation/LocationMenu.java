package me.bteuk.network.gui.navigation;

import me.bteuk.network.Network;
import me.bteuk.network.commands.Back;
import me.bteuk.network.events.EventManager;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.SwitchServer;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.HashSet;

public class LocationMenu extends Gui {

    private HashSet<String> locations;

    private int page;
    private boolean england;

    //This tells us we have the "Nearby Locations" menu open, and when refreshed should update to the current location of the player.
    private boolean nearby;
    private NetworkUser u;

    public LocationMenu(String title, HashSet<String> locations, boolean england, boolean nearby, NetworkUser u) {

        super(45, Component.text(title, NamedTextColor.AQUA, TextDecoration.BOLD));

        this.locations = locations;

        this.england = england;

        this.nearby = nearby;
        this.u = u;

        //On initialization the page is always 1.
        page = 1;

        createGui();

    }

    private void createGui() {

        //If page > 1 set number of iterations that must be skipped.
        int skip = (page - 1) * 21;

        //Slot count.
        int slot = 10;

        //If page is greater than 1 add a previous page button.
        if (page > 1) {
            setItem(18, Utils.createItem(Material.ARROW, 1,
                            Utils.title("Previous Page"),
                            Utils.line("Open the previous page of locations.")),
                    u ->

                    {

                        //Update the gui.
                        page--;
                        this.refresh();
                        u.player.getOpenInventory().getTopInventory().setContents(this.getInventory().getContents());

                    });
        }

        //Iterate through all locations
        for (String location : locations) {

            //Skip iterations if skip > 0.
            if (skip > 0) {
                skip--;
                continue;
            }

            //If the slot is greater than the number that fit in a page, create a new page.
            if (slot > 34) {

                setItem(26, Utils.createItem(Material.ARROW, 1,
                                Utils.title("Next Page"),
                                Utils.line("&fOpen the next page of locations.")),
                        u ->

                        {

                            //Update the gui.
                            page++;
                            this.refresh();
                            u.player.getOpenInventory().getTopInventory().setContents(this.getInventory().getContents());

                        });

                //Stop iterating.
                break;
            }

            //Create location teleport button.
            setItem(slot, Utils.createItem(Material.ENDER_PEARL, 1,
                            Utils.title(location),
                            Utils.line("Click to teleport here.")),

                    u -> {

                        //Get the coordinate id.
                        int coordinate_id = Network.getInstance().globalSQL.getInt("SELECT coordinate FROM location_data WHERE location='" + location + "';");

                        //Get the server of the location.
                        String server = Network.getInstance().globalSQL.getString("SELECT server FROM coordinates WHERE id=" + coordinate_id + ";");

                        //If the plot is on the current server teleport them directly.
                        //Else teleport them to the correct server and them teleport them to the plot.
                        if (server.equals(Network.SERVER_NAME)) {

                            //Close inventory.
                            u.player.closeInventory();

                            //Get location from coordinate id.
                            Location l = Network.getInstance().globalSQL.getCoordinate(coordinate_id);

                            //Set current location for /back
                            Back.setPreviousCoordinate(u.player.getUniqueId().toString(), u.player.getLocation());

                            u.player.teleport(l);
                            u.player.sendMessage(Utils.success("Teleported to &3" + location));

                        } else {

                            //Create teleport event.
                            EventManager.createTeleportEvent(true, u.player.getUniqueId().toString(), "network", "teleport location " + location, u.player.getLocation());

                            //Switch server.
                            SwitchServer.switchServer(u.player, server);

                        }

                    });

            //Increase slot accordingly.
            if (slot % 9 == 7) {
                //Increase row, basically add 3.
                slot += 3;
            } else {
                //Increase value by 1.
                slot++;
            }

        }

        //Return
        setItem(44, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.title("Return"),
                        Utils.line("Open the exploration menu.")),
                u ->

                {

                    //Delete this gui.
                    this.delete();

                    //Switch to navigation menu.
                    //If england is true then return to the england menu.
                    if (england) {
                        u.mainGui = new EnglandMenu();
                    } else {
                        u.mainGui = new ExploreGui(u);
                    }
                    u.mainGui.open(u);

                });
    }

    public void refresh() {

        this.clearGui();

        //If menu is nearby locations, update the locations.
        locations = new HashSet<>(Network.getInstance().globalSQL.getStringList("SELECT location_data.location FROM location_data " +
                "INNER JOIN coordinates ON location_data.coordinate=coordinates.id " +
                "WHERE (((coordinates.x-" + u.player.getLocation().getX() + ")*(coordinates.x-" + u.player.getLocation().getX() + ")) + " +
                "((coordinates.z-" + u.player.getLocation().getZ() + ")*(coordinates.z-" + u.player.getLocation().getZ() + "))) < " +
                ((Network.getInstance().getConfig().getInt("navigation_radius") * 1000) * (Network.getInstance().getConfig().getInt("navigation_radius") * 1000))));

        createGui();

    }
}
