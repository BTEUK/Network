package me.bteuk.network.gui;

import me.bteuk.network.Network;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

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

                    //Check if the user has permission.
                    if (!u.player.hasPermission("uknet.debugstick")) {

                        u.player.sendMessage(Utils.error("You do not have permission to use this command."));
                        return;

                    }

                    //Get the player inventory and check whether they already have the selection tool.
                    PlayerInventory i = u.player.getInventory();

                    //Check if the player already has the selection tool in their inventory.
                    if (i.containsAtLeast(new ItemStack(Material.DEBUG_STICK),1)) {

                        //Get the selection tool from their inventory and swap it with the item in their hand.
                        i.setItem(i.first(new ItemStack(Material.DEBUG_STICK)), i.getItemInMainHand());
                        i.setItemInMainHand(new ItemStack(Material.DEBUG_STICK));

                        u.player.sendMessage(Utils.success("Switched to debug stick from inventory."));

                        //If user has navigator enabled and they have that slot selected, set it to slot 7.
                    } else if (u.navigator && i.getHeldItemSlot() == 8) {

                        i.setItem(7, new ItemStack(Material.DEBUG_STICK));

                        u.player.sendMessage(Utils.success("Set debug stick to slot 8."));

                    } else {

                        //If they don't have the selection tool already set it in their main hand.
                        i.setItemInMainHand(new ItemStack(Material.DEBUG_STICK));

                        u.player.sendMessage(Utils.success("Set debug stick to main hand."));

                    }

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
