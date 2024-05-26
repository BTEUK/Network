package net.bteuk.network.gui.regions;

import net.bteuk.network.Network;
import net.bteuk.network.gui.Gui;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.sql.RegionSQL;
import net.bteuk.network.utils.NetworkUser;
import net.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

import java.util.ArrayList;

/**
 * Lists all region requests made by this player.
 */
public class RegionRequestMenu extends Gui {

    private final RegionSQL regionSQL;

    private int page;

    private final NetworkUser u;

    /**
     * Create the region requests menu.
     *
     * @param u
     * {@link NetworkUser} for whom the gui should be created.
     * This parameter is needed to find the region requests that the player has.
     */
    public RegionRequestMenu(NetworkUser u) {

        super(45, Component.text("Region Requests", NamedTextColor.AQUA, TextDecoration.BOLD));

        page = 1;

        this.u = u;

        regionSQL = Network.getInstance().regionSQL;

        createGui();

    }

    /**
     * Populates the gui with content.
     */
    private void createGui() {

        //Get all regions with uuid.
        ArrayList<String> requests = regionSQL.getStringList("SELECT region FROM region_requests WHERE uuid='" + u.player.getUniqueId() + "';");

        //Slot count.
        int slot = 10;

        //Skip count.
        int skip = 21 * (page - 1);

        //If page is greater than 1 add a previous page button.
        if (page > 1) {
            setItem(18, Utils.createItem(Material.ARROW, 1,
                    Utils.title("Previous Page"),
                    Utils.line("Open the previous page of region requests.")), u ->

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

                setItem(26, Utils.createItem(Material.ARROW, 1,
                        Utils.title("Next Page"),
                        Utils.line("Open the next page of regions requests.")), u ->

                {

                    //Update the gui.
                    page++;
                    this.refresh();
                    u.player.getOpenInventory().getTopInventory().setContents(this.getInventory().getContents());

                });

                //Stop iterating.
                break;

            }

            int finalI = i;
            setItem(slot, Utils.createItem(Material.LIME_CONCRETE, 1,
                            Utils.title("Region " + requests.get(i)),
                            Utils.line("Awaiting review by ").append(Utils.line(

                                    (regionSQL.hasRow("SELECT region FROM region_requests WHERE region='" + requests.get(i) + "' AND uuid='" + u.player.getUniqueId() + "' AND staff_accept=0;"))
                                            ? "a reviewer"
                                            : Network.getInstance().getGlobalSQL().getString("SELECT name FROM player_data WHERE uuid='" +
                                            regionSQL.getString("SELECT owner FROM region_requests WHERE region='" + requests.get(i) + "' AND uuid='" + u.player.getUniqueId() + "';")
                                            + "';")

                            )),
                            Utils.line("Click to cancel")),

                    u -> {

                        //Delete region request.
                        regionSQL.update("DELETE FROM region_requests  WHERE region='" + requests.get(finalI) + "' AND uuid='" + u.player.getUniqueId() + "';");

                        //Close the gui and send feedback.
                        u.player.closeInventory();
                        u.player.sendMessage(ChatUtils.success("Cancelled region request for ")
                                .append(Component.text(requests.get(finalI), NamedTextColor.DARK_AQUA)));

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
                Utils.line("Open the region menu.")), u -> {

            //Delete this gui.
            this.delete();

            //Switch to region menu.
            u.mainGui = new RegionMenu(u);
            u.mainGui.open(u);

        });

    }

    public void refresh() {

        this.clearGui();
        createGui();

    }
}
