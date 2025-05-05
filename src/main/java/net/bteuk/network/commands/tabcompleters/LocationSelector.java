package net.bteuk.network.commands.tabcompleters;

import net.bteuk.network.Network;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class LocationSelector extends AbstractTabCompleter {

    public static List<String> locationSelectorOnArg(String[] args, int argIndexStart) {
        // Get array of locations.
        List<String> locations = Network.getInstance().getGlobalSQL().getStringList("SELECT location FROM " +
                "location_data;");

        // It is possible that the location has multiple words, so the tab completer should work for later words.
        // This is done by returning a list of partial location names including only the part of the location name that is yet to be written.
        int word = args.length - argIndexStart;
        int argIndex = argIndexStart;
        if (word > 1) {
            argIndex = argIndexStart + word - 1;
            locations = locations.stream().map(location -> location.split(" ")).filter(locationArray -> locationArray.length >= word)
                    .map(locationArray -> String.join(" ", Arrays.copyOfRange(locationArray, (word - 1), locationArray.length))).toList();
        }

        return onTabCompleteArg(args, locations, argIndex);
    }

    @Override
    public @NotNull Collection<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return locationSelectorOnArg(args, 0);
    }
}
