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

public class RegionSQL {

    private final BasicDataSource dataSource;

    public RegionSQL(BasicDataSource datasource) {

        this.dataSource = datasource;

    }

    private Connection conn() throws SQLException {
        return dataSource.getConnection();
    }

    public boolean hasRow(String sql) {

        try (Connection conn = conn();
             PreparedStatement statement = conn.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {

            return results.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //Generic update statement, return true if successful.
    public boolean update(String sql) {

        try (Connection conn = conn();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.executeUpdate();

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getInt(String sql) {

        try (Connection conn = conn();
             PreparedStatement statement = conn.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {

            if (results.next()) {

                return results.getInt(1);

            } else {

                return 0;

            }

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public String getString(String sql) {

        try (Connection conn = conn();
             PreparedStatement statement = conn.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {

            if (results.next()) {

                return results.getString(1);

            } else {

                return null;

            }

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<String> getStringList(String sql) {

        ArrayList<String> list = new ArrayList<>();

        try (Connection conn = conn();
             PreparedStatement statement = conn.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {

            while (results.next()) {

                list.add(results.getString(1));

            }

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        return list;
    }

    public ArrayList<Request> getRequestList(String sql) {

        ArrayList<Request> list = new ArrayList<>();

        try (Connection conn = conn();
             PreparedStatement statement = conn.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {

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

        try (Connection conn = conn();
             PreparedStatement statement = conn.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {

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
