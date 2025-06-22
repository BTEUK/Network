package net.bteuk.network.gui.regions;

import net.bteuk.network.Network;
import net.bteuk.network.gui.BuildGui;
import net.bteuk.network.gui.Gui;
import net.bteuk.network.sql.RegionSQL;
import net.bteuk.network.utils.NetworkUser;
import net.bteuk.network.utils.Utils;
import net.bteuk.network.utils.enums.RegionStatus;
import net.bteuk.network.utils.regions.Region;
import net.bteuk.network.utils.regions.RegionMember;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RegionMenu extends Gui {

    private final NetworkUser user;

    private final RegionSQL regionSQL;

    private int page;

    public RegionMenu(NetworkUser user) {

        super(45, Component.text("Region Menu", NamedTextColor.AQUA, TextDecoration.BOLD));

        this.user = user;

        page = 1;

        regionSQL = Network.getInstance().regionSQL;

        createGui();
    }

    private static ItemStack getGuiItem(RegionMember regionMember, RegionStatus regionStatus) {
        return Utils.createItem(getMaterial(regionMember, regionStatus), 1,
                Utils.title("Region " + regionMember.getTag()),
                getLines(regionMember, regionStatus));
    }

    private static Material getMaterial(RegionMember regionMember, RegionStatus regionStatus) {
        if (regionStatus == RegionStatus.INACTIVE) {
            return regionMember.pinned() ? Material.ORANGE_STAINED_GLASS : Material.ORANGE_CONCRETE;
        } else if (regionMember.isOwner()) {
            return regionMember.pinned() ? Material.LIME_STAINED_GLASS : Material.LIME_CONCRETE;
        } else {
            return regionMember.pinned() ? Material.YELLOW_STAINED_GLASS : Material.YELLOW_CONCRETE;
        }
    }

    private static Component[] getLines(RegionMember regionMember, RegionStatus regionStatus) {
        List<Component> lines = new ArrayList<>();
        lines.add(regionMember.isOwner() ? Utils.line("You are the owner of this region.") : Utils.line("You are a " +
                "member of this region."));
        lines.add(Utils.line("Click to open the menu of this region."));
        if (regionStatus == RegionStatus.INACTIVE) {
            lines.add(Utils.line("This region is currently inactive."));
            lines.add(Utils.line("Enter this region to make it active again."));
            if (!regionMember.isOwner()) {
                lines.add(Utils.line("You will then become the region owner."));
            }
        }
        return lines.toArray(new Component[0]);
    }

    private void createGui() {

        // Get regions you are owner or member of.
        List<RegionMember> regionMembers = regionSQL.getRegionMembers(user.player.getUniqueId().toString());

        // Slot count.
        int slot = 10;

        // Skip count.
        int skip = 21 * (page - 1);

        // If page is greater than 1 add a previous page button.
        if (page > 1) {
            setItem(18, Utils.createItem(Material.ARROW, 1,
                            Utils.title("Previous Page"),
                            Utils.line("Open the previous page of regions.")),
                    u ->

                    {

                        // Update the gui.
                        page--;
                        this.refresh();
                        u.player.getOpenInventory().getTopInventory().setContents(this.getInventory().getContents());
                    });
        }

        // Make a button for each plot.
        for (RegionMember regionMember : regionMembers) {

            // If skip is greater than 0, skip this iteration.
            if (skip > 0) {
                skip--;
                continue;
            }

            // If the slot is greater than the number that fit in a page, create a new page.
            if (slot > 34) {

                setItem(26, Utils.createItem(Material.ARROW, 1,
                                Utils.title("Next Page"),
                                Utils.line("Open the next page of regions.")),
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

            Region region = Network.getInstance().getRegionManager().getRegion(regionMember.region());
            RegionStatus status = region.status();

            setItem(slot, getGuiItem(regionMember, status),
                    u -> {

                        // Delete this gui.
                        this.delete();

                        // Switch to region info.
                        u.mainGui = new RegionInfo(region, u.player.getUniqueId().toString());
                        u.mainGui.open(u);
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

        // Check if you have any requests for regions you own.
        if (regionSQL.hasRow("SELECT region FROM region_requests WHERE owner='" + user.player.getUniqueId() + "' AND " +
                "owner_accept=0;")) {

            setItem(39, Utils.createItem(Material.LIME_STAINED_GLASS_PANE, 1,
                            Utils.title("Review Region Requests"),
                            Utils.line("View all region join requests for"),
                            Utils.line("regions that you are the owner of.")),

                    u -> {

                        // Delete this gui and switch to review region requests.
                        this.delete();
                        u.mainGui = new ReviewRegionRequests(false, u.player.getUniqueId().toString());
                        u.mainGui.open(u);
                    });
        }

        // Check if the player has any active region requests.
        if (regionSQL.hasRow("SELECT region FROM region_requests WHERE uuid='" + user.player.getUniqueId() + "';")) {

            setItem(40, Utils.createItem(Material.ORANGE_STAINED_GLASS, 1,
                            Utils.title("Region Requests"),
                            Utils.line("View active regions requests"),
                            Utils.line("that you have made that have"),
                            Utils.line("not yet been accepted.")),

                    u -> {

                        // Delete this gui and switch to region request menu.
                        this.delete();
                        u.mainGui = new RegionRequestMenu(u);
                        u.mainGui.open(u);
                    });
        }

        // Return
        setItem(44, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.title("Return"),
                        Utils.line("Open the building menu.")),
                u -> {

                    // Delete this gui.
                    this.delete();

                    // Switch to plot info.
                    u.mainGui = new BuildGui(u);
                    u.mainGui.open(u);
                });
    }

    public void refresh() {

        this.clearGui();
        createGui();
    }
}
