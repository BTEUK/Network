package me.bteuk.network.utils;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.Arrays;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.round;
import static me.bteuk.network.utils.Constants.LOGGER;

public class Blocks {

    // Add a line.
    public static boolean drawLine(Player player, World world, int[] block1, int[] block2, BlockData block, boolean permanent, boolean skipFirst, ProtectedRegion checkRegionPermission) {

        LOGGER.info(Arrays.toString(block1) + " - " + Arrays.toString(block2));

        //Get length in x and z direction.
        int lengthX = block2[0] - block1[0];
        int lengthZ = block2[1] - block1[1];

        int length = max(abs(lengthX), abs(lengthZ));

        // Iterate over the largest length of the two.
        for (int i = 0; i <= length; i++) {
            if (skipFirst && i == 0) {
                continue;
            }
            // Remove the points from the list.
            int x = (int) (round(block1[0] + ((i * lengthX) / (double) length)));
            int z = (int) (round(block1[1] + ((i * lengthZ) / (double) length)));
            // Check permission.
            if (checkRegionPermission == null || checkRegionPermission.contains(x, 1, z)) {
                drawBlock(player, world, x, z, block, permanent);
            } else {
                return false;
            }
        }
        return true;
    }

    // Draw a specific block.
    private static void drawBlock(Player player, World world, int x, int z, BlockData block, boolean permanent) {
        Location l = new Location(world, x, (world.getHighestBlockYAt(x, z) + 1), z);
        if (permanent) {
            world.setBlockData(l, block);
        } else {
            player.sendBlockChange(l, block);
        }
    }
}
