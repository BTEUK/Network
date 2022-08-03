package me.bteuk.network.gui.regions;

import me.bteuk.network.Network;
import me.bteuk.network.gui.BuildGui;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.sql.RegionSQL;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
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
        ArrayList<String> member = regionSQL.getStringList("SELECT region FROM region_members WHERE uuid='" + user.player.getUniqueId() + "' AND is_owner=1;");

        //Slot count.
        int slot = 10;

        //Skip count.
        int skip = 21 * (page - 1);

        //Total number of regions.
        int total = owner.size() + member.size();

        //If page is greater than 1 add a previous page button.
        if (page > 1) {
            setItem(18, Utils.createItem(Material.ARROW, 1,
                            Utils.chat("&b&lPrevious Page"),
                            Utils.chat("&fOpen the previous page of online users.")),
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
                                Utils.chat("&b&lNext Page"),
                                Utils.chat("&fOpen the next page of regions.")),
                        u ->

                        {

                            //Update the gui.
                            page++;
                            this.refresh();
                            u.player.getOpenInventory().getTopInventory().setContents(this.getInventory().getContents());

                        });

            }

            //If i is less than owner.size it means that we are still iterating through the owners, else we are iterating through the members.
            if (i < owner.size()) {

                setItem(slot, Utils.createItem(Material.LIME_CONCRETE, 1,
                                Utils.chat("&b&lRegion " + owner.get(i)),
                                Utils.chat("&fYou are the owner of this region."),
                                Utils.chat("&fClick to open the menu of this region.")),
                        u -> {

                            //Delete this gui.
                            this.delete();
                            u.regionMenu = null;

                            //Switch to region info.
                            u.regionInfo = new RegionInfo(Network.getInstance().getRegionManager().getRegion(owner.get(finalI)), u.player.getUniqueId().toString());
                            u.regionInfo.open(u);

                        });

            } else {

                setItem(slot, Utils.createItem(Material.YELLOW_CONCRETE, 1,
                                Utils.chat("&b&lRegion " + member.get((i - owner.size()))),
                                Utils.chat("&fYou are a member of this region."),
                                Utils.chat("&fClick to open the menu of this plot.")),
                        u -> {

                            //Delete this gui.
                            this.delete();
                            u.regionMenu = null;

                            //Switch to plot info.
                            u.regionInfo = new RegionInfo(Network.getInstance().getRegionManager().getRegion(member.get((finalI - owner.size()))), u.player.getUniqueId().toString());
                            u.regionInfo.open(u);

                        });

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
        if (regionSQL.hasRow("SELECT region FROM region_requests WHERE owner='" + user.player.getUniqueId() + "' AND owner_request=0;")) {

            setItem(40, Utils.createItem(Material.LIME_STAINED_GLASS_PANE, 1,
                            Utils.chat("&b&lReview Region Requests"),
                            Utils.chat("&fView all region join requests for"),
                            Utils.chat("&fregions that you are the owner of.")),

                    u -> {

                        //Delete this gui and switch to region requests.
                        this.delete();
                        u.regionMenu = null;

                        u.regionRequests = new RegionRequests(false);
                        u.regionRequests.open(u);

                    });
        }


        //Return
        setItem(44, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.chat("&b&lReturn"),
                        Utils.chat("&fOpen the building menu.")),
                u -> {

                    //Delete this gui.
                    this.delete();
                    u.regionMenu = null;

                    //Switch to plot info.
                    u.buildGui = new BuildGui(u);
                    u.buildGui.open(u);

                });
    }

    public void refresh() {

        this.clearGui();
        createGui();

    }
}
