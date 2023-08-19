package me.bteuk.network.commands;

import me.bteuk.network.Network;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Gives a light to the player.
 * Extends GiveItem, which handles the actual giving of the item.
 */
public class GiveLight extends GiveItem {

    public GiveLight(Network instance) {
        super(instance, "light");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {

        return (onCommand(sender, "uknet.light", new ItemStack(Material.LIGHT), "Light"));

    }
}
