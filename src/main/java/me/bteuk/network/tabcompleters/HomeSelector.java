package me.bteuk.network.tabcompleters;

import me.bteuk.network.Network;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class HomeSelector implements TabCompleter {

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command
            command, @NotNull String label, @NotNull String[] args) {

        List<String> homes = Network.getInstance().getGlobalSQL().getStringList("SELECT name FROM home WHERE uuid='" + ((Player) sender).getUniqueId() + "' AND name IS NOT NULL;");
        List<String> returns = new ArrayList<>();

        if (args.length == 0) {

            return homes;

        } else if (args.length == 1) {

            StringUtil.copyPartialMatches(args[0], homes, returns);
            return returns;

        } else {

            return null;

        }
    }
}
