package me.bteuk.network.building_companion;

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
