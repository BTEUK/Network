package me.bteuk.network.gui.plotsystem;

import me.bteuk.network.Network;
import me.bteuk.network.events.EventManager;
import me.bteuk.network.gui.BuildGui;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.sql.PlotSQL;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Roles;
import me.bteuk.network.utils.SwitchServer;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

import java.util.ArrayList;

public class PlotServerLocations extends Gui {

    private int plotDifficulty;
    private Material mDifficulty;
    private String sDifficulty;

    private int plotSize;
    private Material mSize;
    private String sSize;

    private final PlotSQL plotSQL;

    public PlotServerLocations(NetworkUser u) {

        super(45, Component.text("Plot Locations", NamedTextColor.AQUA, TextDecoration.BOLD));

        plotSQL = Network.getInstance().plotSQL;

        //Set default values of gui.

        //Default difficulty will depend on the role of the player.
        //If the player is a guest, default will be easy, if apprentice default will be normal and if jrbuilder default will be hard.
        //All other roles will have default set to random.
        if (Roles.builderRole(u.player).equals("guest")) {
            plotDifficulty = 1;
        } else if (Roles.builderRole(u.player).equals("apprentice")) {
            plotDifficulty = 2;
        } else if (Roles.builderRole(u.player).equals("jrbuilder")) {
            plotDifficulty = 3;
        } else {
            plotDifficulty = 0;
        }
        setDifficulty();

        plotSize = 0;
        mSize = Material.GRAY_CONCRETE;
        sSize = "Random";

        //Create gui.
        createGui();

    }

    private void setDifficulty() {
        if (plotDifficulty == 1) {
            mDifficulty = Material.LIME_CONCRETE;
            sDifficulty = "Easy";
        } else if (plotDifficulty == 2) {
            mDifficulty = Material.YELLOW_CONCRETE;
            sDifficulty = "Normal";
        } else if (plotDifficulty == 3) {
            mDifficulty = Material.RED_CONCRETE;
            sDifficulty = "Hard";
        } else {
            mDifficulty = Material.GRAY_CONCRETE;
            sDifficulty = "Random";
        }
    }

    private void setSize() {
        if (plotSize == 1) {
            mSize = Material.LIME_CONCRETE;
            sSize = "Small";
        } else if (plotSize == 2) {
            mSize = Material.YELLOW_CONCRETE;
            sSize = "Medium";
        } else if (plotSize == 3) {
            mSize = Material.RED_CONCRETE;
            sSize = "Large";
        } else {
            mSize = Material.GRAY_CONCRETE;
            sSize = "Random";
        }
    }

