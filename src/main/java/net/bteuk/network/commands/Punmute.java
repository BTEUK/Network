package net.bteuk.network.commands;

import net.bteuk.network.Network;
import net.bteuk.network.lib.utils.ChatUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Command for personal muting, allows you to mute any player for the current session.
 */
public class Punmute extends PmuteAction {

    private static Component ERROR = ChatUtils.error("/punmute [player]");

    protected Punmute(Network instance)  {
        super(instance, "punmute", ERROR);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        onCommand(sender, args, false);
        return true;
    }
}
