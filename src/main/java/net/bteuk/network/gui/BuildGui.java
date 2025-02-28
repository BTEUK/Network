package net.bteuk.network.gui;

import me.bteuk.progressmapper.guis.LocalFeaturesMenu;
import net.bteuk.network.Network;
import net.bteuk.network.commands.navigation.Back;
import net.bteuk.network.eventing.events.EventManager;
import net.bteuk.network.gui.plotsystem.PlotMenu;
import net.bteuk.network.gui.plotsystem.PlotServerLocations;
import net.bteuk.network.gui.plotsystem.PlotsystemLocations;
import net.bteuk.network.gui.plotsystem.ZoneMenu;
import net.bteuk.network.gui.progressmap.LocalFeatureListGUI;
import net.bteuk.network.gui.regions.RegionInfo;
import net.bteuk.network.gui.regions.RegionMenu;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.utils.NetworkUser;
import net.bteuk.network.utils.SwitchServer;
import net.bteuk.network.utils.Utils;
import net.bteuk.network.utils.enums.RegionStatus;
import net.bteuk.network.utils.enums.ServerType;
import net.bteuk.network.utils.regions.Region;
import net.bteuk.network.utils.regions.RegionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

import static net.bteuk.network.utils.Constants.PROGRESS_MAP;
import static net.bteuk.network.utils.Constants.SERVER_NAME;
import static net.bteuk.network.utils.Constants.SERVER_TYPE;
import static net.bteuk.network.utils.NetworkConfig.CONFIG;

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
                        Utils.line("Click teleport to a random claimable plot."),
                        Utils.line("Available plots of each difficulty:"),
                        Utils.line("Easy: ")
                                .append(Component.text(Network.getInstance().getPlotSQL().getInt("SELECT count(id) FROM plot_data WHERE status='unclaimed' AND difficulty=1;"), NamedTextColor.GRAY)),
                        Utils.line("Normal: ")
                                .append(Component.text(Network.getInstance().getPlotSQL().getInt("SELECT count(id) FROM plot_data WHERE status='unclaimed' AND difficulty=2;"), NamedTextColor.GRAY)),
                        Utils.line("Hard: ")
                                .append(Component.text(Network.getInstance().getPlotSQL().getInt("SELECT count(id) FROM plot_data WHERE status='unclaimed' AND difficulty=3;"), NamedTextColor.GRAY))),
                u -> {

                    int id;

                    if (u.player.hasPermission("uknet.plots.suggested.all")) {

                        //Select a random plot of any difficulty.
                        id = Network.getInstance().getPlotSQL().getInt("SELECT id FROM plot_data WHERE status='unclaimed' ORDER BY RAND() LIMIT 1;");

                    } else if (u.player.hasPermission("uknet.plots.suggested.hard")) {

                        //Select a random plot of the hard difficulty.
                        //Since this is the next plot difficulty to get Builder.
                        id = Network.getInstance().getPlotSQL().getInt("SELECT id FROM plot_data WHERE status='unclaimed' AND difficulty=3 ORDER BY RAND() LIMIT 1;");

                    } else if (u.player.hasPermission("uknet.plots.suggested.normal")) {

                        //Select a random plot of the normal difficulty.
                        //Since this is the next plot difficulty to get Jr.Builder.
                        id = Network.getInstance().getPlotSQL().getInt("SELECT id FROM plot_data WHERE status='unclaimed' AND difficulty=2 ORDER BY RAND() LIMIT 1;");

                    } else if (u.player.hasPermission("uknet.plots.suggested.easy")) {

                        //Select a random plot of the easy difficulty.
                        //Since this is the next plot difficulty to get Apprentice.
                        id = Network.getInstance().getPlotSQL().getInt("SELECT id FROM plot_data WHERE status='unclaimed' AND difficulty=1 ORDER BY RAND() LIMIT 1;");

                    } else {

                        //Select a random plot of any difficulty.
                        id = Network.getInstance().getPlotSQL().getInt("SELECT id FROM plot_data WHERE status='unclaimed' ORDER BY RAND() LIMIT 1;");

                    }

                    if (id == 0) {

                        u.player.sendMessage(ChatUtils.error("There are no plots available, please wait for new plots to be added."));
                        u.player.closeInventory();

                    } else {

                        //Get the server of the plot.
                        String server = Network.getInstance().getPlotSQL().getString("SELECT server FROM location_data WHERE name='"
                                + Network.getInstance().getPlotSQL().getString("SELECT location FROM plot_data WHERE id=" + id + ";")
                                + "';");

                        //If the plot is on the current server teleport them directly.
                        //Else teleport them to the correct server and them teleport them to the plot.
                        if (server.equals(SERVER_NAME)) {

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

        //Claim plot
        //This button only appears when in a plot server, else it'll show the region button.
        if (SERVER_TYPE == ServerType.PLOT) {
            setItem(4, Utils.createItem(Material.EMERALD, 1,
                            Utils.title("Claim Plot"),
                            Utils.line("Click to claim the plot you are currently standing in.")),
                    u -> {

                        //Set the claim event.
                        u.player.closeInventory();
                        Network.getInstance().getGlobalSQL().update("INSERT INTO server_events(uuid,type,server,event) VALUES('"
                                + u.player.getUniqueId()
                                + "','plotsystem','" + SERVER_NAME
                                + "','claim plot');");

                    });
        } else {

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

            //Join region (Jr.Builder+ only)
            //If region is claimable.
            //Check if the player is in a region.
            if (user.inRegion) {

                //Check if you're an owner or member of this region.
                //If true then open the region info menu instead.
                //If you're already waiting for you request to be reviewed then show that.
                if (user.region.isOwner(user.player.getUniqueId().toString())) {

                    setItem(4, Utils.createItem(Material.LIME_GLAZED_TERRACOTTA, 1,
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

                    setItem(4, Utils.createItem(Material.YELLOW_GLAZED_TERRACOTTA, 1,
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

                    setItem(4, Utils.createItem(Material.ORANGE_GLAZED_TERRACOTTA, 1,
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
                            if (user.region.status() == RegionStatus.PUBLIC) {

                                setItem(4, Utils.createItem(Material.DARK_OAK_DOOR, 1,
                                                Utils.title("Join Region"),
                                                Utils.line("Click to join the region you are standing in."),
                                                Utils.line("The region is owned by ")
                                                        .append(Component.text(user.region.ownerName(), NamedTextColor.GRAY)),
                                                Utils.line("The region is public, so they don't need to accept your request.")),
                                        u -> {

                                            u.region.joinRegion(u);
                                            u.player.closeInventory();

                                        });

                            } else {

                                //Join requires owner to approve request.
                                setItem(4, Utils.createItem(Material.DARK_OAK_DOOR, 1,
                                                Utils.title("Join Region"),
                                                Utils.line("Click to request to join the region you are standing in."),
                                                Utils.line("The region is owned by ")
                                                        .append(Component.text(user.region.ownerName(), NamedTextColor.GRAY)),
                                                Utils.line("They must accept the request for you to join.")),
                                        u -> {

                                            u.region.requestRegion(u, false);
                                            u.player.closeInventory();

                                        });
                            }

                        } else

                            //Join region.
                            setItem(4, Utils.createItem(Material.DARK_OAK_DOOR, 1,
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
                                            if (CONFIG.getBoolean("staff_request.always") || u.region.wasClaimed()) {

                                                u.region.requestRegion(u, true);
                                                u.player.closeInventory();

                                            } else {

                                                //Get region coords.
                                                int x = Integer.parseInt(u.region.regionName().split(",")[0]);
                                                int z = Integer.parseInt(u.region.regionName().split(",")[1]);

                                                //Get the radius.
                                                int radius = CONFIG.getInt("staff_request.radius");

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
                        if (user.region.status() == RegionStatus.OPEN) {
                            setItem(4, Utils.createItem(Material.SPYGLASS, 1,
                                    Utils.title("Open Region"),
                                    Utils.line("This region is open to all Jr.Builder+."),
                                    Utils.line("You can build here without claiming.")));

                        } else {

                            //This region is not claimable.
                            setItem(4, Utils.createItem(Material.IRON_DOOR, 1,
                                    Utils.title("Locked Region"),
                                    Utils.line("This region can not be claimed."),
                                    Utils.line("It is either locked or used in the plot system.")));

                        }

                    }
                } else {

                    //Can't claim since you don't have jr.builder.
                    setItem(4, Utils.createItem(Material.STRUCTURE_VOID, 1,
                            Utils.title("Unable to Join Region"),
                            Utils.line("To be able to join a region you"),
                            Utils.line("must gain at least Jr.Builder or above."),
                            Utils.line("For more information type ")
                                    .append(Component.text("/help building", NamedTextColor.GRAY))));

                }
            } else {
                //Show that the user is not in a region.
                setItem(4, Utils.createItem(Material.STRUCTURE_VOID, 1,
                        Utils.title("No Region"),
                        Utils.line("You are currently not standing in a valid region."),
                        Utils.line("This is likely due to being in a lobby.")));
            }
        }

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
        setItem(24, Utils.createItem(Material.ORANGE_SHULKER_BOX, 1,
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
        setItem(17, Utils.createItem(Material.BARREL, 1,
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

                        u.player.sendMessage(ChatUtils.error("You must be at least a Jr.Builder to join zones."));

                    }

                });

        //Menu to teleport to plotsystem locations without going through a plot selection process.
        setItem(22, Utils.createItem(Material.MINECART, 1,
                        Utils.title("Plotsystem Locations"),
                        Utils.line("Teleport to a location"),
                        Utils.line("used by the Plotsystem.")),
                u -> {

                    this.delete();
                    u.mainGui = null;

                    u.mainGui = new PlotsystemLocations();
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

        if (PROGRESS_MAP && user.player.hasPermission("uknet.progressmap.edit"))
        {
            //Progress map edit menu
            setItem(0, Utils.createItem(Material.MAP, 1,
                            Utils.title("Progress Map"),
                            Utils.line("Edit or add areas to the progress map")),
                    u -> {

                        //Deletes this GUI
                        this.delete();
                        u.mainGui = null;

                        LocalFeaturesMenu localFeatures = new LocalFeaturesMenu(
                                Network.getInstance().getConfig().getInt("ProgressMap.ProgressMapID"),
                                Network.getInstance().getConfig().getString("ProgressMap.MapHubAPIKey"),
                                u.player);

                        //Check to see if the location could be established
                        if (localFeatures.getPlayerCoordinates() == null)
                        {
                            u.mainGui = this;
                            u.mainGui.open(u);
                            u.player.sendMessage(ChatUtils.error("Could not locate you"));
                        }
                        else
                        {
                            //Switch to local features menu
                            u.mainGui = new LocalFeatureListGUI(localFeatures, Network.getInstance());
                            u.mainGui.open(u);
                        }
                    });
        }

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
