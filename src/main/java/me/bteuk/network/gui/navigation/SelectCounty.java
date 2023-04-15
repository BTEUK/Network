package me.bteuk.network.gui.navigation;

import me.bteuk.network.gui.Gui;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.enums.AddLocationType;
import me.bteuk.network.utils.enums.Counties;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.Comparator;

public class SelectCounty extends Gui {

    private final AddLocation addLocation;

    public SelectCounty(AddLocation addLocation) {

        super(54, Component.text("Select County", NamedTextColor.AQUA, TextDecoration.BOLD));

        this.addLocation = addLocation;
        createGui();

    }

    private void createGui() {

        //Iterate through counties and add a button for each.
        Counties[] counties = Counties.values();
        Arrays.sort(counties, new CountyComparator());

        int slot = 0;

        for (Counties county: counties) {

            setItem(slot, Utils.createItem(Material.LIME_CONCRETE, 1,
                            Utils.title(county.label),
                            Utils.line("Click to select this county.")),

                    u -> {

                        //Set the county.
                        addLocation.setCounty(county);

                        //Delete this gui.
                        this.delete();
                        addLocation.selectCounty = null;

                        //Return to addlocation.
                        if (addLocation.getType() == AddLocationType.ADD) {
                            u.mainGui.refresh();
                            u.mainGui.open(u);
                        } else {
                            u.staffGui.refresh();
                            u.staffGui.open(u);
                        }

                    });

            slot++;
        }

        //Return
        setItem(53, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.title("Return"),
                        Utils.line("Open the add location menu.")),
                u ->

                {

                    //Delete this gui.
                    this.delete();
                    addLocation.selectCounty = null;

                    //Switch to navigation menu.
                    u.mainGui.open(u);

                });
    }

    public void refresh() {

        this.createGui();
        createGui();

    }
}

//Class to compare counties by label in alphabetical order.
class CountyComparator implements Comparator<Counties> {
    @Override
    public int compare(Counties o1, Counties o2) {
        return (o1.label.compareTo(o2.label));
    }
}
