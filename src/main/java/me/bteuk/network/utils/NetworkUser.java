package me.bteuk.network.utils;

import me.bteuk.network.Network;
import me.bteuk.network.gui.BuildGui;
import me.bteuk.network.gui.plotsystem.*;
import me.bteuk.network.staff.StaffGui;
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

    //Staff user.
    public StaffUser staffUser;

    //Region information.
    public Region region;

    //Navigator in hotbar.
    public boolean navigator;

    public NetworkUser(Player player) {

        this.player = player;

        navigator = Network.getInstance().globalSQL.hasRow("SELECT navigator FROM player_data WHERE uuid='" + player.getUniqueId() + "' AND navigator=1;");

        region = new Region(player.getLocation());

        //If the user is a member of staff, create staff user instance.
        if (player.hasPermission("uknet.staff")) {
            staffUser = new StaffUser();
        }

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
