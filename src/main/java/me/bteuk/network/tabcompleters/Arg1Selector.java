package me.bteuk.network.tabcompleters;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Will implement a TAB completer on the first given argument.
 */
public class Arg1Selector extends AbstractTabcompleter {

    private final List<String> options;

    /**
     * Contructor
     * @param options the options that should be available in the TAB completion on the first argument
     */
    public Arg1Selector(List<String> options) {
        this.options = options;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        return onTabCompleteArg1(args, options);

    }
}
