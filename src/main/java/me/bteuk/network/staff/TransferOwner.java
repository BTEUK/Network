package me.bteuk.network.staff;

import me.bteuk.network.Network;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.regions.Region;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

import java.util.ArrayList;

public class TransferOwner extends Gui {

    private int page;

    private final Region region;

    private final GlobalSQL globalSQL;

    public TransferOwner(Region region) {

        super(45, Component.text("Transfer Ownership", NamedTextColor.AQUA, TextDecoration.BOLD));

        this.region = region;

        page = 1;

        globalSQL = Network.getInstance().globalSQL;

        createGui();

    }

    private void createGui() {

        //Get all members of the region.
        ArrayList<String> region_members = region.getMembers();

        //Slot count.
        int slot = 10;

        //Skip count.
        int skip = 21 * (page - 1);

        //If page is greater than 1 add a previous page button.
        if (page > 1) {
            setItem(18, Utils.createItem(Material.ARROW, 1,
                            Utils.chat("&b&lPrevious Page"),
                            Utils.chat("&fOpen the previous page of region members.")),
                    u ->

                    {

                        //Update the gui.
                        page--;
                        this.refresh();
                        u.player.getOpenInventory().getTopInventory().setContents(this.getInventory().getContents());

                    });
        }

        //Iterate through all online players.
        for (String uuid : region_members) {

            //If the slot is greater than the number that fit in a page, create a new page.
            if (slot > 34) {

                setItem(26, Utils.createItem(Material.ARROW, 1,
                                Utils.chat("&b&lNext Page"),
                                Utils.chat("&fOpen the next page of region members.")),
                        u ->

                        {

                            //Update the gui.
                            page++;
                            this.refresh();
                            u.player.getOpenInventory().getTopInventory().setContents(this.getInventory().getContents());

                        });

            }

            //If skip is greater than 0, skip this iteration.
            if (skip > 0) {
                skip--;
                continue;
            }

            //Add player to gui.
            setItem(slot, Utils.createPlayerSkull(uuid, 1,
                            Utils.chat("&b&lMake " + globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + uuid + "';") + " the region owner."),
                            Utils.chat("&fThe previous owner will be demoted to a member.")),
                    u ->

                    {
                        //Make the previous owner a member.
                        region.makeMember();

                        //Give the new player ownership.
                        region.makeOwner(uuid);

                        //Update any requests to take into account the new region owner.
                        region.updateRequests();

                        //Send message to user.
                        u.player.sendMessage(Utils.chat("&aTransferred ownership of the region to &3" +
                                globalSQL.getString("SELECT name FROM player_data WHERE uuid ='" + region.getOwner() + "';")));

                        //Refresh the gui.
                        this.refresh();

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

        //Return to plot info menu.
        setItem(44, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.chat("&b&lReturn"),
                        Utils.chat("&fReturn to manage region &7" + region.regionName() + ".")),
                u ->

                {

                    //Delete this gui.
                    this.delete();
                    u.staffUser.transferOwner = null;

                    //Switch back to plot info.
                    u.staffUser.manageRegion = new ManageRegion(u, region);
                    u.staffUser.manageRegion.open(u);

                });
    }

    public void refresh() {

        this.clearGui();
        createGui();

    }
}
