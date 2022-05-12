package me.bteuk.network.gui;

import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.enums.PlotDifficulty;
import me.bteuk.network.utils.enums.PlotSize;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

public class PlotServerLocations {

    public static UniqueGui getPlotServerLocations(NetworkUser user) {

        UniqueGui gui = new UniqueGui(27, Component.text("Plot Locations", NamedTextColor.AQUA, TextDecoration.BOLD));

        //Select the plot difficulty and size material and text.
        Material mDifficulty = Material.GRAY_CONCRETE;
        String sDifficulty = "Random";
        Material mSize = Material.GRAY_CONCRETE;
        String sSize = "Random";

        if (user.plotDifficulty == PlotDifficulty.EASY) {
            mDifficulty = Material.LIME_CONCRETE;
            sDifficulty = "Easy";
        } else if (user.plotDifficulty == PlotDifficulty.NORMAL) {
            mDifficulty = Material.YELLOW_CONCRETE;
            sDifficulty = "Normal";
        } else if (user.plotDifficulty == PlotDifficulty.HARD) {
            mDifficulty = Material.RED_CONCRETE;
            sDifficulty = "Hard";
        }

        if (user.plotSize == PlotSize.SMALL) {
            mSize = Material.LIME_CONCRETE;
            sSize = "Small";
        } else if (user.plotSize == PlotSize.MEDIUM) {
            mSize = Material.YELLOW_CONCRETE;
            sSize = "Medium";
        } else if (user.plotSize == PlotSize.LARGE) {
            mSize = Material.RED_CONCRETE;
            sSize = "Large";
        }

        //Select plot difficulty.
        gui.setItem(12, Utils.createItem(mDifficulty, 1,
                        Utils.chat("&b&l" + sDifficulty),
                        Utils.chat("&fClick to toggle the difficulty."),
                        Utils.chat("&fYou will only be teleported to"),
                        Utils.chat("&fplots of the selected difficulty.")),
                u ->

                {

                    //Update the difficulty.
                    if (u.plotDifficulty == PlotDifficulty.EASY) {

                        //Set plot difficulty to next level.
                        //If they are at least apprentice increase to normal, else return to random.
                        if (u.player.hasPermission("group.apprentice")) {
                            u.plotDifficulty = PlotDifficulty.NORMAL;
                        } else {
                            u.plotDifficulty = PlotDifficulty.RANDOM;
                        }

                    } else if (u.plotDifficulty == PlotDifficulty.NORMAL) {

                        //Set plot difficulty to next level.
                        //If they are at least jr.builder increase to hard, else return to random.
                        if (u.player.hasPermission("group.jrbuilder")) {
                            u.plotDifficulty = PlotDifficulty.HARD;
                        } else {
                            u.plotDifficulty = PlotDifficulty.RANDOM;
                        }

                    } else if (u.plotDifficulty == PlotDifficulty.HARD) {

                        //Return the plot difficulty to random.
                        u.plotDifficulty = PlotDifficulty.RANDOM;

                    } else {

                        //Difficulty was set to random previously.
                        //Increase the plot difficulty to easy.
                        u.plotDifficulty = PlotDifficulty.EASY;

                    }

                    //Update the inventory.
                    u.uniqueGui.delete();
                    u.uniqueGui = PlotServerLocations.getPlotServerLocations(u);
                    u.uniqueGui.update(u);
                    u.player.getInventory().setContents(u.uniqueGui.getInventory().getContents());

                });

        //Select plot size.
        gui.setItem(12, Utils.createItem(mSize, 1,
                        Utils.chat("&b&l" + sSize),
                        Utils.chat("&fClick to toggle the size."),
                        Utils.chat("&fYou will only be teleported to"),
                        Utils.chat("&fplots of the selected size.")),
                u ->

                {

                    //Update the Size.
                    if (u.plotSize == PlotSize.SMALL) {

                        //Set plot Size to next level.
                        //If they are at least apprentice increase to normal, else return to random.
                        if (u.player.hasPermission("group.apprentice")) {
                            u.plotSize = PlotSize.MEDIUM;
                        } else {
                            u.plotSize = PlotSize.RANDOM;
                        }

                    } else if (u.plotSize == PlotSize.MEDIUM) {

                        //Set plot Size to next level.
                        //If they are at least jr.builder increase to hard, else return to random.
                        if (u.player.hasPermission("group.jrbuilder")) {
                            u.plotSize = PlotSize.LARGE;
                        } else {
                            u.plotSize = PlotSize.RANDOM;
                        }

                    } else if (u.plotSize == PlotSize.LARGE) {

                        //Return the plot Size to random.
                        u.plotSize = PlotSize.RANDOM;

                    } else {

                        //Size was set to random previously.
                        //Increase the plot Size to easy.
                        u.plotSize = PlotSize.SMALL;

                    }

                    //Update the inventory.
                    u.uniqueGui.delete();
                    u.uniqueGui = PlotServerLocations.getPlotServerLocations(u);
                    u.uniqueGui.update(u);
                    u.player.getInventory().setContents(u.uniqueGui.getInventory().getContents());

                });


        //Select plot size.
        gui.setItem(12, Utils.createItem(Material.DIAMOND_PICKAXE, 1,
                        Utils.chat("&b&lPlot Locations"),
                        Utils.chat("&fClick to choose a location to build a plot.")),
                u ->

                {

                    //Open the build gui.
                    u.uniqueGui = PlotServerLocations.getPlotServerLocations(u);
                    u.uniqueGui.open(u);

                });


        //Choose location.
        gui.setItem(12, Utils.createItem(Material.DIAMOND_PICKAXE, 1,
                        Utils.chat("&b&lPlot Locations"),
                        Utils.chat("&fClick to choose a location to build a plot.")),
                u ->

                {

                    //Open the build gui.
                    u.uniqueGui = PlotServerLocations.getPlotServerLocations(u);
                    u.uniqueGui.open(u);

                });

        return gui;

    }
}
