package me.bteuk.network.sql;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class GlobalSQL {

    private final DataSource dataSource;

    public GlobalSQL(DataSource datasource) {

        this.dataSource = datasource;

    }

    private Connection conn() throws SQLException {
        return dataSource.getConnection();
    }
}
