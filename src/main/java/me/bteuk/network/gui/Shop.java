package me.bteuk.network.gui;

import me.bteuk.network.Network;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

public class Shop extends Gui {

    public Shop() {

        super(27, Component.text("Shop", NamedTextColor.AQUA, TextDecoration.BOLD));

        createGui();

    }

    private void createGui() {

        setItem(10, Utils.createItem(Material.PINK_CONCRETE_POWDER, 1,
                        Utils.title("2023 Card Pack"),
                        Utils.line("Price: 1000")),
                u -> openLocation(u, "Yorkshire",
                        Network.getInstance().globalSQL.getStringList("SELECT location FROM location_data WHERE category='ENGLAND' AND subcategory='YORKSHIRE';"))

        );

        //Return
        setItem(26, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.chat("&b&lReturn"),
                        Utils.chat("&fOpen the navigator main menu.")),
                u ->

                {

                    //Delete this gui.
                    this.delete();
                    u.mainGui = null;

                    //Switch to navigation menu.
                    Network.getInstance().navigatorGui.open(u);

                });
    }

    public void refresh() {

        this.clearGui();
        createGui();

    }
}
