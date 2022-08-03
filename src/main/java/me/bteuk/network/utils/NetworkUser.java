package me.bteuk.network.utils;

import me.bteuk.network.Network;
import me.bteuk.network.gui.BuildGui;
import me.bteuk.network.gui.navigation.ExploreGui;
import me.bteuk.network.gui.plotsystem.*;
import me.bteuk.network.gui.regions.*;
import me.bteuk.network.utils.regions.Region;
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
    public InvitePlotMembers invitePlotMembers;

    public RegionMenu regionMenu;
    public RegionInfo regionInfo;

    public InviteRegionMembers inviteRegionMembers;
    public RegionMembers regionMembers;

    public RegionRequests regionRequests;
    public RegionRequest regionRequest;

    public ExploreGui exploreGui;

    //Staff user.
    public StaffUser staffUser;

    //Region information.
    public boolean inRegion;
    public Region region;
    public int dx;
    public int dz;

    //Navigator in hotbar.
    public boolean navigator;

    public NetworkUser(Player player) {

        this.player = player;

        navigator = Network.getInstance().globalSQL.hasRow("SELECT navigator FROM player_data WHERE uuid='" + player.getUniqueId() + "' AND navigator=1;");

        //Check if the player is in a region.
        if (Network.SERVER_NAME.equals(Network.getInstance().globalSQL.getString("SELECT name FROM server_data WHERE type='EARTH'"))) {
            //Check if they are in the earth world.
            if (player.getLocation().getWorld().getName().equals(Network.getInstance().getConfig().getString("earth_world"))) {
                region = Network.getInstance().getRegionManager().getRegion(player.getLocation());
                inRegion = true;
            }
        } else if (Network.SERVER_NAME.equals(Network.getInstance().globalSQL.getString("SELECT name FROM server_data WHERE type='PLOT';"))) {
            //Check if the player is in a buildable plot world and apply coordinate transform if true.
            if (Network.getInstance().plotSQL.hasRow("SELECT name FROM location_data WHERE name='" + player.getLocation().getWorld().getName() + "';")) {
                dx = -Network.getInstance().plotSQL.getInt("SELECT xTransform FROM location_data WHERE name='" + player.getLocation().getWorld().getName() + "';");
                dz = -Network.getInstance().plotSQL.getInt("SELECT zTransform FROM location_data WHERE name='" + player.getLocation().getWorld().getName() + "';");

                region = Network.getInstance().getRegionManager().getRegion(player.getLocation(), dx, dz);
                inRegion = true;
            }
        }

        //If the user is a member of staff, create staff user instance.
        if (player.hasPermission("uknet.staff")) {
            staffUser = new StaffUser();
        }

    }
}
