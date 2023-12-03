package me.bteuk.network.building_companion;

import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.math.Line;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Utility class to find the best fit rectangle, given an input of 4 points.
 */
public class BestFitRectangle {

    private final NetworkUser user;

    private final double[][] input;

    private double[][] output;

    public BestFitRectangle(NetworkUser user, double[][] input) {
        this.user = user;
        this.input = input;
    }

    public boolean findBestFitRectangleCorners() {

        // Define point A, B, C and D. Where D is the diagonal corner from A.
        // D is the only corner from which point B and C are nearer than A.
        // If no such point exists, the inputs are unusable.
        // By default, we take pointA as the first input value.
        int pointDIndex = getPointDIndex();
        if (pointDIndex == 0) {
            return false;
        }
        Line[] initialLines = getInitialLines(pointDIndex);

        // Create right-angled lines.
        // This implies, line A and B are at the same angle.
        // Line C and D are at the same angle.
        // That the lines together form a rectangle (90-degree corners between the lines).
        Line[] lines = getRightAngledLines(initialLines);

        // Use an iterative process to find the best placement of the line.
        // This will be done by cycling through the lines and adjusting the b-value marginally.
        // We do this until all 4 lines are no longer adjusted by a significant value with each iteration.

    }

    private Line[] getInitialLines(int pointDIndex) {
        double[] pointA = input[0];
        double[] pointB = input[((pointDIndex + 1) % 3) == 0 ? 3 : ((pointDIndex + 1) % 3)];
        double[] pointC = input[((pointDIndex + 2) % 3) == 0 ? 3 : ((pointDIndex + 2) % 3)];
        double[] pointD = input[pointDIndex];

        // Create the initial lines, stored as
        // LineA (a-b), LineB (c-d), LineC (a-c) and LineD (b-d)
        return new Line[]{
                new Line(pointA, pointB),
                new Line(pointC, pointD),
                new Line(pointA, pointC),
                new Line(pointB, pointD)
        };
    }

    private Line[] getRightAngledLines(Line[] initialLines) {

        // Get the angle of each line.
        double[] angles = getAngles(initialLines);
        double averageAngle = getAverageAngle(angles);
        
        return new Line[] {
                new Line(getSlope(averageAngle), initialLines[0].getB()),
                new Line(getSlope(averageAngle), initialLines[1].getB()),
                new Line(getSlope(averageAngle - 90), initialLines[2].getB()),
                new Line(getSlope(averageAngle - 90), initialLines[3].getB())
        };
    }

    private int getPointDIndex() {
        for (int i = 1; i < 4; i++) {
            if (distanceBetween(input[0], input[i]) < distanceBetween(input[((i + 1) % 3) == 0 ? 3 : ((i + 1) % 3)], input[i]) &&
                    distanceBetween(input[0], input[i]) < distanceBetween(input[((i + 2) % 3) == 0 ? 3 : ((i + 2) % 3)], input[i])) {
                return i;
            }
        }
        return 0;
    }

    private double distanceBetween(double[] pointA, double[] pointB) {
        return Math.sqrt((pointA[0] - pointB[0]) * (pointA[0] - pointB[0]) + (pointA[1] - pointB[1]) * (pointA[1] - pointB[1]));
    }

    /**
     * Get the angles of the lines.
     * Add 90 degrees to the last 2 lines.
     * The range is 0 to 180 degrees.
     *
     * @param lines the 4 lines to get the angle of.
     * @return the angles, with range [0, 180)
     */
    private double[] getAngles(Line[] lines) {
        return new double[]{
                toDegreeAngle(lines[0].getA(), 0),
                toDegreeAngle(lines[1].getA(), 0),
                toDegreeAngle(lines[2].getA(), 90),
                toDegreeAngle(lines[3].getA(), 90)
        };
    }

    /**
     * Get the angle of the slope in degrees with range [0, 180)
     *
     * @param slope  the slope of a line
     * @param offset the offset in degrees to add
     * @return the angle in degrees with range [0, 180)
     */
    private double toDegreeAngle(double slope, double offset) {
        double degrees = Math.toDegrees(Math.atan(Math.abs(slope))) + offset;
        return (degrees + 180) % 180;
    }
    
    private double getAverageAngle(double[] angles) {
        return (Arrays.stream(angles).sum() / 4);
    }
    
    private double getSlope(double angle) {
        return Math.tan(Math.toRadians(angle));
    }
}


