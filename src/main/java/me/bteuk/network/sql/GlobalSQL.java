package me.bteuk.network.sql;

import org.bukkit.Bukkit;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
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

    //Generic insert statement, return true is successful.
    public boolean insert(String sql) {

        try (Connection conn = conn();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            success = statement.executeUpdate();

            //If the insert was successful return true;
            if (success > 0) {
                return true;
            } else {

                Bukkit.getLogger().warning("SQL insert " + sql + " failed!");
                return false;

            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
