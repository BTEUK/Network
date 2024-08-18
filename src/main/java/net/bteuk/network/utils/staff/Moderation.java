package net.bteuk.network.utils.staff;

import net.bteuk.network.Network;
import net.bteuk.network.eventing.events.EventManager;
import net.bteuk.network.exceptions.DurationFormatException;
import net.bteuk.network.exceptions.NotBannedException;
import net.bteuk.network.exceptions.NotMutedException;
import net.bteuk.network.lib.dto.DirectMessage;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.utils.Time;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * Utility class for moderation
 */
public class Moderation {

    //Ban the player.
    public static void ban(String uuid, long end_time, String reason) throws NotBannedException {

        //Get time.
        long time = Time.currentTime();

        //If the player is already banned, end the old ban.
        if (isBanned(uuid)) {
            Network.getInstance().getGlobalSQL().update("UPDATE moderation SET end_time=" + time + " WHERE uuid='" + uuid + "' AND end_time>" + time + " AND type='ban';");
        }
        Network.getInstance().getGlobalSQL().update("INSERT INTO moderation(uuid,start_time,end_time,reason,type) VALUES('" + uuid + "'," + time + "," + end_time + ",'" + reason + "','ban');");

        //If the player is currently online, ban them.
        kick(uuid, LegacyComponentSerializer.legacyAmpersand().serialize(getBannedComponent(uuid)));

    }

    //Mute the player.
    public static void mute(String uuid, long end_time, String reason) throws NotMutedException {

        //Get time.
        long time = Time.currentTime();

        //If the player is already muted, end the old mute.
        if (isMuted(uuid)) {
            Network.getInstance().getGlobalSQL().update("UPDATE moderation SET end_time=" + time + " WHERE uuid='" + uuid + "' AND end_time>" + time + " AND type='mute';");
        }
        Network.getInstance().getGlobalSQL().update("INSERT INTO moderation(uuid,start_time,end_time,reason,type) VALUES('" + uuid + "'," + time + "," + end_time + ",'" + reason + "','mute');");

        //Notify the user.
        DirectMessage directMessage = new DirectMessage(uuid, "server", getMutedComponent(uuid), true);
        Network.getInstance().getChat().sendSocketMesage(directMessage);
    }

    //Unban the player.
    public static void unban(String uuid) {
        //Get time.
        long time = Time.currentTime();
        Network.getInstance().getGlobalSQL().update("UPDATE moderation SET end_time=" + time + " WHERE uuid='" + uuid + "' AND end_time>" + time + " AND type='ban';");
    }

    //Unmute the player.
    public static void unmute(String uuid) {
        //Get time.
        long time = Time.currentTime();
        Network.getInstance().getGlobalSQL().update("UPDATE moderation SET end_time=" + time + " WHERE uuid='" + uuid + "' AND end_time>" + time + " AND type='mute';");
    }

    //Kick the player.
    public static void kick(String uuid, String reason) {

        //Kick them with the reason, if online.
        Network.getInstance().getOnlineUserByUuid(uuid).ifPresent(onlineUser -> EventManager.createEvent(uuid, "network",
                onlineUser.getServer(),
                "kick", reason));
    }

    /**
     * Check whether a player is banned.
     *
     * @param uuid the uuid of the player
     * @return true if the player is currently banned, false if not
     */
    public static boolean isBanned(String uuid) {
        return (Network.getInstance().getGlobalSQL().hasRow("SELECT uuid FROM moderation WHERE uuid='" + uuid + "' AND end_time>" + Time.currentTime() + " AND type='ban';"));
    }

    //If the player is currently muted, return true.
    public static boolean isMuted(String uuid) {
        return (Network.getInstance().getGlobalSQL().hasRow("SELECT uuid FROM moderation WHERE uuid='" + uuid + "' AND end_time>" + Time.currentTime() + " AND type='mute';"));
    }

    //Get reason why player is banned.
    public static String getBannedReason(String uuid) {
        return (Network.getInstance().getGlobalSQL().getString("SELECT reason FROM moderation WHERE uuid='" + uuid + "' AND end_time>" + Time.currentTime() + " AND type='ban';"));
    }

    //Get reason why player is muted.
    public static String getMutedReason(String uuid) {
        return (Network.getInstance().getGlobalSQL().getString("SELECT reason FROM moderation WHERE uuid='" + uuid + "' AND end_time>" + Time.currentTime() + " AND type='mute';"));
    }

    //Get duration of ban.
    public static String getBanDuration(String uuid) {
        long time = Network.getInstance().getGlobalSQL().getLong("SELECT end_time FROM moderation WHERE uuid='" + uuid + "' AND end_time>" + Time.currentTime() + " AND type='ban';");
        return Time.getDateTime(time);
    }

