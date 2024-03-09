package me.bteuk.network.tabcompleters;

import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract tab completer, provides the logic for getting the correct arguments based on a list of options and argument index using the provided input.
 */
public abstract class AbstractTabCompleter implements TabCompleter {

    public static List<String> onTabCompleteArg(String[] inputArgs, List<String> options, int argIndex) {

        //Return list.
        List<String> returns = new ArrayList<>();

        //If args is empty then return full array.
        //If args length is 1 then return any matching names with the existing characters.
        //Else return null, the tp command only has 1 valid arg.
        if (inputArgs.length == argIndex) {

            return options;

        } else if (inputArgs.length == (argIndex + 1)) {

            StringUtil.copyPartialMatches(inputArgs[argIndex], options, returns);
            return returns;

        } else {

            return null;

        }
    }
}