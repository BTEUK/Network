package me.bteuk.network.gui.navigation;

import me.bteuk.network.Network;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.enums.Categories;
import me.bteuk.network.utils.enums.Counties;
import me.bteuk.network.utils.enums.Regions;
import me.bteuk.network.utils.navigation.LocationNameListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

public class AddLocation extends Gui {

    private String name;
    private Categories category;
    private Counties county;

    private LocationNameListener locationNameListener;

    public AddLocation() {

        super(27, Component.text("Add Location", NamedTextColor.AQUA, TextDecoration.BOLD));

        //Set default category to England, since it is the most common.
        category = Categories.ENGLAND;

        //Set default county to London, since it is the most common.
        county = Counties.GREATER_LONDON;

        createGui();

    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCounty(Counties county) {
        this.county = county;
    }

    private void createGui() {

        //Set/edit name.
        if (name != null) {
            setItem(11, Utils.createItem(Material.SPRUCE_SIGN, 1,
                            Utils.chat("&b&lUpdate Location Name"),
                            Utils.chat("&fEdit the location name."),
                            Utils.chat("&fThe current name is '" + name + "'."),
                            Utils.chat("&fYou can type the name in chat.")),

                    u -> {

                        if (locationNameListener != null) {
                            locationNameListener.unregister();
                        }

                        locationNameListener = new LocationNameListener(u.player, this);
                        u.player.sendMessage(Utils.chat("&aWrite the location name in chat, the first message counts. You can include spaces in the name."));
                        u.player.closeInventory();

                    });
        } else {
            setItem(11, Utils.createItem(Material.SPRUCE_SIGN, 1,
                            Utils.title("Set Location Name"),
                            Utils.line("Add the location name."),
                            Utils.line("You can type the name in chat.")),

                    u -> {

                        if (locationNameListener != null) {
                            locationNameListener.unregister();
                        }

                        locationNameListener = new LocationNameListener(u.player, this);
                        u.player.sendMessage(Utils.chat("&aWrite the location name in chat, the first message counts. You can include spaces in the name."));
                        u.player.closeInventory();

                    });
        }

        //Select category.
        setItem(15, Utils.createItem(Material.MAP, 1,
                        Utils.title("Select Category"),
                        Utils.line("Click to cycle through categories."),
                        Utils.line("Current category is:"),
                        Utils.chat("&b&7" + category.label),
                        Utils.line("Available categories are:"),
                        Utils.line("England, Scotland, Wales, Northern Ireland"),
                        Utils.line("and Overseas Territories & Crown Dependencies")),

                u -> {

                    //Cycle to next category and refresh the gui.
                    Categories[] categories = Categories.values();
                    for (int i = 0; i < categories.length; i++) {
                        if (categories[i] == category) {
                            //Get next.
                            if (i == categories.length - 1) {
                                category = categories[0];
                            } else {
                                category = categories[i + 1];
                            }
                            break;
                        }
                    }

                    //Update gui.
                    this.refresh();
                    u.player.getOpenInventory().getTopInventory().setContents(this.getInventory().getContents());

                });

        //If category is England, select county.
        //Has to have it's own menu since there's too many option to cycle through.
        if (category == Categories.ENGLAND) {

            setItem(16, Utils.createItem(Material.COMPASS, 1,
                            Utils.title("Select County"),
                            Utils.line("Click to select a county."),
                            Utils.line("Current county is:"),
                            Utils.chat("&b&7" + county)),

                    u -> {

                        //Open select county menu.
                        //Don't delete this gui however.
                        u.selectCounty = new SelectCounty();
                        u.selectCounty.open(u);

                    });
        }

        /*
        Add location
        Accept if created by reviewer.
        Add location to database
        Add request to database
        Notify reviewers if online using reviewer chat channel
         */
        setItem(13, Utils.createItem(Material.EMERALD, 1,
                        Utils.title("Add Location"),
                        Utils.line("Your location will be added to the exploration menu."),
                        Utils.line("However, it must first be accepted by a reviewer.")),

                u -> {

                    //Get globalSQL.
                    GlobalSQL globalSQL = Network.getInstance().globalSQL;

                    //Checks:
                    //Name has been set
                    if (name == null) {

                        u.player.sendMessage(Utils.error("You have not set a name for the location."));
                        u.player.closeInventory();

                        //Name isn't duplicate
                    } else if (globalSQL.hasRow("SELECT location FROM location_data WHERE location='" + name)) {

                        u.player.sendMessage(Utils.error("A location with this name already exists."));
                        u.player.closeInventory();

                    } else if (globalSQL.hasRow("SELECT location FROM location_requests WHERE location = '" + name + "';")) {

                        u.player.sendMessage(Utils.error("A location with this name has already been requested."));
                        u.player.closeInventory();

                    } else {

                        //Create location coordinate.
                        int coordinate_id = Network.getInstance().globalSQL.addCoordinate(u.player.getLocation());

                        //If category is england.
                        if (category == Categories.ENGLAND) {
                            //If player is reviewer, skip review.
                            if (u.player.hasPermission("group.reviewer")) {

                                addLocation(u, globalSQL, name, category, county.region, coordinate_id);

                            } else {

                                requestLocation(u, globalSQL, name, category, county.region, coordinate_id);

                            }
                        } else {
                            //If player is reviewer, skip review.
                            if (u.player.hasPermission("group.reviewer")) {

                                addLocation(u, globalSQL, name, category, null, coordinate_id);

                            } else {

                                requestLocation(u, globalSQL, name, category, null, coordinate_id);

                            }
                        }
                    }

                });

        //Return
        setItem(26, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.chat("&b&lReturn"),
                        Utils.chat("&fOpen the explore menu.")),
                u ->

                {

                    //Delete this gui.
                    this.delete();
                    u.addLocation = null;

                    //Switch to navigation menu.
                    u.exploreGui = new ExploreGui(u);
                    u.exploreGui.open(u);

                });
    }

