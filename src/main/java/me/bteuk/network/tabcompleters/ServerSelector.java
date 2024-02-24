package me.bteuk.network.tabcompleters;

import me.bteuk.network.Network;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static me.bteuk.network.utils.Constants.SERVER_NAME;

public class ServerSelector implements TabCompleter {

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Get array of online players, excluding yourself.
        ArrayList<String> servers = Network.getInstance().getGlobalSQL().getStringList("SELECT name FROM server_data WHERE server<>'" + SERVER_NAME + ";");
        ArrayList<String> returns = new ArrayList<>();

        //If args is empty then return full array.
        //If args length is 1 then return any matching names with the existing characters.
        //Else return null, the tp command only has 1 valid arg.
        if (args.length == 0) {

            return servers;

        } else if (args.length == 1) {

            StringUtil.copyPartialMatches(args[0], servers, returns);
            return returns;

        } else {

            return null;

        }
    }
}
