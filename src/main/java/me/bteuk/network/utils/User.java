package me.bteuk.network.utils;

import me.bteuk.network.gui.UniqueGui;
import org.bukkit.entity.Player;

import java.util.*;

public class User {

    //Player instance.
    public Player player;

    public String previousGui;

    //Unique gui for this user.
    public UniqueGui uniqueGui;

    public User(Player player) {

        this.player = player;

    }
}
