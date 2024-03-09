package me.bteuk.network.lobby;

import io.papermc.lib.PaperLib;
import lombok.Getter;
import me.bteuk.network.Network;
import me.bteuk.network.events.EventManager;
import me.bteuk.network.listeners.ClickableItemListener;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.SwitchServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static me.bteuk.network.utils.Constants.LOGGER;
import static me.bteuk.network.utils.Constants.SERVER_NAME;
import static me.bteuk.network.utils.NetworkConfig.CONFIG;

/**
 * This class manages the ingame map in the lobby.
 * When a player enters the map area they will have an item in the 5th slot of their hotbar.
 * Clicking on the item will list all nearby locations (radius of 50km) to the players position.
 * Locations will be sorted by distance to the player.
 */
public class Map extends AbstractReloadableComponent implements Listener {

    private final Network instance;

    /**
     * Indicates whether them map is enabled.
     */
    @Getter
    private boolean enabled;

    /**
     * The server that has the physical map.
     */
    private String server;

    /**
     * Coordinates of the map.
     */
    private Location map_location;

    /**
     * Map command.
     */
    private MapCommand map_command;

    /**
     * HashMap of the map markers.
     * Key: Location of the marker on the map.
     * Value: Name of the marker (warp or subcategory).
     */
    private HashMap<Location, String> map_markers;

    private ItemStack clickableItem;
    private ClickableItemListener clickableItemListener;


    private final double radius = 0.5;

    public Map(Network instance) {
        this.instance = instance;
    }

    /**
     * Loads the map using the config from map.yml
     * If there are issues with the config, then loading will be unsuccessful and the map will not be enabled.
     */
    @Override
    public void load() {

        if (enabled) {
            LOGGER.warning("An attempt was made to load the Map while it is already enabled.");
            return;
        }

        // Check if the map is enabled.
        if (!CONFIG.getBoolean("map.enabled")) {
            enabled = false;
            return;
        }

        // Get the server of the map, this is important in deciding which features to enable.
        // If the server is not
        server = CONFIG.getString("map.server");
        if (server == null || !instance.getGlobalSQL().hasRow("SELECT * FROM server_data WHERE name='" + server + "';")) {
            enabled = false;
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
                enabled = false;
                LOGGER.warning("The map world does not exist on this server, disabling the map.");
                return;
            }
            // Set the world, the coordinates have already been set.
            map_location.setWorld(Bukkit.getWorld(world));

            // Load the map markers and linked location.
            try {
                loadMarkers();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            // Register the move listener.
            Bukkit.getServer().getPluginManager().registerEvents(this, instance);
        }

        // Enable the map command.
        map_command = new MapCommand(instance, this, "map");


        enabled = true;

    }

    /**
     * Unloads the map if currently enabled.
     */
    @Override
    public void unload() {
        if (enabled) {
            // Disable the movement listener.
            PlayerMoveEvent.getHandlerList().unregister(this);

            // Disable the clickable item.
            clickableItemListener.unregister();

            // Remove all visible markers.
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        NetworkUser user = instance.getUser(e.getPlayer());
        if (user != null) {
            if (user.isHasMapItem() && !isNearMarker(e.getTo())) {
                // The player has the map item, but it not near a marker, remove the map item.
                user.setHasMapItem(false);
                removeMapItem(e.getPlayer());
            } else if (!user.isHasMapItem() && isNearMarker(e.getTo())) {
                // The player is near a marker, give them the map item.
                user.setHasMapItem(true);
                giveMapItem(e.getPlayer());
            }
        }
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

    protected void addMarker(Player p, String marker) {

    }

    protected void removeMarker(Player p, String marker) {

    }

    private void loadMarkers() throws SQLException {

        // Retrieve all the markers from the database.
        List<Integer> markers = instance.getGlobalSQL().getIntList("SELECT id FROM location_marker");

        markers.forEach(id -> {
            // Get the name;
            String location = instance.getGlobalSQL().getString("SELECT location FROM location_marker WHERE id=" + id + ";");
            int coordinate_id = instance.getGlobalSQL().getInt("SELECT coordinate_id FROM location_marker WHERE id=" + id + ";");
            if (location == null) {
                // Load subcategory.
                int subcategory_id = instance.getGlobalSQL().getInt("SELECT subcategory FROM location_marker WHERE id=" + id + ";");
                loadCategoryMarker(subcategory_id, coordinate_id);
            } else {
                loadLocationMarker(location, coordinate_id);
            }
        });
    }

    private void loadLocationMarker(String name, int coordinate_id) {
        map_markers.put(instance.getGlobalSQL().getCoordinate(coordinate_id), name);

        // Create the visual marker.
    }

    private void loadCategoryMarker(int category_id, int coordinate_id) {
        map_markers.put(instance.getGlobalSQL().getCoordinate(coordinate_id), instance.getGlobalSQL().getString("SELECT name FROM location_subcategory WHERE id=" + category_id + ";"));

        // Create the visual marker.
    }

    private void removeLocationMarker() {

    }

    private void removeCategoryMarker() {

    }

    private void giveMapItem(Player p) {
        // Set the clickable item in slot 5 of the players inventory.
        p.getInventory().setItem(4, clickableItem);
    }

    private void removeMapItem(Player p) {
        // Remove the clickable item from the players inventory, no matter which slot it's in.
        for (int i = 0; i < p.getInventory().getSize(); i++) {
            if (clickableItem.isSimilar(p.getInventory().getItem(i))) {
                p.getInventory().clear(i);
            }
        }
    }

    /**
     * Check if the location is within the radius of a marker.
     *
     * @param l the location to check.
     * @return whether the location is within the radius of a marker
     */
    private boolean isNearMarker(Location l) {
        return map_markers.keySet().stream().anyMatch(location -> location.distance(l) < radius);
    }
}