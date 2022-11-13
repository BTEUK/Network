package me.bteuk.network.gui;

import me.bteuk.network.Network;
import me.bteuk.network.commands.Back;
import me.bteuk.network.events.EventManager;
import me.bteuk.network.gui.plotsystem.PlotMenu;
import me.bteuk.network.gui.plotsystem.PlotServerLocations;
import me.bteuk.network.gui.regions.RegionMenu;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.SwitchServer;
import me.bteuk.network.utils.enums.ServerType;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.regions.Region;
import me.bteuk.network.utils.regions.RegionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

public class BuildGui extends Gui {

    private final NetworkUser user;

    public BuildGui(NetworkUser user) {

        super(27, Component.text("Building Menu", NamedTextColor.AQUA, TextDecoration.BOLD));

        this.user = user;

        createGui();

    }

    private void createGui() {

        //Teleport to random unclaimed plot.
        setItem(20, Utils.createItem(Material.ENDER_PEARL, 1,
                        Utils.chat("&b&lRandom Plot"),
                        Utils.chat("&fClick teleport to a random claimable plot.")),
                u -> {

                    int id;

                    if (u.player.hasPermission("group.jrbuilder")) {

                        //Select a random plot of any difficulty.
                        id = Network.getInstance().plotSQL.getInt("SELECT id FROM plot_data WHERE status='unclaimed' ORDER BY RAND() LIMIT 1;");

                    } else if (u.player.hasPermission("group.apprentice")) {

                        //Select a random plot of difficulty easy and normal.
                        id = Network.getInstance().plotSQL.getInt("SELECT id FROM plot_data WHERE status='unclaimed' AND (difficulty=1 OR difficulty=2) ORDER BY RAND() LIMIT 1;");

                    } else {

                        //Select a random plot of difficulty easy.
                        id = Network.getInstance().plotSQL.getInt("SELECT id FROM plot_data WHERE status='unclaimed' AND difficulty=1 ORDER BY RAND() LIMIT 1;");

                    }

                    if (id == 0) {

                        u.player.sendMessage(Utils.chat("&cThere are no plots available, please wait for new plots to be added."));
                        u.player.closeInventory();

                    } else {

                        //Get the server of the plot.
                        String server = Network.getInstance().plotSQL.getString("SELECT server FROM location_data WHERE name='"
                                + Network.getInstance().plotSQL.getString("SELECT location FROM plot_data WHERE id=" + id + ";")
                                + "';");

                        //If the plot is on the current server teleport them directly.
                        //Else teleport them to the correct server and them teleport them to the plot.
                        if (server.equals(Network.SERVER_NAME)) {

                            u.player.closeInventory();

                            //Set current location for /back
                            Back.setPreviousCoordinate(u.player.getUniqueId().toString(), u.player.getLocation());

                            EventManager.createTeleportEvent(false, u.player.getUniqueId().toString(), "plotsystem", "teleport plot " + id, u.player.getLocation());

                        } else {

                            //Set the server join event.
                            EventManager.createTeleportEvent(true, u.player.getUniqueId().toString(), "plotsystem", "teleport plot " + id, u.player.getLocation());

                            //Teleport them to another server.
                            u.player.closeInventory();
                            SwitchServer.switchServer(u.player, server);

                        }
                    }
                });

        //Claim plot
        setItem(2, Utils.createItem(Material.EMERALD, 1,
                        Utils.chat("&b&lClaim Plot"),
                        Utils.chat("&fClick to claim the plot your are currently standing in.")),
                u -> {

                    //If server type is plot, then send a plot claim event to the database.
                    if (Network.SERVER_TYPE == ServerType.PLOT) {

                        //Set the claim event.
                        u.player.closeInventory();
                        Network.getInstance().globalSQL.update("INSERT INTO server_events(uuid,type,server,event) VALUES('"
                                + u.player.getUniqueId()
                                + "','plotsystem','" + Network.SERVER_NAME
                                + "','claim plot');");

                    } else {

                        u.player.closeInventory();
                        u.player.sendMessage(Utils.chat("&cYou must be standing in a plot to claim it."));

                    }
                });


        //Choose location.
        setItem(19, Utils.createItem(Material.DIAMOND_PICKAXE, 1,
                        Utils.chat("&b&lPlot Locations"),
                        Utils.chat("&fClick to choose a location to build a plot.")),
                u ->

                {

                    //Delete this gui.
                    this.delete();
                    u.mainGui = null;

                    //Switch to the plot location gui.
                    u.mainGui = new PlotServerLocations();
                    u.mainGui.open(u);

                });

        //Join region (Jr.Builder+ only)
        //If region is claimable.
        //Check if the player is in a region.
        //TODO: If the player is not able to claim a region change the icon.
        if (user.inRegion) {

            //Check if region is claimable.
            if (user.region.isClaimable()) {

                //If the region has an owner.
                if (user.region.hasOwner()) {

                    //Check if the region is public.
                    if (user.region.isPublic()) {

                        setItem(5, Utils.createItem(Material.DARK_OAK_DOOR, 1,
                                        Utils.chat("&b&lJoin Region"),
                                        Utils.chat("&fClick to join the region you are standing in."),
                                        Utils.chat("&fThe region is owned by " + user.region.ownerName() + "."),
                                        Utils.chat("&fThe region is public, so they don't need to accept your request.")),
                                u -> {

                                    //If the user is Jr.Builder+ go through the region joining process.
                                    if (u.player.hasPermission("group.jrbuilder")) {

                                        u.region.joinRegion(u);
                                        u.player.closeInventory();

                                    } else {

                                        //If they are not a Jr.Builder, cancel.
                                        u.player.closeInventory();
                                        u.player.sendMessage(Utils.chat("&cYou must be at least a Jr.Builder to join a region."));

                                    }
                                });

                    } else {

                        //Join requires owner to approve request.
                        setItem(5, Utils.createItem(Material.DARK_OAK_DOOR, 1,
                                        Utils.chat("&b&lJoin Region"),
                                        Utils.chat("&fClick to request to join the region you are standing in."),
                                        Utils.chat("&fThe region is owned by " + user.region.ownerName() + "."),
                                        Utils.chat("&fThey must accept the request for you to join.")),
                                u -> {

                                    //If the user is Jr.Builder+ go through the region joining process.
                                    if (u.player.hasPermission("group.jrbuilder")) {

                                        u.region.requestRegion(u, false);
                                        u.player.closeInventory();

                                    } else {

                                        //If they are not a Jr.Builder, cancel.
                                        u.player.closeInventory();
                                        u.player.sendMessage(Utils.chat("&cYou must be at least a Jr.Builder to join a region."));

                                    }
                                });
                    }

                } else

                    //Join region.
                    setItem(5, Utils.createItem(Material.DARK_OAK_DOOR, 1,
                                    Utils.chat("&b&lJoin Region"),
                                    Utils.chat("&fClick to join the region you are standing in."),
                                    Utils.chat("&fThe region currently has no active owner."),
                                    Utils.chat("&fJoining the region will make you region owner.")),
                            u -> {

                                //If the user is Jr.Builder+ go through the region joining process.
                                if (u.player.hasPermission("group.jrbuilder")) {


                                    //If the player is a Jr.Builder
                                    //Check if any nearby regions are claimed by someone else.
                                    //If true then the region needs to be checked by a staff member.
                                    if (u.player.hasPermission("group.builder")) {

                                        //If staff approval is always required do that.
                                        if (Network.getInstance().getConfig().getBoolean("staff_request.always")) {

                                            u.region.requestRegion(u, true);

                                        } else {

                                            //Get region coords.
                                            int x = Integer.parseInt(u.region.regionName().split(",")[0]);
                                            int z = Integer.parseInt(u.region.regionName().split(",")[1]);

                                            //Get the radius.
                                            int radius = Network.getInstance().getConfig().getInt("staff_request.always");

                                            //For zero radius, skip.
                                            if (radius == 0) {

                                                u.region.joinRegion(u);
                                                u.player.closeInventory();

                                            } else {

                                                //Subtract the config radius value.
                                                x -= radius;
                                                z -= radius;

                                                //Get the region manager.
                                                RegionManager regionManager = Network.getInstance().getRegionManager();

                                                //Iterate through all regions in the radius.
                                                for (int i = x; i <= x + radius*2; i++) {
                                                    for (int j = z; j <= z + radius*2; j++) {

                                                        String regionName = i + "," + j;

                                                        //If the region exists, check if it has an owner that is not the player.
                                                        if (regionManager.exists(regionName)) {

                                                            Region region = regionManager.getRegion(regionName);

                                                            if (region.hasOwner()) {
                                                                if (!region.getOwner().equals(u.player.getUniqueId().toString())) {

                                                                    //Staff approval is required.
                                                                    u.region.requestRegion(u, true);
                                                                    break;

                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                    } else {

                                        u.region.joinRegion(u);
                                        u.player.closeInventory();

                                    }

                                } else {

                                    //If they are not a Jr.Builder, cancel.
                                    u.player.closeInventory();
                                    u.player.sendMessage(Utils.chat("&cYou must be at least a Jr.Builder to join a region."));

                                }
                            });

            } else {

                //If the region is open.
                if (user.region.isOpen()) {
                    setItem(5, Utils.createItem(Material.END_GATEWAY, 1,
                            Utils.chat("&b&lOpen Region"),
                            Utils.chat("&fThis region is open to all Jr.Builder+."),
                            Utils.chat("&fYou can build here without claiming.")));

                } else {

                    //This region is not claimable.
                    setItem(5, Utils.createItem(Material.IRON_DOOR, 1,
                            Utils.chat("&b&lLocked Region"),
                            Utils.chat("&fThis region can not be claimed."),
                            Utils.chat("&fIt is either locked or used in the plot system.")));

                }

            }
        } else {
            //Show that the user is not in a region.
            setItem(5, Utils.createItem(Material.STRUCTURE_VOID, 1,
                    Utils.chat("&b&lNo Region"),
                    Utils.chat("&fYou are currently not standing in a valid region."),
                    Utils.chat("&fThis is likely due to being in a lobby.")));
        }

        //Plot menu.
        setItem(21, Utils.createItem(Material.CHEST, 1,
                        Utils.chat("&b&lPlot Menu"),
                        Utils.chat("&fView all your active plots.")),
                u ->

                {

                    //Delete this gui.
                    this.delete();
                    u.mainGui = null;

                    //Switch to plot menu.
                    u.mainGui = new PlotMenu(u);
                    u.mainGui.open(u);

                });

        //Region menu.
        setItem(23, Utils.createItem(Material.CHEST, 1,
                        Utils.chat("&b&lRegion Menu"),
                        Utils.chat("&fView all regions you can build in.")),
                u ->

                {

                    //Delete this gui.
                    this.delete();
                    u.mainGui = null;

                    //Switch to plot menu.
                    u.mainGui = new RegionMenu(u);
                    u.mainGui.open(u);

                });

        //Spawn
        setItem(17, Utils.createItem(Material.RED_BED, 1,
                        Utils.chat("&b&lSpawn"),
                        Utils.chat("&fTeleport to spawn.")),
                u ->

                {

                    //Connect to the lobby server.
                    u.player.closeInventory();
                    if (!Network.SERVER_NAME.equalsIgnoreCase(Network.getInstance().globalSQL.getString("SELECT name FROM server_data WHERE type='LOBBY';"))) {
                        SwitchServer.switchServer(u.player, Network.getInstance().globalSQL.getString("SELECT name FROM server_data WHERE type='LOBBY';"));
                    } else {
                        u.player.sendMessage(Utils.error("You are already in Spawn."));
                    }

                });

        //Return
        setItem(26, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.chat("&b&lReturn"),
                        Utils.chat("&fOpen the navigator main menu.")),
                u ->

                {

                    //Delete this gui.
                    this.delete();
                    u.mainGui = null;

                    //Switch to navigation menu.
                    Network.getInstance().navigatorGui.open(u);

                });
    }

    public void refresh() {

        this.clearGui();
        createGui();

    }
}
