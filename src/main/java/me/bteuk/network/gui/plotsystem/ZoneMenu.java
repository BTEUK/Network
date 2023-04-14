package me.bteuk.network.gui.plotsystem;

import me.bteuk.network.Network;
import me.bteuk.network.events.EventManager;
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

        List all zones that you are a member of, then all public zones that can be joined.

        Return button in the last slot.

         */

        //Get all zones that you are the owner of, order by is_owner, so zones you own show first.
        ArrayList<Integer> zones = plotSQL.getIntList("SELECT id FROM zones WHERE status='open';");

        //Slot count.
        int slot = 10;

        //Make a button for each plot.
        for (int i = 0; i < zones.size(); i++) {

            int finalI = i;

            //If you are the zone owner, or a member, open the zone info menu.
            //If the zone is public then join the zone by clicking.
            //If the zone is private, do nothing.
            if (plotSQL.hasRow("SELECT uuid FROM zone_members WHERE uuid='" + user.player.getUniqueId() + "' AND id=" + zones.get(i) + ";")) {

                setItem(slot, Utils.createItem(
                                (plotSQL.hasRow("SELECT uuid FROM zone_members WHERE uuid='" + user.player.getUniqueId() + "' AND id=" + zones.get(i) + " AND is_owner=1;") ? Material.LIME_CONCRETE : Material.YELLOW_CONCRETE),
                                1,
                                Utils.title("Zone " + zones.get(i)),
                                Utils.line("Click to open the menu of this zone.")),
                        u -> {

                            //Delete this gui.
                            this.delete();
                            u.mainGui = null;

                            //Switch to zone info.
                            u.mainGui = new ZoneInfo(zones.get(finalI), u.player.getUniqueId().toString());
                            u.mainGui.open(u);

                        });

            } else if (plotSQL.hasRow("SELECT id FROM zones WHERE id=" + zones.get(i) + " AND is_public=1;")) {

                setItem(slot, Utils.createItem(Material.LIGHT_BLUE_CONCRETE,
                                1,
                                Utils.title("Zone " + zones.get(i)),
                                Utils.line("Click to join this zone.")),
                        u -> {

                            //Add server event to join zone.
                            EventManager.createEvent(u.player.getUniqueId().toString(), "plotsystem",
                                    plotSQL.getString("SELECT server FROM location_data WHERE name='" +
                                            plotSQL.getString("SELECT location FROM zones WHERE id=" + zones.get(finalI) + ";") + "';"),
                                    "join zone " + zones.get(finalI));


                        });

            } else {

                setItem(slot, Utils.createItem(Material.BARRIER,
                        1,
                        Utils.title("Zone " + zones.get(i)),
                        Utils.line("This zone is private,"),
                        Utils.line("to join this zone you must be"),
                        Utils.line("invited by ")
                                .append(Component.text(Network.getInstance().globalSQL.getString("SELECT name FROM player_data WHERE uuid='" +
                                        plotSQL.getString("SELECT uuid FROM zone_members WHERE id=" + zones.get(i) + " AND is_owner=1;") + "';"), NamedTextColor.GRAY))));

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
