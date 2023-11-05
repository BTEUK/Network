package me.bteuk.network.utils;

import me.bteuk.network.Network;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static java.lang.Math.ceil;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static me.bteuk.network.utils.Constants.EARTH_WORLD;
import static me.bteuk.network.utils.Constants.LOGGER;

/**
 * Scans the region files of the world and outputs a file of all the regions.
 * This is only available for the Earth server.
 */
public class WorldScanner {

    private final Set<RegionFile> regions;

    public WorldScanner() {
        regions = new HashSet<>();
    }

    /**
     * Load all the region file names into the set of regions.
     */
    public void loadRegions() {
        regions.clear();

        File folder = new File(Network.getInstance().getServer().getWorldContainer().getAbsolutePath() + "/" + EARTH_WORLD + "/region");

        if (!folder.exists()) {
            LOGGER.warning("The directory " + folder.getName() + " does not exist.");
            return;
        }

        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            if (file.isFile() && file.getName().endsWith(".mca")) {
                regions.add(new RegionFile(file));
            }
        }

        LOGGER.info("Loaded regions");

    }

    /**
     * Creates an image from the regions, using pixels for the regions. And the x,z coordinates for the location.
     */
    public void drawRegions() {
        final int[] minX = {Integer.MAX_VALUE};
        final int[] maxX = {Integer.MIN_VALUE};
        final int[] minZ = {Integer.MAX_VALUE};
        final int[] maxZ = {Integer.MIN_VALUE};

        //Iterate through all the regions to get the min and max.
        regions.forEach(region -> {
            minX[0] = min(region.x, minX[0]);
            maxX[0] = max(region.x, maxX[0]);
            minZ[0] = min(region.z, minZ[0]);
            maxZ[0] = max(region.z, maxZ[0]);
        });

        int width = maxX[0] - minX[0];
        int height = maxZ[0] - minZ[0];

        int widthSize = (int) ceil(width / 250d);
        int heightSize = (int) ceil(height / 250d);

        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.setColor(Color.white);
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(Color.BLACK);

        regions.forEach(region -> {
            g2d.fillRect(region.x - minX[0], region.z - minZ[0], widthSize, heightSize);
            LOGGER.info("Drawn region at " + (region.x - minX[0]) + ", " + (region.z - minZ[0]));
        });

        File file = new File("regionDrawing.png");
        try {
            ImageIO.write(bufferedImage, "png", file);
            LOGGER.info("Drawn regions to file " + file.getAbsolutePath());

        } catch (IOException e) {
            LOGGER.info("Failed to save image");
        }
    }

    /**
     * Region file stored as x,z
     */
    private static class RegionFile {

        private int x = 0;
        private int z = 0;

        private RegionFile(File file) {
            String[] fileSplit = file.getName().split("\\.");
            if (fileSplit.length == 4) {
                try {
                    x = Integer.parseInt(fileSplit[1]);
                    z = Integer.parseInt(fileSplit[2]);
                } catch (NumberFormatException ex) {
                    LOGGER.info(file.getName() + " has an invalid filename");
                }
            }
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof RegionFile rf) {
                return (rf.x == this.x && rf.z == this.z);
            }
            return false;
        }
    }
}
