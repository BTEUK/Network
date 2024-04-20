package net.bteuk.network.lobby;

import eu.decentsoftware.holograms.api.holograms.Hologram;
import io.papermc.lib.PaperLib;
import net.bteuk.network.Network;
import net.bteuk.network.eventing.events.EventManager;
import net.bteuk.network.gui.navigation.LocationMenu;
import net.bteuk.network.utils.Holograms;
import net.bteuk.network.utils.NetworkUser;
import net.bteuk.network.utils.SwitchServer;
import net.bteuk.network.utils.Utils;
import net.bteuk.network.utils.enums.Category;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static net.bteuk.network.utils.Constants.LOGGER;
import static net.bteuk.network.utils.Constants.SERVER_NAME;
import static net.bteuk.network.utils.NetworkConfig.CONFIG;

/**
 * This class manages the ingame map in the lobby.
 * When a player enters the map area they will have an item in the 5th slot of their hotbar.
 * Clicking on the item will list all nearby locations (radius of 50km) to the players position.
 * Locations will be sorted by distance to the player.
 */
public class Map extends AbstractReloadableComponent {

    private static final String AQUA = "&b&l";

    private static final String GOLD = "&6&l";

    private final Network instance;

    /**
     * The server that has the physical map.
     */
    private String server;

    /**
     * Coordinates of the map.
     */
    private Location map_location;

    /**
     * HashMap of the holograms
     * Key: Hologram
     * Value: Click action for the hologram
     */
    private HashMap<Hologram, HologramClickAction> holograms;

    private HologramClickEvent hologramClickEvent;

    public Map(Network instance) {
        this.instance = instance;
    }

