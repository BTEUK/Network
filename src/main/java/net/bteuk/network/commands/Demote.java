package net.bteuk.network.commands;

import net.bteuk.network.Network;
import net.bteuk.network.lib.utils.ChatUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Command to remove a role from a player.
 */
public class Demote extends PromotionAction {
    private static Component ERROR = ChatUtils.error("/demote [player] [role]");

    public Demote(Network instance) {
        super(instance, "demote", ERROR);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        onCommand(sender, args, true);
        return true;
    }
}