package me.bteuk.network.sql;

import javax.sql.DataSource;
import javax.xml.transform.Result;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class NavigationSQL {

    private final DataSource dataSource;

    public NavigationSQL(DataSource datasource) {

        this.dataSource = datasource;

    }

    private Connection conn() throws SQLException {
        return dataSource.getConnection();
    }

    public boolean hasRow(String sql) {

        try (Connection conn = conn();
             PreparedStatement statement = conn().prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {

            return results.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
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
