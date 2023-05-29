package me.bteuk.network.commands.staff;

import me.bteuk.network.Network;
import me.bteuk.network.exceptions.DurationFormatException;
import me.bteuk.network.exceptions.NotBannedException;
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

import java.util.Arrays;

public class Ban extends Moderation implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Check if sender is player, then check permissions
        if (sender instanceof Player p) {
            if (!p.hasPermission("uknet.ban")) {
                p.sendMessage(Utils.error("You do not have permission to use this command."));
                return true;
            }
        }

        //Check args.
        if (args.length < 3) {
            sender.sendMessage(Utils.error("/ban <player> <duration> <reason>"));
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

        //Get the duration of the ban.
        long time;
        try {

            time = getDuration(args[1]);

        } catch (DurationFormatException e) {
            sender.sendMessage(Utils.error("Duration must be in ymdh format, for example 1y6m, which is 1 year and 6 months or 2d12h is 2 days and 12 hours."));
            return true;
        }

        //Get end time of current time plus time.
        long end_time = Time.currentTime() + time;

        //Combine all remaining args to create a reason.
        String sArgs = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        String reason = StringUtils.substring(sArgs, 2);

        try {
            ban(uuid, end_time, reason);
        } catch (NotBannedException e) {
            sender.sendMessage(Utils.error("An error occurred while banned this player, please contact an admin for support."));
            e.printStackTrace();
            return true;
        }

        sender.sendMessage(Utils.success("Banned ")
                .append(Component.text(args[0], NamedTextColor.DARK_AQUA))
                .append(Utils.success(" until "))
                .append(Component.text(Time.getDateTime(end_time), NamedTextColor.DARK_AQUA))
                .append(Utils.success(" for reason: "))
                .append(Component.text(reason, NamedTextColor.DARK_AQUA)));

        return false;
    }
}