    private void createGui() {

        setDifficulty();

        setSize();

        //Select plot difficulty.
        setItem(3, Utils.createItem(mDifficulty, 1,
                        Utils.chat("&b&l" + sDifficulty),
                        Utils.chat("&fClick to toggle the difficulty."),
                        Utils.chat("&fYou will only be teleported to"),
                        Utils.chat("&fplots of the selected difficulty.")),
                u ->

                {

                    //Update the difficulty.
                    plotDifficulty = (plotDifficulty == 3) ? 0 : plotDifficulty + 1;

                    //Update the gui.
                    this.refresh();
                    u.player.getOpenInventory().getTopInventory().setContents(getInventory().getContents());

                });

        //Select plot size.
        setItem(5, Utils.createItem(mSize, 1,
                        Utils.chat("&b&l" + sSize),
                        Utils.chat("&fClick to toggle the size."),
                        Utils.chat("&fYou will only be teleported to"),
                        Utils.chat("&fplots of the selected size.")),
                u ->

                {

                    //Update the Size.
                    plotSize = (plotSize == 3) ? 0 : plotSize + 1;

                    //Update the gui.
                    this.refresh();
                    u.player.getOpenInventory().getTopInventory().setContents(getInventory().getContents());

                });

        //Get all locations from database.
        ArrayList<String> locations = plotSQL.getStringList("SELECT name FROM location_data");

        //Starting slot.
        int slot = 10;

        //Iterate through locations and add them to the gui.
        for (String location : locations) {

            //Create location button.
            setItem(slot, Utils.createItem(Material.DIAMOND_PICKAXE, 1,
                            Utils.chat("&b&l" + plotSQL.getString("SELECT alias FROM location_data WHERE name='" + location + "';")),
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

                        if (plotDifficulty == 0 && plotSize == 0) {

                            if (Roles.builderRole(u.player).equals("jrbuilder")) {

                                //Select a random plot of the hard difficulty.
                                //Since this is the next plot difficulty to get Builder.
                                id = Network.getInstance().plotSQL.getInt("SELECT id FROM plot_data WHERE location = '" + location +
                                        "' AND status='unclaimed' AND difficulty=3 ORDER BY RAND() LIMIT 1;");

                            } else if (Roles.builderRole(u.player).equals("apprentice")) {

                                //Select a random plot of the normal difficulty.
                                //Since this is the next plot difficulty to get Jr.Builder.
                                id = Network.getInstance().plotSQL.getInt("SELECT id FROM plot_data WHERE location = '" + location +
                                        "' AND status='unclaimed' AND difficulty=2 ORDER BY RAND() LIMIT 1;");

                            } else if (Roles.builderRole(u.player).equals("default")) {

                                //Select a random plot of the easy difficulty.
                                //Since this is the next plot difficulty to get Apprentice.
                                id = Network.getInstance().plotSQL.getInt("SELECT id FROM plot_data WHERE location = '" + location +
                                        "' AND status='unclaimed' AND difficulty=1 ORDER BY RAND() LIMIT 1;");

                            } else {

                                //Select a random plot of any difficulty.
                                id = Network.getInstance().plotSQL.getInt("SELECT id FROM plot_data WHERE location = '" + location +
                                        "' AND status='unclaimed' ORDER BY RAND() LIMIT 1;");

                            }

                        } else if (plotDifficulty == 0) {
                            //Pick plot with random difficulty but fixed size.

                            if (Roles.builderRole(u.player).equals("jrbuilder")) {

                                //Select a random plot of the hard difficulty.
                                //Since this is the next plot difficulty to get Builder.
                                id = Network.getInstance().plotSQL.getInt("SELECT id FROM plot_data WHERE location = '" + location +
                                        "' AND status='unclaimed' AND difficulty=3 AND size=" + plotSize + " ORDER BY RAND() LIMIT 1;");

                            } else if (Roles.builderRole(u.player).equals("apprentice")) {

                                //Select a random plot of the normal difficulty.
                                //Since this is the next plot difficulty to get Jr.Builder.
                                id = Network.getInstance().plotSQL.getInt("SELECT id FROM plot_data WHERE location = '" + location +
                                        "' AND status='unclaimed' AND difficulty=2 AND size=" + plotSize + " ORDER BY RAND() LIMIT 1;");

                            } else if (Roles.builderRole(u.player).equals("default")) {

                                //Select a random plot of the easy difficulty.
                                //Since this is the next plot difficulty to get Apprentice.
                                id = Network.getInstance().plotSQL.getInt("SELECT id FROM plot_data WHERE location = '" + location +
                                        "' AND status='unclaimed' AND difficulty=1 AND size=" + plotSize + " ORDER BY RAND() LIMIT 1;");

                            } else {
                                //Select a random plot of any difficulty.
                                id = Network.getInstance().plotSQL.getInt("SELECT id FROM plot_data WHERE location = '" + location +
                                        "' AND status='unclaimed' AND size=" + plotSize + " ORDER BY RAND() LIMIT 1;");
                            }


                        } else if (plotSize == 0) {
                            //Pick plot with random size but fixed difficulty.

                            id = Network.getInstance().plotSQL.getInt("SELECT id FROM plot_data WHERE location='" + location +
                                    "' AND status='unclaimed' AND difficulty=" + plotDifficulty + " ORDER BY RAND() LIMIT 1;");

                        } else {
                            //Both size and difficulty are specified.

                            //Select a random plot of any difficulty.
                            id = Network.getInstance().plotSQL.getInt("SELECT id FROM plot_data WHERE location='" + location +
                                    "' AND status='unclaimed' AND difficulty=" + plotDifficulty + " AND size=" + plotSize + " ORDER BY RAND() LIMIT 1;");

                        }

                        //If no plots fit the specified parameters the id will be 0.
                        if (id == 0) {

                            u.player.sendMessage(Utils.chat("&cNo plots are available with the specified settings,"));
                            u.player.sendMessage(Utils.chat("&ctry another location or change the settings."));

                        } else {

                            //Get the server of the plot.
                            String server = Network.getInstance().plotSQL.getString("SELECT server FROM location_data WHERE name='"
                                    + Network.getInstance().plotSQL.getString("SELECT location FROM plot_data WHERE id=" + id + ";")
                                    + "';");

                            //If the plot is on the current server teleport them directly.
                            //Else teleport them to the correct server and then teleport them to the plot.
                            if (server.equals(Network.SERVER_NAME)) {

                                u.player.closeInventory();

                                EventManager.createTeleportEvent(false, u.player.getUniqueId().toString(), "plotsystem", "teleport plot " + id, u.player.getLocation());

                            } else {

                                //Set the server join event.
                                EventManager.createTeleportEvent(true, u.player.getUniqueId().toString(), "plotsystem", "teleport plot " + id, u.player.getLocation());

                                //Teleport them to another server.
                                this.delete();
                                SwitchServer.switchServer(u.player, server);

                            }
                        }

                    });

            //Increase slot accordingly.
            if (slot % 9 == 7) {
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
        setItem(44, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.chat("&b&lReturn"),
                        Utils.chat("&fOpen the build menu.")),
                u ->

                {

                    //Switch back to build menu.
                    //Delete this gui.
                    this.delete();
                    u.mainGui = null;

                    //Create build menu.
                    u.mainGui = new BuildGui(u);
                    u.mainGui.open(u);

                });
    }

    public void refresh() {

        clearGui();
        createGui();

    }
}
