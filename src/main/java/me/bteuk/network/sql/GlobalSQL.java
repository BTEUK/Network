package me.bteuk.network.sql;

import org.apache.commons.dbcp2.BasicDataSource;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class GlobalSQL {

    private final BasicDataSource dataSource;
    private int success;

    public GlobalSQL(BasicDataSource datasource) {

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

            success = statement.executeUpdate();

            //If the insert was successful return true;
            if (success > 0) {
                return true;
            } else {

                Bukkit.getLogger().warning("SQL update " + sql + " failed!");
                return false;

            }

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

    public long getLong(String sql) {

        try (Connection conn = conn();
             PreparedStatement statement = conn.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {

            if (results.next()) {

                return results.getLong(1);

            } else {

                return 0;

            }

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public ResultSet getResultSet(String sql) {

        try (Connection conn = conn();
             PreparedStatement statement = conn.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {

            return results;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
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

    //Get a hashmap of all events for this server for the Network plugin.
    public HashMap<String, String> getEvents(String serverName, HashMap<String, String> map) {

        //Try and get all events for this server.
        try (Connection conn = conn();
             PreparedStatement statement = conn.prepareStatement("SELECT uuid,event FROM server_events WHERE server=" + serverName + " AND type='network';");
             ResultSet results = statement.executeQuery()) {

            while (results.next()) {

                map.put(results.getString(1), results.getString(2));

            }
        } catch (SQLException e) {
            e.printStackTrace();
            return map;
        }

        //Try and delete all events for this server.
        try (Connection conn = conn();
             PreparedStatement statement = conn.prepareStatement("DELETE FROM server_events WHERE server=" + serverName + " AND type='network';")) {

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            return map;
        }

        //Return the map.
        return map;

    }
}
