package net.bteuk.network.commands;

import net.bteuk.network.Network;
import net.bteuk.network.commands.tabcompleters.PlayerSelector;
import net.bteuk.network.exceptions.NotMutedException;
import net.bteuk.network.lib.dto.DirectMessage;
import net.bteuk.network.lib.utils.ChatUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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
        super(instance, "msg");
        this.instance = instance;
        command.setTabCompleter(new PlayerSelector());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // Send a direct message to the player, if not muted.
        Player p = getPlayer(sender);
        if (p == null) {
            return true;
        }

        // Get the uuid of the player.
        if (args.length < 2) {
            p.sendMessage(ERROR);
            return true;
        }

        // Search for the uuid of the player.
        String uuid = instance.getGlobalSQL().getString("SELECT uuid FROM player_data WHERE name='" + args[0] + "';");
        if (uuid == null) {
            p.sendMessage(ERROR);
            return true;
        }

        if (isMuted(p.getUniqueId().toString())) {
            try {
                // Send message and cancel command.
                p.sendMessage(getMutedComponent(p.getUniqueId().toString()));
                return true;
            } catch (NotMutedException ex) {
                // Ignored
            }
        }

        // Send direct message, the message is created using all other command arguments.
        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        DirectMessage directMessage = getDirectMessage(message, p.getName(), p.getUniqueId().toString(), args[0], uuid);
        // Also send the message to the sender.
        p.sendMessage(directMessage.getComponent());
        instance.getChat().sendSocketMesage(directMessage);
        return true;

    }
}
