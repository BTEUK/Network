package net.bteuk.network.commands.staff;

import net.bteuk.network.CustomChat;
import net.bteuk.network.Network;
import net.bteuk.network.gui.staff.StaffGui;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.utils.NetworkUser;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.bteuk.network.utils.Constants.LOGGER;
import static net.bteuk.network.utils.Constants.STAFF_CHAT;

public class Staff implements CommandExecutor {

    //Constructor to enable the command.
    public Staff(Network instance) {

        //Register command.
        PluginCommand command = instance.getCommand("staff");

        if (command == null) {
            LOGGER.warning("Home command not added to plugin.yml, it will therefore not be enabled.");
            return;
        }

        //Set executor.
        command.setExecutor(this);

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {

        //Check if the sender is a player.
        if (!(sender instanceof Player p)) {

            sender.sendMessage(ChatUtils.error("This command can only be used by a player."));
            return true;

        }

        //Check if user is member of staff.
        if (!(p.hasPermission("uknet.staff"))) {

            p.sendMessage(ChatUtils.error("You do not have permission to use this command."));
            return true;

        }

        //Get user.
        NetworkUser u = Network.getInstance().getUser(p);

        if (u == null) {return true;}

        //If first arg is chat, switch the player to and from staff chat if enabled.
        if (args.length > 0 && STAFF_CHAT) {
            if (args[0].equalsIgnoreCase("chat")) {
                String channel = "global";
                if (u.getChatChannel().equals("staff")) {
                    u.player.sendMessage(ChatUtils.success("Disabled staff chat."));
                } else {
                    // Set the chat channel to staff.
                    channel = "staff";
                    u.player.sendMessage(ChatUtils.success("Enabled staff chat."));
                }
                // Set channel.
                u.setChatChannel(channel);
                Network.getInstance().getGlobalSQL().update("UPDATE player_data SET chat_channel='" + channel + "' WHERE uuid='"+ p.getUniqueId() + "';");
            } else {
                // Send message in staff chat.
                Network.getInstance().chat.sendSocketMesage(CustomChat.getChatMessage(Component.text(String.join(" ", args)), u));
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
