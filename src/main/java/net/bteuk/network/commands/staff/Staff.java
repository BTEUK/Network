package net.bteuk.network.commands.staff;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.bteuk.network.CustomChat;
import net.bteuk.network.Network;
import net.bteuk.network.commands.AbstractCommand;
import net.bteuk.network.gui.staff.StaffGui;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.utils.NetworkUser;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.bteuk.network.lib.enums.ChatChannels.GLOBAL;
import static net.bteuk.network.lib.enums.ChatChannels.STAFF;
import static net.bteuk.network.utils.Constants.LOGGER;
import static net.bteuk.network.utils.Constants.STAFF_CHAT;

public class Staff extends AbstractCommand {

    public static void openStaffMenu(NetworkUser u) {

        // Check if the gui exists.
        // If it does refresh and open it.
        // If no gui exists open the staff menu.

        if (u.staffGui != null) {

            u.staffGui.refresh();
            u.staffGui.open(u);
        } else {

            u.staffGui = new StaffGui(u);
            u.staffGui.open(u);
        }
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {

        // Check if the sender is a player.
        Player p = getPlayer(stack);
        if (p == null) {
            return;
        }

        NetworkUser u = Network.getInstance().getUser(p);

        // Check if user is member of staff.
        // Architects can open the menu, but not use the staff chat.
        if (!(hasPermission(p, "uknet.staff"))) {
            if (hasPermission(p, "uknet.staff.menu")) {
                openStaffMenu(u);
            }
            return;
        }

        // If u is null, cancel.
        if (u == null) {
            LOGGER.severe("User " + p.getName() + " can not be found!");
            p.sendMessage(ChatUtils.error("User can not be found, please relog!"));
            return;
        }

        // If first arg is chat, switch the player to and from staff chat if enabled.
        if (args.length > 0 && STAFF_CHAT) {
            if (args[0].equalsIgnoreCase("chat")) {
                String channel = GLOBAL.getChannelName();
                if (u.getChatChannel().equals(STAFF.getChannelName())) {
                    u.player.sendMessage(ChatUtils.success("Disabled staff chat."));
                } else {
                    // Set the chat channel to staff.
                    channel = STAFF.getChannelName();
                    u.player.sendMessage(ChatUtils.success("Enabled staff chat."));
                }
                // Set channel.
                u.setChatChannel(channel);
                Network.getInstance().getGlobalSQL().update("UPDATE player_data SET chat_channel='" + channel + "' " +
                        "WHERE uuid='" + p.getUniqueId() + "';");
            } else {
                // Send message in staff chat, by temporarily setting the players channel to staff.
                u.setChatChannel(STAFF.getChannelName());
                Network.getInstance().getChat()
                        .sendSocketMesage(CustomChat.getChatMessage(Component.text(String.join(" ", args)), u));
                u.setChatChannel(GLOBAL.getChannelName());
            }
            return;
        }

        // If the player has a previous gui, open that.
        openStaffMenu(u);
    }
}
