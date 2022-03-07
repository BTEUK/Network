package me.bteuk.network.utils;

import me.bteuk.network.gui.UniqueGui;
import org.bukkit.entity.Player;

public class NetworkUser {

    //Player instance.
    public Player player;

    //Unique gui for this user.
    public UniqueGui uniqueGui;

    public NetworkUser(Player player) {

        this.player = player;

    }
}
