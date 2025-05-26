package net.bteuk.network.commands;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.bteuk.network.Network;
import net.bteuk.network.commands.tabcompleters.PlayerSelector;
import net.bteuk.network.exceptions.NotMutedException;
import net.bteuk.network.lib.dto.DirectMessage;
import net.bteuk.network.lib.dto.PrivateMessage;
import net.bteuk.network.lib.enums.ChatChannels;
import net.bteuk.network.lib.utils.ChatUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static net.bteuk.network.CustomChat.getDirectMessage;
import static net.bteuk.network.utils.staff.Moderation.getMutedComponent;
import static net.bteuk.network.utils.staff.Moderation.isMuted;

/**
 * General message command, includes /tell, /w and /msg
 */
public class Msg extends AbstractCommand {

    private static final Component ERROR = ChatUtils.error("/msg [player] <message>");

    private final Network instance;

    public Msg(Network instance) {
        this.instance = instance;
        setTabCompleter(new PlayerSelector());
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {

        // Check if the sender is a player.
        Player player = getPlayer(stack);
        if (player == null) {
            return;
        }

        // Get the uuid of the player.
        if (args.length < 2) {
            player.sendMessage(ERROR);
            return;
        }

        /*
        // Search for the uuid of the player.
        // Also retrieve the name, as it is possible the cases aren't correct.
        String uuid = instance.getGlobalSQL().getString("SELECT uuid FROM player_data WHERE name='" + args[0] + "';");
        String name = instance.getGlobalSQL().getString("SELECT name FROM player_data WHERE name='" + args[0] + "';");
        if (uuid == null) {
            player.sendMessage(ERROR);
            return;
        }

        if (isMuted(player.getUniqueId().toString())) {
            try {
                // Send message and cancel command.
                player.sendMessage(getMutedComponent(player.getUniqueId().toString()));
                return;
            } catch (NotMutedException ex) {
                // Ignored
            }
        }

         */
        // Send direct message, the message is created using all other command arguments.
        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        //DirectMessage directMessage = getDirectMessage(message, player.getName(), player.getUniqueId().toString(),name, uuid, ChatChannels.GLOBAL);
        PrivateMessage privateMessage = new PrivateMessage(ChatChannels.GLOBAL.getChannelName(), player.getName(), args[0],message, false);
        instance.getChat().sendSocketMesage(privateMessage);
    }
}
