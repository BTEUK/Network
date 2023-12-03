package me.bteuk.network.building_companion;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

class BestFitRectangleTest {

    @Test
    void testSimpleCorners() {

        double[][] input = new double[][] {
                new double[] {0, 0},
                new double[] {4, 0.5},
                new double[] {1, 3},
                new double[] {4, 3}
        };

        BestFitRectangle rectangle = new BestFitRectangle(null, input);
        rectangle.findBestFitRectangleCorners();

        System.out.println(Arrays.deepToString(rectangle.getOutput()));

    }

    @Test
    void testPerfectRectangle() {

        double[][] input = new double[][] {
                new double[] {0, 0},
                new double[] {3, 2},
                new double[] {3, 0},
                new double[] {0, 2}
        };

        BestFitRectangle rectangle = new BestFitRectangle(null, input);
        rectangle.findBestFitRectangleCorners();

        System.out.println(Arrays.deepToString(rectangle.getOutput()));

    }

    @Test
    void testComplexCorners() {

        double[][] input = new double[][] {
                new double[] {25, 25},
                new double[] {27, 30},
                new double[] {22, 29},
                new double[] {28, 27}
        };

        BestFitRectangle rectangle = new BestFitRectangle(null, input);
        rectangle.findBestFitRectangleCorners();

        System.out.println(Arrays.deepToString(rectangle.getOutput()));

    }

    @Test
    void testComplexNumbers() {

        double[][] input = new double[][] {
                new double[] {25.667, 25.1},
                new double[] {27.99, 30.2},
                new double[] {22.5, 29.12345},
                new double[] {28.678, 27.123}
        };

        BestFitRectangle rectangle = new BestFitRectangle(null, input);
        rectangle.findBestFitRectangleCorners();

        System.out.println(Arrays.deepToString(rectangle.getOutput()));

    }
}