package me.bteuk.network.sql;

import javax.sql.DataSource;

public class GlobalSQL {

    private DataSource dataSource;

    public GlobalSQL(DataSource datasource) {

        this.dataSource = datasource;

    }
}
