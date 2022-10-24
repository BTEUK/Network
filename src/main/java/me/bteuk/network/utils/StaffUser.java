package me.bteuk.network.utils;

import me.bteuk.network.staff.KickMembers;
import me.bteuk.network.gui.regions.RegionRequest;
import me.bteuk.network.gui.regions.RegionRequests;
import me.bteuk.network.staff.*;

public class StaffUser {

    //TODO: Similarly to NetworkUser, attempt to merge the guis.

    public StaffGui staffGui;

    public RegionRequests regionRequests;
    public RegionRequest regionRequest;

    public ManageRegion manageRegion;
    public TransferOwner transferOwner;
    public KickMembers kickMembers;

    public LocationRequests locationRequests;
    public LocationRequest locationRequest;

    public StaffUser() {

    }
}
