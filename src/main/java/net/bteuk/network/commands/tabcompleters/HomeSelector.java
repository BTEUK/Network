package net.bteuk.network.commands.tabcompleters;

import net.bteuk.network.Network;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HomeSelector extends AbstractTabCompleter {

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command
            command, @NotNull String label, @NotNull String[] args) {

        List<String> homes = Network.getInstance().getGlobalSQL().getStringList("SELECT name FROM home WHERE uuid='" + ((Player) sender).getUniqueId() + "' AND name IS NOT NULL;");

        return onTabCompleteArg(args, homes, 0);
    }
}
