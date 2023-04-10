package me.bteuk.network.gui;

import me.bteuk.network.Network;
import me.bteuk.network.commands.Back;
import me.bteuk.network.events.EventManager;
import me.bteuk.network.gui.plotsystem.PlotMenu;
import me.bteuk.network.gui.plotsystem.PlotServerLocations;
import me.bteuk.network.gui.plotsystem.PlotsystemLocations;
import me.bteuk.network.gui.plotsystem.ZoneMenu;
import me.bteuk.network.gui.regions.RegionInfo;
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
                        Utils.title("Random Plot"),
                        Utils.line("Click teleport to a random claimable plot.")),
                u -> {

                    int id;

                    if (u.player.hasPermission("uknet.plots.suggested.all")) {

                        //Select a random plot of any difficulty.
                        id = Network.getInstance().plotSQL.getInt("SELECT id FROM plot_data WHERE status='unclaimed' ORDER BY RAND() LIMIT 1;");

                    } else if (u.player.hasPermission("uknet.plots.suggested.hard")) {

                        //Select a random plot of the hard difficulty.
                        //Since this is the next plot difficulty to get Builder.
                        id = Network.getInstance().plotSQL.getInt("SELECT id FROM plot_data WHERE status='unclaimed' AND difficulty=3 ORDER BY RAND() LIMIT 1;");

                    } else if (u.player.hasPermission("uknet.plots.suggested.normal")) {

                        //Select a random plot of the normal difficulty.
                        //Since this is the next plot difficulty to get Jr.Builder.
                        id = Network.getInstance().plotSQL.getInt("SELECT id FROM plot_data WHERE status='unclaimed' AND difficulty=2 ORDER BY RAND() LIMIT 1;");

                    } else if (u.player.hasPermission("uknet.plots.suggested.easy")) {

                        //Select a random plot of the easy difficulty.
                        //Since this is the next plot difficulty to get Apprentice.
                        id = Network.getInstance().plotSQL.getInt("SELECT id FROM plot_data WHERE status='unclaimed' AND difficulty=1 ORDER BY RAND() LIMIT 1;");

                    } else {

                        //Select a random plot of any difficulty.
                        id = Network.getInstance().plotSQL.getInt("SELECT id FROM plot_data WHERE status='unclaimed' ORDER BY RAND() LIMIT 1;");

                    }

                    if (id == 0) {

                        u.player.sendMessage(Utils.error("There are no plots available, please wait for new plots to be added."));
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
                        Utils.title("Claim Plot"),
                        Utils.line("Click to claim the plot you are currently standing in.")),
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
                        u.player.sendMessage(Utils.error("You must be standing in a plot to claim it."));

                    }
                });


        //Choose location.
        setItem(19, Utils.createItem(Material.DIAMOND_PICKAXE, 1,
                        Utils.title("Plot Locations"),
                        Utils.line("Click to choose a location to build a plot.")),
                u ->

                {

                    //Delete this gui.
                    this.delete();
                    u.mainGui = null;

                    //Switch to the plot location gui.
                    u.mainGui = new PlotServerLocations(u);
                    u.mainGui.open(u);

                });

        //Join region (Jr.Builder+ only)
        //If region is claimable.
        //Check if the player is in a region.
        if (user.inRegion) {

            //Check if you're an owner or member of this region.
            //If true then open the region info menu instead.
            //If you're already waiting for you request to be reviewed then show that.
            if (user.region.isOwner(user.player.getUniqueId().toString())) {

                setItem(5, Utils.createItem(Material.LIME_GLAZED_TERRACOTTA, 1,
                                Utils.title("Region " + user.region.getTag(user.player.getUniqueId().toString())),
                                Utils.line("You are the owner of this region."),
                                Utils.line("Click to open the menu of this region.")),
                        u -> {

                            //Delete this gui.
                            this.delete();

                            //Switch to region info.
                            u.mainGui = new RegionInfo(user.region, u.player.getUniqueId().toString());
                            u.mainGui.open(u);

                        });

            } else if (user.region.isMember(user.player.getUniqueId().toString())) {

                setItem(5, Utils.createItem(Material.YELLOW_GLAZED_TERRACOTTA, 1,
                                Utils.title("Region " + user.region.getTag(user.player.getUniqueId().toString())),
                                Utils.line("You are a member of this region."),
                                Utils.line("Click to open the menu of this plot.")),
                        u -> {

                            //Delete this gui.
                            this.delete();

                            //Switch to plot info.
                            u.mainGui = new RegionInfo(user.region, u.player.getUniqueId().toString());
                            u.mainGui.open(u);

                        });

            } else if (user.region.hasRequest(user)) {

                setItem(5, Utils.createItem(Material.ORANGE_GLAZED_TERRACOTTA, 1,
                                Utils.title("Region " + user.region.getTag(user.player.getUniqueId().toString())),
                                Utils.line("You have requested to join this region."),
                                Utils.line("The request is still pending."),
                                Utils.line("Click to cancel the request.")),
                        u -> {

                            //Close the gui.
                            u.player.closeInventory();

                            //Cancel the request.
                            u.region.cancelRequest(u);

                        });

            } else if (user.player.hasPermission("uknet.regions.join")) {

                //Check if region is claimable.
                if (user.region.isClaimable()) {

                    //If the region has an owner.
                    if (user.region.hasActiveOwner()) {

                        //Check if the region is public.
                        if (user.region.isPublic()) {

                            setItem(5, Utils.createItem(Material.DARK_OAK_DOOR, 1,
                                            Utils.title("Join Region"),
                                            Utils.line("Click to join the region you are standing in."),
                                            Utils.line("The region is owned by " + user.region.ownerName() + "."),
                                            Utils.line("The region is public, so they don't need to accept your request.")),
                                    u -> {

                                        u.region.joinRegion(u);
                                        u.player.closeInventory();

                                    });

                        } else {

                            //Join requires owner to approve request.
                            setItem(5, Utils.createItem(Material.DARK_OAK_DOOR, 1,
                                            Utils.title("Join Region"),
                                            Utils.line("Click to request to join the region you are standing in."),
                                            Utils.line("The region is owned by " + user.region.ownerName() + "."),
                                            Utils.line("They must accept the request for you to join.")),
                                    u -> {

                                        u.region.requestRegion(u, false);
                                        u.player.closeInventory();

                                    });
                        }

                    } else

                        //Join region.
                        setItem(5, Utils.createItem(Material.DARK_OAK_DOOR, 1,
                                        Utils.title("Join Region"),
                                        Utils.line("Click to join the region you are standing in."),
                                        Utils.line("The region currently has no active owner."),
                                        Utils.line("Joining the region will make you the region owner.")),
                                u -> {

                                    //If the player does not have the bypass permission.
                                    //Check if any nearby regions are claimed by someone else.
                                    //If true then the region needs to be checked by a staff member.
                                    if (!u.player.hasPermission("uknet.regions.staff_request.bypass")) {

                                        //If staff approval is always required do that or if the region was previously claimed.
                                        if (Network.getInstance().getConfig().getBoolean("staff_request.always") || u.region.wasClaimed()) {

                                            u.region.requestRegion(u, true);
                                            u.player.closeInventory();

                                        } else {

                                            //Get region coords.
                                            int x = Integer.parseInt(u.region.regionName().split(",")[0]);
                                            int z = Integer.parseInt(u.region.regionName().split(",")[1]);

                                            //Get the radius.
                                            int radius = Network.getInstance().getConfig().getInt("staff_request.radius");

                                            //For zero radius, skip.
                                            if (radius != 0) {

                                                //Subtract the config radius value.
                                                x -= radius;
                                                z -= radius;

                                                //Get the region manager.
                                                RegionManager regionManager = Network.getInstance().getRegionManager();

                                                //Iterate through all regions in the radius.
                                                for (int i = x; i <= x + radius * 2; i++) {
                                                    for (int j = z; j <= z + radius * 2; j++) {

                                                        String regionName = i + "," + j;

                                                        //If the region exists, check if it has an owner that is not the player.
                                                        if (regionManager.exists(regionName)) {

                                                            Region region = regionManager.getRegion(regionName);

                                                            if (region.hasOwner()) {
                                                                if (!region.getOwner().equals(u.player.getUniqueId().toString())) {

                                                                    //Staff approval is required.
                                                                    u.region.requestRegion(u, true);
                                                                    u.player.closeInventory();
                                                                    return;

                                                                }
                                                            }
                                                        }
                                                    }
                                                }


                                            }

                                            u.region.joinRegion(u);
                                            u.player.closeInventory();
                                        }

                                    } else {

                                        u.region.joinRegion(u);
                                        u.player.closeInventory();

                                    }
                                });

                } else {

                    //If the region is open.
                    if (user.region.isOpen()) {
                        setItem(5, Utils.createItem(Material.SPYGLASS, 1,
                                Utils.title("Open Region"),
                                Utils.line("This region is open to all Jr.Builder+."),
                                Utils.line("You can build here without claiming.")));

                    } else {

                        //This region is not claimable.
                        setItem(5, Utils.createItem(Material.IRON_DOOR, 1,
                                Utils.title("Locked Region"),
                                Utils.line("This region can not be claimed."),
                                Utils.line("&fIt is either locked or used in the plot system.")));

                    }

                }
            } else {

                //Can't claim since you don't have jr.builder.
                setItem(5, Utils.createItem(Material.STRUCTURE_VOID, 1,
                        Utils.title("Unable to Join Region"),
                        Utils.line("To be able to join a region you"),
                        Utils.line("must gain at least Jr.Builder or above."),
                        Utils.line("For more information type &7/help building")));

            }
        } else {
            //Show that the user is not in a region.
            setItem(5, Utils.createItem(Material.STRUCTURE_VOID, 1,
                    Utils.title("No Region"),
                    Utils.line("You are currently not standing in a valid region."),
                    Utils.line("This is likely due to being in a lobby.")));
        }

        /*
        Region Join Button

        Claimable:
        -   No active owner
            - uknet.regions.staff_request.bypass: Join region without request.
            - staff_request.always: Staff request
            - Check radius if any nearby region is claimed.
        -   Has active owner
            - Default (Owner request)
            - Public (No request needed)





         */


        //Plot menu.
        setItem(21, Utils.createItem(Material.CHEST, 1,
                        Utils.title("Plot Menu"),
                        Utils.line("View all your active plots.")),
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
        setItem(6, Utils.createItem(Material.ORANGE_SHULKER_BOX, 1,
                        Utils.title("Region Menu"),
                        Utils.line("View all regions you can build in.")),
                u ->

                {

                    //Delete this gui.
                    this.delete();
                    u.mainGui = null;

                    //Switch to plot menu.
                    u.mainGui = new RegionMenu(u);
                    u.mainGui.open(u);

                });

        //Zone menu.
        setItem(23, Utils.createItem(Material.BARREL, 1,
                        Utils.title("Zone Menu"),
                        Utils.line("View all zones you can build in.")),
                u ->

                {

                    //Must be a jr.builder to open this menu.
                    if (u.player.hasPermission("uknet.zones.join")) {

                        //Delete this gui.
                        this.delete();
                        u.mainGui = null;

                        //Switch to plot menu.
                        u.mainGui = new ZoneMenu(u);
                        u.mainGui.open(u);

                    } else {

                        u.player.sendMessage(Utils.error("You must be at least a Jr.Builder to join zones."));

                    }

                });

        //Menu to teleport to plotsystem locations without going through a plot selection process.
        setItem(24, Utils.createItem(Material.MINECART, 1,
                        Utils.title("Plotsystem Locations"),
                        Utils.line("Teleport to a location"),
                        Utils.line("used by the Plotsystem.")),
                u -> {

                    this.delete();
                    u.mainGui = null;

                    u.mainGui = new PlotsystemLocations(u);
                    u.mainGui.open(u);

                });

        //Building utils menu.
        setItem(8, Utils.createItem(Material.NETHERITE_AXE, 1,
                        Utils.title("Building Utils"),
                        Utils.line("Open the building utils menu.")),
                u -> {

                    this.delete();
                    u.mainGui = null;

                    u.mainGui = new UtilsGui();
                    u.mainGui.open(u);

                });

        //Spawn
        setItem(17, Utils.createItem(Material.RED_BED, 1,
                        Utils.title("Spawn"),
                        Utils.line("Teleport to spawn.")),
                u ->

                {

                    u.player.closeInventory();

                    //If server is Lobby, teleport to spawn.
                    if (Network.SERVER_TYPE == ServerType.LOBBY) {

                        Back.setPreviousCoordinate(u.player.getUniqueId().toString(), u.player.getLocation());
                        u.player.teleport(Network.getInstance().getLobby().spawn);
                        u.player.sendMessage(Utils.success("Teleported to spawn."));

                    } else {

                        //Set teleport event to go to spawn.
                        EventManager.createTeleportEvent(true, u.player.getUniqueId().toString(), "network", "teleport spawn", u.player.getLocation());
                        SwitchServer.switchServer(u.player, Network.getInstance().globalSQL.getString("SELECT name FROM server_data WHERE type='LOBBY';"));

                    }

                });

        //Return
        setItem(26, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.title("Return"),
                        Utils.line("Open the navigator main menu.")),
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
