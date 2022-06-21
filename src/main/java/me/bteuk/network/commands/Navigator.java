package me.bteuk.network.commands;

import me.bteuk.network.Network;
import me.bteuk.network.gui.NavigatorGui;
import me.bteuk.network.utils.NetworkUser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Navigator implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {

        //Check if the sender is a player.
        if (!(sender instanceof Player p)) {

            sender.sendMessage("This command can only be used by a player.");
            return true;

        }

        //Get user.
        NetworkUser u = Network.getInstance().getUser(p);

        //If the player has a previous gui, open that.
        if (u.uniqueGui != null) {

            u.uniqueGui.open(u);
            return true;

        } else {

            //Open the navigator.
            u.uniqueGui = NavigatorGui.createNavigator();
            u.uniqueGui.open(u);

        }

        return true;
    }
}
