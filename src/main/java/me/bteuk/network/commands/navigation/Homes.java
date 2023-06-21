package me.bteuk.network.commands.navigation;

import me.bteuk.network.Network;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static me.bteuk.network.utils.Constants.LOGGER;

public class Homes implements CommandExecutor, TabCompleter {

    //Constructor to enable the command.
    public Homes(Network instance) {

        //Register command.
        PluginCommand command = instance.getCommand("homes");

        if (command == null) {
            LOGGER.warning("Homes command not added to plugin.yml, it will therefore not be enabled.");
            return;
        }

        //Set executor.
        command.setExecutor(this);

        //Set tab completer.
        command.setTabCompleter(this);

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Check if sender is a player and that they have permission.
        if (!(sender instanceof Player p)) {

            sender.sendMessage(Utils.error("This command can only be used by a player."));
            return true;

        }

        //Check if the player has permission.
        if (!p.hasPermission("uknet.navigation.homes")) {

            p.sendMessage(Utils.error("You do not have permission to use this command."));
            return true;

        }

        //Set default page to 1.
        int page = 1;

        //Check if a page is specified.
        if (args.length > 0) {
            try {

                page = Integer.parseInt(args[0]);

            } catch (NumberFormatException e) {
                //Ignore the arguments altogether.
            }
        }

        //Get all homes in alphabetical order.
        ArrayList<String> homes = Network.getInstance().globalSQL.getStringList("SELECT name FROM home WHERE uuid='" + p.getUniqueId() + "' ORDER BY name ASC;");

        //If there are no locations notify the user.
        if (homes.isEmpty()) {
            p.sendMessage(Utils.error("You don't have any homes set."));
            return true;
        }

        int pages = (((homes.size() - 1) / 16) + 1);

        //Get the first 16 homes and add them to a string.
        //If the page is greater than 1 and there are enough homes to support that, then post that page.

        if (((page - 1) * 16) >= homes.size()){

            if (homes.size() <= 16) {
                p.sendMessage(Utils.error("There is only ")
                        .append(Component.text("1", NamedTextColor.DARK_RED))
                        .append(Utils.error(" page of homes.")));
            } else {
                p.sendMessage(Utils.error("There are only ")
                        .append(Component.text(pages, NamedTextColor.DARK_RED))
                        .append(Utils.error(" pages of homes.")));
            }

            return true;

        }

        //Show this page of homes.
        Component message = Component.text("");

        //If this isn't the first page show command for previous page.
        if (page > 1) {

            //Create previousPage button with hover and click event.
            Component previousPage = Component.text("⏪⏪⏪", TextColor.color(212, 113, 15));
            previousPage = previousPage.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Utils.line("Click to view the previous page of homes.")));
            previousPage = previousPage.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/homes " + (page - 1 )));

            //Add previousPage button at the start of the first line.
            message = message.append(previousPage);
            message = message.append(Component.text(" "));

        }

        message = message.append(Component.text("Page ", NamedTextColor.GREEN)
                .append(Component.text(page, TextColor.color(245, 221, 100)))
                .append(Component.text("/", NamedTextColor.GREEN))
                .append(Component.text(pages, TextColor.color(245, 221, 100))));

        //If this isn't the last page show command for the next page.
        if ((page * 16) < homes.size()) {

            //Create previousPage button with hover and click event.
            Component nextPage = Component.text("⏩⏩⏩\n", TextColor.color(212, 113, 15));
            nextPage = nextPage.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Utils.line("Click to view the next page of homes.")));
            nextPage = nextPage.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/homes " + (page + 1 )));

            //Add previousPage button at the start of the first line.
            message = message.append(Component.text(" "));
            message = message.append(nextPage);

        } else {

            message = message.append(Component.text("\n"));

        }

        //Number of entries to skip.
        int skip = (page - 1) * 16;

        for (String home : homes) {
            if (skip > 0) {
                skip--;
                continue;
            }

            //If it isn't the last entry, as a comma at the end.
            //This is calculated by it either being the 16th entry, or the last in the list.
            if (((homes.indexOf(home) + 1) % 16) == 0 || (homes.indexOf(home) + 1 == homes.size())) {

                message = message.append(createHome(home));
                break;

            } else {

                message = message.append(createHome(home)).append(Component.text(", ", NamedTextColor.WHITE));

            }
        }

        p.sendMessage(message);

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command
            command, @NotNull String label, @NotNull String[] args) {

        List<String> homes = Network.getInstance().globalSQL.getStringList("SELECT name FROM home WHERE uuid='" + ((Player) sender).getUniqueId() + " AND name IS NOT NULL;");
        List<String> returns = new ArrayList<>();

        if (args.length == 0) {

            return homes;

        } else if (args.length == 1) {

            StringUtil.copyPartialMatches(args[0], homes, returns);
            return returns;

        } else {

            return null;

        }
    }

    private Component createHome(String name) {

        Component home;
        if (name == null) {
            home = Component.text("<default>", TextColor.color(245, 221, 100));
            home = home.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("Click to teleport to your default home.")));
            home = home.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/home"));
        } else {
            home = Component.text(name, TextColor.color(245, 221, 100));
            home = home.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("Click to teleport to " + name)));
            home = home.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/home " + name));
        }

        return home;

    }
}
