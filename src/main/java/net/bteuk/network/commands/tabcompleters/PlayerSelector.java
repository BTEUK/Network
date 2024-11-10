package net.bteuk.network.commands.tabcompleters;

import net.bteuk.network.Network;
import net.bteuk.network.lib.dto.OnlineUser;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Online player name selector for the 1st command argument.
 */
public class PlayerSelector extends AbstractTabCompleter {

    private final int argIndex;

    private final boolean excludeSelf;

    public PlayerSelector() {
        this(0, true);
    }

    public PlayerSelector(boolean excludeSelf) {
        this(0, excludeSelf);
    }

    public PlayerSelector(int argIndex, boolean excludeSelf) {
        this.argIndex = argIndex;
        this.excludeSelf = excludeSelf;
    }

    @Override
    public @NotNull Collection<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        //Get array of online players, excluding yourself.
        List<String> names = Network.getInstance().getOnlineUsers().stream().map(OnlineUser::getName).collect(Collectors.toList());
        if (excludeSelf && (sender instanceof Player p)) {
            names.remove(p.getName());
        }
        return onTabCompleteArg(args, names, argIndex);
    }
}
