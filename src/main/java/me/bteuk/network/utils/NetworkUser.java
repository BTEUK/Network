package me.bteuk.network.utils;

import me.bteuk.network.gui.UniqueGui;
import me.bteuk.network.utils.enums.PlotDifficulty;
import me.bteuk.network.utils.enums.PlotSize;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class NetworkUser {

    //Player instance.
    public Player player;

    //Plot Categories.
    public PlotSize plotSize = PlotSize.RANDOM;
    public PlotDifficulty plotDifficulty = PlotDifficulty.RANDOM;

    //Unique gui for this user.
    public UniqueGui uniqueGui;

    public NetworkUser(Player player) {

        this.player = player;

    }

    public String getRegion(Location l) {
        double x = l.getX();
        double z = l.getZ();
        int rX = (int) Math.floor((x/512));
        int rZ = (int) Math.floor((z/512));
        return (rX + "," + rZ);
    }

    public String getRegion() {
        double x = player.getLocation().getX();
        double z = player.getLocation().getZ();
        int rX = (int) Math.floor((x/512));
        int rZ = (int) Math.floor((z/512));
        return (rX + "," + rZ);
    }
}
