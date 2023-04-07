package me.bteuk.network.gui.plotsystem;

import me.bteuk.network.Network;
import me.bteuk.network.gui.BuildGui;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.sql.PlotSQL;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

import java.util.ArrayList;

public class ZoneMenu extends Gui {

    private final NetworkUser user;
    private final PlotSQL plotSQL;

    public ZoneMenu(NetworkUser user) {

        super(45, Component.text("Zone Menu", NamedTextColor.AQUA, TextDecoration.BOLD));

        this.user = user;
        plotSQL = Network.getInstance().plotSQL;

        createGui();

    }

    private void createGui() {

        /*
        Gui layout:

        Similar to the plots, first list the zones you own, this can be a maximum of 5, then list the zones you are a member of.

        Return button in the last slot.

         */

        //Get all zones that you are the owner of, order by is_owner, so zones you own show first.
        ArrayList<Integer> zones = plotSQL.getIntList("SELECT id FROM zone_members WHERE uuid='" + user.player.getUniqueId() + "' ORDER BY is_owner DESC;");

        //Slot count.
        int slot = 10;

        //Make a button for each plot.
        for (int i = 0; i < zones.size(); i++) {

            int finalI = i;

            //Change the colour of the material for plot owners/members.
            //Lime for owners, yellow for members.
            if (plotSQL.hasRow("SELECT uuid FROM zone_members WHERE uuid='" + user.player.getUniqueId() + "' AND id=" + zones.get(i) + " AND is_owner=1;"))

                setItem(slot, Utils.createItem(
                                (plotSQL.hasRow("SELECT uuid FROM zone_members WHERE uuid='" + user.player.getUniqueId() + "' AND id=" + zones.get(i) + " AND is_owner=1;") ? Material.LIME_CONCRETE : Material.YELLOW_CONCRETE),
                                1,
                                Utils.title("Zone " + zones.get(i)),
                                Utils.line("Click to open the menu of this zone.")),
                        u -> {

                            //Delete this gui.
                            this.delete();
                            u.mainGui = null;

                            //Switch to plot info.
                            u.mainGui = new ZoneInfo(zones.get(finalI), u.player.getUniqueId().toString());
                            u.mainGui.open(u);

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
                        Utils.line("Open the building menu.")),
                u -> {

                    //Delete this gui.
                    this.delete();
                    u.mainGui = null;

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
