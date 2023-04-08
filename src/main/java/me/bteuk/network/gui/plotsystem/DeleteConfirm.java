package me.bteuk.network.gui.plotsystem;

import me.bteuk.network.Network;
import me.bteuk.network.events.EventManager;
import me.bteuk.network.gui.BuildGui;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.sql.PlotSQL;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.enums.RegionType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

public class DeleteConfirm extends Gui {

    private final int id;
    private final RegionType regionType;

    public DeleteConfirm(int id, RegionType regionType) {

        super(27, Component.text("Delete " + regionType.label + " " + id, NamedTextColor.AQUA, TextDecoration.BOLD));

        this.id = id;
        this.regionType = regionType;

        createGui();

    }

    private void createGui() {

        //Get plot sql.
        PlotSQL plotSQL = Network.getInstance().plotSQL;

        //Delete plot
        setItem(13, Utils.createItem(Material.TNT, 1,
                        Utils.title("Delete " + regionType.label + " " + id),
                        Utils.line("Delete the " + regionType.label + " and its contents.")),
                u -> {

                    //Delete this inventory.
                    this.delete();
                    u.mainGui = null;

                    //Create plot or zone menu, so the next time you open the navigator you return to that.
                    //Then add server event to delete plot or zone.
                    if (regionType == RegionType.PLOT) {

                        u.mainGui = new PlotMenu(u);

                        //Add server event to delete plot or zone.
                        EventManager.createEvent(u.player.getUniqueId().toString(), "plotsystem", plotSQL.getString("SELECT server FROM location_data WHERE name='" +
                                plotSQL.getString("SELECT location FROM plot_data WHERE id=" + id + ";") + "';"), "delete plot " + id);

                    } else if (regionType == RegionType.ZONE) {

                        u.mainGui = new ZoneMenu(u);

                        //Add server event to delete plot or zone.
                        EventManager.createEvent(u.player.getUniqueId().toString(), "plotsystem", plotSQL.getString("SELECT server FROM location_data WHERE name='" +
                                plotSQL.getString("SELECT location FROM zones WHERE id=" + id + ";") + "';"), "delete zone " + id);

                    }

                });

        //Return to plot info menu.
        setItem(26, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.title("Return"),
                        Utils.line("Return to the menu of " + regionType.label + " " + id + ".")),
                u ->

                {

                    //Delete this inventory.
                    this.delete();
                    u.mainGui = null;

                    //Switch back to plot or zone info.
                    if (regionType == RegionType.PLOT) {

                        u.mainGui = new PlotInfo(id, u.player.getUniqueId().toString());

                    } else if (regionType == RegionType.ZONE) {

                        u.mainGui = new ZoneInfo(id, u.player.getUniqueId().toString());

                    } else {

                        u.mainGui = new BuildGui(u);

                    }

                    u.mainGui.open(u);

                });
    }

    public void refresh() {

        this.clearGui();
        createGui();

    }
}
