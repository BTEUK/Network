package me.bteuk.network.commands.staff;

import me.bteuk.network.Network;
import me.bteuk.network.staff.StaffGui;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
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

            sender.sendMessage(Utils.error("This command can only be used by a player."));
            return true;

        }

        //Check if user is member of staff.
        if (!(p.hasPermission("uknet.staff"))) {

            p.sendMessage(Utils.error("You do not have permission to use this command."));
            return true;

        }

        //Get user.
        NetworkUser u = Network.getInstance().getUser(p);

        if (u == null) {return true;}

        //If first arg is chat, switch the player to and from staff chat.
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("chat")) {
                if (u.staffChat) {
                    u.player.sendMessage(Utils.success("Disabled staff chat."));
                } else {
                    u.player.sendMessage(Utils.success("Enabled staff chat."));
                }
                //Invert enabled/disabled of staff chat.
                u.staffChat = !u.staffChat;
                Network.getInstance().globalSQL.update("UPDATE player_data SET staff_chat=1-staff_chat WHERE uuid='"+ p.getUniqueId() + "';");
            } else {
                //Send message in staff chat.
                Network.getInstance().chat.broadcastPlayerMessage(p, Component.text(String.join(" ", args)), "uknet:staff");
            }
            return true;
        }

        //If the player has a previous gui, open that.
        openStaffMenu(u);

        return true;
    }

    public static void openStaffMenu(NetworkUser u) {

        //Check if the gui exists.
        //If it does refresh and open it.
        //If no gui exists open the staff menu.

        if (u.staffGui != null) {

            u.staffGui.refresh();
            u.staffGui.open(u);

        } else {

            u.staffGui = new StaffGui(u);
            u.staffGui.open(u);

        }
    }
}
