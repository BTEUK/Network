package net.bteuk.network.sql;

import net.bteuk.network.lib.utils.ChatUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static net.bteuk.network.utils.Constants.LOGGER;
import static net.bteuk.network.utils.NetworkConfig.CONFIG;

public class Tutorials {

    private BasicDataSource dataSource;

    public static final Component TUTORIAL_REQUIRED_MESSAGE =
            ChatUtils.error("You need applicant to claim a plot, you can get this by completing a tutorial.")
                    .append(ChatUtils.error(" Click here to open the tutorial menu!"))
                    .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/navigator tutorials"));

    public Tutorials() {
        try {
            mysqlSetup();
            LOGGER.info("Enabled Tutorials support");
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.severe("Failed to connect to the tutorials database, please check that you have set the config values correctly.");
        }
    }

    private Connection conn() throws SQLException {
        return dataSource.getConnection();
    }

    //Creates the mysql connection.
    private void mysqlSetup() throws SQLException {

        String host = CONFIG.getString("tutorials.database.host");
        int port = CONFIG.getInt("tutorials.database.port");
        String username = CONFIG.getString("tutorials.database.username");
        String password = CONFIG.getString("tutorials.database.password");

        dataSource = new BasicDataSource();

        dataSource.setUrl("jdbc:mysql://" + host + ":" + port + "/" + CONFIG.getString("tutorials.database.name") + "?&useSSL=false&");
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        testDataSource(dataSource);

    }

    private void testDataSource(BasicDataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            if (!connection.isValid(1000)) {
                throw new SQLException("Could not establish database connection.");
            }
        }
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
}
