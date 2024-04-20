package net.bteuk.network.commands.tabcompleters;

import net.bteuk.network.Network;
import net.bteuk.network.utils.enums.Category;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NavigationTabCompleter extends AbstractTabCompleter {

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (args.length > 0) {
            switch(args[0].toUpperCase()) {

                // If arg[0] is remove, update or suggested, then use locations as arg[1] selector.
                case "UPDATE", "REMOVE", "SUGGESTED" -> {
                    return LocationSelector.locationSelectorOnArg(args, 1);
                }

                // If arg[0] is subcategory then the selector depends on the next argument.
                case "SUBCATEGORY" -> {
                    // If arg[1] is add then list all categories for arg[2].
                    // If arg[1] is remove then give all subcategories for arg[2].
                    if (args.length > 1 && args[1].equalsIgnoreCase("add")) {
                        return onTabCompleteArg(args, Arrays.stream(Category.values()).filter(Category::isSelectable).map(Category::toString).collect(Collectors.toList()), 2);
                    } else if (args.length > 1 && args[1].equalsIgnoreCase("remove")) {
                        return onTabCompleteArg(args, Network.getInstance().getGlobalSQL().getStringList("SELECT name FROM location_subcategory"), 2);
                    }
                }
            }
        }
        return null;
    }
}
