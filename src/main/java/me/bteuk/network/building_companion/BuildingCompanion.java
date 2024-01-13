package me.bteuk.network.building_companion;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.bteuk.network.Network;
import me.bteuk.network.exceptions.NoBuildPermissionException;
import me.bteuk.network.exceptions.RegionNotFoundException;
import me.bteuk.network.exceptions.building_companion.DistanceLimitException;
import me.bteuk.network.exceptions.building_companion.OutsidePlotException;
import me.bteuk.network.utils.Blocks;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.enums.ServerType;
import me.bteuk.network.utils.regions.Region;
import me.bteuk.network.utils.regions.RegionManager;
import me.bteuk.network.worldguard.WorldguardMembers;
import me.bteuk.network.worldguard.WorldguardUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static me.bteuk.network.utils.Constants.REGIONS_ENABLED;
import static me.bteuk.network.utils.Constants.SERVER_TYPE;

/**
 * This class stored all the information about the building companion.
 * It is player-specific and will be enabled when a player activates it.
 * Listeners will be registered in here, along with any tool-related variables.
 */
public class BuildingCompanion {

    private static final int MAX_DISTANCE = 2;

    private static final long TIMEOUT = 20 * 15;

    // Set of inputs for each corner. No duplicates can exist in a set,
    // this prevents the player from teleporting to the same corner multiple times
    // causing the weighting to be skewed.
    private final Set<Set<double[]>> input_corners;

    private final Set<Listener> listeners;

    // The player that this building companion is for.
    private final NetworkUser user;

    private World world;

    private final HashMap<UUID, SavedOutline> saved_outlines;

    private boolean asyncActive = false;

    public BuildingCompanion(NetworkUser user) {

        this.user = user;
        this.world = user.player.getWorld();
        input_corners = new HashSet<>();
        saved_outlines = new HashMap<>();

        // Enable the tpll listener.
        listeners = new HashSet<>();
        listeners.add(new TpllListener(this));

    }

    /**
     * Disable the building companion.
     */
    public void disable() {
        // Unregister the events.
        listeners.forEach(HandlerList::unregisterAll);
    }

    public void clearSelection() {
        input_corners.clear();
        sendFeedback(Component.text("Your selection has been cleared.", NamedTextColor.YELLOW));
    }

    public void checkChangeWorld(World playerWorld) {
        // If the world has changed, clear all saved data.
        if (!playerWorld.equals(world)) {
            world = user.player.getWorld();
            input_corners.clear();
            saved_outlines.clear();
            sendFeedback(Component.text("You have switched worlds, resetting building companion data.", NamedTextColor.YELLOW));
        }
    }

