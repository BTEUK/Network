package me.bteuk.network.commands;

import me.bteuk.network.Network;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
        StringBuilder message = new StringBuilder();

        if (((page - 1) * 16) >= locations.size()){

            if (locations.size() <= 16) {
                p.sendMessage(Utils.error("There is only &41 &cpage of warps."));
            } else {
                p.sendMessage(Utils.error("There are only &4" + pages + " &cpages of warps."));
            }

            return true;

        }

        //Show this page of warps.
        message.append(Utils.line("Page &7" + page + "&f/&7" + pages + "&f:\n"));

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

                message.append(Component.text(location, NamedTextColor.GRAY));
                break;

            } else {

                message.append(Component.text(location, NamedTextColor.GRAY).append(Utils.line(", ")));

            }
        }

        p.sendMessage(message.toString());

        //If this isn't the first page show command for previous page.
        if (page > 1) {

            p.sendMessage(Utils.line("To view the previous page type: &7/warps " + (page - 1)));

        }

        //If this isn't the last page show command for the next page.
        if ((page * 16) < locations.size()) {

            p.sendMessage(Utils.line("To view the next page type: &7/warps " + (page + 1)));

        }

        return true;
    }
}
