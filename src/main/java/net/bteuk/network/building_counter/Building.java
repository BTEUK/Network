package net.bteuk.network.building_counter;

import net.bteuk.network.Network;
import org.bukkit.Location;

public class Building {
    public Integer buildingId;
    public Location coordinate;
    public String playerId;
    public Integer coordinateId;

    public Building(int bId,int cId, String pId)
    {
        buildingId = bId;
        coordinate = Network.getInstance().getGlobalSQL().getLocation(cId);
        playerId = pId;
        coordinateId = cId;
    }

    public Building(int bId, Location c, String pId, int cId)
    {
        buildingId = bId;
        coordinate = c;
        playerId = pId;
        coordinateId = cId;
    }

    public Building()
    {
        buildingId = null;
        coordinate = null;
        playerId = null;
        coordinateId = null;
    }

}
