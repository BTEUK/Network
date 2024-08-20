package net.bteuk.network.commands.tabcompleters;

import net.bteuk.network.Network;
import net.bteuk.network.lib.dto.OnlineUser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Online player name selector for the 1st command argument.
 */
public class PlayerSelector extends AbstractTabCompleter {

    private final int argIndex;

    public PlayerSelector() {
        this.argIndex = 0;
    }

    public PlayerSelector(int argIndex) {
        this.argIndex = argIndex;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Get array of online players, excluding yourself.
        List<String> names = Network.getInstance().getOnlineUsers().stream().map(OnlineUser::getName).collect(Collectors.toList());
        if (sender instanceof Player p) {
            names.remove(p.getName());
        }
        return onTabCompleteArg(args, names, argIndex);
    }
}
