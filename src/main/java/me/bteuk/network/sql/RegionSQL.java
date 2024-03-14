package me.bteuk.network.sql;

import me.bteuk.network.Network;
import me.bteuk.network.utils.regions.Inactivity;
import me.bteuk.network.utils.regions.Region;
import me.bteuk.network.utils.regions.Request;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class RegionSQL extends AbstractSQL {
    public RegionSQL(BasicDataSource datasource) {
        super(datasource);
    }

    public ArrayList<Request> getRequestList(String sql) {

        ArrayList<Request> list = new ArrayList<>();

        try (
                Connection conn = conn();
                PreparedStatement statement = conn.prepareStatement(sql);
                ResultSet results = statement.executeQuery()
        ) {

            while (results.next()) {

                list.add(new Request(results.getString(1), results.getString(2)));

            }

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        return list;
    }

    public ArrayList<Inactivity> getInactives(String sql) {

        ArrayList<Inactivity> list = new ArrayList<>();
        Region region;

        try (
                Connection conn = conn();
                PreparedStatement statement = conn.prepareStatement(sql);
                ResultSet results = statement.executeQuery()
        ) {

            while (results.next()) {

                region = Network.getInstance().getRegionManager().getRegion(results.getString(1));

                list.add(new Inactivity(region, results.getString(2)));

            }

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        return list;
    }
}
