package net.bteuk.network.commands;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.bteuk.network.Network;
import net.bteuk.network.lib.dto.ReplyMessage;
import net.bteuk.network.lib.enums.ChatChannels;
import net.bteuk.network.lib.utils.ChatUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class Reply  extends AbstractCommand {

    private final Msg msgCommand;
    private final String ERROR = "Usage: /r [message]";
    private final Network instance = Network.getInstance();
    public Reply(Msg msgCommand) {
        this.msgCommand = msgCommand;
    }
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {

        // Check if the sender is a player.
        Player player = getPlayer(stack);
        if (player == null) {
            return;
        }

        // Get the uuid of the player.
        if (args.length < 1) {
            player.sendMessage(ChatUtils.error(ERROR));
            return;
        }
        String message = String.join(" ", Arrays.copyOfRange(args, 0, args.length));
        ReplyMessage replymessage = new ReplyMessage(ChatChannels.GLOBAL.getChannelName(),player.getName(),message,false);
        instance.getChat().sendSocketMessage(replymessage);
    }


}
