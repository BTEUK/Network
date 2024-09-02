package net.bteuk.network.commands;

import net.bteuk.network.Network;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class Me extends AbstractCommand {

    public Me(Network instance) {
        super(instance, "me");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // Send a direct message to the player, if not muted.
        sender.sendMessage(NO_PERMISSION);
        return true;
    }
}
