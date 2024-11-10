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

import static net.bteuk.network.utils.staff.Moderation.isMuted;
import static net.bteuk.network.utils.staff.Moderation.unmute;

public class Unmute extends AbstractCommand {

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {

        //Check if sender is player, then check permissions
        CommandSender sender = stack.getSender();
        if (sender instanceof Player) {
            if (!hasPermission(sender, "uknet.mute")) {
                return;
            }
        }

        //Check args.
        if (args.length < 1) {
            sender.sendMessage(ChatUtils.error("/unmute <player>"));
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

        sender.sendMessage(unmutePlayer(name, uuid));
    }

    /**
     * Unmute the player and return the feedback so the executor can be notified of success/failure.
     *
     * @param name
     * Name of the muted player.
     * @param uuid
     * Uuid of the muted player.
     * @return
     * The Component to display to the executor.
     */
    public Component unmutePlayer(String name, String uuid) {

        //Check if the player is currently muted.
        if (isMuted(uuid)) {

            //Unban the player.
            unmute(uuid);

            //Send feedback.
            return (ChatUtils.success("Unmuted ")
                    .append(Component.text(name, NamedTextColor.DARK_AQUA)));

        } else {
            return (ChatUtils.error(name + " is not currently muted."));

        }
    }
}
