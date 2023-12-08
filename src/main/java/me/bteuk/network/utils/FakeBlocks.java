package me.bteuk.network.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.round;

public class FakeBlocks {

    // Add a line.
    public static void drawLine(Player player, World world, int[] block1, int[] block2, BlockData block) {

        //Get length in x and z direction.
        int lengthX = block2[0] - block1[0];
        int lengthZ = block2[1] - block1[1];

        int length = max(abs(lengthX), abs(lengthZ));

        //Iterate over the largest length of the two.
        for (int i = 0; i <= length; i++) {
            // Remove the points from the list.
            drawBlock(player, world,
                    ((int) (round(block1[0] + 0.5 + ((i * lengthX) / (double) length)))),
                    ((int) (round(block1[1] + 0.5 + ((i * lengthZ) / (double) length)))),
                    block);
        }
    }

    // Draw a specific block.
    private static void drawBlock(Player player, World world, int x, int z, BlockData block) {
        player.sendBlockChange(
                new Location(
                        world, x,
                        (world.getHighestBlockYAt(x, z) + 1),
                        z
                ), block);
    }
}
