package me.bteuk.network.tabcompleters;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TabCompleterForArg implements TabCompleter {

    private final int index;
    private final List<String> options;

    /**
     * Contructor
     *
     * @param index the index for which the tab completer should work, the first index is 0 which is the first argument of the command.
     * @param options the options that should be available in the TAB completion on the first argument
     */
    public TabCompleterForArg(int index, List<String> options) {
        this.index = index;
        this.options = options;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return onTabCompleteForArg(args, index, options);
    }

    public static List<String> onTabCompleteForArg(String[] inputArgs, int index, List<String> options) {

        //Return list.
        List<String> returns = new ArrayList<>();

        //If args is the index then return full array.
        //If args length is 1 greater than the index then return any matching names with the existing characters.
        //Else return null, the tp command only has 1 valid arg.
        if (inputArgs.length == index) {
            return options;
        } else if (inputArgs.length == (index + 1)) {
            StringUtil.copyPartialMatches(inputArgs[index], options, returns);
            return returns;
        } else {
            return null;
        }
    }
}
