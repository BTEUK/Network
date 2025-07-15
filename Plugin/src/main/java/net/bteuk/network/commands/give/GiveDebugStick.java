package net.bteuk.network.commands.give;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Gives a debug stick to the player.
 * Extends GiveItem, which handles the actual giving of the item.
 */
public class GiveDebugStick extends GiveItem {
    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        onCommand(stack, "uknet.debugstick", ItemStack.of(Material.DEBUG_STICK), "Debug Stick");
    }
}
