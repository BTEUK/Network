package me.bteuk.network.utils;

import me.bteuk.network.Network;
import org.bukkit.Location;

public class Region {

    private int regionX;
    private int regionZ;

    public boolean inRegion;

    public Region(Location l) {

        //If the server is earth and the world is the earth world set the current region.
        if (Network.getInstance().getConfig().getBoolean("earth_server") && Network.getInstance().getConfig().getString("earth_world").equals(l.getWorld().getName())) {

            setRegion(l);
            inRegion = true;

        }

    }

    public void setRegion(Location l) {
        regionX = (int) Math.floor((l.getX()/512));
        regionZ = (int) Math.floor((l.getZ()/512));
    }

    public String getRegion() {
        return (regionX + "," + regionZ);
    }

    public String getRegion(Location l) {
        return ((int) Math.floor((l.getX()/512)) + "," + (int) Math.floor((l.getZ()/512)));
    }

    public boolean equals(Location l) {
        if ((int) Math.floor((l.getX()/512)) == regionX && (int) Math.floor((l.getZ()/512)) == regionZ) {
            return true;
        } else {
            return false;
        }
    }
}
