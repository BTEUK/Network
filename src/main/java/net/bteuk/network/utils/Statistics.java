package net.bteuk.network.utils;

/*

This class will deal with tracking statistics of players.

 */

import net.bteuk.network.Network;

public class Statistics {
    // Add tpll to statistics.
    public static void addTpll(String uuid, String date) {
        // If date doesn't exist, create it.
        if (Network.getInstance().getGlobalSQL().hasRow("SELECT uuid FROM statistics WHERE uuid='" + uuid + "' AND " +
                "on_date='" + date + "';")) {
            Network.getInstance().getGlobalSQL().update("UPDATE statistics SET tpll=tpll+1 WHERE uuid='" + uuid + "' " +
                    "AND on_date='" + date + "';");
        } else {
            Network.getInstance().getGlobalSQL()
                    .update("INSERT INTO statistics(uuid,on_date,tpll) VALUES('" + uuid + "','" + date + "',1);");
        }
    }
}
