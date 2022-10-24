package me.bteuk.network.gui.navigation;

import me.bteuk.network.gui.Gui;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

public class EnglandMenu extends Gui {

    public EnglandMenu() {

        super(27, Component.text("Exploration Menu", NamedTextColor.AQUA, TextDecoration.BOLD));

        createGui();

    }

    private void createGui() {

        //England
        setItem(2, Utils.createItem(Material.ORANGE_CONCRETE_POWDER, 1,
                        Utils.title("England"),
                        Utils.line("Click to pick from"),
                        Utils.line("locations in England.")),
                u -> {

                    //Get all
                    this.delete();
                    u.mainGui = new LocationMenu();
                    u.mainGui.open(u);

                }

        );

        //Return
        setItem(26, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.title("Return"),
                        Utils.line("Open the exploration menu.")),
                u ->

                {

                    //Delete this gui.
                    this.delete();

                    //Switch to exploration menu.
                    u.mainGui = new ExploreGui(u);
                    u.mainGui.open(u);

                });
    }

    public void refresh() {

        this.clearGui();
        createGui();

    }
}
