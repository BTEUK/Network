package me.bteuk.network.sql;

import com.mysql.cj.jdbc.CallableStatement;

import javax.sql.DataSource;
import javax.xml.transform.Result;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlotSQL {

    private final DataSource dataSource;

    public PlotSQL(DataSource datasource) {

        this.dataSource = datasource;

    }

    private Connection conn() throws SQLException {
        return dataSource.getConnection();
    }

    public int getInt(String sql) {

        try (Connection conn = conn();
             PreparedStatement statement = conn.prepareStatement(sql);
             ResultSet results = statement.executeQuery();) {

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
             ResultSet results = statement.executeQuery();) {

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
