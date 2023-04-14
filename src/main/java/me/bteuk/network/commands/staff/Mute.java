package me.bteuk.network.commands.staff;

import me.bteuk.network.Network;
import me.bteuk.network.staff.Moderation;
import me.bteuk.network.utils.Time;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Mute implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Check if sender is player, then check permissions
        if (sender instanceof Player p) {
            if (!p.hasPermission("uknet.mute")) {
                p.sendMessage(Utils.error("You do not have permission to use this command."));
                return true;
            }
        }

        //Check args.
        if (args.length < 3) {
            sender.sendMessage(Utils.error("/mute <player> <duration> <reason>"));
            return true;
        }

        //Check player.
        //If uuid exists for name.
        if (!Network.getInstance().globalSQL.hasRow("SELECT uuid FROM player_data WHERE name='" + args[0] + "';")) {
            sender.sendMessage(Component.text(args[0], NamedTextColor.DARK_RED)
                    .append(Utils.error(" is not a valid player.")));
            return true;
        }

        String uuid = Network.getInstance().globalSQL.getString("SELECT uuid FROM player_data WHERE name='" + args[0] + "';");

        //Add random letter at the end of the duration string so it'll always split into 2 parts.
        String sDuration = args[1] + "q";

        //Check for valid duration.
        //xyxmxdxh format
        String[] duration;
        long time = 0;

        //Check years
        duration = sDuration.split("y");

        if (duration.length == 2) {
            try {

                int years = Integer.parseInt(duration[0]);

                //Convert years to milliseconds and add to time.
                //We're assuming a year is 365 days.
                time += years * 365 * 24 * 60 * 60 * 1000L;

            } catch (NumberFormatException e) {
                sender.sendMessage(Utils.error("Duration must be in ymdh format, for example 1y6m, which is 1 year and 6 months or 2d12h is 2 days and 12 hours."));
                return true;
            }
        }

        //Check months
        duration = sDuration.split("m");

        if (duration.length == 2) {
            try {

                int months = Integer.parseInt(duration[0]);

                //Convert months to milliseconds and add to time.
                //We're assuming a month is 30 days.
                time += months * 30 * 24 * 60 * 60 * 1000L;

            } catch (NumberFormatException e) {
                sender.sendMessage(Utils.error("Duration must be in ymdh format, for example 1y6m, which is 1 year and 6 months or 2d12h is 2 days and 12 hours."));
                return true;
            }
        }

        //Check days
        duration = sDuration.split("d");

        if (duration.length == 2) {
            try {

                int days = Integer.parseInt(duration[0]);

                //Convert days to milliseconds and add to time.
                time += days * 24 * 60 * 60 * 1000L;

            } catch (NumberFormatException e) {
                sender.sendMessage(Utils.error("Duration must be in ymdh format, for example 1y6m, which is 1 year and 6 months or 2d12h is 2 days and 12 hours."));
                return true;
            }
        }

        //Check hours
        duration = sDuration.split("h");

        if (duration.length == 2) {
            try {

                int hours = Integer.parseInt(duration[0]);

                //Convert hours to milliseconds and add to time.
                time += hours * 60 * 60 * 1000L;

            } catch (NumberFormatException e) {
                sender.sendMessage(Utils.error("Duration must be in ymdh format, for example 1y6m, which is 1 year and 6 months or 2d12h is 2 days and 12 hours."));
                return true;
            }
        }

        //Get end time of current time plus time.
        long end_time = Time.currentTime() + time;

        //Combine all remaining args to create a reason.
        String sArgs = String.join(" ", args);
        String reason = StringUtils.substring(sArgs, 2);

        Moderation mod = new Moderation();
        mod.mute(uuid, end_time, reason);

        sender.sendMessage(Utils.success("Muted ")
                .append(Component.text(args[0], NamedTextColor.DARK_AQUA))
                .append(Utils.success(" until "))
                .append(Component.text(Time.getDateTime(end_time), NamedTextColor.DARK_AQUA))
                .append(Utils.success(" for reason: "))
                .append(Component.text(reason, NamedTextColor.DARK_AQUA)));

        return false;
    }
}
