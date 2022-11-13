package me.bteuk.network.gui.navigation;

import me.bteuk.network.Network;
import me.bteuk.network.commands.Back;
import me.bteuk.network.events.EventManager;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.utils.SwitchServer;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.HashSet;

public class LocationMenu extends Gui {

    private final HashSet<String> locations;

    private int page;
    private boolean england;

    public LocationMenu(String title, HashSet<String> locations, boolean england) {

        super(45, Component.text(title, NamedTextColor.AQUA, TextDecoration.BOLD));

        this.locations = locations;

        this.england = england;

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
                            Utils.chat("&b&lPrevious Page"),
                            Utils.chat("&fOpen the previous page of locations.")),
                    u ->

                    {

                        //Update the gui.
                        page--;
                        this.refresh();
                        u.player.getOpenInventory().getTopInventory().setContents(this.getInventory().getContents());

                    });
        }

        //Iterate through all locations
        for (String location: locations) {

            //Skip iterations if skip > 0.
            if (skip > 0) {
                skip --;
                continue;
            }

            //If the slot is greater than the number that fit in a page, create a new page.
            if (slot > 34) {

                setItem(26, Utils.createItem(Material.ARROW, 1,
                                Utils.chat("&b&lNext Page"),
                                Utils.chat("&fOpen the next page of locations.")),
                        u ->

                        {

                            //Update the gui.
                            page++;
                            this.refresh();
                            u.player.getOpenInventory().getTopInventory().setContents(this.getInventory().getContents());

                        });
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

                            //If world is in plot system add coordinate transform.
                            String world = Network.getInstance().globalSQL.getString("SELECT world FROM coordinates WHERE id=" + coordinate_id + ";");
                            if (Network.getInstance().plotSQL.hasRow("SELECT name FROM location_data WHERE name='" + world + "';")) {
                                l.setX(l.getX() + Network.getInstance().plotSQL.getInt("SELECT xTransform FROM location_data WHERE name='" + world + "';"));
                                l.setZ(l.getZ() + Network.getInstance().plotSQL.getInt("SELECT zTransform FROM location_data WHERE name='" + world + "';"));
                            }

                            //Set current location for /back
                            Back.setPreviousCoordinate(u.player.getUniqueId().toString(), u.player.getLocation());

                            u.player.teleport(l);
                            u.player.sendMessage(Utils.chat("&aTeleported to &3" + location));

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
                        Utils.chat("&b&lReturn"),
                        Utils.chat("&fOpen the exploration menu.")),
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
        createGui();

    }
}
