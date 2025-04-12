package net.bteuk.network.gui.plotsystem;

import net.bteuk.network.Network;
import net.bteuk.network.eventing.events.EventManager;
import net.bteuk.network.gui.Gui;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.sql.GlobalSQL;
import net.bteuk.network.sql.PlotSQL;
import net.bteuk.network.utils.Utils;
import net.bteuk.network.utils.enums.RegionType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

import java.util.ArrayList;

public class PlotsystemMembers extends Gui {

    private final int id;
    private final RegionType regionType;
    private final GlobalSQL globalSQL;
    private final PlotSQL plotSQL;
    private int page;

    public PlotsystemMembers(int id, RegionType regionType) {

        super(45, Component.text("Manage Members", NamedTextColor.AQUA, TextDecoration.BOLD));

        this.id = id;
        this.regionType = regionType;

        page = 1;

        globalSQL = Network.getInstance().getGlobalSQL();
        plotSQL = Network.getInstance().getPlotSQL();

        createGui();
    }

    private void createGui() {

        ArrayList<String> members;

        // Get members of the plot or zone.
        if (regionType == RegionType.PLOT) {

            members = plotSQL.getStringList("SELECT uuid FROM plot_members WHERE id=" + id + " AND is_owner=0;");
        } else if (regionType == RegionType.ZONE) {

            members = plotSQL.getStringList("SELECT uuid FROM zone_members WHERE id=" + id + " AND is_owner=0;");
        } else {

            Network.getInstance().getLogger().warning("PlotsystemMembers has been created without a valid regionType " +
                    "(PLOT or ZONE)!");
            return;
        }

        // Slot count.
        int slot = 10;

        // Skip count.
        int skip = 21 * (page - 1);

        // If page is greater than 1 add a previous page button.
        if (page > 1) {
            setItem(18, Utils.createItem(Material.ARROW, 1,
                            Utils.title("Previous Page"),
                            Utils.line("Open the previous page of " + regionType.label + " members.")),
                    u ->

                    {

                        // Update the gui.
                        page--;
                        this.refresh();
                        u.player.getOpenInventory().getTopInventory().setContents(this.getInventory().getContents());
                    });
        }

        // Iterate through all online players.
        if (members != null) {
            for (String uuid : members) {

                // If the slot is greater than the number that fit in a page, create a new page.
                if (slot > 34) {

                    setItem(26, Utils.createItem(Material.ARROW, 1,
                                    Utils.title("Next Page"),
                                    Utils.line("Open the previous page of " + regionType.label + " members.")),
                            u ->

                            {

                                // Update the gui.
                                page++;
                                this.refresh();
                                u.player.getOpenInventory().getTopInventory()
                                        .setContents(this.getInventory().getContents());
                            });

                    // Stop iterating.
                    break;
                }

                // If skip is greater than 0, skip this iteration.
                if (skip > 0) {
                    skip--;
                    continue;
                }

                // Add player to gui.
                setItem(slot, Utils.createPlayerSkull(uuid, 1,
                                Utils.title("Kick " + globalSQL.getString(
                                        "SELECT name FROM player_data WHERE uuid='" + uuid + "';") + " from your " + regionType.label + "."),
                                Utils.line("Click to remove them as member of your " + regionType.label + "."),
                                Utils.line("They will no longer be able to build in it.")),
                        u ->

                        {

                            if (regionType == RegionType.PLOT) {

                                if (plotSQL.hasRow(
                                        "SELECT id FROM plot_members WHERE id=" + id + " AND uuid='" + uuid + "';")) {

                                    // Kick the member from the plot.
                                    EventManager.createEvent(uuid, "plotsystem", plotSQL.getString("SELECT server " +
                                                    "FROM location_data WHERE name='" +
                                                    plotSQL.getString(
                                                            "SELECT location FROM plot_data WHERE id=" + id + ";") +
                                                    "';"),
                                            "kick plot " + id);

                                    // Return to the previous menu, since otherwise the gui won't have updated.
                                    this.delete();
                                    u.mainGui = null;

                                    // Switch back to plot info.
                                    u.mainGui = new PlotInfo(u, id);
                                    u.mainGui.open(u);
                                } else {
                                    u.player.sendMessage(ChatUtils.error("This player is not a member of your Plot."));
                                }
                            } else {

                                if (plotSQL.hasRow(
                                        "SELECT id FROM zone_members WHERE id=" + id + " AND uuid='" + uuid + "';")) {

                                    // Kick the member from the plot.
                                    EventManager.createEvent(uuid, "plotsystem", plotSQL.getString("SELECT server " +
                                            "FROM location_data WHERE name='" +
                                            plotSQL.getString("SELECT location FROM zones WHERE id=" + id + ";") +
                                            "';"), "kick zone " + id);

                                    // Return to the previous menu, since otherwise the gui won't have updated.
                                    this.delete();
                                    u.mainGui = null;

                                    // Switch back to plot info.
                                    u.mainGui = new ZoneInfo(u, id, u.player.getUniqueId().toString());
                                    u.mainGui.open(u);
                                } else {
                                    u.player.sendMessage(ChatUtils.error("This player is not a member of your Zone."));
                                }
                            }
                        });

                // Increase slot accordingly.
                if (slot % 9 == 7) {
                    // Increase row, basically add 3.
                    slot += 3;
                } else {
                    // Increase value by 1.
                    slot++;
                }
            }
        }

        // Return
        setItem(44, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.title("Return"),
                        Utils.line("Open the " + regionType.label + " info for this " + regionType.label + ".")),
                u -> {

                    // Delete this gui.
                    this.delete();
                    u.mainGui = null;

                    // Switch back to plot info.
                    if (regionType == RegionType.PLOT) {

                        u.mainGui = new PlotInfo(u, id);
                    } else {

                        u.mainGui = new ZoneInfo(u, id, u.player.getUniqueId().toString());
                    }

                    u.mainGui.open(u);
                });
    }

    public void refresh() {

        this.clearGui();
        createGui();
    }
}
