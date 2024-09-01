package net.bteuk.network.sql;

import org.apache.commons.dbcp2.BasicDataSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.stream.Collectors;

import static net.bteuk.network.utils.Constants.LOGGER;
import static net.bteuk.network.utils.NetworkConfig.CONFIG;

public class DatabaseInit {

    //Setup the tables for the database.
    public boolean initDb(ClassLoader loader, String fileName, BasicDataSource dataSource) {
        // first lets read our setup file.
        // This file contains statements to create our inital tables.
        // it is located in the resources.
        String setup;
        try (InputStream in = loader.getResourceAsStream(fileName)) {
            // Legacy way
            setup = new BufferedReader(new InputStreamReader(in)).lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            LOGGER.severe("Could not read db setup file.");
            return false;
        }
        // Mariadb can only handle a single query per statement. We need to split at ;.
        String[] queries = setup.split(";");
        // execute each query to the database.
        for (String query : queries) {
            // If you use the legacy way you have to check for empty queries here.
            if (query.trim().isEmpty()) continue;
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.execute();
            } catch (SQLException sqlException) {
                LOGGER.severe("Error in sql syntax: " + sqlException.getMessage());
                return false;
            }
        }
        LOGGER.info("Database setup complete for " + fileName);
        return true;
    }

    //Creates the mysql connection.
    public BasicDataSource mysqlSetup(String database) throws SQLException {

        String host = CONFIG.getString("host");
        int port = CONFIG.getInt("port");
        String username = CONFIG.getString("username");
        String password = CONFIG.getString("password");

        BasicDataSource dataSource = new BasicDataSource();

        dataSource.setUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?&useSSL=false&");
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
