package me.bteuk.network.commands;

import me.bteuk.network.Network;
import me.bteuk.network.staff.StaffGui;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.StaffUser;
import me.bteuk.network.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Staff implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {

        //Check if the sender is a player.
        if (!(sender instanceof Player p)) {

            sender.sendMessage(Utils.chat("&cThis command can only be used by a player."));
            return true;

        }

        //Check if user is member of staff.
        if (!(p.hasPermission("uknet.staff"))) {

            p.sendMessage(Utils.chat("&cYou do not have permission to use this command."));
            return true;

        }

        //Get user.
        NetworkUser u = Network.getInstance().getUser(p);

        if (u == null) {return true;}

        //If the player has a previous gui, open that.
        openStaffMenu(u);

        return true;
    }

    public static void openStaffMenu(NetworkUser u) {

        //Check if the user has a staff instance, if not create it.
        if (u.staffUser == null) {

            u.staffUser = new StaffUser();

        }

        //Check if any of the guis are not null.
        //If not then open the first inventory found after refreshing its contents.
        //If no gui exists open the staff menu.

        if (u.staffUser.staffGui != null) {

            u.staffUser.staffGui.refresh();
            u.staffUser.staffGui.open(u);

        } else if (u.staffUser.regionRequests != null) {

            u.staffUser.regionRequests.refresh();
            u.staffUser.regionRequests.open(u);

        } else {

            u.staffUser.staffGui = new StaffGui(u);
            u.staffUser.staffGui.open(u);

        }
    }
}
