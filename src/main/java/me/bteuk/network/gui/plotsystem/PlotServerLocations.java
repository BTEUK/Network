package me.bteuk.network.gui.plotsystem;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.bteuk.network.Network;
import me.bteuk.network.gui.BuildGui;
import me.bteuk.network.gui.UniqueGui;
import me.bteuk.network.sql.PlotSQL;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

import java.util.ArrayList;

public class PlotServerLocations {

    public static UniqueGui getPlotServerLocations(NetworkUser user) {

        UniqueGui gui = new UniqueGui(45, Component.text("Plot Locations", NamedTextColor.AQUA, TextDecoration.BOLD));
        PlotSQL plotSQL = Network.getInstance().plotSQL;

        //Select the plot difficulty and size material and text.
        Material mDifficulty = Material.GRAY_CONCRETE;
        String sDifficulty = "Random";
        Material mSize = Material.GRAY_CONCRETE;
        String sSize = "Random";

        if (user.plotDifficulty == 1) {
            mDifficulty = Material.LIME_CONCRETE;
            sDifficulty = "Easy";
        } else if (user.plotDifficulty == 2) {
            mDifficulty = Material.YELLOW_CONCRETE;
            sDifficulty = "Normal";
        } else if (user.plotDifficulty == 3) {
            mDifficulty = Material.RED_CONCRETE;
            sDifficulty = "Hard";
        }

        if (user.plotSize == 1) {
            mSize = Material.LIME_CONCRETE;
            sSize = "Small";
        } else if (user.plotSize == 2) {
            mSize = Material.YELLOW_CONCRETE;
            sSize = "Medium";
        } else if (user.plotSize == 3) {
            mSize = Material.RED_CONCRETE;
            sSize = "Large";
        }

        //Select plot difficulty.
        gui.setItem(3, Utils.createItem(mDifficulty, 1,
                        Utils.chat("&b&l" + sDifficulty),
                        Utils.chat("&fClick to toggle the difficulty."),
                        Utils.chat("&fYou will only be teleported to"),
                        Utils.chat("&fplots of the selected difficulty.")),
                u ->

                {

                    //Update the difficulty.
                    if (u.plotDifficulty == 1) {

                        //Set plot difficulty to next level.
                        //If they are at least apprentice increase to normal, else return to random.
                        if (u.player.hasPermission("group.apprentice")) {
                            u.plotDifficulty = 2;
                        } else {
                            u.plotDifficulty = 0;
                        }

                    } else if (u.plotDifficulty == 2) {

                        //Set plot difficulty to next level.
                        //If they are at least jr.builder increase to hard, else return to random.
                        if (u.player.hasPermission("group.jrbuilder")) {
                            u.plotDifficulty = 3;
                        } else {
                            u.plotDifficulty = 0;
                        }

                    } else if (u.plotDifficulty == 3) {

                        //Return the plot difficulty to random.
                        u.plotDifficulty = 0;

                    } else {

                        //Difficulty was set to random previously.
                        //Increase the plot difficulty to easy.
                        u.plotDifficulty = 1;

                    }

                    //Update the gui.
                    u.uniqueGui.update(u, PlotServerLocations.getPlotServerLocations(u));

                });

        //Select plot size.
        gui.setItem(5, Utils.createItem(mSize, 1,
                        Utils.chat("&b&l" + sSize),
                        Utils.chat("&fClick to toggle the size."),
                        Utils.chat("&fYou will only be teleported to"),
                        Utils.chat("&fplots of the selected size.")),
                u ->

                {

                    //Update the Size.
                    if (u.plotSize == 1) {

                        //Set plot Size to next level.
                        //If they are at least apprentice increase to normal, else return to random.
                        if (u.player.hasPermission("group.apprentice")) {
                            u.plotSize = 2;
                        } else {
                            u.plotSize = 0;
                        }

                    } else if (u.plotSize == 2) {

                        //Set plot Size to next level.
                        //If they are at least jr.builder increase to hard, else return to random.
                        if (u.player.hasPermission("group.jrbuilder")) {
                            u.plotSize = 3;
                        } else {
                            u.plotSize = 0;
                        }

                    } else if (u.plotSize == 3) {

                        //Return the plot Size to random.
                        u.plotSize = 0;

                    } else {

                        //Size was set to random previously.
                        //Increase the plot Size to easy.
                        u.plotSize = 1;

                    }

                    //Update the gui.
                    u.uniqueGui.update(u, PlotServerLocations.getPlotServerLocations(u));

                });

        //Get all locations from database.
        ArrayList<String> locations = plotSQL.getStringList("SELECT name FROM location_data");

        //Starting slot.
        int slot = 10;

        //Iterate through locations and add them to the gui.
        for (String location : locations) {

            //Create location button.
            gui.setItem(slot, Utils.createItem(Material.DIAMOND_PICKAXE, 1,
                            Utils.chat("&b&l" + location),
                            Utils.chat("&fClick to teleport to a plot in this location"),
                            Utils.chat("&fsubject to the settings shown above."),
                            Utils.chat("&fAvailable plots of each difficulty:"),
                            Utils.chat("&fEasy: &7" + plotSQL.getInt("SELECT count(id) FROM plot_data WHERE location='" + location + "' AND status='unclaimed' AND difficulty=1;")),
                            Utils.chat("&fMedium: &7" + plotSQL.getInt("SELECT count(id) FROM plot_data WHERE location='" + location + "' AND status='unclaimed' AND difficulty=2;")),
                            Utils.chat("&fHard: &7" + plotSQL.getInt("SELECT count(id) FROM plot_data WHERE location='" + location + "' AND status='unclaimed' AND difficulty=3;"))),
                    u ->

                    {

                        //Check if a plot is available with the given parameters.
                        //If difficulty and size are 0 pick a random plot within the parameters that is allowed for the player.
                        int id;

                        if (u.plotDifficulty == 0 && u.plotSize == 0) {

                            if (u.player.hasPermission("group.jrbuilder")) {

                                //Select a random plot of any difficulty.
                                id = Network.getInstance().plotSQL.getInt("SELECT id FROM plot_data WHERE location='" + location + "' AND status='unclaimed' ORDER BY RAND() LIMIT 1;");

                            } else if (u.player.hasPermission("group.apprentice")) {

                                //Select a random plot of difficulty easy and normal.
                                id = Network.getInstance().plotSQL.getInt("SELECT id FROM plot_data WHERE location='" + location + "' AND status='unclaimed' AND (difficulty=1 OR difficulty=2) ORDER BY RAND() LIMIT 1;");

                            } else {

                                //Select a random plot of difficulty easy.
                                id = Network.getInstance().plotSQL.getInt("SELECT id FROM plot_data WHERE location='" + location + "' AND status='unclaimed' AND difficulty=1 ORDER BY RAND() LIMIT 1;");

                            }

                        } else if (u.plotDifficulty == 0) {
                            //Pick plot with random difficulty but fixed size.

                            if (u.player.hasPermission("group.jrbuilder")) {

                                //Select a random plot of any difficulty.
                                id = Network.getInstance().plotSQL.getInt("SELECT id FROM plot_data WHERE location='" + location + "' AND status='unclaimed' AND size=" + u.plotSize + " ORDER BY RAND() LIMIT 1;");

                            } else if (u.player.hasPermission("group.apprentice")) {

                                //Select a random plot of difficulty easy and normal.
                                id = Network.getInstance().plotSQL.getInt("SELECT id FROM plot_data WHERE location='" + location + "' AND status='unclaimed' AND (difficulty=1 OR difficulty=2) AND size=" + u.plotSize + " ORDER BY RAND() LIMIT 1;");

                            } else {

                                //Select a random plot of difficulty easy.
                                id = Network.getInstance().plotSQL.getInt("SELECT id FROM plot_data WHERE location='" + location + "' AND status='unclaimed' AND difficulty=1 AND size=" + u.plotSize + "  ORDER BY RAND() LIMIT 1;");

                            }


                        } else if (u.plotSize == 0) {
                            //Pick plot with random size but fixed difficulty.

                            id = Network.getInstance().plotSQL.getInt("SELECT id FROM plot_data WHERE location='" + location + "' AND status='unclaimed' AND difficulty=" + u.plotDifficulty + " ORDER BY RAND() LIMIT 1;");

                        } else {
                            //Both size and difficulty are specified.

                            //Select a random plot of any difficulty.
                            id = Network.getInstance().plotSQL.getInt("SELECT id FROM plot_data WHERE location='" + location + "' AND status='unclaimed' AND difficulty=" + u.plotDifficulty + " AND size=" + u.plotSize + " ORDER BY RAND() LIMIT 1;");

                        }

                        //If no plots fit the specified parameters the id will be 0.
                        if (id == 0) {

                            u.player.sendMessage("&cNo plots are available with the specified settings,");
                            u.player.sendMessage("&ctry another location or change the settings.");

                        } else {

                            //Get the server of the plot.
                            String server = Network.getInstance().plotSQL.getString("SELECT server FROM location_data WHERE name='"
                                    + Network.getInstance().plotSQL.getString("SELECT location FROM plot_data WHERE id=" + id + ";")
                                    + "';");

                            //If the plot is on the current server teleport them directly.
                            //Else teleport them to the correct server and them teleport them to the plot.
                            if (server.equals(Network.SERVER_NAME)) {

                                u.player.closeInventory();
                                Network.getInstance().globalSQL.update("INSERT INTO server_events(uuid,type,server,event) VALUES('"
                                        + u.player.getUniqueId() + "','plotsystem','"
                                        + Network.SERVER_NAME
                                        + "','teleport plot " + id + "');");

                            } else {

                                //Set the server join event.
                                Network.getInstance().globalSQL.update("INSERT INTO join_events(uuid,type,event) VALUES('"
                                        + u.player.getUniqueId() + "','plotsystem','teleport plot " + id + "');");

                                //Teleport them to another server.
                                u.player.closeInventory();
                                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                                out.writeUTF("Connect");
                                out.writeUTF(server);

                            }
                        }

                    });

            //Increase slot accordingly.
            if (slot%9 == 7) {
                //Increase row, basically add 3.
                slot += 3;
            } else if (slot == 34) {
                //Last possible slot, end iteration.
                break;
            } else {
                //Increase value by 1.
                slot++;
            }

        }

        //Return
        gui.setItem(12, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.chat("&b&lReturn"),
                        Utils.chat("&fOpen the build menu.")),
                u ->

                {

                    //Delete this inventory.
                    u.uniqueGui.delete();
                    u.player.closeInventory();

                    //Open the build menu.
                    u.uniqueGui = BuildGui.createBuildGui(u);
                    u.uniqueGui.open(u);

                });

        return gui;

    }
}
