package me.bteuk.network.commands;

import me.bteuk.network.server_conversion.Navigation_database;
import me.bteuk.network.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

public class Database implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Check if sender is console.
        if (!(sender instanceof ConsoleCommandSender console)) {

            sender.sendMessage(Utils.error("This command can only be used through the console."));
            return true;

        }

        //Check args.
        if (args.length < 2) {
            console.sendMessage(Utils.error("/database convert navigation"));
            return true;
        }

        //Check first arg.
        if (args[0].equalsIgnoreCase("convert")) {

            //Check second arg.
            if (args[1].equalsIgnoreCase("navigation")) {

                //Convert navigation. If successfull it will show up in console.
                //If an error occurs then there will be an error log.
                Navigation_database nav = new Navigation_database();
                nav.navigation();

            } else {
                console.sendMessage(Utils.error("/database convert navigation"));
            }
        } else {
            console.sendMessage(Utils.error("/database convert navigation"));
        }

        return true;

    }
}
