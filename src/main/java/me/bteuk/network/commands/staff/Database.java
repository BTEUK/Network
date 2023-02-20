package me.bteuk.network.commands.staff;

import me.bteuk.network.server_conversion.Navigation_database;
import me.bteuk.network.server_conversion.UKnet_database;
import me.bteuk.network.server_conversion.regions.DatabaseRegions;
import me.bteuk.network.server_conversion.regions.WGRegions;
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
            console.sendMessage(Utils.error("/database convert navigation|playerdata|regions"));
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

            } else if (args[1].equalsIgnoreCase("playerdata")) {

                //Convert playerdata. If successfull it will show up in console.
                //If an error occurs then there will be an error log.
                UKnet_database pd = new UKnet_database();
                pd.player_data();

            } else if (args[2].equalsIgnoreCase("regions")) {

                //Convert regions and region members. If successfull it will show up in console.
                //If an error occurs then there will be an error log.
                DatabaseRegions dr = new DatabaseRegions();
                dr.getRegions();
                dr.convertMembers();
                dr.convertLogs();

                //Convert region to worldguard.
                WGRegions.convertWGRegions();


            } else {
                console.sendMessage(Utils.error("/database convert navigation|playerdata"));
            }



        } else {
            console.sendMessage(Utils.error("/database convert navigation|playerdata"));
        }

        return true;

    }
}