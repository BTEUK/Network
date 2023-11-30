package me.bteuk.network.building_companion;

import lombok.Getter;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class stored all the information about the building companion.
 * It is player-specific and will be enabled when a player activates it.
 * Listeners will be registered in here, along with any tool-related variables.
 */
public class BuildingCompanion {

    private static final int MAX_DISTANCE = 2;

    // Set of inputs for each corner. No duplicates can exist in a set,
    // this prevents the player from teleporting to the same corner multiple times
    // causing the weighting to be skewed.
    private final Set<Set<double[]>> input_corners;

    private final Map<ItemStack, ItemEvent> itemEvents;

    // The player that this building companion is for.
    private final NetworkUser user;

    private World world;

    public BuildingCompanion(NetworkUser user) {

        this.user = user;
        this.world = user.player.getWorld();
        itemEvents = new HashMap<>();
        input_corners = new HashSet<>();

    }

    /**
     * Attempt to add a location to the set of inputs.
     * If it is near the average of a set, add it to the set.
     * If there are 4 sets of inputs already, and it is not near
     * an average of one of those sets, ignore the input.
     *
     * @param location the location to add
     */
    public void addLocation(Location location) {
        // If the world has changed, clear the set first.
        if (!location.getWorld().equals(world)) {
            world = user.player.getWorld();
            input_corners.clear();
            sendFeedback(Component.text("You have switched worlds, resetting existing corners.", NamedTextColor.YELLOW));
        }

        double[] input = new double[]{
                location.getX(),
                location.getZ()
        };
        // Check if it's near an existing corner.
        for (Set<double[]> corner : input_corners) {
            if (isNearCorner(input, getAverage(corner))) {
                if (corner.contains(input)) {
                    sendFeedback(Utils.success("This location has already been recorded."));
                } else {
                    corner.add(input);
                    sendFeedback(Utils.success("Updated existing corner with new location"));
                }
            }
        }
        // It is not near an existing corner, add if the limit has not yet been reached.
        if (input_corners.size() < 4) {
            Set<double[]> new_corner = new HashSet<>();
            new_corner.add(input);
            input_corners.add(new_corner);
            sendFeedback(Utils.success("New corner recorded."));
        } else {
            sendFeedback(Utils.success("You have already recorded 4 corners, and it is not close enough to an existing one."));
        }

        // Notify the player if they have 4 corners selected.
        if (input_corners.size() == 4) {
            addDrawOutlinesEvent();
            sendFeedback(Utils.success("You have 4 corners selected, you can proceed with the next step by clicking on the stick."));
        }
    }

    protected boolean playerEquals(Player p) {
        return p.equals(user.player);
    }

    protected boolean runIfEqualsItemEvent(ItemStack item) {
        ItemEvent event = itemEvents.get(item);
        if (event != null) {
            event.runEvent();
            return true;
        } else {
            return false;
        }
    }

    private boolean isNearCorner(double[] input, double[] corner) {
        return ((input[0]-corner[0])*(input[0]-corner[0]) + (input[1]-corner[1])*(input[1]-corner[1])) <= (MAX_DISTANCE*MAX_DISTANCE);
    }

    private double[] getAverage(Set<double[]> input) {
        if (input == null || input.isEmpty()) {
            return null;
        }
        return new double[]{
                input.stream().map(location -> location[0]).reduce(0d, Double::sum),
                input.stream().map(location -> location[1]).reduce(0d, Double::sum)
        };
    }

    private void sendFeedback(Component feedback) {
        user.player.sendMessage(feedback);
    }

    private void addDrawOutlinesEvent() {
        // Add stick item to draw outlines.
        itemEvents.put(
                Utils.createItem(Material.STICK, 1, Utils.success("Create Outlines")),
                new ItemEvent(2, this::drawOutline)
        );
    }

    private void removeDrawOutlinesEvent() {
        itemEvents.remove(Utils.createItem(Material.STICK, 1, Utils.success("Create Outlines")));
    }

    private void drawOutline() {

    }

    private record ItemEvent(@Getter int slot, Runnable event) {
        public void runEvent() {
            event.run();
        }
    }
}
