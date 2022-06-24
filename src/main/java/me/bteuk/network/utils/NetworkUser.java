package me.bteuk.network.utils;

import me.bteuk.network.gui.BuildGui;
import me.bteuk.network.gui.plotsystem.*;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class NetworkUser {

    //Player instance.
    public Player player;

    //Gui's
    public BuildGui buildGui;
    public PlotServerLocations plotServerLocations;
    public PlotMenu plotMenu;
    public PlotInfo plotInfo;
    public AcceptedPlotFeedback acceptedPlotFeedback;
    public DeniedPlotFeedback deniedPlotFeedback;

    public DeleteConfirm deleteConfirm;

    public PlotMembers plotMembers;
    public InviteMembers inviteMembers;

    //Region information.
    public Region region;

    public NetworkUser(Player player) {

        this.player = player;

        region = new Region(player.getLocation());

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
