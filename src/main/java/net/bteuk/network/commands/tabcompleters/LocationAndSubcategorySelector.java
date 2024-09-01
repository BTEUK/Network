package net.bteuk.network.commands.tabcompleters;

import net.bteuk.network.Network;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LocationAndSubcategorySelector extends AbstractTabCompleter {

    private final int argIndex;

    /**
     * Contructor
     *
     * @param argIndex the index for which the tab completer should be.
     */
    public LocationAndSubcategorySelector(int argIndex) {
        this.argIndex = argIndex;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Get array of locations.
        ArrayList<String> locations = Network.getInstance().getGlobalSQL().getStringList("SELECT location FROM location_data;");

        // Add subcategories.
        locations.addAll(Network.getInstance().getGlobalSQL().getStringList("SELECT name FROM location_subcategory"));
        return onTabCompleteArg(args, locations, argIndex);
    }
}
