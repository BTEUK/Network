package me.bteuk.network.commands;

import me.bteuk.network.Network;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class Warps implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Check if sender is player.
        if (!(sender instanceof Player p)) {
            sender.sendMessage(Utils.error("You must be a player to use this command."));
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

        //Get all locations in alphabetical order.
        ArrayList<String> locations = Network.getInstance().globalSQL.getStringList("SELECT location FROM location_data ORDER BY location ASC;");

        //If there are no locations notify the user.
        if (locations.isEmpty()) {
            p.sendMessage(Utils.error("There are currently no warps available."));
            return true;
        }

        int pages = (((locations.size() - 1) / 16) + 1);

        //Get the first 16 locations and add them to a string.
        //If the page is greater than 1 and there are enough locations to support that, then post that page.

        if (((page - 1) * 16) >= locations.size()){

            if (locations.size() <= 16) {
                p.sendMessage(Utils.error("There is only ")
                        .append(Component.text("1", NamedTextColor.DARK_RED))
                        .append(Utils.error(" page of warps.")));
            } else {
                p.sendMessage(Utils.error("There are only ")
                        .append(Component.text(pages, NamedTextColor.DARK_RED))
                        .append(Utils.error(" pages of warps.")));
            }

            return true;

        }

        //Show this page of warps.
        Component message = Component.text("Page ", NamedTextColor.GREEN)
                .append(Component.text(page, TextColor.color(245, 221, 100)))
                .append(Component.text("/", NamedTextColor.GREEN))
                .append(Component.text(pages, TextColor.color(245, 221, 100)));

        //If this isn't the first page show command for previous page.
        if (page > 1) {

            //Create previousPage button with hover and click event.
            Component previousPage = Component.text("⏪⏪⏪", TextColor.color(212, 113, 15));
            previousPage = previousPage.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Utils.line("Click to view the previous page of warps.")));
            previousPage = previousPage.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/warps " + (page - 1 )));
            previousPage = previousPage.append(Component.text(" "));

            //Add previousPage button at the start of the first line.
            message = previousPage.append(message);

        }

        //If this isn't the last page show command for the next page.
        if ((page * 16) < locations.size()) {

            //Create previousPage button with hover and click event.
            Component nextPage = Component.text("⏩⏩⏩\n", TextColor.color(212, 113, 15));
            nextPage = nextPage.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Utils.line("Click to view the next page of warps.")));
            nextPage = nextPage.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/warps " + (page + 1 )));

            //Add previousPage button at the start of the first line.
            message = message.append(Component.text(" "));
            message = message.append(nextPage);

        } else {

            message = message.append(Component.text("\n"));

        }

        //Number of entries to skip.
        int skip = (page - 1) * 16;

        for (String location : locations) {
            if (skip > 0) {
                skip--;
                continue;
            }

            //If it isn't the last entry, as a comma at the end.
            //This is calculated by it either being the 16th entry, or the last in the list.
            if (((locations.indexOf(location) + 1) % 16) == 0 || (locations.indexOf(location) + 1 == locations.size())) {

                message = message.append(createWarp(location));
                break;

            } else {

                message = message.append(createWarp(location)).append(Component.text(", ", NamedTextColor.WHITE));

            }
        }

        p.sendMessage(message);

        return true;
    }

    private Component createWarp(String name) {

        Component warp = Component.text(name, TextColor.color(245, 221, 100));
        warp = warp.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("Click to teleport to " + name)));
        warp = warp.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/warp " + name));

        return warp;

    }
}
