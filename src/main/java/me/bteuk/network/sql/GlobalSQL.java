package me.bteuk.network.sql;

import org.bukkit.Bukkit;

import javax.sql.DataSource;
import javax.xml.transform.Result;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GlobalSQL {

    private final DataSource dataSource;
    private int success;

    public GlobalSQL(DataSource datasource) {

        this.dataSource = datasource;

    }

    private Connection conn() throws SQLException {
        return dataSource.getConnection();
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
}
