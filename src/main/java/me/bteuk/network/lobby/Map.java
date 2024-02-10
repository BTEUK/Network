package me.bteuk.network.lobby;

import me.bteuk.network.Network;
import me.bteuk.network.commands.navigation.Tpll;
import me.bteuk.network.gui.navigation.LocationMenu;
import me.bteuk.network.listeners.ClickableItemListener;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import net.buildtheearth.terraminusminus.projection.OutOfProjectionBoundsException;
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
import java.util.LinkedHashMap;
import java.util.List;

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
        bounds = new int[2][2];
        addBounds(section);
        coordinates = new double[4][2];
        addCoordinates(section);
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
            Location l;
            try {
                l = getLocationOnMap(user.player.getLocation());
            } catch (OutOfProjectionBoundsException e) {
                LOGGER.warning("Map contains invalid coordinates, please setup the map correctly.");
                user.player.sendMessage(Utils.error("An error occurred, please contact an admin."));
                return;
            }
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
        NetworkUser user = instance.getUser(e.getPlayer());
        if (user != null) {
            if (user.isInMap() && !isInMapArea(e.getTo())) {
                // The player was in the map area, but has exited it, remove the map item.
                user.setInMap(false);
                removeMapItem(e.getPlayer());
            } else if (!user.isInMap() && isInMapArea(e.getTo())) {
                // The player has entered the map area, give them the map item.
                user.setInMap(true);
                giveMapItem(e.getPlayer());
            }
        }
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

    private void addBounds(ConfigurationSection section) {
        bounds[0][0] = section.getInt("north_west.x");
        bounds[0][1] = section.getInt("north_west.z");
        bounds[1][0] = section.getInt("south_east.x");
        bounds[1][1] = section.getInt("south_east.z");
    }

    /**
     * The coordinates are stored in an array, in the other of.
     * North-west, north-east, south-west, south-east.
     *
     * @param section configuration section.
     */
    private void addCoordinates(ConfigurationSection section) {
        List<?> corners = section.getList("corners");
        if (corners == null) {
            return;
        }
        for (int i = 0; i < corners.size(); i++) {
            LinkedHashMap<?, ?> corner = (LinkedHashMap<?, ?>) corners.get(i);
            coordinates[i][0] = (double) corner.get("latitude");
            coordinates[i][1] = (double) corner.get("longitude");
        }
    }

    /**
     * Check if the bounds of the map are valid for both Minecraft and irl coordinates.
     *
     * @return if the bounds are valid
     */
    private boolean validBounds() {
        return (bounds[0][0] != bounds[1][0]) && (bounds[0][1] != bounds[1][1]) &&
                (coordinates[0][0] != coordinates[1][0]) && (coordinates[0][1] != coordinates[1][1]);
    }

    /**
     * Check whether the location is in the map area.
     *
     * @param l the location to check.
     * @return whether the location is in the map area.
     */
    private boolean isInMapArea(Location l) {
        return l.getX() >= bounds[0][0] && l.getX() <= bounds[1][0] &&
                l.getZ() >= bounds[0][1] && l.getZ() <= bounds[1][1];
    }

    /**
     * Get the location on the map using the given location.
     * Comparing the location to the coordinate bound, get the coordinate of the location.
     * Then convert the coordinates to the location in Minecraft.
     *
     * @param l the location on the map.
     * @return the Minecraft coordinates of the location on the map.
     */
    private Location getLocationOnMap(Location l) throws OutOfProjectionBoundsException {
        double xDis = (l.getX() - bounds[0][0]) / (bounds[1][0] - bounds[0][0]);
        double zDis = (l.getZ() - bounds[0][1]) / (bounds[1][1] - bounds[0][1]);
        double lat = ((1 - zDis) * coordinates[0][0]) + (zDis * coordinates[2][0]) + (xDis * (coordinates[1][0] - coordinates[0][0]));
        double lon = ((1 - zDis) * coordinates[0][1]) + (zDis * coordinates[2][1]) + (xDis * (coordinates[1][1] - coordinates[0][1]));

        double[] coords = Tpll.bteGeneratorSettings.projection().fromGeo(lon, lat);
        Location newLocation = l.clone();
        newLocation.setX(coords[0]);
        newLocation.setZ(coords[1]);
        LOGGER.info("Location is " + lat + ", " + lon + ": " + coords[0] + ", " + coords[1]);
        return newLocation;
    }
}