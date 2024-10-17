package net.bteuk.network.commands.tabcompleters;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class MultiArgSelector extends AbstractTabCompleter {

    private final List<AbstractTabCompleter> tabCompleters;

    public MultiArgSelector(List<AbstractTabCompleter> tabCompleters) {
        this.tabCompleters = tabCompleters;
    }

    @Override
    public Collection<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        for (AbstractTabCompleter tabCompleter : tabCompleters) {
            Collection<String> result = tabCompleter.onTabComplete(sender, args);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
}