    public boolean saveOutlines(String sUuid, BlockData block, boolean permanent) {
        UUID uuid = UUID.fromString(sUuid);
        SavedOutline outline = saved_outlines.get(uuid);
        if (outline == null) {
            sendFeedback(Utils.error("The outlines are not longer available."));
            return false;
        } else {
            saved_outlines.remove(uuid);
            return drawOutlines(outline, block, permanent);
        }
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
        double[] input = new double[]{
                location.getX(),
                location.getZ()
        };

        boolean done = false;
        // Check if it's near an existing corner.
        for (Set<double[]> corner : input_corners) {
            if (isNearCorner(input, getAverage(corner))) {
                if (contains(corner, input)) {
                    sendFeedback(Utils.success("This location has already been recorded."));
                } else {
                    corner.add(input);
                    sendFeedback(Utils.success("Updated existing corner with new location"));
                }
                done = true;
            }
        }
        // It is not near an existing corner, add if the limit has not yet been reached.
        if (!done) {
            if (input_corners.size() < 4) {
                Set<double[]> new_corner = new HashSet<>();
                new_corner.add(input);
                input_corners.add(new_corner);
                sendFeedback(Utils.success("New corner recorded."));
            } else {
                sendFeedback(Utils.error("You have already recorded 4 corners, and it is not close enough to an existing one."));
            }
        }

        // Notify the player if they have 4 corners selected.
        if (input_corners.size() == 4) {
            //addDrawOutlinesEvent();
            sendFeedback(Utils.success("You have 4 corners selected, click here to draw the outlines.")
                    .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/buildingcompanion drawoutlines")));
        }
    }

    protected boolean playerNotEquals(Player p) {
        return !p.equals(user.player);
    }

    private boolean isNearCorner(double[] input, double[] corner) {
        return ((input[0] - corner[0]) * (input[0] - corner[0]) + (input[1] - corner[1]) * (input[1] - corner[1])) <= (MAX_DISTANCE * MAX_DISTANCE);
    }

    private double[] getAverage(Set<double[]> input) {
        if (input == null || input.isEmpty()) {
            return null;
        }
        return new double[]{
                input.stream().mapToDouble(location -> location[0]).average().orElseThrow(),
                input.stream().mapToDouble(location -> location[1]).average().orElseThrow()
        };
    }

    private void sendFeedback(Component feedback) {
        user.player.sendMessage(feedback);
    }

    public void drawOutlines() {
        if (input_corners.size() == 4 && world.equals(user.player.getWorld()) && !asyncActive) {
            // Get the average of the corners.
            // Use an async task to not block the main thread.
            int taskId = drawOutlinesTask().getTaskId();
            // Run a task later to cancel the task if it has not yet been completed.
            Bukkit.getScheduler().runTaskLater(Network.getInstance(), () -> {
                if (asyncActive && Bukkit.getScheduler().isCurrentlyRunning(taskId)) {
                    sendFeedback(Utils.error("Drawing outlines task timed out, the selection was too difficult to process."));
                    clearSelection();
                }
            }, TIMEOUT);
        } else if (asyncActive) {
            sendFeedback(Utils.error("The outlines are already being drawn."));
        } else if (!world.equals(user.player.getWorld())) {
            sendFeedback(Utils.error("You have switched worlds, unable to draw outlines."));
        } else {
            sendFeedback(Utils.error("You must select at least 4 corners to draw outlines."));
        }
    }

    private BukkitTask drawOutlinesTask() {
        return Bukkit.getScheduler().runTaskAsynchronously(Network.getInstance(), () -> {

            double[][] corners = input_corners.stream().map(this::getAverage).toArray(double[][]::new);

            // Fit the corners to a rectangle.
            BestFitRectangle rectangle = new BestFitRectangle(corners);
            if (rectangle.findBestFitRectangleCorners()) {
                double[][] fitted_corners = rectangle.getOutput();

                // Get Minecraft usable corners from the output.
                // Find the option with the least error, while keeping the walls parallel.
                int[][] output = MinecraftRectangleConverter.convertRectangleToMinecraftCoordinates(fitted_corners);

                // Optimise the corners for Minecraft.
                int[][] finalOutput = MinecraftRectangleConverter.optimiseForBlockSize(output);

                // Draw the lines with fake blocks.
                drawTempOutlinesTaskWithFeedback(finalOutput);
            } else {
                sendFeedback(Utils.error("Unable to generate a rectangle using the given corners, clearing selection."));
                clearSelection();
            }
            asyncActive = false;
        });
    }

    private void drawTempOutlinesTaskWithFeedback(int[][] corners) {
        // Draw the lines with fake blocks.
        Bukkit.getScheduler().runTask(Network.getInstance(), () -> {
            SavedOutline outline = new SavedOutline(UUID.randomUUID(), corners, world);
            if (drawOutlines(outline, Material.ORANGE_CONCRETE.createBlockData(), false)) {
                sendFeedback(Utils.success("The outlines have been drawn."));
                saved_outlines.put(outline.uuid(), outline);
                sendFeedback(Component.text("Save outlines: ", NamedTextColor.YELLOW)
                        .append(Component.text("[Yes]", NamedTextColor.GREEN)
                                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/buildingcompanion save " + outline.uuid())))
                        .append(Component.text(" - ", NamedTextColor.YELLOW))
                        .append(Component.text("[No]", NamedTextColor.RED)
                                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/buildingcompanion remove " + outline.uuid()))));
            }
        });
    }

    /**
     * Draw the outlines, check if the player can build there first.
     *
     * @param outline   the {@link SavedOutline} outline
     * @param block     the block to draw the outline with
     * @param permanent should the outline be a real block, or temporary
     * @return whether the player has permission to build here, else don't draw the outline
     */
    private boolean drawOutlines(SavedOutline outline, BlockData block, boolean permanent) {
        ProtectedRegion wgRegion = null;
        if (SERVER_TYPE == ServerType.PLOT) {
            // Get region at first corner. If no region is found return false.
            try {
                wgRegion = WorldguardUtils.getRegionAt(world, BlockVector3.at(outline.corners()[0][0], 1, outline.corners()[0][1]));
                if (wgRegion == null) {
                    throw new RegionNotFoundException("No region found at location.");
                } else {
                    // Check region build permission.
                    if (!WorldguardMembers.isMemberOrOwner(wgRegion, user.player)) {
                        throw new NoBuildPermissionException("No permission to build in this region.");
                    }
                }
            } catch (Exception e) {
                sendFeedback(Utils.error("All or part of your selection is not in a plot you can build in, cancelled drawing outlines."));
                return false;
            }
        } else if (REGIONS_ENABLED) {
            RegionManager manager = Network.getInstance().getRegionManager();
            for (int[] point : outline.corners()) {
                Region region = manager.getRegion(new Location(world, point[0], 1, point[1]), user.dx, user.dz);
                if (!region.canBuild(user.player)) {
                    sendFeedback(Utils.error("All or part of your selection is not in a region you can build in, cancelled drawing outlines."));
                    return false;
                }
            }
        }
        try {
            Blocks.drawLine(user.player, world, outline.corners()[0], outline.corners()[1], block, permanent, true, wgRegion);
            Blocks.drawLine(user.player, world, outline.corners()[1], outline.corners()[3], block, permanent, true, wgRegion);
            Blocks.drawLine(user.player, world, outline.corners()[3], outline.corners()[2], block, permanent, true, wgRegion);
            Blocks.drawLine(user.player, world, outline.corners()[2], outline.corners()[0], block, permanent, true, wgRegion);
            return true;
        } catch (Exception e) {
            sendFeedback(Utils.error(e.getMessage()));
            return false;
        }
    }

    private static boolean contains(Set<double[]> list, double[] input) {
        for (double[] array : list) {
            if (Arrays.equals(array, input)) {
                return true;
            }
        }
        return false;
    }
}
