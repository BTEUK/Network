package me.bteuk.network.sql;

import org.apache.commons.dbcp2.BasicDataSource;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.sql.*;
import java.util.ArrayList;

import static me.bteuk.network.utils.Constants.SERVER_NAME;

public class GlobalSQL {

    private final BasicDataSource dataSource;

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

    public double getDouble(String sql) {

        try (Connection conn = conn();
             PreparedStatement statement = conn.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {

            if (results.next()) {

                return results.getDouble(1);

            } else {

                return 0;

            }

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public float getFloat(String sql) {

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
    public ArrayList<String[]> getEvents(String serverName, ArrayList<String[]> list) {

        //Try and get all events for this server.
        try (Connection conn = conn();
             PreparedStatement statement = conn.prepareStatement("SELECT uuid,event,message FROM server_events WHERE server='" + serverName + "' AND type='network';");
             ResultSet results = statement.executeQuery()) {

            while (results.next()) {

                list.add(new String[]{results.getString(1), results.getString(2), results.getString(3)});

            }
        } catch (SQLException e) {
            e.printStackTrace();
            return list;
        }

        //Try and delete all events for this server.
        try (Connection conn = conn();
             PreparedStatement statement = conn.prepareStatement("DELETE FROM server_events WHERE server='" + serverName + "' AND type='network';")) {

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            return list;
        }

        //Return the map.
        return list;

    }

    //Add new coordinate to database and return the id.
    public int addCoordinate(Location l) {

        return (addCoordinate(SERVER_NAME, l.getWorld().getName(), l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch()));

    }

    //Add new coordinate to database and return the id.
    public int addCoordinate(String server, Location l) {

        return (addCoordinate(server, l.getWorld().getName(), l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch()));

    }

    //Add new coordinate using values, rather than location.
    public int addCoordinate(String server, String world, double x, double y, double z, float yaw, float pitch) {

        try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
                "INSERT INTO coordinates(server,world, x, y, z, yaw, pitch) VALUES(?, ?, ?, ?, ?, ?, ?);",
                Statement.RETURN_GENERATED_KEYS
        )) {
            statement.setString(1, server);
            statement.setString(2, world);
            statement.setDouble(3, x);
            statement.setDouble(4, y);
            statement.setDouble(5, z);
            statement.setFloat(6, yaw);
            statement.setFloat(7, pitch);
            statement.executeUpdate();

            //If the id does not exist return 0.
            ResultSet results = statement.getGeneratedKeys();
            if (results.next()) {

                return results.getInt(1);

            } else {

                return 0;

            }

        } catch (SQLException sql) {

            sql.printStackTrace();
            return 0;

        }
    }

    //Update an existing coordinate.
    public void updateCoordinate(int coordinateID, String server, Location l) {

        try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
                "UPDATE coordinates SET server=?, world=?, x=?, y=?, z=?, yaw=?, pitch=? WHERE id=?;"
        )) {
            statement.setString(1, server);
            statement.setString(2, l.getWorld().getName());
            statement.setDouble(3, l.getX());
            statement.setDouble(4, l.getY());
            statement.setDouble(5, l.getZ());
            statement.setFloat(6, l.getYaw());
            statement.setFloat(7, l.getPitch());
            statement.setInt(8, coordinateID);
            statement.executeUpdate();

        } catch (SQLException sql) {

            sql.printStackTrace();

        }

    }

    //Update an existing coordinate.
    public void updateCoordinate(int coordinateID, Location l) {

        updateCoordinate(coordinateID, SERVER_NAME, l);

    }

    //Get coordinate from database by id.
    //World must be on this server else this will throw a null pointer exception.
    public Location getCoordinate(int coordinateID) {

        try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
                "SELECT * FROM coordinates WHERE id=" + coordinateID + ";"
        ); ResultSet results = statement.executeQuery()) {

            results.next();
            return (new Location(Bukkit.getWorld(results.getString("world")),
                    results.getDouble("x"),
                    results.getDouble("y"),
                    results.getDouble("z"),
                    results.getFloat("yaw"),
                    results.getFloat("pitch")));

        } catch (SQLException sql) {

            sql.printStackTrace();
            return null;

        }

    }
}
