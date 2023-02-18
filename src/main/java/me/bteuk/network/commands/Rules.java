package me.bteuk.network.commands;

import me.bteuk.network.Network;
import me.bteuk.network.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Rules implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player p)) {

            sender.sendMessage(Utils.error("This command can only be used by a player."));
            return true;

        }

        //Open rules book.
        p.openBook(Network.getInstance().getLobby().getRules());
        return true;

    }
}
