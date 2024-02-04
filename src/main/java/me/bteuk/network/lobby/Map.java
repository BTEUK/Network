package me.bteuk.network.lobby;

import me.bteuk.network.Network;
import me.bteuk.network.gui.navigation.LocationMenu;
import me.bteuk.network.listeners.ClickableItemListener;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Objects;
import java.util.Set;

import static me.bteuk.network.utils.Constants.LOGGER;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_RED;

/**
 * This class manages the ingame map in the lobby.
 * When a player enters the map area they will have an item in the 5th slot of their hotbar.
 * Clicking on the item will list all nearby locations (radius of 50km) to the players position.
 * Locations will be sorted by distance to the player.
 */
public class Map extends AbstractLobbyComponent implements Listener {

    private final Network instance;

    /**
     * Indicates whether them map is enabled.
     */
    private boolean enabled;

    private ItemStack clickableItem;
    private ClickableItemListener clickableItemListener;

    private int[][] bounds;
    private double[][] coordinates;

    private int radius = 50;

    private static final String INVALID_MAP_CONFIG = "The map.yml file is invalid, please delete it and let it regenerate.";

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

        //Create map.yml if not exists.
        //The data folder should already exist since the plugin will always create config.yml first.
        File configFile = new File(instance.getDataFolder(), "map.yml");
        if (!configFile.exists()) {
            instance.saveResource("map.yml", false);
        }

        //Load the config.
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        //Check if the map is enabled.
        if (!config.getBoolean("enabled")) {
            enabled = false;
            return;
        }

        //Get the coordinates.
        ConfigurationSection section = config.getConfigurationSection("bounds");
        if (section == null) {
            LOGGER.warning(INVALID_MAP_CONFIG);
            enabled = false;
            return;
        }
        Set<String> keys = section.getKeys(false);
        if (keys.size() != 4) {
            LOGGER.warning(INVALID_MAP_CONFIG);
            enabled = false;
            return;
        }
        bounds = new int[4][2];
        coordinates = new double[4][2];
        for (int i = 0; i < keys.size(); i++) {
            addBounds(i, (String) keys.toArray()[i], section);
        }
        if (!validBounds()) {
            LOGGER.warning("The map bounds are not valid, please check the config.");
            enabled = false;
            return;
        }

        // Set nearby location radius.
        radius = config.getInt("range");

        // Create and register the clickable map item.
        clickableItem = Utils.createItem(Material.ENDER_PEARL, 1, Utils.success("Teleport to nearby locations!"),
                Utils.line("Click to open a menu"), Utils.line("that lists all nearby warps."));
        clickableItemListener = new ClickableItemListener(instance, clickableItem, user -> {
            // Get current location on map.
            Location l = user.getLocationWithCoordinateTransform();
            // Create temporary gui.
            LocationMenu gui = new LocationMenu("Locations within " + radius + "km", l, radius);
            if (gui.isEmpty()) {
                user.player.sendMessage(Utils.error("There are no locations within a ")
                        .append(Component.text(radius + "km", DARK_RED))
                        .append(Utils.error(" range.")));
            } else {
                gui.open(user);
            }
        });

        // Register the move listener.
        Bukkit.getServer().getPluginManager().registerEvents(this, instance);

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
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {

    }

    private void giveMapItem(Player p) {
        // Set the clickable item in slot 5 of the players inventory.
        p.getInventory().setItem(5, clickableItem);
    }

    private void removeMapItem(Player p) {
        // Remove the clickable item from the players inventory, no matter which slot it's in.
        for (int i = 0; i < p.getInventory().getSize(); i++) {
            if (Objects.equals(p.getInventory().getItem(i), clickableItem)) {
                p.getInventory().clear(i);
            }
        }
    }

    private void addBounds(int idx, String key, ConfigurationSection section) {
        bounds[idx][0] = section.getInt(key + ".x");
        bounds[idx][1] = section.getInt(key + ".z");
        coordinates[idx][0] = section.getDouble(key + ".latitude");
        coordinates[idx][1] = section.getDouble(key + ".longitude");
    }

    /**
     * Check if the bounds of the map are valid for both Minecraft and irl coordinates.
     * Invalid means that all the coordinates are the same or that the Minecraft coordinates don't form a rectangle.
     * @return if the bounds are valid
     */
    private boolean validBounds() {
        int x = bounds[0][0];
        int z = bounds[0][1];
        double lat = coordinates[0][0];
        double lon = coordinates[0][1];
        for (int i = 1; i < 4; i++) {
            if (bounds[i][0] != bounds[0][0]) {
                x = bounds[i][0];
            }
            if (bounds[i][1] != bounds[0][1]) {
                z = bounds[i][1];
            }
            if (coordinates[i][0] != coordinates[0][0]) {
                lat = coordinates[i][0];
            }
            if (coordinates[i][1] != coordinates[0][1]) {
                lon = coordinates[i][1];
            }
        }
        return (!(x == bounds[0][0] || z == bounds[0][1] || lat == coordinates[0][0] || lon == coordinates[0][1]) &&
                bounds[0][0] == bounds[2][0] && bounds[1][0] == bounds[3][0] && bounds[0][1] == bounds[2][1] && bounds[1][1] == bounds[3][1]);
    }
}