package me.bteuk.network.utils.math;

import com.sk89q.worldedit.math.BlockVector2;

import java.util.List;

public class Point {

    /**
     * Get the average point from a list of 2d points.
     * @param points list of {@link BlockVector2}
     * @return the average point as {@link BlockVector2}
     */
    public static BlockVector2 getAveragePoint(List<BlockVector2> points) {

        double size = points.size();
        double x = 0;
        double z = 0;

        for (BlockVector2 bv : points) {
            x += bv.getX() / size;
            z += bv.getZ() / size;
        }

        return (BlockVector2.at(x, z));

    }
}
