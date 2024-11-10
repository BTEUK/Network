package net.bteuk.network.commands.tabcompleters;

import net.bteuk.network.Network;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

import static net.bteuk.network.utils.Constants.SERVER_NAME;

public class ServerSelector extends AbstractTabCompleter {

    @Override
    public @NotNull Collection<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        //Get array of online players, excluding yourself.
        ArrayList<String> servers = Network.getInstance().getGlobalSQL().getStringList("SELECT name FROM server_data WHERE server<>'" + SERVER_NAME + ";");

        return onTabCompleteArg(args, servers, 0);
    }
}
