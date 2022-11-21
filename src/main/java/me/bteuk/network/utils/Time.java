package me.bteuk.network.utils;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class Time {

    //Gets current time in milliseconds.
    public static long currentTime() {

        return System.currentTimeMillis();
    }

    //Converts milliseconds to date.
    public static String getDate(long time) {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        formatter.setTimeZone(TimeZone.getTimeZone("Europe/London"));
        Date date = new Date(time);
        return formatter.format(date);
    }

    //Converts milliseconds to minutes.
    public static long minutes(long time) {
        return ((time / 1000) / 60);
    }

    //Converts milliseconds to seconds.
    public static long seconds(long time) {
        return ((time / 1000) % 60);
    }

    //Returns minute/minutes depending on the time provided.
    public static String minuteString(long time) {
        if (minutes(time) == 1) {
            return "minute";
        } else {
            return "minutes";
        }
    }

    //Returns second/seconds depending on the time provided.
    public static String secondString(long time) {
        if (seconds(time) == 1) {
            return "second";
        } else {
            return "seconds";
        }
    }

    //Format moderation time string to time in milliseconds added to current time.
    public static long getFormattedTime(String input) {
        long time = currentTime();




        return time;
    }
}
