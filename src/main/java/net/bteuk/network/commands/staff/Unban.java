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

import static net.bteuk.network.utils.staff.Moderation.isBanned;
import static net.bteuk.network.utils.staff.Moderation.unban;

public class Unban extends AbstractCommand {

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {

        //Check if sender is player, then check permissions
        CommandSender sender = stack.getSender();
        if (sender instanceof Player) {
            if (!hasPermission(sender, "uknet.ban")) {
                return;
            }
        }

        //Check args.
        if (args.length < 1) {
            sender.sendMessage(ChatUtils.error("/unban <player>"));
            return;
        }

        //Check player.
        //If uuid exists for name.
        if (!Network.getInstance().getGlobalSQL().hasRow("SELECT uuid FROM player_data WHERE name='" + args[0] + "';")) {
            sender.sendMessage(Component.text(args[0], NamedTextColor.DARK_RED)
                    .append(ChatUtils.error(" is not a valid player.")));
            return;
        }

        String uuid = Network.getInstance().getGlobalSQL().getString("SELECT uuid FROM player_data WHERE name='" + args[0] + "';");
        String name = Network.getInstance().getGlobalSQL().getString("SELECT name FROM player_data WHERE name='" + args[0] + "';");

        sender.sendMessage(unbanPlayer(name, uuid));
    }

    /**
     * Unban the player and return the feedback so the executor can be notified of success/failure.
     *
     * @param name
     * Name of the banned player.
     * @param uuid
     * Uuid of the banned player.
     * @return
     * The Component to display to the executor.
     */
    public Component unbanPlayer(String name, String uuid) {

        //Check if the player is currently banned.
        if (isBanned(uuid)) {

            //Unban the player.
            unban(uuid);

            //Send feedback.
            return (ChatUtils.success("Unbanned ")
                    .append(Component.text(name, NamedTextColor.DARK_AQUA)));

        } else {
            return (ChatUtils.error(name + " is not currently banned."));
        }
    }
}
