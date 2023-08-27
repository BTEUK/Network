package me.bteuk.network.commands.give;

import me.bteuk.network.Network;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Gives a barrier to the player.
 * Extends GiveItem, which handles the actual giving of the item.
 */
public class GiveBarrier extends GiveItem {

    public GiveBarrier(Network instance) {
        super(instance, "barrier");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {

        return (onCommand(sender, "uknet.barrier", new ItemStack(Material.BARRIER), "Barrier"));

    }
}
