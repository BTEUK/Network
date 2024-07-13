package net.bteuk.network.commands.tabcompleters;

import net.bteuk.network.Network;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

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
        ArrayList<String> uuids;
        if (sender instanceof Player p) {
            uuids = Network.getInstance().getGlobalSQL().getStringList("SELECT uuid FROM online_users WHERE uuid<>'" + p.getUniqueId() + "';");
        } else {
            uuids = Network.getInstance().getGlobalSQL().getStringList("SELECT uuid FROM online_users;");
        }
        ArrayList<String> names = new ArrayList<>();
        for (String uuid : uuids) {
            names.add(Network.getInstance().getGlobalSQL().getString("SELECT name FROM player_data WHERE uuid='" + uuid + "';"));
        }

        return onTabCompleteArg(args, names, argIndex);
    }
}
