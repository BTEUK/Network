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
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import static me.bteuk.network.utils.Constants.LOGGER;

public class DebugStick implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {

        //Check if the sender is a player.
        if (!(sender instanceof Player p)) {

            sender.sendMessage(Utils.error("You must be a player to use this command."));
            return true;

        }

        //Check if the user has permission.
        if (!p.hasPermission("uknet.debugstick")) {

            p.sendMessage(Utils.error("You do not have permission to use this command."));
            return true;

        }

        //Get the player inventory and check whether they already have the selection tool.
        PlayerInventory i = p.getInventory();

        //Get user.
        NetworkUser u = Network.getInstance().getUser(p);

        //If u is null, cancel.
        if (u == null) {
            LOGGER.severe("User " + p.getName() + " can not be found!");
            p.sendMessage("User can not be found, please relog!");
            return true;
        }

        //Check if the player already has the selection tool in their inventory.
        if (i.containsAtLeast(new ItemStack(Material.DEBUG_STICK), 1)) {

            //Get the selection tool from their inventory and swap it with the item in their hand.
            i.setItem(i.first(new ItemStack(Material.DEBUG_STICK)), i.getItemInMainHand());
            i.setItemInMainHand(new ItemStack(Material.DEBUG_STICK));

            p.sendMessage(Utils.success("Switched to debug stick from inventory."));

        } else if (u.navigator && i.getHeldItemSlot() == 8) {

            i.setItem(7, new ItemStack(Material.DEBUG_STICK));

            u.player.sendMessage(Utils.success("Set debug stick to slot 8."));

        } else {

            //If they don't have the selection tool already set it in their main hand.
            i.setItemInMainHand(new ItemStack(Material.DEBUG_STICK));

            p.sendMessage(Utils.success("Set debug stick to main hand."));

        }

        return true;
    }
}
