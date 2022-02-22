package me.bteuk.network.utils;

import me.bteuk.network.gui.UniqueGui;
import org.bukkit.entity.Player;

import java.util.*;

public class User {

    //Player instance.
    public Player player;

    public UUID previousGui;
    public UUID currentGui;

    //Unique gui for this user.
    public UniqueGui uniqueGui;

    public User(Player player) {

        this.player = player;

    }
}
