package net.bteuk.network.commands;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.bteuk.network.Network;
import net.bteuk.network.lib.utils.ChatUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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

        String uuid = instance.getGlobalSQL().getString("SELECT player_to_id FROM last_messages WHERE player_from_id='" + player.getUniqueId() + "';");
        if (uuid == null) {
            player.sendMessage(ChatUtils.error("You have no last messaged player."));
            return;
        }
        String name = instance.getGlobalSQL().getString("SELECT name FROM player_data WHERE uuid='" + uuid + "';");
        String[] newargs = new String[args.length + 1];
        newargs[0] = name;
        System.arraycopy(args, 0, newargs, 1, args.length);
        msgCommand.execute(stack,newargs);
    }


}
