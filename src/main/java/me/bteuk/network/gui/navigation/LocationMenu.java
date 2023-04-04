package me.bteuk.network.gui.navigation;

import me.bteuk.network.Network;
import me.bteuk.network.commands.Back;
import me.bteuk.network.events.EventManager;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.SwitchServer;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.enums.Categories;
import me.bteuk.network.utils.enums.Counties;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;

public class LocationMenu extends Gui {

    private LinkedHashSet<String> locations;

    private int page;

    private final String type;
    private final String returnMenu;
    private final NetworkUser u;

    //Method to determine the search parameters when getting the locations to display in the menu.
    private LinkedHashSet<String> getLocations() {

        switch (type) {

            //UK Categories (Excluding England)
            case "Scotland" -> {return new LinkedHashSet<>(Network.getInstance().globalSQL.getStringList("SELECT location FROM location_data WHERE category='SCOTLAND' ORDER BY location ASC;"));}
            case "Wales" -> {return new LinkedHashSet<>(Network.getInstance().globalSQL.getStringList("SELECT location FROM location_data WHERE category='WALES' ORDER BY location ASC;"));}
            case "Northern Ireland" -> {return new LinkedHashSet<>(Network.getInstance().globalSQL.getStringList("SELECT location FROM location_data WHERE category='NORTHERN_IRELAND' ORDER BY location ASC;"));}
            case "Other" -> {return new LinkedHashSet<>(Network.getInstance().globalSQL.getStringList("SELECT location FROM location_data WHERE category='OTHER' ORDER BY location ASC;"));}
            case "Suggested" -> {return new LinkedHashSet<>(Network.getInstance().globalSQL.getStringList("SELECT location FROM location_data WHERE suggested=1 ORDER BY location ASC;"));}

            //Regions of England
            case "Yorkshire" -> {return new LinkedHashSet<>(Network.getInstance().globalSQL.getStringList("SELECT location FROM location_data WHERE category='ENGLAND' AND subcategory='YORKSHIRE' ORDER BY location ASC;"));}
            case "West Midlands" -> {return new LinkedHashSet<>(Network.getInstance().globalSQL.getStringList("SELECT location FROM location_data WHERE category='ENGLAND' AND subcategory='WEST_MIDLANDS' ORDER BY location ASC;"));}
            case "London" -> {return new LinkedHashSet<>(Network.getInstance().globalSQL.getStringList("SELECT location FROM location_data WHERE category='ENGLAND' AND subcategory='LONDON' ORDER BY location ASC;"));}
            case "East Midlands" -> {return new LinkedHashSet<>(Network.getInstance().globalSQL.getStringList("SELECT location FROM location_data WHERE category='ENGLAND' AND subcategory='EAST_MIDLANDS' ORDER BY location ASC;"));}
            case "East of England" -> {return new LinkedHashSet<>(Network.getInstance().globalSQL.getStringList("SELECT location FROM location_data WHERE category='ENGLAND' AND subcategory='EAST_OF_ENGLAND' ORDER BY location ASC;"));}
            case "North West" -> {return new LinkedHashSet<>(Network.getInstance().globalSQL.getStringList("SELECT location FROM location_data WHERE category='ENGLAND' AND subcategory='NORTH_WEST' ORDER BY location ASC;"));}
            case "North East" -> {return new LinkedHashSet<>(Network.getInstance().globalSQL.getStringList("SELECT location FROM location_data WHERE category='ENGLAND' AND subcategory='NORTH_EAST' ORDER BY location ASC;"));}
            case "South East" -> {return new LinkedHashSet<>(Network.getInstance().globalSQL.getStringList("SELECT location FROM location_data WHERE category='ENGLAND' AND subcategory='SOUTH_EAST' ORDER BY location ASC;"));}
            case "South West" -> {return new LinkedHashSet<>(Network.getInstance().globalSQL.getStringList("SELECT location FROM location_data WHERE category='ENGLAND' AND subcategory='SOUTH_WEST' ORDER BY location ASC;"));}

            //Nearby locations to the player.
            case "Nearby" -> {return getNearbyLocations();}

            //This is for the 'search' case where the location is based on the user input.
            default -> {return searchLocations();}

        }
    }

