package net.bteuk.network.commands;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

public class Me extends AbstractCommand {

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        stack.getSender().sendMessage(NO_PERMISSION);
    }
}
