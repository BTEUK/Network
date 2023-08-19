package me.bteuk.network.commands;

import me.bteuk.network.Network;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import static me.bteuk.network.utils.Constants.LOGGER;

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
