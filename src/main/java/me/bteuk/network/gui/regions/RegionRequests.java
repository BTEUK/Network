package me.bteuk.network.gui.regions;

import me.bteuk.network.Network;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.sql.RegionSQL;
import me.bteuk.network.staff.StaffGui;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.regions.Request;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

import java.util.ArrayList;

public class RegionRequests extends Gui {

    private final RegionSQL regionSQL;

    private int page;

    private final boolean staff;

    public RegionRequests(boolean staff) {

        super(45, Component.text("Region Requests", NamedTextColor.AQUA, TextDecoration.BOLD));

        page = 1;

        regionSQL = Network.getInstance().regionSQL;

        this.staff = staff;

        createGui();

    }

    private void createGui() {

        //Get all regions with uuid.
        ArrayList<Request> requests;
        if (staff) {
            requests = regionSQL.getRequestList("SELECT region,uuid FROM region_requests WHERE staff_accept=0;");
        } else {
            requests = regionSQL.getRequestList("SELECT region,uuid FROM region_requests WHERE owner_accept=0;");
        }

        //Slot count.
        int slot = 10;

        //Skip count.
        int skip = 21 * (page - 1);

        //If page is greater than 1 add a previous page button.
        if (page > 1) {
            setItem(18, Utils.createItem(Material.ARROW, 1, Utils.title("Previous Page"), Utils.line("Open the previous page of region requests.")), u ->

            {

                //Update the gui.
                page--;
                this.refresh();
                u.player.getOpenInventory().getTopInventory().setContents(this.getInventory().getContents());

            });
        }

        //Make a button for each plot.
        for (int i = 0; i < requests.size(); i++) {

            //If skip is greater than 0, skip this iteration.
            if (skip > 0) {
                skip--;
                continue;
            }

            //If the slot is greater than the number that fit in a page, create a new page.
            if (slot > 34) {

                setItem(26, Utils.createItem(Material.ARROW, 1, Utils.title("Next Page"), Utils.line("Open the next page of regions requests.")), u ->

                {

                    //Update the gui.
                    page++;
                    this.refresh();
                    u.player.getOpenInventory().getTopInventory().setContents(this.getInventory().getContents());

                });

            }

            int finalI = i;
            setItem(slot, Utils.createItem(Material.LIME_CONCRETE, 1, Utils.title("Region " + requests.get(i).region), Utils.line("Requested by &7" + Network.getInstance().globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + requests.get(i).uuid + "';")), Utils.line("Click to open the menu for this request.")), u -> {

                //Delete this gui.
                this.delete();
                if (staff) {

                    u.staffGui = null;

                    //Switch to region request.
                    u.staffGui = new RegionRequest(requests.get(finalI), true);
                    u.staffGui.open(u);

                } else {

                    u.mainGui = null;

                    //Switch to region request.
                    u.mainGui = new RegionRequest(requests.get(finalI), false);
                    u.mainGui.open(u);

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
        if (staff) {

            setItem(44, Utils.createItem(Material.SPRUCE_DOOR, 1, Utils.title("Return"), Utils.line("Open the staff menu.")), u ->

            {

                //Delete this gui.
                this.delete();
                u.staffGui = null;

                //Switch to staff menu.
                u.staffGui = new StaffGui(u);
                u.staffGui.open(u);

            });
        } else {

            setItem(44, Utils.createItem(Material.SPRUCE_DOOR, 1, Utils.title("Return"), Utils.line("Open the region menu.")), u ->

            {

                //Delete this gui.
                this.delete();
                u.mainGui = null;

                //Switch to staff menu.
                u.mainGui = new RegionMenu(u);
                u.mainGui.open(u);

            });
        }
    }

    public void refresh() {

        this.clearGui();
        createGui();

    }
}