    public void addLocation(NetworkUser u, GlobalSQL globalSQL, String name, Categories category, Regions region, int coordinate_id) {

        if (region == null) {
            globalSQL.update("INSERT INTO location_data(location,category,coordinate) " +
                    "VALUES('" + name + "','" + category + "','" + coordinate_id + ";");
        } else {
            globalSQL.update("INSERT INTO location_requests(location,category,subcategory,coordinate) " +
                    "VALUES('" + name + "','" + category + "','" + county.region + "," + coordinate_id + ";");
        }

        u.player.sendMessage(Utils.success("Location &7" + name + " &aadded to exploration menu."));

        //Delete gui.
        this.delete();
        u.addLocation = null;

        u.exploreGui = new ExploreGui(u);
        u.player.closeInventory();

    }

    public void requestLocation(NetworkUser u, GlobalSQL globalSQL, String name, Categories category, Regions region, int coordinate_id) {

        if (region ==  null) {
            globalSQL.update("INSERT INTO location_requests(location,category,coordinate) " +
                    "VALUES('" + name + "','" + category + "','" + coordinate_id + ";");
        } else {
            globalSQL.update("INSERT INTO location_requests(location,category,subcategory,coordinate) " +
                    "VALUES('" + name + "','" + category + "','" + county.region + "," + coordinate_id + ";");
        }

        //Notify reviewers.
        Network.getInstance().chat.broadcastMessage("&aA new location has been requested.", "uknet:reviewer");

        u.player.sendMessage(Utils.success("Location &7" + name + " &arequested."));

        //Delete gui.
        this.delete();
        u.addLocation = null;

        u.exploreGui = new ExploreGui(u);
        u.player.closeInventory();
    }

    public void refresh() {

        this.clearGui();
        createGui();

    }
}
