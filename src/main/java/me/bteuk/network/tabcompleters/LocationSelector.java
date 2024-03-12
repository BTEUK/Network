package me.bteuk.network.tabcompleters;

import me.bteuk.network.Network;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LocationSelector extends AbstractTabCompleter {

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        return locationSelectorOnArg(args, 0);
    }

    public static List<String> locationSelectorOnArg(String[] args, int argIndex) {
        //Get array of locations.
        ArrayList<String> locations = Network.getInstance().getGlobalSQL().getStringList("SELECT location FROM location_data;");

        return onTabCompleteArg(args, locations, argIndex);
    }
}
