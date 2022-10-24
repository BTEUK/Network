package me.bteuk.network.commands;

import me.bteuk.network.Network;
import me.bteuk.network.gui.navigation.AddLocation;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AddLocationCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //If sender is player, get user.
        //Open 'AddLocation' gui.
        if (sender instanceof Player p) {

            NetworkUser u = Network.getInstance().getUser(p);
            if (u.mainGui != null) {
                u.mainGui.delete();
            }
            u.mainGui = new AddLocation();
            u.mainGui.open(u);

        } else {
            sender.sendMessage(Utils.error("This command can only be used by a player."));
        }

        return true;
    }
}
