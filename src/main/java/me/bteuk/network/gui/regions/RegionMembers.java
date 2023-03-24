package me.bteuk.network.gui.regions;

import me.bteuk.network.Network;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.utils.Time;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.regions.Region;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
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
                            Utils.title("Switch Mode"),
                            Utils.line("Converts gui to kick members."),
                            Utils.line("Clicking on a player head"),
                            Utils.line("will kick them from the region.")),

                    u -> {

                        transfer = !transfer;
                        this.refresh();

                    });
        } else {

            setItem(8, Utils.createItem(Material.MAGENTA_GLAZED_TERRACOTTA, 1,
                            Utils.title("Switch Mode"),
                            Utils.line("Converts gui to transfer ownership."),
                            Utils.line("Clicking on a player head will"),
                            Utils.line("make them the owner of the region.")),

                    u -> {

                        transfer = !transfer;
                        this.refresh();

                    });
        }

        //If page is greater than 1 add a previous page button.
        if (page > 1) {
            setItem(18, Utils.createItem(Material.ARROW, 1,
                            Utils.title("Previous Page"),
                            Utils.line("Open the previous page of region members.")),
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

            //If uuid is yours, skip.
            if (uuid.equals(region.getOwner())) {
                continue;
            }

            //If the slot is greater than the number that fit in a page, create a new page.
            if (slot > 34) {

                setItem(26, Utils.createItem(Material.ARROW, 1,
                                Utils.title("Next Page"),
                                Utils.line("Open the next page of region members.")),
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

            //If skip is greater than 0, skip this iteration.
            if (skip > 0) {
                skip--;
                continue;
            }

            //Add player to gui.
            if (transfer) {

                setItem(slot, Utils.createPlayerSkull(uuid, 1,
                                Utils.title("Make " + globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + uuid + "';") + " the region owner."),
                                Utils.line("Most recently in this region at " + Time.getDateTime(region.lastActive(uuid))),
                                Utils.line("You will be demoted to region member.")),
                        u ->

                        {

                            //Make the previous owner a member.
                            region.makeMember();

                            //Give the new player ownership.
                            region.makeOwner(uuid);

                            //Update any requests to take into account the new region owner.
                            region.updateRequests();

                            //Send message to user.
                            u.player.sendMessage(Utils.success("Transferred ownership of the region to &3" +
                                    globalSQL.getString("SELECT name FROM player_data WHERE uuid ='" + region.getOwner() + "';")));

                            //Send message to new owner.
                            Network.getInstance().globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" + uuid + "','&aYou are now the owner of region &3" + region.getTag(uuid) + "');");

                            //Return to region info.
                            this.delete();
                            u.mainGui = null;

                            u.mainGui = new RegionInfo(region, u.player.getUniqueId().toString());
                            u.mainGui.open(u);

                        });
            } else {

                setItem(slot, Utils.createPlayerSkull(uuid, 1,
                                Utils.title("Kick " + globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + uuid + "';") + " from the region."),
                                Utils.line("Most recently in this region at " + Time.getDateTime(region.lastActive(uuid)))),
                        u ->

                        {
                            //Remove them from the region.
                            region.leaveRegion(uuid, "&cYou have been kicked from region &4" + region.getTag(uuid));

                            //Send message to user.
                            u.player.sendMessage(Utils.success("Kicked &3" +
                                    globalSQL.getString("SELECT name FROM player_data WHERE uuid ='" + uuid + "';") + " &afrom the region"));

                            //Refresh the gui.
                            //Delay this action so the user can be kicked, even if on another server.
                            Bukkit.getScheduler().runTaskLater(Network.getInstance(), this::refresh, 20L);

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
                        Utils.title("Return"),
                        Utils.line("Return to the menu of region &7" + region.getTag(region.getOwner()) + "&f.")),
                u ->

                {

                    //Delete this gui.
                    this.delete();
                    u.mainGui = null;

                    //Switch back to plot info.
                    u.mainGui = new RegionInfo(region, u.player.getUniqueId().toString());
                    u.mainGui.open(u);

                });
    }

    public void refresh() {

        this.clearGui();
        createGui();

    }
}
