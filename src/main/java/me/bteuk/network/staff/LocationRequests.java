package me.bteuk.network.staff;

import me.bteuk.network.Network;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

import java.util.ArrayList;

public class LocationRequests extends Gui {

    public LocationRequests() {

        super(27, Component.text("Location Requests", NamedTextColor.AQUA, TextDecoration.BOLD));

        createGui();

    }

    private void createGui() {

        //Get all locationRequests.
        ArrayList<String> locations = Network.getInstance().globalSQL.getStringList("SELECT location FROM location_requests;");

        //This gui only supports 7 requests due to limited space allocated.
        //However since a large volume of requests is not expected at any point, this should be sufficient.
        //If the number does exceed 7 then they will just not show up until previous have been cleared.
        int slot = 10;

        for (String location : locations) {

            setItem(slot, Utils.createItem(Material.ORANGE_CONCRETE, 1,
                            Utils.title("location"),
                            Utils.line("Click to review this location request.")),
                    u -> {

                        //Opens location request.
                        this.delete();
                        u.staffGui = null;

                        u.staffGui = new LocationRequest(location);
                        u.staffGui.open(u);

                    });

        }

        //Return
        setItem(26, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.chat("&b&lReturn"),
                        Utils.chat("&fOpen the staff menu.")),
                u ->

                {

                    //Delete this gui.
                    this.delete();
                    u.staffGui = null;

                    //Switch to staff menu.
                    u.staffGui = new StaffGui(u);
                    u.staffGui.open(u);

                });

    }

    public void refresh() {

        this.clearGui();
        createGui();

    }

}
