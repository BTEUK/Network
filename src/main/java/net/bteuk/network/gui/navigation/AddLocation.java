package net.bteuk.network.gui.navigation;

import lombok.Getter;
import lombok.Setter;
import net.bteuk.network.Network;
import net.bteuk.network.commands.navigation.Back;
import net.bteuk.network.eventing.events.EventManager;
import net.bteuk.network.gui.Gui;
import net.bteuk.network.sql.GlobalSQL;
import net.bteuk.network.gui.staff.LocationRequests;
import net.bteuk.network.gui.staff.StaffGui;
import net.bteuk.network.utils.NetworkUser;
import net.bteuk.network.utils.SwitchServer;
import net.bteuk.network.utils.Utils;
import net.bteuk.network.eventing.listeners.navigation.LocationNameListener;
import net.bteuk.network.utils.enums.AddLocationType;
import net.bteuk.network.utils.enums.Category;
import net.bteuk.network.utils.enums.ServerType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.Arrays;

import static net.bteuk.network.utils.Constants.SERVER_NAME;
import static net.bteuk.network.utils.Constants.SERVER_TYPE;

public class AddLocation extends Gui {

    private String old_name;
    @Setter
    private String name;

    @Getter
    private Category category = Category.ENGLAND;

    @Setter
    private String subcategory = "None";
    private int coordinate_id;

    public SelectSubcategory selectSubcategory;

    private LocationNameListener locationNameListener;

    @Getter
    private final AddLocationType type;

    private GlobalSQL globalSQL;

    public AddLocation(AddLocationType type) {

        super(27, Component.text(type.label + " Location", NamedTextColor.AQUA, TextDecoration.BOLD));

        this.type = type;

        createGui();

    }

    //This is used when location details need to be updated.
    public AddLocation(AddLocationType type, String name, int coordinate_id, Category category, String subcategory) {

        super(27, Component.text(type.label + " Location", NamedTextColor.AQUA, TextDecoration.BOLD));

        //Set name.
        this.old_name = name;
        this.name = name;

        //Set coordinate id.
        this.coordinate_id = coordinate_id;

        //Set category from input.
        this.category = category;

        if (subcategory != null) {
            this.subcategory = subcategory;
        }

        this.type = type;

        createGui();

    }

