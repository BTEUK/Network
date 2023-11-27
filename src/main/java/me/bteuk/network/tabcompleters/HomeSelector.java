package me.bteuk.network.tabcompleters;

import me.bteuk.network.Network;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HomeSelector implements TabCompleter {

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command
            command, @NotNull String label, @NotNull String[] args) {

        if (sender instanceof Player p) {
            List<String> homes = Network.getInstance().globalSQL.getStringList("SELECT name FROM home WHERE uuid='" + p.getUniqueId() + "' AND name IS NOT NULL;");
            return TabCompleterForArg.onTabCompleteForArg(args, 0, homes);
        } else {
            return null;
        }
    }
}
