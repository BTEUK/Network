package me.bteuk.network.gui.regions;

import me.bteuk.network.Network;
import me.bteuk.network.gui.BuildGui;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.sql.RegionSQL;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.enums.RegionStatus;
import me.bteuk.network.utils.regions.Region;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

import java.util.ArrayList;

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

    private void createGui() {

        //Get regions you are owner or member of.
        ArrayList<String> owner = regionSQL.getStringList("SELECT region FROM region_members WHERE uuid='" + user.player.getUniqueId() + "' AND is_owner=1;");
        ArrayList<String> member = regionSQL.getStringList("SELECT region FROM region_members WHERE uuid='" + user.player.getUniqueId() + "' AND is_owner=0;");

        //Slot count.
        int slot = 10;

        //Skip count.
        int skip = 21 * (page - 1);

        //Total number of regions.
        int total = owner.size() + member.size();

        //If page is greater than 1 add a previous page button.
        if (page > 1) {
            setItem(18, Utils.createItem(Material.ARROW, 1,
                            Utils.title("Previous Page"),
                            Utils.line("Open the previous page of regions.")),
                    u ->

                    {

                        //Update the gui.
                        page--;
                        this.refresh();
                        u.player.getOpenInventory().getTopInventory().setContents(this.getInventory().getContents());

                    });
        }

        //Make a button for each plot.
        for (int i = 0; i < total; i++) {

            int finalI = i;

            //If skip is greater than 0, skip this iteration.
            if (skip > 0) {
                skip--;
                continue;
            }

            //If the slot is greater than the number that fit in a page, create a new page.
            if (slot > 34) {

                setItem(26, Utils.createItem(Material.ARROW, 1,
                                Utils.title("Next Page"),
                                Utils.line("Open the next page of regions.")),
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

            //If i is less than owner.size it means that we are still iterating through the owners, else we are iterating through the members.
            Region region;
            if (i < owner.size()) {

                region = Network.getInstance().getRegionManager().getRegion(owner.get(i));

                //If the region is inactive change the colour of the icon so the user knows it's inactive.
                if (region.status() == RegionStatus.INACTIVE) {
                    setItem(slot, Utils.createItem(Material.ORANGE_CONCRETE, 1,
                                    Utils.title("Region " + region.getTag(user.player.getUniqueId().toString())),
                                    Utils.line("You are the owner of this region."),
                                    Utils.line("Click to open the menu of this region."),
                                    Utils.line("This region is currently inactive."),
                                    Utils.line("Enter this region to make it active again.")),
                            u -> {

                                //Delete this gui.
                                this.delete();

                                //Switch to region info.
                                u.mainGui = new RegionInfo(Network.getInstance().getRegionManager().getRegion(owner.get(finalI)), u.player.getUniqueId().toString());
                                u.mainGui.open(u);

                            });

                } else {

                    setItem(slot, Utils.createItem(Material.LIME_CONCRETE, 1,
                                    Utils.title("Region " + region.getTag(user.player.getUniqueId().toString())),
                                    Utils.line("You are the owner of this region."),
                                    Utils.line("Click to open the menu of this region.")),
                            u -> {

                                //Delete this gui.
                                this.delete();

                                //Switch to region info.
                                u.mainGui = new RegionInfo(Network.getInstance().getRegionManager().getRegion(owner.get(finalI)), u.player.getUniqueId().toString());
                                u.mainGui.open(u);

                            });
                }

            } else {

                region = Network.getInstance().getRegionManager().getRegion(member.get((finalI - owner.size())));

                //If the region is inactive change the colour of the icon so the user knows it's inactive.
                if (region.status() == RegionStatus.INACTIVE) {

                    setItem(slot, Utils.createItem(Material.LIME_CONCRETE, 1,
                                    Utils.title("Region " + region.getTag(user.player.getUniqueId().toString())),
                                    Utils.line("You are the owner of this region."),
                                    Utils.line("Click to open the menu of this region."),
                                    Utils.line("This region is currently inactive."),
                                    Utils.line("Enter this region to make it active again."),
                                    Utils.line("You will then become the region owner.")),
                            u -> {

                                //Delete this gui.
                                this.delete();

                                //Switch to region info.
                                u.mainGui = new RegionInfo(Network.getInstance().getRegionManager().getRegion(member.get(finalI - owner.size())), u.player.getUniqueId().toString());
                                u.mainGui.open(u);

                            });
                } else {

                    setItem(slot, Utils.createItem(Material.YELLOW_CONCRETE, 1,
                                    Utils.title("Region " + region.getTag(user.player.getUniqueId().toString())),
                                    Utils.line("You are a member of this region."),
                                    Utils.line("Click to open the menu of this plot.")),
                            u -> {

                                //Delete this gui.
                                this.delete();

                                //Switch to plot info.
                                u.mainGui = new RegionInfo(Network.getInstance().getRegionManager().getRegion(member.get((finalI - owner.size()))), u.player.getUniqueId().toString());
                                u.mainGui.open(u);

                            });
                }

            }

            //Increase slot accordingly.
            if (slot % 9 == 7) {
                //Increase row, basically add 3.
                slot += 3;
            } else {
                //Increase value by 1.
                slot++;
            }
        }

        //Check if you have any requests for regions you own.
        if (regionSQL.hasRow("SELECT region FROM region_requests WHERE owner='" + user.player.getUniqueId() + "' AND owner_accept=0;")) {

            setItem(39, Utils.createItem(Material.LIME_STAINED_GLASS_PANE, 1,
                            Utils.title("Review Region Requests"),
                            Utils.line("View all region join requests for"),
                            Utils.line("regions that you are the owner of.")),

                    u -> {

                        //Delete this gui and switch to review region requests.
                        this.delete();
                        u.mainGui = new ReviewRegionRequests(false);
                        u.mainGui.open(u);

                    });
        }

        //Check if the player has any active region requests.
        if (regionSQL.hasRow("SELECT region FROM region_requests WHERE uuid='" + user.player.getUniqueId() + "';")) {

            setItem(40, Utils.createItem(Material.ORANGE_STAINED_GLASS, 1,
                            Utils.title("Region Requests"),
                            Utils.line("View active regions requests"),
                            Utils.line("that you have made that have"),
                            Utils.line("not yet been accepted.")),

                    u -> {

                        //Delete this gui and switch to region request menu.
                        this.delete();
                        u.mainGui = new RegionRequestMenu(u);
                        u.mainGui.open(u);

                    });

        }


        //Return
        setItem(44, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.title("Return"),
                        Utils.line("Open the building menu.")),
                u -> {

                    //Delete this gui.
                    this.delete();

                    //Switch to plot info.
                    u.mainGui = new BuildGui(u);
                    u.mainGui.open(u);

                });
    }

    public void refresh() {

        this.clearGui();
        createGui();

    }
}
