package net.bteuk.network.sql;

import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.SQLException;

import static net.bteuk.network.utils.NetworkConfig.CONFIG;

public class DatabaseInit {

    // Creates the mysql connection.
    public BasicDataSource mysqlSetup(String database) throws SQLException {

        String host = CONFIG.getString("host");
        int port = CONFIG.getInt("port");
        String username = CONFIG.getString("username");
        String password = CONFIG.getString("password");

        BasicDataSource dataSource = new BasicDataSource();

        dataSource.setUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?&allowPublicKeyRetrieval=true" +
                "&useSSL=false&");
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        testDataSource(dataSource);
        return dataSource;
    }

    private void testDataSource(BasicDataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            if (!connection.isValid(1000)) {
                throw new SQLException("Could not establish database connection.");
            }
        }
    }
}