    /**
     * Loads the map using the config from map.yml
     * If there are issues with the config, then loading will be unsuccessful and the map will not be enabled.
     */
    @Override
    public void load() {
        if (isEnabled()) {
            LOGGER.warning("An attempt was made to load the Map while it is already enabled.");
            return;
        }

        // Check if the map is enabled.
        if (!CONFIG.getBoolean("map.enabled")) {
            setEnabled(false);
            return;
        }

        // Get the server of the map, this is important in deciding which features to enable.
        // If the server is not
        server = CONFIG.getString("map.server");
        if (server == null || !instance.getGlobalSQL().hasRow("SELECT * FROM server_data WHERE name='" + server + "';")) {
            setEnabled(false);
            LOGGER.warning("The map has been enabled without a valid server, disabling the map.");
            return;
        }

        // Set the location of the map.
        // If the map is on this server the world must exist.
        // Set the coordinates of the location first, the server is not relevant for this part.
        map_location = new Location(null, CONFIG.getDouble("map.location.x", 0), CONFIG.getDouble("map.location.y", 0),
                CONFIG.getDouble("map.location.z", 0), (float) CONFIG.getDouble("map.location.yaw", 0), (float) CONFIG.getDouble("map.location.pitch", 0));
        if (Objects.equals(SERVER_NAME, server)) {
            String world = CONFIG.getString("map.location.world");
            if (world == null || Bukkit.getWorld(world) == null) {
                setEnabled(false);
                LOGGER.warning("The map world does not exist on this server, disabling the map.");
                return;
            }
            // Set the world, the coordinates have already been set.
            map_location.setWorld(Bukkit.getWorld(world));

            // Register the hologram click event.
            hologramClickEvent = new HologramClickEvent(instance, this);

            // Load the map markers and linked location.
            try {
                loadMarkers();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        // Enable the map command.
        new MapCommand(instance, this, "map");

        setEnabled(true);
    }

    /**
     * Unloads the map if currently enabled.
     */
    @Override
    public void unload() {
        if (isEnabled()) {
            // Remove all visible markers.
            holograms.forEach((hologram, clickAction) -> hologram.delete());
            holograms.clear();

            // Disable the hologram click event.
            hologramClickEvent.unregister();
            hologramClickEvent = null;
            setEnabled(false);
        }
    }

    public HologramClickAction getHologramClickAction(Hologram hologram) {
        return holograms.get(hologram);
    }

    protected void teleport(Player p) {
        // If the map is on this server teleport the player directly, else switch server first.
        if (Objects.equals(SERVER_NAME, server)) {
            PaperLib.teleportAsync(p, map_location);
        } else {
            // Create teleport event.
            EventManager.createTeleportEvent(true, p.getUniqueId().toString(), "network", String.format("teleport %f %f %f %f %f",
                            map_location.getX(), map_location.getY(), map_location.getZ(), map_location.getYaw(), map_location.getPitch()),
                    "&aTeleporting to the map.", p.getLocation());

            // Switch server.
            SwitchServer.switchServer(p, server);
        }
    }

    /**
     * Add a new marker to the map.
     * @param l location where the marker should be placed
     * @param marker the name of the marker, must be the name of a location or subcategory
     * @return feedback for the player
     */
    protected Component addMarker(Location l, String marker) {

        // Adjust the location to have the correct y.
        Location marker_location = l.clone();
        marker_location.setY(l.getY() + 0.5);

        // Check of the name is valid.
        if (instance.getGlobalSQL().hasRow("SELECT location FROM location_data WHERE location='" + marker + "';")) {
            // Check if marker does not already exist.
            if (instance.getGlobalSQL().hasRow("SELECT location FROM location_marker WHERE location='" + marker + "';")) {
                return Component.text(marker, NamedTextColor.DARK_RED).append(Utils.error(" already exists on the map."));
            }
            // Create coordinate id.
            int coordinate_id = instance.getGlobalSQL().addCoordinate(marker_location);
            // Add marker.
            instance.getGlobalSQL().update("INSERT INTO location_marker(location,coordinate_id) VALUES('" + marker + "'," + coordinate_id + ");");
            reload();
            return Utils.success("Added marker for location ").append(Component.text(marker, NamedTextColor.DARK_AQUA));
        } else {
            // Get the subcategory id.
            int subcategory_id = instance.getGlobalSQL().getInt("SELECT id FROM location_subcategory WHERE name='" + marker + "';");
            if (subcategory_id == 0) {
                return Component.text(marker, NamedTextColor.DARK_RED).append(Utils.error(" is not a valid location or subcategory name."));
            }
            // Check if marker does not already exist.
            if (instance.getGlobalSQL().hasRow("SELECT subcategory FROM location_marker WHERE subcategory='" + subcategory_id + "';")) {
                return Component.text(marker, NamedTextColor.DARK_RED).append(Utils.error(" already exists on the map."));
            }
            // Create coordinate id.
            int coordinate_id = instance.getGlobalSQL().addCoordinate(marker_location);
            // Add marker.
            instance.getGlobalSQL().update("INSERT INTO location_marker(subcategory,coordinate_id) VALUES('" + subcategory_id + "'," + coordinate_id + ");");
            reload();
            return Utils.success("Added marker for subcategory ").append(Component.text(marker, NamedTextColor.DARK_AQUA));
        }
    }

    protected Component removeMarker(String marker) {
        // Check if the marker is a valid location.
        if (instance.getGlobalSQL().hasRow("SELECT location FROM location_marker WHERE location='" + marker + "';")) {
            // Remove coordinate id.
            int coordinate_id = instance.getGlobalSQL().getInt("SELECT coordinate_id FROM location_marker WHERE location='" + marker + "';");
            // Remove marker of location.
            instance.getGlobalSQL().update("DELETE FROM location_marker WHERE location='" + marker + "';");
            instance.getGlobalSQL().update("DELETE FROM coordinates WHERE id=" + coordinate_id);
            reload();
            return Utils.success("Removed marker for location ").append(Component.text(marker, NamedTextColor.DARK_AQUA));
        } else {
            // Else check if it's a valid subcategory.
            int subcategory_id = instance.getGlobalSQL().getInt("SELECT id FROM location_subcategory WHERE name='" + marker + "';");
            if (subcategory_id == 0) {
                return Component.text(marker, NamedTextColor.DARK_RED).append(Utils.error(" is not a valid marker."));
            }
            // Remove coordinate id.
            int coordinate_id = instance.getGlobalSQL().getInt("SELECT coordinate_id FROM location_marker WHERE subcategory=" + subcategory_id + ";");
            // Remove marker of location.
            instance.getGlobalSQL().update("DELETE FROM location_marker WHERE subcategory=" + subcategory_id + ";");
            instance.getGlobalSQL().update("DELETE FROM coordinates WHERE id=" + coordinate_id);
            reload();
            return Utils.success("Removed marker for subcategory ").append(Component.text(marker, NamedTextColor.DARK_AQUA));
        }
    }

    private void loadMarkers() throws SQLException {

        // Create the holograms map.
        holograms = new HashMap<>();

        // Retrieve all the markers from the database.
        List<Integer> markers = instance.getGlobalSQL().getIntList("SELECT id FROM location_marker");

        markers.forEach(id -> {
            // Get the name
            String location = instance.getGlobalSQL().getString("SELECT location FROM location_marker WHERE id=" + id + ";");
            int coordinate_id = instance.getGlobalSQL().getInt("SELECT coordinate_id FROM location_marker WHERE id=" + id + ";");
            if (location == null) {
                // Load subcategory.
                int subcategory_id = instance.getGlobalSQL().getInt("SELECT subcategory FROM location_marker WHERE id=" + id + ";");
                loadSubcategoryMarker(subcategory_id, coordinate_id);
            } else {
                loadLocationMarker(location, coordinate_id);
            }
        });
    }

    private void loadLocationMarker(String name, int coordinate_id) {

        Hologram hologram = createMarker(name, coordinate_id, false);

        if (hologram == null) {
            LOGGER.warning(String.format("Hologram %s was not created due to an error, a hologram with this name probably already exists.", name));
            return;
        }

        // Create the click action.
        HologramClickAction clickAction = u -> teleportToLocation(u, name);
        holograms.put(hologram, clickAction);
    }

    private void loadSubcategoryMarker(int subcategory_id, int coordinate_id) {

        // Get subcategory name.
        String subcategory = instance.getGlobalSQL().getString("SELECT name FROM location_subcategory WHERE id=" + subcategory_id + ";");

        if (subcategory == null) {
            LOGGER.warning(String.format("Subcategory with id %d does not exist!", subcategory_id));
            return;
        }

        Hologram hologram = createMarker(subcategory, coordinate_id, true);

        if (hologram == null) {
            LOGGER.warning(String.format("Hologram %s was not created due to an error, a hologram with this name probably already exists.", subcategory));
            return;
        }

        // Create the click action.
        HologramClickAction clickAction = u -> Bukkit.getScheduler().runTask(instance, () -> openSubcategoryMenu(u, subcategory));
        holograms.put(hologram, clickAction);
    }

    private Hologram createMarker(String name, int coordinate_id, boolean subcategory) {
        // Get location.
        Location l = instance.getGlobalSQL().getLocation(coordinate_id);

        // Create a hologram for the location.
        if (l.getWorld() == null) {
            LOGGER.warning(String.format("Unable to create hologram %s, world can not be found.", name));
            return null;
        }
        return Holograms.createHologram(name, l, appendColour(Arrays.asList(name, "↓"), subcategory));
    }

    private static List<String> appendColour(List<String> lines, boolean subcategory) {
        List<String> newList = new ArrayList<>();
        lines.forEach(line -> {
            if (subcategory) {
                newList.add(GOLD + line);
            } else {
                newList.add(AQUA + line);
            }
        });
        return newList;
    }

    private void teleportToLocation(NetworkUser u, String location) {
        //Get coordinate id.
        int coordinate_id = instance.getGlobalSQL().getInt("SELECT coordinate FROM location_data WHERE location='" + location + "';");

        // Get the server.
        String server = instance.getGlobalSQL().getString("SELECT server FROM coordinates WHERE id=" + coordinate_id + ";");

        if (server == null) {
            u.sendMessage(Utils.error("An error occurred, please contact a server administrator."));
            return;
        }

        // Create teleport event.
        EventManager.createTeleportEvent(true, u.player.getUniqueId().toString(), "network",
                "teleport location " + location, u.player.getLocation());

        SwitchServer.switchServer(u.player, server);
    }

    private void openSubcategoryMenu(NetworkUser u, String subcategory) {
        // Get the subcategory id.
        int id = instance.getGlobalSQL().getInt("SELECT id FROM location_subcategory WHERE name='" + subcategory + "';");

        if (id == 0) {
            u.sendMessage(Utils.error("An error occurred, please contact a server administrator."));
            return;
        }

        // Get all locations for the subcategory.
        List<String> locations = instance.getGlobalSQL().getStringList("SELECT location FROM location_data WHERE subcategory=" + id + ";");

        // Create temporary location menu.
        LocationMenu menu = new LocationMenu(subcategory, u, Category.TEMPORARY, null, locations.toArray(String[]::new));
        menu.setDeleteOnClose(true);

        // Open the menu.
        menu.open(u);
    }

    public interface HologramClickAction {
        void click(NetworkUser u);
    }
}