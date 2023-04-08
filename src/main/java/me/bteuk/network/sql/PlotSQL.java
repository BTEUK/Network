package me.bteuk.network.sql;

import org.apache.commons.dbcp2.BasicDataSource;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class PlotSQL {

    private final BasicDataSource dataSource;

    public PlotSQL(BasicDataSource datasource) {

        this.dataSource = datasource;

    }

    private Connection conn() throws SQLException {
        return dataSource.getConnection();
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

    public ArrayList<Integer> getIntList(String sql) {

        ArrayList<Integer> list = new ArrayList<>();

        try (Connection conn = conn();
             PreparedStatement statement = conn.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {

            while (results.next()) {

                list.add(results.getInt(1));

            }

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        return list;
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

    public int[][] getPlotCorners(int plotID) {

        try (Connection conn = conn();
             PreparedStatement statement = conn.prepareStatement("SELECT COUNT(corner) FROM plot_corners WHERE id=" + plotID + ";");
             ResultSet results = statement.executeQuery()) {

            results.next();

            int[][] corners = new int[results.getInt(1)][2];

            corners = getPlotCorners(corners, plotID);

            return corners;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private int[][] getPlotCorners(int[][] corners, int plotID) {

        try (Connection conn = conn();
             PreparedStatement statement = conn.prepareStatement("SELECT x,z FROM plot_corners WHERE id=" + plotID + ";");
             ResultSet results = statement.executeQuery()) {

            for (int i = 0; i < corners.length; i++) {

                results.next();
                corners[i][0] = results.getInt(1);
                corners[i][1] = results.getInt(2);

            }

            return corners;

        } catch (SQLException e) {
            e.printStackTrace();
            return corners;
        }
    }
}
