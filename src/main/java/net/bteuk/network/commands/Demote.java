package net.bteuk.network.commands;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.bteuk.network.Network;
import net.bteuk.network.lib.utils.ChatUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Command to remove a role from a player.
 */
public class Demote extends PromotionAction {
    private static final Component ERROR = ChatUtils.error("/demote [player] [role]");

    public Demote(Network instance) {
        super(instance, ERROR);
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();
        if (hasPermission(sender, "uknet.staff.demote")) {
            onCommand(sender, args, true);
        }
    }
}