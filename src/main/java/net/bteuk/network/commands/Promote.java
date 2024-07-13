package net.bteuk.network.commands;

import net.bteuk.network.Network;
import net.bteuk.network.lib.utils.ChatUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Command to add a role to a player.
 */
public class Promote extends PromotionAction {
    private static Component ERROR = ChatUtils.error("/promote [player] [role]");

    public Promote(Network instance) {
        super(instance, "promote", ERROR);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        onCommand(sender, args, false);
        return true;
    }
}