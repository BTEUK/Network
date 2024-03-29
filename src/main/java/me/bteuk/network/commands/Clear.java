package me.bteuk.network.commands;

import me.bteuk.network.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Clear implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Check if the sender is a player.
        if (!(sender instanceof Player p)) {

            sender.sendMessage(Utils.error("This command can only be run by a player."));
            return true;

        }

        //Check permission.
        if (!p.hasPermission("uknet.clear")) {

            p.sendMessage(Utils.error("You do not have permission to use this command."));
            return true;

        }

        //Clear inventory.
        p.getInventory().clear();
        p.sendMessage(Utils.success("Cleared your inventory."));

        return true;

    }
}
