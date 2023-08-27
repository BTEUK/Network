package me.bteuk.network.commands.give;

import me.bteuk.network.Network;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Gives a debug stick to the player.
 * Extends GiveItem, which handles the actual giving of the item.
 */
public class GiveDebugStick extends GiveItem {

    public GiveDebugStick(Network instance) {
        super(instance, "debugstick");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {

        return (onCommand(sender, "uknet.debugstick", new ItemStack(Material.DEBUG_STICK), "Debug Stick"));

    }
}
