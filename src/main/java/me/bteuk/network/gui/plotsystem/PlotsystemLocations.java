package me.bteuk.network.gui.plotsystem;

import me.bteuk.network.Network;
import me.bteuk.network.events.EventManager;
import me.bteuk.network.gui.BuildGui;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.sql.PlotSQL;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.SwitchServer;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.ArrayList;

public class PlotsystemLocations extends Gui {

    private final PlotSQL plotSQL;
    private final GlobalSQL globalSQL;

    private int counter;

    public PlotsystemLocations(NetworkUser user) {

        super(45, Component.text("Plotsystem Locations", NamedTextColor.AQUA, TextDecoration.BOLD));

        plotSQL = Network.getInstance().plotSQL;
        globalSQL = Network.getInstance().globalSQL;

        createGui();

    }

    private void createGui() {

        counter = 0;

        //Get plotsystem locations.
        ArrayList<String> locations = plotSQL.getStringList("SELECT name FROM location_data;");

        //Slot count.
        int slot = 10;

        //Make a button for each plot.
        for (String name : locations) {

            setItem(slot, Utils.createItem(nextIcon(), 1,
                            Utils.title(plotSQL.getString("SELECT alias FROM location_data WHERE name='" + name + "';")),
                            Utils.chat("&fClick to open the menu of this plot.")),
                    u -> {

                        //Teleport to centre of the plotsystem location.
                        //Get coordinate ids for min and max.
                        int min = plotSQL.getInt("SELECT coordMin FROM location_data WHERE name='" + name + "';");
                        int max = plotSQL.getInt("SELECT coordMax FROM location_data WHERE name='" + name + "';");

                        //Get middle.
                        double x = ((globalSQL.getDouble("SELECT x FROM coordinates WHERE id=" + max + ";") +
                                globalSQL.getDouble("SELECT x FROM coordinates WHERE id=" + min + ";"))/2) +
                                plotSQL.getInt("SELECT xTransform FROM location_data WHERE name='" + name + "';");

                        double z = ((globalSQL.getDouble("SELECT z FROM coordinates WHERE id=" + max + ";") +
                                globalSQL.getDouble("SELECT z FROM coordinates WHERE id=" + min + ";"))/2) +
                                plotSQL.getInt("SELECT zTransform FROM location_data WHERE name='" + name + "';");

                        String server = plotSQL.getString("SELECT server FROM location_data WHERE name='" + name + "';");

                        if (server.equals(Network.SERVER_NAME)) {

                            u.player.closeInventory();

                            //Teleport to the location.
                            World world = Bukkit.getWorld(name);
                            double y = world.getHighestBlockYAt((int) x, (int) z);
                            y++;

                            EventManager.createTeleportEvent(false, u.player.getUniqueId().toString(), "network", "teleport " + name + " " + x + " " + y + " " + z + " "
                                            + u.player.getLocation().getYaw() + " " + u.player.getLocation().getPitch(),
                                    "&aTeleported to location &3" + plotSQL.getString("SELECT alias FROM location_data WHERE name='" + name + "';"), u.player.getLocation());

                        } else {

                            //Set the server join event.
                            EventManager.createTeleportEvent(true, u.player.getUniqueId().toString(), "network", "teleport " + name + " " + x + " " + z + " "
                                            + u.player.getLocation().getYaw() + " " + u.player.getLocation().getPitch(),
                                    "&aTeleported to location &3" + plotSQL.getString("SELECT alias FROM location_data WHERE name='" + name + "';"), u.player.getLocation());

                            //Teleport them to another server.
                            this.delete();
                            SwitchServer.switchServer(u.player, server);

                        }
                    });

            //Increase slot accordingly.
            if (slot % 9 == 7) {
                //Increase row, basically add 3.
                slot += 3;
            } else {
                //Increase value by 1.
                slot++;
            }
        }

        //Return
        setItem(44, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.chat("&b&lReturn"),
                        Utils.chat("&fOpen the building menu.")),
                u -> {

                    //Delete this gui.
                    this.delete();
                    u.mainGui = null;

                    //Switch to plot info.
                    u.mainGui = new BuildGui(u);
                    u.mainGui.open(u);

                });
    }

    public void refresh() {

        this.clearGui();
        createGui();

    }

    private Material nextIcon() {

        Material mat;

        switch (counter) {

            case 1 -> mat = Material.SPRUCE_BOAT;
            case 2 -> mat = Material.BIRCH_BOAT;
            case 3 -> mat = Material.JUNGLE_BOAT;
            case 4 -> mat = Material.ACACIA_BOAT;
            case 5 -> mat = Material.DARK_OAK_BOAT;
            default -> mat = Material.OAK_BOAT;

        }

        if (counter == 5) {
            counter = 0;
        } else {
            counter++;
        }

        return mat;

    }
}
