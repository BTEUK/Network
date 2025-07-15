package net.bteuk.network.commands;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.bteuk.network.Network;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Rules extends AbstractCommand {

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {

        // Check if the sender is a player.
        Player player = getPlayer(stack);
        if (player == null) {
            return;
        }

        // Open rules book.
        player.openBook(Network.getInstance().getLobby().getRules());
    }
}
