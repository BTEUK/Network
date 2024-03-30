package net.bteuk.network.commands;

import net.bteuk.network.Network;
import net.bteuk.network.exceptions.InvalidFormatException;
import net.bteuk.network.commands.tabcompleters.FixedArgSelector;
import net.bteuk.network.utils.Utils;
import net.bteuk.network.utils.enums.TimesOfDay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static java.lang.Math.floorMod;

public class Ptime extends AbstractCommand {

    private static final Component RESET_PLAYER_TIME = Utils.success("Reset player time, using server time.");
    private static final Component SET_PLAYER_TIME = Utils.success("Set player time to ");
    private static final Component INVALID_FORMAT = Utils.error("Invalid time format, try using HH:mm or Minecraft ticks.");

    public Ptime(Network instance) {
        super(instance, "ptime");
        command.setTabCompleter(new FixedArgSelector(Arrays.stream(TimesOfDay.values()).map(t -> t.label).toList(), 0));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        Player p = getPlayer(sender);

        if (p == null) {
            sender.sendMessage(COMMAND_ONLY_BY_PLAYER);
            return true;
        }

        //Permission check.
        if (!p.hasPermission("uknet.ptime")) {
            p.sendMessage(NO_PERMISSION);
            return true;
        }

        //No args implies setting the player time to the default (server time).
        if (args.length == 0) {
            p.resetPlayerTime();
            p.sendMessage(RESET_PLAYER_TIME);
            return true;
        }

        int ticks;

        //Check arguments.
        //There are multiple accepted formats.
        //First check for written times, supported times are "sunrise", "day", "morning", "noon", "afternoon", "sunset", "night", "midnight".
        try {
            if (Arrays.stream(TimesOfDay.values()).map(t -> t.label).anyMatch(t -> args[0].equalsIgnoreCase(t))) {

                ticks = TimesOfDay.valueOf(args[0].toUpperCase()).ticks;

                //Check if the argument contains AM or PM get the number before that and convert it to Minecraft time.
            } else if (args[0].toLowerCase().contains("pm")) {

                String[] firstArg = args[0].toLowerCase().split("pm");

                //Try parsing the first arg as a number.
                int time = Integer.parseInt(firstArg[0]);

                if (time > 0 && time <= 12) {
                    ticks = (12000 + convertHourToTicks(time % 12)) % 24000;
                } else {
                    throw new InvalidFormatException("Invalid time format");
                }
            } else if (args[0].toLowerCase().contains("am")) {

                String[] firstArg = args[0].toLowerCase().split("am");

                //Try parsing the first arg as a number.
                int time = Integer.parseInt(firstArg[0]);

                if (time > 0 && time <= 12) {
                    ticks = convertHourToTicks(time % 12);
                } else {
                    throw new InvalidFormatException("Invalid time format");
                }
                //If the argument contains : use a 24-hour clock and include minutes.
            } else if (args[0].contains(":")) {

                String[] firstArg = args[0].split(":");

                int hours = Integer.parseInt(firstArg[0]);
                int minutes = Integer.parseInt(firstArg[1]);

                if (hours < 0 || hours > 23 || minutes < 0 || minutes > 59) {
                    throw new InvalidFormatException("Invalid time format");
                }

                ticks = convertHourToTicks(hours) + convertMinutesToTicks(minutes);

            } else {
                //If the arguments is between 3-5 numbers then use Minecraft time directly.
                int minecraftTicks = Integer.parseInt(args[0]);

                if (minecraftTicks > 0) {
                    ticks = minecraftTicks % 24000;
                } else {
                    throw new InvalidFormatException("Invalid time format");
                }
            }
        } catch (NumberFormatException | InvalidFormatException ex) {
            p.sendMessage(INVALID_FORMAT);
            return true;
        }

        //Set time and send feedback.
        p.setPlayerTime(ticks, false);
        p.sendMessage(SET_PLAYER_TIME.append(Component.text(args[0] + " (" + ticks + ")", NamedTextColor.DARK_AQUA)));

        return true;
    }

    private int convertHourToTicks(int hour) {
        hour -= 6;
        hour = floorMod(hour, 24);
        return hour * 1000;
    }

    private int convertMinutesToTicks(int minutes) {
        return minutes * 1000 / 60;
    }
}
