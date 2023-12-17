package me.bteuk.network.utils;

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
    public static void drawLine(Player player, World world, int[] block1, int[] block2, BlockData block, boolean permanent, boolean skipFirst) {

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
            drawBlock(player, world,
                    ((int) (round(block1[0] + ((i * lengthX) / (double) length)))),
                    ((int) (round(block1[1] + ((i * lengthZ) / (double) length)))),
                    block, permanent);
        }
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
