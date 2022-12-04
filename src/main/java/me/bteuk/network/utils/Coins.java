package me.bteuk.network.utils;

/*

This class deals with all things related to coins.
Coins is the arbitrary term use to represent the ingame currency.
This currency will have a different name for players
The currency will be earned by building and will be purchasable with real money in the future.

 */

import me.bteuk.network.Network;

public class Coins {

    //Add coins to a specific player.
    public void addCoins(String uuid, int coins) {

        Network.getInstance().globalSQL.update("UPDATE coins SET coins=coins+" + coins + " WHERE uuid='" + uuid + "';");

    }

    //Remove coins from a specific player.
    public void removeCoins(String uuid, int coins) {

        Network.getInstance().globalSQL.update("UPDATE coins SET coins=coins-" + coins + " WHERE uuid='" + uuid + "';");

    }

    //Check if the player has at least this many coins.
    public boolean hasEnoughCoins(String uuid, int minimum) {

        return (Network.getInstance().globalSQL.hasRow("SELECT uuid FROM coins WHERE uuid='" + uuid + "' AND coins>=" + minimum + ";"));

    }
}
