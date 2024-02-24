package me.bteuk.network.gui.navigation;

import me.bteuk.network.Network;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.enums.AddLocationType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

import java.util.List;

public class SelectSubcategory extends Gui {

    private final AddLocation addLocation;

    private int page = 1;

    public SelectSubcategory(AddLocation addLocation) {

        super(45, Component.text("Select Subcategory", NamedTextColor.AQUA, TextDecoration.BOLD));

        this.addLocation = addLocation;
        createGui();

    }

    private void createGui() {

        //Iterate through subcatories, starting with 'None'.
        List<String> subcategories = Network.getInstance().getGlobalSQL().getStringList("SELECT name FROM location_subcategory WHERE category='" + addLocation.getCategory() + "' ORDER BY name ASC;");
        subcategories.add(0, "None");


        //If page > 1 set number of iterations that must be skipped.
        int skip = (page - 1) * 21;

        //Slot count.
        int slot = 10;

        //If page is greater than 1 add a previous page button.
        if (page > 1) {
            setItem(18, Utils.createItem(Material.ARROW, 1,
                            Utils.title("Previous Page"),
                            Utils.line("Open the previous page of subcategories.")),
                    u ->

                    {
                        //Update the gui.
                        page--;
                        this.refresh();
                        u.player.getOpenInventory().getTopInventory().setContents(this.getInventory().getContents());
                    });
        }

        for (String subcategory : subcategories) {

            //Skip iterations if skip > 0.
            if (skip > 0) {
                skip--;
                continue;
            }

            setItem(slot, Utils.createItem(Material.LIME_CONCRETE, 1,
                            Utils.title(subcategory),
                            Utils.line("Click to select this subcategory.")),

                    u -> {
                        //Set the county.
                        addLocation.setSubcategory(subcategory);
                        returnToAddLocation(u);
                    });
            slot++;

            //If the slot is greater than the number that fit in a page, create a new page.
            if (slot > 34) {

                setItem(26, Utils.createItem(Material.ARROW, 1,
                                Utils.title("Next Page"),
                                Utils.line("Open the next page of locations.")),
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
                        Utils.line("Open the previous menu.")),
                this::returnToAddLocation);
    }

    public void refresh() {

        this.createGui();
        createGui();

    }

    private void returnToAddLocation(NetworkUser u) {
        //Delete this gui.
        this.delete();
        addLocation.selectSubcategory = null;

        //Return to addlocation.
        if (addLocation.getType() == AddLocationType.ADD) {
            u.mainGui.refresh();
            u.mainGui.open(u);
        } else {
            u.staffGui.refresh();
            u.staffGui.open(u);
        }
    }
}
