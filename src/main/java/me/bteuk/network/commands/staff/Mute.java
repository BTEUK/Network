package me.bteuk.network.commands.staff;

import me.bteuk.network.Network;
import me.bteuk.network.exceptions.DurationFormatException;
import me.bteuk.network.exceptions.NotMutedException;
import me.bteuk.network.utils.staff.Moderation;
import me.bteuk.network.utils.Time;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static me.bteuk.network.utils.Constants.LOGGER;

public class Mute extends Moderation implements CommandExecutor {

    //Constructor to enable the command.
    public Mute(Network instance) {

        //Register command.
        PluginCommand command = instance.getCommand("mute");

        if (command == null) {
            LOGGER.warning("Mute command not added to plugin.yml, it will therefore not be enabled.");
            return;
        }

        //Set executor.
        command.setExecutor(this);

    }

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
        String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

        sender.sendMessage(mutePlayer(args[0], uuid, end_time, reason));
        return true;

    }

    /**
     * Mute the player and return the feedback so the executor can be notified of success/failure.
     *
     * @param name
     * Name of the player to mute.
     * @param uuid
     * Uuid of the player to mute.
     * @param end_time
     * Time for the mute to end in milliseconds.
     * @param reason
     * Reason for muting the player.
     * @return
     * The Component to display to the executor.
     */
    public Component mutePlayer(String name, String uuid, long end_time, String reason) {

        try {
            mute(uuid, end_time, reason);
        } catch (NotMutedException e) {
            e.printStackTrace();
            return Utils.error("An error occurred while muting this player, please contact an admin for support.");
        }

        return Utils.success("Muted ")
                .append(Component.text(name, NamedTextColor.DARK_AQUA))
                .append(Utils.success(" until "))
                .append(Component.text(Time.getDateTime(end_time), NamedTextColor.DARK_AQUA))
                .append(Utils.success(" for reason: "))
                .append(Component.text(reason, NamedTextColor.DARK_AQUA));
    }
}
