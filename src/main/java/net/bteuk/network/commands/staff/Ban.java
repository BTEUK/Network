package net.bteuk.network.commands.staff;

import net.bteuk.network.Network;
import net.bteuk.network.exceptions.DurationFormatException;
import net.bteuk.network.exceptions.NotBannedException;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.utils.Time;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static net.bteuk.network.utils.Constants.LOGGER;
import static net.bteuk.network.utils.staff.Moderation.ban;
import static net.bteuk.network.utils.staff.Moderation.getDuration;

public class Ban implements CommandExecutor {

    //Constructor to enable the command.
    public Ban(Network instance) {

        //Register command.
        PluginCommand command = instance.getCommand("ban");

        if (command == null) {
            LOGGER.warning("Ban command not added to plugin.yml, it will therefore not be enabled.");
            return;
        }

        //Set executor.
        command.setExecutor(this);

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Check if sender is player, then check permissions
        if (sender instanceof Player p) {
            if (!p.hasPermission("uknet.ban")) {
                p.sendMessage(ChatUtils.error("You do not have permission to use this command."));
                return true;
            }
        }

        //Check args.
        if (args.length < 3) {
            sender.sendMessage(ChatUtils.error("/ban <player> <duration> <reason>"));
            return true;
        }

        //Check player.
        //If uuid exists for name.
        if (!Network.getInstance().getGlobalSQL().hasRow("SELECT uuid FROM player_data WHERE name='" + args[0] + "';")) {
            sender.sendMessage(ChatUtils.error("%s is not a valid player."));
            return true;
        }

        String uuid = Network.getInstance().getGlobalSQL().getString("SELECT uuid FROM player_data WHERE name='" + args[0] + "';");
        String name = Network.getInstance().getGlobalSQL().getString("SELECT name FROM player_data WHERE name='" + args[0] + "';");

        //Get the duration of the ban.
        long time;
        try {

            time = getDuration(args[1]);

        } catch (DurationFormatException e) {
            sender.sendMessage(ChatUtils.error("Duration must be in ymdh format, for example 1y6m, which is 1 year and 6 months or 2d12h is 2 days and 12 hours."));
            return true;
        }

        //Get end time of current time plus time.
        long end_time = Time.currentTime() + time;

        //Combine all remaining args to create a reason.
        String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

        sender.sendMessage(banPlayer(name, uuid, end_time, reason));
        return true;

    }

    /**
     * Ban the player and return the feedback so the executor can be notified of success/failure.
     *
     * @param name
     * Name of the player to ban.
     * @param uuid
     * Uuid of the player to ban.
     * @param end_time
     * Time for the ban to end in milliseconds.
     * @param reason
     * Reason for banning the player.
     * @return
     * The Component to display to the executor.
     */
    public Component banPlayer(String name, String uuid, long end_time, String reason) {

        try {
            ban(uuid, end_time, reason);
        } catch (NotBannedException e) {
            e.printStackTrace();
            return ChatUtils.error("An error occurred while banning this player, please contact an admin for support.");
        }

        return ChatUtils.success("Banned ")
                .append(Component.text(name, NamedTextColor.DARK_AQUA))
                .append(ChatUtils.success(" until "))
                .append(Component.text(Time.getDateTime(end_time), NamedTextColor.DARK_AQUA))
                .append(ChatUtils.success(" for reason: "))
                .append(Component.text(reason, NamedTextColor.DARK_AQUA));
    }
}