    //Get duration of mute.
    public static String getMuteDuration(String uuid) {
        long time = Network.getInstance().getGlobalSQL().getLong("SELECT end_time FROM moderation WHERE uuid='" + uuid + "' AND end_time>" + Time.currentTime() + " AND type='mute';");
        return Time.getDateTime(time);
    }

    /**
     * Get Component for banned player to display.
     * This assumes that the player is banned, else this will throw an exception.
     *
     * @param uuid the uuid of the banned player
     * @return the component of the banned message with reason and duration
     * @throws NotBannedException if the player is not banned
     */
    public static Component getBannedComponent(String uuid) throws NotBannedException {
        if (isBanned(uuid)) {
            return Component.text("You have been banned for ", NamedTextColor.RED)
                    .append(Component.text(getBannedReason(uuid), NamedTextColor.DARK_RED))
                    .append(Component.text(" until ", NamedTextColor.RED))
                    .append(Component.text(getBanDuration(uuid), NamedTextColor.DARK_RED));
        } else {
            throw new NotBannedException("The user with uuid " + uuid + " is not banned.");
        }
    }

    /**
     * Get Component for muted player to display.
     * This assumes that the player is muted, else this will throw an exception.
     *
     * @param uuid the uuid of the muted player
     * @return the component of the muted message with reason and duration
     * @throws NotMutedException if the player is not muted
     */
    public static Component getMutedComponent(String uuid) throws NotMutedException {
        if (isMuted(uuid)) {
            return ChatUtils.error("You have been muted for ")
                    .append(Component.text(getMutedReason(uuid), NamedTextColor.DARK_RED))
                    .append(ChatUtils.error(" until "))
                    .append(Component.text(getMuteDuration(uuid), NamedTextColor.DARK_RED));
        } else {
            throw new NotMutedException("The user with uuid " + uuid + " is not muted.");
        }
    }

    /**
     * Convert a string to a long time for the ban duration.
     *
     * @param formattedInput input string in ymdh format
     * @return duration in milliseconds after converting the input string
     * @throws DurationFormatException if the input string is not formatted correctly
     */
    public static long getDuration(String formattedInput) throws DurationFormatException {

        if (formattedInput == null) {
            throw new NullPointerException();
        }

        //Add random letter at the end of the duration string, so it'll always split into 2 parts.
        String sDuration = formattedInput + "q";

        //Check for valid duration.
        //ymdh format (year, month, day, hour)
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

                //Remove the first part from the string as we've already converted it.
                sDuration = duration[1];

            } catch (NumberFormatException e) {
                throw new DurationFormatException("Duration must be in ymdh format, for example 1y6m, which is 1 year and 6 months or 2d12h is 2 days and 12 hours.");
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

                //Remove the first part from the string as we've already converted it.
                sDuration = duration[1];

            } catch (NumberFormatException e) {
                throw new DurationFormatException("Duration must be in ymdh format, for example 1y6m, which is 1 year and 6 months or 2d12h is 2 days and 12 hours.");
            }
        }

        //Check days
        duration = sDuration.split("d");

        if (duration.length == 2) {
            try {

                int days = Integer.parseInt(duration[0]);

                //Convert days to milliseconds and add to time.
                time += days * 24 * 60 * 60 * 1000L;

                //Remove the first part from the string as we've already converted it.
                sDuration = duration[1];

            } catch (NumberFormatException e) {
                throw new DurationFormatException("Duration must be in ymdh format, for example 1y6m, which is 1 year and 6 months or 2d12h is 2 days and 12 hours.");
            }
        }

        //Check hours
        duration = sDuration.split("h");

        if (duration.length == 2) {
            try {

                int hours = Integer.parseInt(duration[0]);

                //Convert hours to milliseconds and add to time.
                time += hours * 60 * 60 * 1000L;

                //Remove the first part from the string as we've already converted it.
                sDuration = duration[1];

            } catch (NumberFormatException e) {
                throw new DurationFormatException("Duration must be in ymdh format, for example 1y6m, which is 1 year and 6 months or 2d12h is 2 days and 12 hours.");
            }
        }

        //If the time is 0, or the string does not end with just the character q, then the format was not correct.
        if (time == 0 || !sDuration.equals("q")) {
            throw new DurationFormatException("Duration must be in ymdh format, for example 1y6m, which is 1 year and 6 months or 2d12h is 2 days and 12 hours.");
        } else {
            return time;
        }
    }
}
