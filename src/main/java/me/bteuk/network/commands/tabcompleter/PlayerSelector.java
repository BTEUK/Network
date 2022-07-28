package me.bteuk.network.commands.tabcompleter;

import me.bteuk.network.Network;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PlayerSelector implements TabCompleter {

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Get array of online players, excluding yourself.
        ArrayList<String> uuids = Network.getInstance().globalSQL.getStringList("SELECT uuid FROM online_users;");
        ArrayList<String> names = new ArrayList<>();
        ArrayList<String> returns = new ArrayList<>();
        for (String uuid : uuids) {
            names.add(Network.getInstance().globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + uuid + "';"));
        }

        //If args is empty then return full array.
        //If args length is 1 then return any matching names with the existing characters.
        //Else return null, the tp command only has 1 valid arg.
        if (args.length == 0) {

            return names;

        } else if (args.length == 1) {

            StringUtil.copyPartialMatches(args[0], names, returns);
            return returns;

        } else {

            return null;

        }
    }
}
