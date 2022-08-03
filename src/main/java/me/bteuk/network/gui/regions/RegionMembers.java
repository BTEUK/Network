package me.bteuk.network.gui.regions;

import me.bteuk.network.Network;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.staff.ManageRegion;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.regions.Region;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

import java.util.ArrayList;

public class RegionMembers extends Gui {

    private int page;

    private final Region region;

    private final GlobalSQL globalSQL;

    private boolean transfer;

    public RegionMembers(Region region) {

        super(45, Component.text("Region Members", NamedTextColor.AQUA, TextDecoration.BOLD));

        this.region = region;

        page = 1;

        transfer = false;

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

        //Which from kick member to transfer owner.
        if (transfer) {

            setItem(8, Utils.createItem(Material.MAGENTA_GLAZED_TERRACOTTA, 1,
                            Utils.chat("&b&lSwitch Mode"),
                            Utils.chat("&fConverts gui to kick members."),
                            Utils.chat("&fClicking on a player head"),
                            Utils.chat("&fwill kick them from the region.")),

                    u -> {

                        transfer = !transfer;
                        this.refresh();

                    });
        } else {

            setItem(8, Utils.createItem(Material.MAGENTA_GLAZED_TERRACOTTA, 1,
                            Utils.chat("&b&lSwitch Mode"),
                            Utils.chat("&fConverts gui to transfer ownership."),
                            Utils.chat("&fClicking on a player head will"),
                            Utils.chat("&fmake them the owner of the region.")),

                    u -> {

                        transfer = !transfer;
                        this.refresh();

                    });
        }

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
            if (transfer) {

                setItem(slot, Utils.createPlayerSkull(uuid, 1,
                                Utils.chat("&b&lMake " + globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + uuid + "';") + " the region owner."),
                                Utils.chat("&fYou will be demoted to region members.")),
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

                            //Return to region info.
                            this.delete();
                            u.regionMembers = null;

                            u.regionInfo = new RegionInfo(region, u.player.getUniqueId().toString());
                            u.regionInfo.open(u);

                        });
            } else {

                setItem(slot, Utils.createPlayerSkull(uuid, 1,
                                Utils.chat("&b&lKick " + globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + uuid + "';") + " from the region.")),
                        u ->

                        {
                            //Remove them from the region.
                            region.leaveRegion(uuid);

                            //Send message to user.
                            u.player.sendMessage(Utils.chat("&aKicked &3" +
                                    globalSQL.getString("SELECT name FROM player_data WHERE uuid ='" + region.getOwner() + "';") + " &afrom the region"));

                            //Refresh the gui.
                            this.refresh();

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

        //Return to plot info menu.
        setItem(44, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.chat("&b&lReturn"),
                        Utils.chat("&fReturn to manage region &7" + region.regionName() + ".")),
                u ->

                {

                    //Delete this gui.
                    this.delete();
                    u.staffUser.kickMembers = null;

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
