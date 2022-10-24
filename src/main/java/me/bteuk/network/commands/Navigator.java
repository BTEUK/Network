package me.bteuk.network.commands;

import me.bteuk.network.Network;
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

        if (u == null) {return true;}

        //If the player has a previous gui, open that.
        openNavigator(u);

        return true;
    }

    public static void openNavigator(NetworkUser u) {

        //Check if the mainGui is not null.
        //If not then open it after refreshing its contents.
        //If no gui exists open the navigator.

        if (u.mainGui != null) {

            u.mainGui.refresh();
            u.mainGui.open(u);

        } else {

            Network.getInstance().navigatorGui.open(u);

        }
    }
}
