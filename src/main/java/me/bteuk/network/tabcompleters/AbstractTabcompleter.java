package me.bteuk.network.tabcompleters;

import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractTabcompleter implements TabCompleter {

    public List<String> onTabCompleteArg1(String[] inputArgs, List<String> options) {

        //Return list.
        List<String> returns = new ArrayList<>();

        //If args is empty then return full array.
        //If args length is 1 then return any matching names with the existing characters.
        //Else return null, the tp command only has 1 valid arg.
        if (inputArgs.length == 0) {

            return options;

        } else if (inputArgs.length == 1) {

            StringUtil.copyPartialMatches(inputArgs[0], options, returns);
            return returns;

        } else {

            return null;

        }
    }
}
