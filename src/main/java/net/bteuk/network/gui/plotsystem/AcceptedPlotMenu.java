package net.bteuk.network.gui.plotsystem;

import com.destroystokyo.paper.profile.PlayerProfile;
import lombok.Getter;
import lombok.Setter;
import net.bteuk.network.Network;
import net.bteuk.network.gui.Gui;
import net.bteuk.network.sql.GlobalSQL;
import net.bteuk.network.sql.PlotSQL;
import net.bteuk.network.utils.NetworkUser;
import net.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.codehaus.plexus.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This Gui class allows the player to view accepted plots.
 * Default is filtered to view the completed plots by the user.
 * However, the filter can be altered to view all accepted plots,
 * or those by a specific user, granted they have completed at least one plot.
 */
public class AcceptedPlotMenu extends Gui {

    private final PlotSQL plotSQL;
    private final GlobalSQL globalSQL;

    private final FilterMenu filterMenu;

    @Setter
    private PlotInfo plotInfo;

    /**
     * Filter that is applied when fetching all plots.
     * The uuid is the value.
     * An empty string implies all users.
     */
    @Getter
    @Setter
    private String filter;

    @Setter
    private int page = 1;

    /**
     * Hashmap to store the {@link PlayerProfile} so they do not need to be called on each refresh.
     */
    private final Map<String, PlayerProfile> playerProfiles = new ConcurrentHashMap<>();

    private final Map<String, CompletableFuture<Void>> completableProfiles = new ConcurrentHashMap<>();

    public AcceptedPlotMenu(NetworkUser user) {

        super(45, Component.text("Accepted Plot Menu", NamedTextColor.AQUA, TextDecoration.BOLD));

        filterMenu = new FilterMenu(this, user);

        plotSQL = Network.getInstance().getPlotSQL();
        globalSQL = Network.getInstance().getGlobalSQL();

        filter = user.player.getUniqueId().toString();
        createGui();

    }

    private void createGui() {

        // Fetch accepted plots.
        HashMap<Integer, String> plots;
        if (StringUtils.isEmpty(filter)) {
            plots = plotSQL.getIntStringMap("SELECT id,uuid FROM accept_data ORDER BY accept_time DESC;");
        } else {
            plots = plotSQL.getIntStringMap("SELECT id,uuid FROM accept_data WHERE uuid='" + filter + "' ORDER BY accept_time DESC;");
        }

        // Set the filter.
        // Open the filter menu.
        setItem(4, Utils.createItem(
                        Material.SPRUCE_SIGN, 1, Utils.title("Set filter"),
                        Utils.line("The current filter is set to: ").append(Component.text(
                                StringUtils.isEmpty(filter) ? "All Players" : globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + filter + "';"), NamedTextColor.GRAY
                        )),
                        Utils.line("Click to select a different filter.")),
                filterMenu::open);

        // Slot count.
        int slot = 10;

        // If page > 1 set number of iterations that must be skipped.
        int skip = (page - 1) * 21;

        //If page is greater than 1 add a previous page button.
        if (page > 1) {
            setItem(18, Utils.createItem(Material.ARROW, 1,
                            Utils.title("Previous Page"),
                            Utils.line("Open the previous page of accepted plots.")),
                    u ->

                    {
                        //Update the gui.
                        page--;
                        this.refresh();
                        u.player.getOpenInventory().getTopInventory().setContents(this.getInventory().getContents());
                    });
        }

        //Make a button for each plot.
        for (int plotID : plots.keySet()) {

            //Skip iterations if skip > 0.
            if (skip > 0) {
                skip--;
                continue;
            }

            //If the slot is greater than the number that fit in a page, create a new page.
            if (slot > 34) {

                setItem(26, Utils.createItem(Material.ARROW, 1,
                                Utils.title("Next Page"),
                                Utils.line("Open the next page of accepted plots.")),
                        u ->

                        {
                            //Update the gui.
                            page++;
                            this.refresh();
                            u.player.getOpenInventory().getTopInventory().setContents(this.getInventory().getContents());
                        });

                //Stop iterating.
                break;
            }

            // The icon is the player head of the plot builder.
            PlayerProfile profile = playerProfiles.get(plots.get(plotID));
            if (profile == null) {
                profile = Bukkit.createProfile(UUID.fromString(plots.get(plotID)));
                // Add player profiles to a list of futures, they will be fetched asynchronously.
                completableProfiles.put(plots.get(plotID), getCompletableProfile(profile));
                playerProfiles.put(plots.get(plotID), profile);
            }
            setItem(slot, Utils.createPlayerSkull(profile, 1,
                            Utils.title("Plot " + plotID),
                            Utils.line("Completed by: ").append(Component.text(globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + plots.get(plotID) + "';"), NamedTextColor.GRAY)),
                            Utils.line("Click to open the menu of this plot.")),
                    u -> {
                        //Switch to plot info.
                        if (plotInfo != null) {
                            plotInfo.deleteThis();
                        }
                        plotInfo = new PlotInfo(u, plotID);
                        plotInfo.setAcceptedPlotMenu(this);
                        plotInfo.open(u);
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
                        Utils.title("Return"),
                        Utils.line("Open the plot menu.")),
                u -> {
                    //Delete this gui.
                    this.delete();
                    u.mainGui = null;

                    //Return to the plot menu.
                    u.mainGui = new PlotMenu(u);
                    u.mainGui.open(u);
                });

        // Complete the player-profiles and refresh the gui.
        completeProfiles();
    }

    private void completeProfiles() {
        CompletableFuture<Void> future = CompletableFuture.allOf((CompletableFuture<?>) completableProfiles.values());
        completableProfiles.clear();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                future.get();
            } catch (Exception e) {
                // Ignored
            }
            // Refresh the gui.
            Bukkit.getScheduler().runTask(Network.getInstance(), this::refresh);
        });
    }

    public void refresh() {
        this.clearGui();
        createGui();
    }

    private CompletableFuture<Void> getCompletableProfile(PlayerProfile profile) {
        return CompletableFuture.runAsync(profile::complete);
    }

    @Override
    public void delete() {
        if (filterMenu != null) {
            filterMenu.deleteThis();
        }
        if (plotInfo != null) {
            plotInfo.deleteThis();
        }
        super.delete();
    }
}
