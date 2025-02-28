package net.bteuk.network.gui;

import net.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class UtilsGui extends Gui {

    public UtilsGui() {

        super(27, Component.text("Building Utils", NamedTextColor.AQUA, TextDecoration.BOLD));

        createGui();

    }

    public void createGui() {

        //Get debug stick.
        setItem(13, Utils.createItem(Material.DEBUG_STICK, 1,
                        Utils.title("Debug Stick"),
                        Utils.line("Click to get the debug stick.")),
                u ->

                {

                    //Delete this gui.
                    u.player.closeInventory();

                    //Run the command to get the item.
                    u.player.performCommand("debugstick");

                });

        //Get light block.
        setItem(11, Utils.createItem(Material.LIGHT, 1,
                        Utils.title("Light"),
                        Utils.line("Click to get a light.")),
                u ->

                {

                    //Delete this gui.
                    u.player.closeInventory();

                    //Run the command to get the item.
                    u.player.performCommand("light");

                });

        //Get barrier block.
        setItem(15, Utils.createItem(Material.BARRIER, 1,
                        Utils.title("Barrier"),
                        Utils.line("Click to get a barrier.")),
                u ->

                {

                    //Delete this gui.
                    u.player.closeInventory();

                    //Run the command to get the item.
                    u.player.performCommand("barrier");

                });

        //Return
        setItem(26, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.title("Return"),
                        Utils.line("Open the navigator main menu.")),
                u ->

                {

                    //Delete this gui.
                    this.delete();
                    u.mainGui = null;

                    //Switch to navigation menu.
                    u.mainGui = new BuildGui(u);
                    u.mainGui.open(u);

                });
    }

    @Override
    public void refresh() {

        clearGui();
        createGui();

    }
}
