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

        //Check if any of the guis are not null.
        //If not then open the first inventory found after refreshing its contents.
        //If no gui exists open the navigator.

        if (u.buildGui != null) {

            u.buildGui.refresh();
            u.buildGui.open(u);

        } else if (u.plotServerLocations != null) {

            u.plotServerLocations.refresh();
            u.plotServerLocations.open(u);

        } else if (u.plotMenu != null) {

            u.plotMenu.refresh();
            u.plotMenu.open(u);

        } else if (u.plotInfo != null) {

            u.plotInfo.refresh();
            u.plotInfo.open(u);

        } else if (u.acceptedPlotFeedback != null) {

            u.acceptedPlotFeedback.refresh();
            u.acceptedPlotFeedback.open(u);

        } else if (u.deniedPlotFeedback != null) {

            u.deniedPlotFeedback.refresh();
            u.deniedPlotFeedback.open(u);

        } else if (u.deleteConfirm != null) {

            u.deleteConfirm.refresh();
            u.deleteConfirm.open(u);

        } else if (u.plotMembers != null) {

            u.plotMembers.refresh();
            u.plotMembers.open(u);

        } else if (u.inviteMembers != null) {

            u.inviteMembers.refresh();
            u.inviteMembers.open(u);

        } else {

            Network.getInstance().navigator.open(u);

        }
    }
}
