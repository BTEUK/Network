package me.bteuk.network.gui.navigation;

import me.bteuk.network.Network;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.enums.Categories;
import me.bteuk.network.utils.enums.Counties;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

public class AddLocation extends Gui {

    private String name = null;
    private Categories category = null;
    private Counties county = null;

    private final NetworkUser u;

    public AddLocation(NetworkUser u) {

        super(27, Component.text("Add Location", NamedTextColor.AQUA, TextDecoration.BOLD));

        this.u = u;

    }

    private void createGui() {

        //Set name.


        //Select category.


        //If category is England, select county.



        //Return
        setItem(26, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.chat("&b&lReturn"),
                        Utils.chat("&fOpen the explore menu.")),
                u ->

                {

                    //Delete this gui.
                    this.delete();
                    u.addLocation = null;

                    //Switch to navigation menu.
                    u.exploreGui = new ExploreGui(u);
                    u.exploreGui.open(u);

                });
    }

    public void refresh() {

        this.clearGui();
        createGui();

    }
}
