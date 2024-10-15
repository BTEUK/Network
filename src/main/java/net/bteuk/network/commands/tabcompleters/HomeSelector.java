package net.bteuk.network.commands.tabcompleters;

import net.bteuk.network.Network;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class HomeSelector extends AbstractTabCompleter {

    @Override
    public Collection<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        List<String> homes = Network.getInstance().getGlobalSQL().getStringList("SELECT name FROM home WHERE uuid='" + ((Player) sender).getUniqueId() + "' AND name IS NOT NULL;");

        return onTabCompleteArg(args, homes, 0);
    }
}
