package net.bteuk.network.utils;

/*

This class will deal with tracking statistics of players.

 */

import net.bteuk.network.Network;

public class Statistics {

    //Saves the online-time of player from previous save till now.
    public static void save(NetworkUser u, String date, long time) {

        //Get time difference from previous save and set previous save to current time.
        long time_diff = time - u.last_time_log;
        u.last_time_log = time;

        //Add time difference to active session.
        u.active_time += time_diff;

        //Add time to database, if date doesn't exist, create it.
        if (Network.getInstance().getGlobalSQL().hasRow("SELECT uuid FROM statistics WHERE uuid='" + u.player.getUniqueId() + "' AND on_date='" + date + "';")) {
            Network.getInstance().getGlobalSQL().update("UPDATE statistics SET playtime=playtime+" + time_diff + " WHERE uuid='" + u.player.getUniqueId() + "' AND on_date='" + date + "';");
        } else {
            Network.getInstance().getGlobalSQL().update("INSERT INTO statistics(uuid,on_date,playtime) VALUES('" + u.player.getUniqueId() + "','" + date + "'," + time_diff + ");");
        }

    }

    //Saves the online-time of all online players from the previous ave till now.
    public static void saveAll() {

        //Get current time.
        long time = Time.currentTime();

        //Get current date.
        String date = Time.getDate(time);

        //Iterate through online users.
        //If player is afk, skip.
        for (NetworkUser u: Network.getInstance().getUsers()) {
            if (!u.afk) {
                save(u, date, time);
            }
        }
    }

    //Add message to statistics.
    public static void addMessage(String uuid, String date) {
        //If date doesn't exist, create it.
        if (Network.getInstance().getGlobalSQL().hasRow("SELECT uuid FROM statistics WHERE uuid='" + uuid + "' AND on_date='" + date + "';")) {
            Network.getInstance().getGlobalSQL().update("UPDATE statistics SET messages=messages+1 WHERE uuid='" + uuid + "' AND on_date='" + date + "';");
        } else {
            Network.getInstance().getGlobalSQL().update("INSERT INTO statistics(uuid,on_date,messages) VALUES('" + uuid + "','" + date + "',1);");
        }
    }

    //Add tpll to statistics.
    public static void addTpll(String uuid, String date) {
        //If date doesn't exist, create it.
        if (Network.getInstance().getGlobalSQL().hasRow("SELECT uuid FROM statistics WHERE uuid='" + uuid + "' AND on_date='" + date + "';")) {
            Network.getInstance().getGlobalSQL().update("UPDATE statistics SET tpll=tpll+1 WHERE uuid='" + uuid + "' AND on_date='" + date + "';");
        } else {
            Network.getInstance().getGlobalSQL().update("INSERT INTO statistics(uuid,on_date,tpll) VALUES('" + uuid + "','" + date + "',1);");
        }
    }
}