    private void createGui() {

        //Get globalSQL.
        globalSQL = Network.getInstance().getGlobalSQL();

        //Set/edit name.
        if (name != null) {
            setItem(11, Utils.createItem(Material.SPRUCE_SIGN, 1,
                            Utils.title("Update Location Name"),
                            Utils.line("Edit the location name."),
                            Utils.line("The current name is: ")
                                    .append(Component.text(name, NamedTextColor.GRAY)),
                            Utils.line("You can type the name in chat.")),

                    u -> {

                        if (locationNameListener != null) {
                            locationNameListener.unregister();
                        }

                        locationNameListener = new LocationNameListener(u.player, this);
                        u.player.sendMessage(Utils.success("Write the location name in chat, the first message counts. You can include spaces in the name."));
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
                        u.player.sendMessage(Utils.success("Write the location name in chat, the first message counts. You can include spaces in the name."));
                        u.player.closeInventory();

                    });
        }

        //Select category.
        setItem(15, Utils.createItem(Material.MAP, 1,
                        Utils.title("Select Category"),
                        Utils.line("Click to cycle through categories."),
                        Utils.line("Current category is: ")
                                .append(Component.text(category.getLabel(), NamedTextColor.GRAY)),
                        Utils.line("Available categories are:"),
                        Utils.line("England, Scotland, Wales, Northern Ireland and Other")),

                u -> {

                    //Cycle to next category and refresh the gui.
                    Category[] categories = Arrays.stream(Category.values()).filter(Category::isSelectable).toArray(Category[]::new);
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

        //Select subcategory.
        setItem(16, Utils.createItem(Material.COMPASS, 1,
                        Utils.title("Select Subcategory"),
                        Utils.line("Click to select a subcategory."),
                        Utils.line("This is optional, you can"),
                        Utils.line("leave it as 'None' if there"),
                        Utils.line("are no suitable options"),
                        Utils.line("Current subcategory is: ")
                                .append(Component.text(subcategory, NamedTextColor.GRAY))),

                u -> {

                    //Open select county menu.
                    selectSubcategory = new SelectSubcategory(this);
                    selectSubcategory.open(u);

                });

        //Teleport location update.
        //Teleport to location.
        //These options are not needed for adding a location as that is based on the players current location.
        if (type != AddLocationType.ADD) {

            //Teleport to location.
            setItem(21, Utils.createItem(Material.ENDER_PEARL, 1,
                            Utils.title("Teleport to Location"),
                            Utils.line("Click to teleport to the location.")),
                    u -> {

                        //Close inventory.
                        u.player.closeInventory();

                        //If location is on this server teleport the player, else switch server.
                        //Teleport to location.
                        String server = globalSQL.getString("SELECT server FROM coordinates WHERE id=" + coordinate_id + ";");
                        if (SERVER_NAME.equalsIgnoreCase(server)) {
                            //Get location from coordinate id.
                            Location l = globalSQL.getLocation(coordinate_id);

                            //Set current location for /back
                            Back.setPreviousCoordinate(u.player.getUniqueId().toString(), u.player.getLocation());

                            u.player.teleport(l);
                        } else {
                            //Create teleport event and switch server.
                            EventManager.createTeleportEvent(true, u.player.getUniqueId().toString(), "network", "teleport location_request " + name, u.player.getLocation());
                            SwitchServer.switchServer(u.player, server);
                        }

                    });

            //Update teleport location.
            setItem(23, Utils.createItem(Material.ACACIA_BOAT, 1,
                            Utils.title("Update teleport location"),
                            Utils.line("Click to set the teleport"),
                            Utils.line("location to your current position.")),
                    u -> {

                        Location l = u.player.getLocation();

                        //If server is plotsystem add the necessary coordinate transformation.
                        if (SERVER_TYPE == ServerType.PLOT) {

                            String worldName = u.player.getLocation().getWorld().getName();

                            //If location exists.
                            if (Network.getInstance().getPlotSQL().hasRow("SELECT name FROM location_data WHERE name='" + worldName + "';")) {

                                //Add coordinate transformation.
                                l = new Location(
                                        l.getWorld(),
                                        l.getX() - Network.getInstance().getPlotSQL().getInt("SELECT xTransform FROM location_data WHERE name='" + worldName + "';"),
                                        l.getY(),
                                        l.getZ() - Network.getInstance().getPlotSQL().getInt("SELECT zTransform FROM location_data WHERE name='" + worldName + "';"),
                                        l.getYaw(),
                                        l.getPitch()
                                );

                            }
                        }


                        globalSQL.updateCoordinate(coordinate_id, l);
                        u.player.sendMessage(Utils.success("Updated location to your current position."));

                    });
        }

        /*
        Add location
        Accept if created by reviewer.
        Add location to database
        Add request to database
        Notify reviewers if online using reviewer chat channel
         */
        if (type == AddLocationType.ADD) {
            setItem(13, Utils.createItem(Material.EMERALD, 1,
                            Utils.title("Add Location"),
                            Utils.line("Your location will be added to the exploration menu."),
                            Utils.line("However, it must first be accepted by a reviewer.")),

                    u -> {

                        //Checks:
                        //Name has been set
                        if (name == null) {

                            u.player.sendMessage(Utils.error("You have not set a name for the location."));
                            u.player.closeInventory();

                            //Name isn't duplicate (location or subcategory.
                        } else if (globalSQL.hasRow("SELECT location FROM location_data WHERE location='" + name + "';") || globalSQL.hasRow("SELECT name FROM location_subcategory WHERE name='" + name + "';")) {

                            u.player.sendMessage(Utils.error("A location or subcategory with this name already exists."));
                            u.player.closeInventory();

                        } else if (globalSQL.hasRow("SELECT location FROM location_requests WHERE location = '" + name + "';")) {

                            u.player.sendMessage(Utils.error("A location with this name has already been requested."));
                            u.player.closeInventory();

                        } else {

                            Location l = u.player.getLocation();

                            //If server is plotsystem add the necessary coordinate transformation.
                            if (SERVER_TYPE == ServerType.PLOT) {

                                String worldName = u.player.getLocation().getWorld().getName();

                                //If location exists.
                                if (Network.getInstance().getPlotSQL().hasRow("SELECT name FROM location_data WHERE name='" + worldName + "';")) {

                                    //Add coordinate transformation.
                                    l = new Location(
                                            l.getWorld(),
                                            l.getX() - Network.getInstance().getPlotSQL().getInt("SELECT xTransform FROM location_data WHERE name='" + worldName + "';"),
                                            l.getY(),
                                            l.getZ() - Network.getInstance().getPlotSQL().getInt("SELECT zTransform FROM location_data WHERE name='" + worldName + "';"),
                                            l.getYaw(),
                                            l.getPitch()
                                    );

                                }
                            }

                            //Create location coordinate.
                            coordinate_id = globalSQL.addCoordinate(l);

                            if (u.player.hasPermission("uknet.navigation.add")) {

                                addLocation(u);

                            } else {

                                requestLocation(u);

                            }
                        }

                    });
        } else if (type == AddLocationType.UPDATE) {
            setItem(4, Utils.createItem(Material.EMERALD, 1,
                            Utils.title("Update Location"),
                            Utils.line("The location will be updated"),
                            Utils.line("with the selected settings.")),

                    u -> {

                        //Checks:
                        //Name isn't duplicate
                        if (globalSQL.hasRow("SELECT location FROM location_data WHERE location='" + name + " AND coordinate<>" + coordinate_id + "';") || globalSQL.hasRow("SELECT name FROM location_subcategory WHERE name='" + name + "';")) {

                            u.player.sendMessage(Utils.error("Another location or subcategory with this name already exists."));
                            u.player.closeInventory();

                        } else if (globalSQL.hasRow("SELECT location FROM location_requests WHERE location = '" + name + " AND coordinate<>" + coordinate_id + "';")) {

                            u.player.sendMessage(Utils.error("A location with this name has already been requested."));
                            u.player.closeInventory();

                        } else {

                            updateLocation(u);

                        }
                    });
        } else if (type == AddLocationType.REVIEW) {

            //Accept request.
            setItem(3, Utils.createItem(Material.LIME_CONCRETE, 1,
                            Utils.title("Accept Location Request"),
                            Utils.line("Location will be added to"),
                            Utils.line("the exploration menu as well as"),
                            Utils.line("the list of warps.")),
                    u -> {

                        //Accept request.
                        acceptRequest(u);

                        //Delete gui and return to previous menu.
                        this.delete();

                        u.staffGui = new LocationRequests();
                        u.staffGui.open(u);
                    });

            //Deny request.
            setItem(5, Utils.createItem(Material.RED_CONCRETE, 1,
                            Utils.title("Deny Location Request"),
                            Utils.line("Location request will be denied.")),
                    u -> {

                        //Delete request.
                        globalSQL.update("DELETE FROM location_requests WHERE location='" + name + "';");

                        //Notify player.
                        u.player.sendMessage(Utils.error("Denied location request ")
                                .append(Component.text(name, NamedTextColor.DARK_RED)));

                        //Delete gui and return to previous menu.
                        this.delete();

                        u.staffGui = new LocationRequests();
                        u.staffGui.open(u);

                    });
        }

        //Return
        if (type == AddLocationType.ADD) {
            setItem(26, Utils.createItem(Material.SPRUCE_DOOR, 1,
                            Utils.title("Return"),
                            Utils.line("Open the explore menu.")),
                    u ->

                    {

                        //Delete this gui.
                        this.delete();

                        //Switch to navigation menu.
                        u.mainGui = new ExploreGui(u);
                        u.mainGui.open(u);

                    });
        } else if (type == AddLocationType.REVIEW) {
            setItem(26, Utils.createItem(Material.SPRUCE_DOOR, 1,
                            Utils.title("Return"),
                            Utils.line("Return to location requests.")),
                    u -> {

                        //Delete gui and return to previous menu.
                        this.delete();

                        u.staffGui = new LocationRequests();
                        u.staffGui.open(u);

                    });
        } else {
            setItem(26, Utils.createItem(Material.SPRUCE_DOOR, 1,
                            Utils.title("Return"),
                            Utils.line("Return to staff menu.")),
                    u -> {

                        //Delete gui and return to previous menu.
                        this.delete();

                        u.staffGui = new StaffGui(u);
                        u.staffGui.open(u);

                    });
        }

    }

    private void addLocation(NetworkUser u) {

        // If the subcategory has been set, find the subcategory id.
        int subcategory_id = 0;
        if (!subcategory.equals("None")) {
            subcategory_id = globalSQL.getInt("SELECT id FROM location_subcategory WHERE name='" + subcategory + "';");
            if (subcategory_id == 0) {
                u.player.sendMessage(Utils.error("The subcategory no longer exists, adding location without subcategory."));
            }
        }

        if (subcategory_id == 0) {
            globalSQL.update("INSERT INTO location_data(location,category,coordinate) " +
                    "VALUES('" + name + "','" + category + "'," + coordinate_id + ");");
        } else {
            globalSQL.update("INSERT INTO location_data(location,category,subcategory,coordinate) " +
                    "VALUES('" + name + "','" + category + "'," + subcategory_id + "," + coordinate_id + ");");
        }

        u.player.sendMessage(Utils.success("Location ")
                .append(Component.text(name, NamedTextColor.DARK_AQUA))
                .append(Utils.success(" added to exploration menu.")));

        //Delete gui.
        this.delete();
        u.mainGui = null;

        u.mainGui = new ExploreGui(u);
        u.player.closeInventory();
    }

    public void updateLocation(NetworkUser u) {

        // If the subcategory has been set, find the subcategory id.
        int subcategory_id = 0;
        if (!subcategory.equals("None")) {
            subcategory_id = globalSQL.getInt("SELECT id FROM location_subcategory WHERE name='" + subcategory + "';");
            if (subcategory_id == 0) {
                u.player.sendMessage(Utils.error("The subcategory no longer exists, adding location without subcategory."));
            }
        }

        if (subcategory_id == 0) {
            globalSQL.update("UPDATE location_data SET location='" + name + "',category='" + category + "' WHERE location='" + old_name + "';");
        } else {
            globalSQL.update("UPDATE location_data SET location='" + name + "',category='" + category + "',subcategory=" + subcategory_id + " WHERE location='" + old_name + "';");
        }

        u.player.sendMessage(Utils.success("Updated location ")
                .append(Component.text(name, NamedTextColor.DARK_AQUA)));

        //Delete gui.
        this.delete();
        u.staffGui = null;

        u.player.closeInventory();

    }

    public void acceptRequest(NetworkUser u) {

        //Delete request.
        globalSQL.update("DELETE FROM location_requests WHERE location='" + old_name + "';");

        // If the subcategory has been set, find the subcategory id.
        int subcategory_id = 0;
        if (!subcategory.equals("None")) {
            subcategory_id = globalSQL.getInt("SELECT id FROM location_subcategory WHERE name='" + subcategory + "';");
            if (subcategory_id == 0) {
                u.player.sendMessage(Utils.error("The subcategory no longer exists, adding location without subcategory."));
            }
        }

        //Add location.
        if (subcategory_id == 0) {
            globalSQL.update("INSERT INTO location_data(location,category,coordinate) " +
                    "VALUES('" + name + "','" + category + "'," + coordinate_id + ");");
        } else {
            globalSQL.update("INSERT INTO location_data(location,category,subcategory,coordinate) " +
                    "VALUES('" + name + "','" + category + "'," + subcategory_id + "," + coordinate_id + ");");
        }

        //Notify player.
        u.player.sendMessage(Utils.success("Accepted location request ")
                .append(Component.text(name, NamedTextColor.DARK_AQUA)));

    }

    public void requestLocation(NetworkUser u) {

        // If the subcategory has been set, find the subcategory id.
        int subcategory_id = 0;
        if (!subcategory.equals("None")) {
            subcategory_id = globalSQL.getInt("SELECT id FROM location_subcategory WHERE name='" + subcategory + "';");
            if (subcategory_id == 0) {
                u.player.sendMessage(Utils.error("The subcategory no longer exists, adding location without subcategory."));
            }
        }

        if (subcategory_id == 0) {
            globalSQL.update("INSERT INTO location_requests(location,category,coordinate) " +
                    "VALUES('" + name + "','" + category + "'," + coordinate_id + ");");
        } else {
            globalSQL.update("INSERT INTO location_requests(location,category,subcategory,coordinate) " +
                    "VALUES('" + name + "','" + category + "'," + subcategory_id + "," + coordinate_id + ");");
        }

        //Notify reviewers.
        Network.getInstance().chat.broadcastMessage(Utils.success("A new location has been requested."), "uknet:reviewer");

        u.player.sendMessage(Utils.success("Location ")
                .append(Component.text(name, NamedTextColor.DARK_AQUA))
                .append(Utils.success(" requested.")));

        //Delete gui.
        this.delete();
        u.mainGui = null;

        u.mainGui = new ExploreGui(u);
        u.player.closeInventory();
    }

    public void refresh() {

        this.clearGui();
        createGui();

    }

    //Override delete method to make sure selectCounty is also deleted.
    @Override
    public void delete() {
        super.delete();

        //If selectCounty exists, delete it.
        if (selectSubcategory != null) {
            selectSubcategory.delete();
            selectSubcategory = null;
        }
    }
}
