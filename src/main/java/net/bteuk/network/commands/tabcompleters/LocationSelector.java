package net.bteuk.network.commands.tabcompleters;

import net.bteuk.network.Network;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LocationSelector extends AbstractTabCompleter {

    public static List<String> locationSelectorOnArg(String[] args, int argIndex) {
        // Get array of locations.
        ArrayList<String> locations = Network.getInstance().getGlobalSQL().getStringList("SELECT location FROM " +
                "location_data;");

        return onTabCompleteArg(args, locations, argIndex);
    }

    @Override
    public @NotNull Collection<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return locationSelectorOnArg(args, 0);
    }
}
