package me.bteuk.network.tabcompleters;

import me.bteuk.network.Network;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static me.bteuk.network.utils.Constants.SERVER_NAME;

public class ServerSelector extends AbstractTabCompleter {

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Get array of online players, excluding yourself.
        ArrayList<String> servers = Network.getInstance().getGlobalSQL().getStringList("SELECT name FROM server_data WHERE server<>'" + SERVER_NAME + ";");

        return onTabCompleteArg(args, servers, 0);
    }
}
