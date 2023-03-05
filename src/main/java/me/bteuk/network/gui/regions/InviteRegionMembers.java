package me.bteuk.network.gui.regions;

import me.bteuk.network.Network;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.sql.RegionSQL;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.regions.Region;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

import java.util.ArrayList;

public class InviteRegionMembers extends Gui {

    private int page;

    private final Region region;

    private final GlobalSQL globalSQL;
    private final RegionSQL regionSQL;

    public InviteRegionMembers(Region region) {

        super(45, Component.text("Invite Members", NamedTextColor.AQUA, TextDecoration.BOLD));

        this.region = region;

        page = 1;

        globalSQL = Network.getInstance().globalSQL;
        regionSQL = Network.getInstance().regionSQL;

        createGui();

    }

    private void createGui() {

        //Get all online players in the network.
        ArrayList<String> online_users = globalSQL.getStringList("SELECT uuid FROM online_users;");

        //Slot count.
        int slot = 10;

        //Skip count.
        int skip = 21 * (page - 1);

        //If page is greater than 1 add a previous page button.
        if (page > 1) {
            setItem(18, Utils.createItem(Material.ARROW, 1,
                            Utils.title("Previous Page"),
                            Utils.line("Open the previous page of online users.")),
                    u ->

                    {

                        //Update the gui.
                        page--;
                        this.refresh();
                        u.player.getOpenInventory().getTopInventory().setContents(this.getInventory().getContents());

                    });
        }

        //Iterate through all online players.
        for (String uuid : online_users) {

            //If the slot is greater than the number that fit in a page, create a new page.
            if (slot > 34) {

                setItem(26, Utils.createItem(Material.ARROW, 1,
                                Utils.title("Next Page"),
                                Utils.line("Open the next page of online users.")),
                        u ->

                        {

                            //Update the gui.
                            page++;
                            this.refresh();
                            u.player.getOpenInventory().getTopInventory().setContents(this.getInventory().getContents());

                        });

            }

            //Check whether the player is not already the owner or member of the region, if true skip them.
            if (regionSQL.hasRow("SELECT uuid FROM region_members WHERE region='" + region.regionName() + "' AND uuid='" + uuid + "';")) {
                continue;
            }

            //If skip is greater than 0, skip this iteration.
            if (skip > 0) {
                skip--;
                continue;
            }

            //Add player to gui.
            setItem(slot, Utils.createPlayerSkull(uuid, 1,
                            Utils.title("Invite " + globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + uuid + "';") + " to your plot."),
                            Utils.line("They will receive an invitation in chat.")),
                    u ->

                    {

                        //Check if the player is still online.
                        if (globalSQL.hasRow("SELECT uuid FROM online_users WHERE uuid='" + uuid + "';")) {

                            //Check if the player is not already a member of the plot.
                            if (!regionSQL.hasRow("SELECT region FROM region_members WHERE region='" + region.regionName() + "' AND uuid='" + uuid + "';")) {

                                //Check if the player has not already been invited.
                                if (!regionSQL.hasRow("SELECT region FROM region_invites WHERE region='" + region.regionName() + "' AND uuid='" + uuid + "';")) {

                                    //Send invite via chat.
                                    //The invite will be active until they disconnect from the network.
                                    //They will need to run a command to actually join the plot.
                                    regionSQL.update("INSERT INTO region_invites(region,owner,uuid) VALUES('" + region.regionName() + "','" +
                                            u.player.getUniqueId() + "','" + uuid + "');");

                                    globalSQL.update("INSERT INTO server_events(uuid,type,server,event) VALUES('" + uuid + "','network','" +
                                            globalSQL.getString("SELECT server FROM online_users WHERE uuid='" + uuid + "';") + "','invite region " + region.regionName() + "')");

                                    u.player.sendMessage(Utils.success("Invited &3" + globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + uuid + "'") + " &ato region " + region.getTag(u.player.getUniqueId().toString()) + "."));

                                } else {
                                    u.player.sendMessage(Utils.error("You've already invited this player to your plot."));
                                }

                            } else {
                                u.player.sendMessage(Utils.error("This player is already a member of your region."));
                            }

                        } else {
                            u.player.sendMessage(Utils.error("This player is no longer online."));
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
