package me.bteuk.network.building_companion;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static me.bteuk.network.utils.Constants.LOGGER;

public class MinecraftRectangleConverter {

    public static int[][] convertRectangleToMinecraftCoordinates(double[][] input) {
        double error = Double.POSITIVE_INFINITY;
        int[][] bestFit = new int[4][2];
        // Get all the possible points.
        int[][] cornerA = getCorner(input[0]);
        int[][] cornerB = getCorner(input[1]);
        int[][] cornerC = getCorner(input[2]);
        int[][] cornerD = getCorner(input[3]);
        // Iterate through all the options.
        for (int i = 0; i < cornerA.length; i++) {
            for (int j = 0; j < cornerB.length; j++) {
                for (int k = 0; k < cornerC.length; k++) {
                    for (int l = 0; l < cornerD.length; l++) {
                        double newError = getSummedDistance(input, cornerA[i], cornerB[j], cornerC[k], cornerD[l]);
                        if (isValidShape(cornerA[i], cornerB[j], cornerC[k], cornerD[l]) && newError < error) {
                            error = newError;
                            bestFit = new int[][] {cornerA[i], cornerB[j], cornerC[k], cornerD[l]};
                        }
                    }
                }
            }
        }
        return bestFit;
    }

    /**
     * Since Minecraft blocks are 1x1 metre, the location we found will always be the minimum point of the block,
     * therefore we want to subtract 1 from the highest x and z edge of the rectangle.
     * Since x = 13 actually means that the outside edge of the block will be at x = 14.
     *
     * @param input the corners
     * @return the corrected corners
     */
    public static int[][] optimiseForBlockSize(int[][] input) {
        // Subtract 1 block from the edges on the higher x and z.
        // Remove 1x from the highest 2 x values, unless the 2nd highest is equal to the 3d highest, then only remove from the first.
        // Do the same for the z values.
        List<Integer> x_sorted = Arrays.stream(input).map(coord -> coord[0]).sorted(Collections.reverseOrder()).toList();
        List<Integer> z_sorted = Arrays.stream(input).map(coord -> coord[1]).sorted(Collections.reverseOrder()).toList();
        boolean checkX = false;
        boolean checkZ = false;
        // Create a clone that doesn't reference values of the input array.
        int[][] output = Arrays.stream(input).map(int[]::clone).toArray(int[][]::new);
        for (int i = 0; i < input.length; i++) {
            if (input[i][0] == x_sorted.get(0)) {
                output[i][0]--;
            } else if (input[i][0] == x_sorted.get(1)) {
                if (!Objects.equals(x_sorted.get(1), x_sorted.get(2))) {
                    output[i][0]--;
                } else {
                    checkX = true;
                }
            }
            if (input[i][1] == z_sorted.get(0)) {
                output[i][1]--;
            } else if (input[i][1] == z_sorted.get(1)) {
                if (!Objects.equals(z_sorted.get(1), z_sorted.get(2))) {
                    output[i][1]--;
                } else {
                    checkZ = true;
                }
            }
        }
        System.out.println(checkX + ", " + checkZ);
        if (checkX && !checkZ) {
            // The 2nd largest X that was also shifted in Z needs to be shifted for the rectangle to still be accurate.
            for (int i = 0; i < input.length; i++) {
                if (input[i][0] == x_sorted.get(1) && input[i][1] != output[i][1]) {
                    output[i][0]--;
                }
            }
        } else if (checkZ && !checkX) {
            // The 2nd largest Z that was also shifted in X needs to be shifted for the rectangle to still be accurate.
            for (int i = 0; i < input.length; i++) {
                if (input[i][1] == z_sorted.get(1) && input[i][0] != output[i][0]) {
                    output[i][1]--;
                }
            }
        }
        return output;
    }

    /**
     * Get all 4 options for a corner.
     *
     * @param corner the corner
     * @return the options as ints
     */
    private static int[][] getCorner(double[] corner) {
        return new int[][]{
                new int[]{(int) Math.floor(corner[0]), (int) Math.floor(corner[1])},
                new int[]{(int) Math.floor(corner[0]), (int) Math.ceil(corner[1])},
                new int[]{(int) Math.ceil(corner[0]), (int) Math.floor(corner[1])},
                new int[]{(int) Math.ceil(corner[0]), (int) Math.ceil(corner[1])}
        };
    }

    /**
     * Check if the shape is valid (parallel walls at the minimum).
     *
     * @return whether the shape is valid.
     */
    private static boolean isValidShape(int[] cornerA, int[] cornerB, int[] cornerC, int[] cornerD) {
        // Check that A-B and C-D have the same angle.
        // Check that A-C and B-D have the same angle.
        return ((cornerA[0] - cornerB[0]) == (cornerC[0] - cornerD[0])) && ((cornerA[1] - cornerB[1]) == (cornerC[1] - cornerD[1])) &&
                ((cornerA[0] - cornerC[0]) == (cornerB[0] - cornerD[0])) && ((cornerA[1] - cornerC[1]) == (cornerB[1] - cornerD[1]));
    }

    private static double getSummedDistance(double[][] input, int[] cornerA, int[] cornerB, int[] cornerC, int[] cornerD) {
        return distanceBetween(cornerA, input[0]) + distanceBetween(cornerB, input[1])
                + distanceBetween(cornerC, input[2]) + distanceBetween(cornerD, input[3]);
    }

    private static double distanceBetween(int[] pointA, double[] pointB) {
        return Math.sqrt((pointA[0] - pointB[0]) * (pointA[0] - pointB[0]) + (pointA[1] - pointB[1]) * (pointA[1] - pointB[1]));
    }
}
