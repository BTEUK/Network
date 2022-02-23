package me.bteuk.network.gui;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.bteuk.network.Main;
import me.bteuk.network.utils.ServerType;
import me.bteuk.network.utils.User;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

public class BuildGui {

    public static UniqueGui createBuildGui(User user) {

        UniqueGui gui = new UniqueGui(27, Component.text("Building Menu", NamedTextColor.AQUA, TextDecoration.BOLD));

        //Teleport to random unclaimed plot.
        gui.setItem(12, Utils.createItem(Material.ENDER_PEARL, 1,
                        Utils.chat("&b&lRandom Plot"),
                        Utils.chat("&fClick teleport to a random claimable plot.")),
                u -> {

                    int id = 0;

                    if (u.player.hasPermission("group.jrbuilder")) {

                        //Select a random plot of any difficulty.
                        id = Main.getInstance().plotSQL.getInt("SELECT id FROM plot_data WHERE status='unclaimed' ORDER BY RAND() LIMIT 1;");

                    } else if (u.player.hasPermission("group.apprentice")) {

                        //Select a random plot of difficulty easy and normal.
                        id = Main.getInstance().plotSQL.getInt("SELECT id FROM plot_data WHERE status='unclaimed' AND (difficulty=1 OR difficulty=2) ORDER BY RAND() LIMIT 1;");

                    } else {

                        //Select a random plot of difficulty easy.
                        id = Main.getInstance().plotSQL.getInt("SELECT id FROM plot_data WHERE status='unclaimed' AND difficulty=1 ORDER BY RAND() LIMIT 1;");

                    }

                    if (id == 0) {

                        u.player.sendMessage(Utils.chat("&cThere are no plots available, please wait new plots to be added."));
                        u.player.closeInventory();

                    } else {

                        //Get the server of the plot.
                        String server = Main.getInstance().plotSQL.getString("SELECT server FROM location_data WHERE name="
                                + Main.getInstance().plotSQL.getString("SELECT location FROM plot_data WHERE id=" + id + ";")
                                + ";");

                        //If the plot is on the current server teleport them directly.
                        //Else teleport them to the correct server and them teleport them to the plot.
                        if (server == Main.SERVER_NAME) {

                            u.player.closeInventory();
                            Main.getInstance().globalSQL.insert("INSERT INTO server_events(uuid,server,event) VALUES("
                                    + u.player.getUniqueId()
                                    + "," + Main.SERVER_NAME
                                    + ",teleport plot " + id + ");");

                        } else {

                            //Set the server join event.
                            Main.getInstance().globalSQL.insert("INSERT INTO join_events(uuid,event) VALUES("
                                    + u.player.getUniqueId()
                                    + "," + "teleport plot " + id + ");");

                            //Teleport them to another server.
                            u.player.closeInventory();
                            ByteArrayDataOutput out = ByteStreams.newDataOutput();
                            out.writeUTF("Connect");
                            out.writeUTF(server);

                        }
                    }
                });

        //Claim plot
        gui.setItem(12, Utils.createItem(Material.EMERALD, 1,
                        Utils.chat("&b&lRandom Plot"),
                        Utils.chat("&fClick teleport to a random claimable plot.")),
                u -> {

                    //If server type is plot, then send a plot claim event to the database.
                    if (Main.SERVER_TYPE == ServerType.PLOT) {

                        //Set the claim event.
                        u.player.closeInventory();
                        Main.getInstance().globalSQL.insert("INSERT INTO server_events(uuid,server,event) VALUES("
                                + u.player.getUniqueId()
                                + "," + Main.SERVER_NAME
                                + ",claim plot);");

                    } else {

                        u.player.closeInventory();
                        u.player.sendMessage(Utils.chat("&cYou must be standing in a plot to claim it."));

                    }
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

        //Join region (Jr.Builder+ only)
        gui.setItem(12, Utils.createItem(Material.DIAMOND_PICKAXE, 1,
                        Utils.chat("&b&lPlot Locations"),
                        Utils.chat("&fClick to choose a location to build a plot.")),
                u -> {

                    if (Main.SERVER_TYPE == ServerType.EARTH) {

                        //If the user is Jr.Builder+ go through the region joining process.
                        if (u.player.hasPermission("group.jrbuilder")) {


                        } else {

                            //If they are not a Jr.Builder, cancel.
                            u.player.closeInventory();
                            u.player.sendMessage(Utils.chat("&cYou must be at least a Jr.Builder to claim a region."));

                        }

                    } else {

                        //If the server is not an earth server, cancel.
                        u.player.closeInventory();
                        u.player.sendMessage(Utils.chat("&cThis is not a valid region to claim."));

                    }
                });

        //Plot menu.
        gui.setItem(12, Utils.createItem(Material.CHEST, 1,
                        Utils.chat("&b&lPlot Menu"),
                        Utils.chat("&fView all your active plots.")),
                u -> {

                    //Delete this inventory.
                    u.uniqueGui.delete();
                    u.player.closeInventory();

                    //Open the plot menu.
                    u.uniqueGui = PlotMenu.getPlotMenu(u);
                    u.uniqueGui.open(u);

                });

        //Feedback menu.
        //Create item

        //Tutorial menu.
        //Create item

        //Spawn
        gui.setItem(12, Utils.createItem(Material.DIAMOND_PICKAXE, 1,
                        Utils.chat("&b&lSpawn"),
                        Utils.chat("&fTeleport to spawn.")),
                u ->

                {

                    //Connect to the lobby server.
                    u.player.closeInventory();
                    ByteArrayDataOutput out = ByteStreams.newDataOutput();
                    out.writeUTF("Connect");
                    out.writeUTF(Main.getInstance().navigationSQL.getString("SELECT FROM server_data WHERE type='lobby';"));

                });

        //Return
        gui.setItem(12, Utils.createItem(Material.DIAMOND_PICKAXE, 1,
                        Utils.chat("&b&lReturn"),
                        Utils.chat("&fOpen the navigator main menu.")),
                u ->

                {

                    //Open the navigator.
                    u.player.closeInventory();
                    Main.getInstance().navigator.open(u);

                });

        return gui;

    }
}