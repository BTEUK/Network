package net.bteuk.network.commands.give;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Gives a barrier to the player.
 * Extends GiveItem, which handles the actual giving of the item.
 */
public class GiveBarrier extends GiveItem {
    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        onCommand(stack, "uknet.barrier", ItemStack.of(Material.BARRIER), "Barrier");
    }
}
