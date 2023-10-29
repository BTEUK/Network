package me.bteuk.network.lobby;

import me.bteuk.network.Network;
import me.bteuk.network.utils.NetworkUser;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;

import java.io.File;
import java.util.Objects;
import java.util.Set;

import static me.bteuk.network.utils.Constants.LOGGER;

/**
 * This class manages the ingame map in the lobby.
 * When a player enters the map area they will have an item in the 5th slot of their hotbar.
 * Clicking on the item will list all nearby locations (radius of 50km) to the players position.
 * Locations will be sorted by distance to the player.
 */
public class Map implements Listener {

    private final Network instance;

    /**
     * Indicates whether them map is enabled.
     */
    private boolean enabled;

    private int[][] bounds;
    private double[][] coordinates;

    public Map(Network instance) {
        this.instance = instance;
    }

    /**
     * Reloads the map.
     */
    protected void reload() {

        //If the map is enabled, first unload it, then load it again.
        if (enabled) {
            unload();
        }

        load();

    }

    /**
     * Loads the map using the config from map.yml
     * If there are issues with the config, then loading will be unsuccessful and the map will not be enabled.
     */
    private void load() {

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
            LOGGER.warning("The map.yml file is invalid, please delete it to let it regenerate.");
            enabled = false;
            return;
        }
        Set<String> keys = section.getKeys(false);
        if (keys.size() != 4) {
            LOGGER.warning("The map.yml file is invalid, please delete it to let it regenerate.");
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

        enabled = true;

    }

    /**
     * Unloads the map if currently enabled.
     */
    private void unload() {
        if (enabled) {

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