    private LinkedHashSet<String> getNearbyLocations() {

        Location l = u.player.getLocation();
        String worldName = l.getWorld().getName();

        //Check if world is in plotsystem.
        if (Network.getInstance().plotSQL.hasRow("SELECT name FROM location_data WHERE name='" + worldName + "';")) {

            //Add coordinate transformation.
            l = new Location(
                    Bukkit.getWorld(worldName),
                    u.player.getLocation().getX() - Network.getInstance().plotSQL.getInt("SELECT xTransform FROM location_data WHERE name='" + worldName + "';"),
                    u.player.getLocation().getY(),
                    u.player.getLocation().getZ() - Network.getInstance().plotSQL.getInt("SELECT zTransform FROM location_data WHERE name='" + worldName + "';"),
                    u.player.getLocation().getYaw(),
                    u.player.getLocation().getPitch()
            );

        }

        return new LinkedHashSet<>(Network.getInstance().globalSQL.getStringList("SELECT location_data.location FROM location_data INNER JOIN coordinates ON location_data.coordinate=coordinates.id " +
                "WHERE ((((coordinates.x/1000)-" + (l.getX() / 1000) + ")*((coordinates.x/1000)-" + (l.getX() / 1000) + ")) + " +
                "(((coordinates.z/1000)-" + (l.getZ() / 1000) + ")*((coordinates.z/1000)-" + (l.getZ() / 1000) + "))) < " +
                (Network.getInstance().getConfig().getInt("navigation_radius") * Network.getInstance().getConfig().getInt("navigation_radius")) +
                " ORDER BY ((((coordinates.x/1000)-" + (l.getX() / 1000) + ")*((coordinates.x/1000)-" + (l.getX() / 1000) + ")) + " +
                "(((coordinates.z/1000)-" + (l.getZ() / 1000) + ")*((coordinates.z/1000)-" + (l.getZ() / 1000) + "))) ASC;"));

    }

    private LinkedHashSet<String> searchLocations() {

        //Search for locations that include this phrase.
        ArrayList<String> locations = Network.getInstance().globalSQL.getStringList("SELECT location FROM location_data WHERE location LIKE '%" + type + "%';");

        //Also search for any regions or counties.
        for (Counties county : Counties.values()) {
            if (StringUtils.containsIgnoreCase(county.label, type)) {

                locations.addAll(Network.getInstance().globalSQL.getStringList("SELECT location FROM location_data WHERE subcategory='" + county.region + "';"));

            }
        }

        for (Categories category : Categories.values()) {
            if (StringUtils.containsIgnoreCase(category.label, type)) {

                locations.addAll(Network.getInstance().globalSQL.getStringList("SELECT location FROM location_data WHERE category='" + category + "';"));

            }
        }

        locations.sort(Comparator.naturalOrder());

        return new LinkedHashSet<>(locations);

    }

    //Function to get the gui for the return button.
    private Gui getReturnGui() {
        if (returnMenu.equals("England")) {

            return new EnglandMenu();

        } else {

            return new ExploreGui(u);

        }
    }

    public LocationMenu(String title, NetworkUser u, String type, String returnMenu) {

        super(45, Component.text(title, NamedTextColor.AQUA, TextDecoration.BOLD));

        this.u = u;

        this.type = type;
        this.returnMenu = returnMenu;

        this.locations = getLocations();

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

                            String worldName = Network.getInstance().globalSQL.getString("SELECT world FROM coordinates WHERE id=" + coordinate_id + ";");

                            //Check if world is in plotsystem.
                            if (Network.getInstance().plotSQL.hasRow("SELECT name FROM location_data WHERE name='" + worldName + "';")) {

                                //Add coordinate transformation.
                                l = new Location(
                                        Bukkit.getWorld(worldName),
                                        l.getX() + Network.getInstance().plotSQL.getInt("SELECT xTransform FROM location_data WHERE name='" + worldName + "';"),
                                        l.getY(),
                                        l.getZ() + Network.getInstance().plotSQL.getInt("SELECT zTransform FROM location_data WHERE name='" + worldName + "';"),
                                        l.getYaw(),
                                        l.getPitch()
                                );

                            }

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
                    u.mainGui = getReturnGui();
                    u.mainGui.open(u);

                });
    }

    public void refresh() {

        this.clearGui();

        //Refresh the location list.
        locations = getLocations();

        //Check if page has content.
        //Else set it to the maximum possible.
        if (Math.ceil(locations.size()/21.0) < page) {
            page = locations.size()/21;
        }

        createGui();

    }

    public boolean isEmpty() {
        return locations.isEmpty();
    }
}
