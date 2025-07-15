package net.bteuk.network.commands.staff;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.bteuk.network.Network;
import net.bteuk.network.commands.AbstractCommand;
import net.bteuk.network.lib.utils.ChatUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static net.bteuk.network.utils.staff.Moderation.kick;

public class Kick extends AbstractCommand {

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {

        // Check if sender is player, then check permissions
        CommandSender sender = stack.getSender();
        if (sender instanceof Player) {
            if (!hasPermission(sender, "uknet.kick")) {
                return;
            }
        }

        // Check args.
        if (args.length < 2) {
            sender.sendMessage(ChatUtils.error("/kick <player> <reason>"));
            return;
        }

        // Check player.
        // If uuid exists for name.
        if (!Network.getInstance().getGlobalSQL()
                .hasRow("SELECT uuid FROM player_data WHERE name='" + args[0] + "';")) {
            sender.sendMessage(Component.text(args[0], NamedTextColor.DARK_RED)
                    .append(ChatUtils.error(" is not a valid player.")));
            return;
        }

        String uuid =
                Network.getInstance().getGlobalSQL()
                        .getString("SELECT uuid FROM player_data WHERE name='" + args[0] + "';");
        String name =
                Network.getInstance().getGlobalSQL()
                        .getString("SELECT name FROM player_data WHERE name='" + args[0] + "';");

        // Check if player is online.
        if (!Network.getInstance().isOnlineOnNetwork(uuid)) {
            sender.sendMessage(Component.text(name, NamedTextColor.DARK_RED)
                    .append(ChatUtils.error(" is not online.")));
            return;
        }

        // Combine all remaining args to create a reason.
        String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        sender.sendMessage(kickPlayer(name, uuid, reason));
    }

    public Component kickPlayer(String name, String uuid, String reason) {

        kick(uuid, reason);

        return (ChatUtils.success("Kicked ")
                .append(Component.text(name, NamedTextColor.DARK_AQUA))
                .append(ChatUtils.success(" for reason: "))
                .append(Component.text(reason, NamedTextColor.DARK_AQUA)));
    }
}
