package net.bteuk.network.api.impl;

import net.bteuk.network.api.CoordinateAPI;
import net.bteuk.network.api.entity.NetworkLocation;
import net.bteuk.network.sql.GlobalSQL;

public class CoordinateAPIImpl implements CoordinateAPI {

    private final GlobalSQL globalSQL;

    public CoordinateAPIImpl(GlobalSQL globalSQL) {
        this.globalSQL = globalSQL;
    }

    /**
     * Add a coordinate to the database and return its id.
     * @param location the location to create a coordinate of
     * @return the coordinate id
     */
    @Override
    public int addCoordinate(NetworkLocation location) {
        return globalSQL.addCoordinate(location);
    }
}
