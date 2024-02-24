package me.bteuk.network.gui.staff;

import me.bteuk.network.Network;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.gui.navigation.AddLocation;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.enums.AddLocationType;
import me.bteuk.network.utils.enums.Category;
import me.bteuk.network.utils.enums.Regions;
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
                            Utils.title(location),
                            Utils.line("Click to review this location request.")),
                    u -> {

                        //Opens location request.
                        this.delete();
                        u.staffGui = null;

                        int coordinate_id = Network.getInstance().globalSQL.getInt("SELECT coordinate FROM location_requests WHERE location='" + location + "';");
                        Category category = Category.valueOf(Network.getInstance().globalSQL.getString("SELECT category FROM location_requests WHERE location='" + location + "';"));

                        Regions subcategory = null;
                        if (category == Category.ENGLAND) {
                            subcategory = Regions.valueOf(Network.getInstance().globalSQL.getString("SELECT subcategory FROM location_requests WHERE location='" + location + "';"));
                        }

                        u.staffGui = new AddLocation(AddLocationType.REVIEW, location, coordinate_id, category, subcategory);
                        u.staffGui.open(u);

                    });

            slot++;

            if (slot > 16) {
                break;
            }

        }

        //Return
        setItem(26, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.title("Return"),
                        Utils.line("Open the staff menu.")),
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
