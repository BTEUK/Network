package me.bteuk.network;

import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;
import com.mysql.cj.jdbc.MysqlDataSource;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.sql.NavigationSQL;
import me.bteuk.network.sql.PlotSQL;
import me.bteuk.network.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public final class Main extends JavaPlugin {

    //MySQL
    private String host, username, password;
    private int port;

    //Global Database
    private String global_database;
    private GlobalSQL globalSQL;
    private DataSource global_dataSource;

    //Plot Database
    private String plot_database;
    private PlotSQL plotSQL;
    private DataSource plot_dataSource;

    //Navigation Database
    private String navigation_database;
    private NavigationSQL navigationSQL;
    private DataSource navigation_dataSource;

    //Server Name
    public static String SERVER_NAME;

    private static Main instance;
    private static FileConfiguration config;

    @Override
    public void onEnable() {

        //Config Setup
        Main.instance = this;
        Main.config = this.getConfig();

        saveDefaultConfig();

        if (!config.getBoolean("enabled")) {

            Bukkit.getLogger().warning(Utils.chat("&cThe config must be configured before the plugin can be enabled!"));
            Bukkit.getLogger().warning(Utils.chat("&cPlease edit the database values in the config, give the server a unique name and then set 'enabled: true'"));
            return;

        }

        //Setup MySQL
        try {

            global_database = config.getString("database.global");
            global_dataSource = mysqlSetup(global_database);
            globalSQL = new GlobalSQL(global_dataSource);

            navigation_database = config.getString("database.navigation");
            navigation_dataSource = mysqlSetup(navigation_database);
            navigationSQL = new NavigationSQL(navigation_dataSource);

            plot_database = config.getString("database.plot");
            plot_dataSource = mysqlSetup(plot_database);
            plotSQL = new PlotSQL(plot_dataSource);

        } catch (SQLException /*| IOException*/ e) {
            e.printStackTrace();
            Bukkit.getLogger().severe(Utils.chat("&cFailed to connect to the database, please check that you have set the config values correctly."));
            return;
        }

        //Set the server name from config.
        SERVER_NAME = config.getString("server_name");

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    //Creates the mysql connection.
    private DataSource mysqlSetup(String database) throws SQLException {

        host = config.getString("host");
        port = config.getInt("port");
        database = config.getString("database");
        username = config.getString("username");
        password = config.getString("password");

        MysqlDataSource dataSource = new MysqlConnectionPoolDataSource();

        dataSource.setServerName(host);
        dataSource.setPortNumber(port);
        dataSource.setDatabaseName(database + "?&useSSL=false&");
        dataSource.setUser(username);
        dataSource.setPassword(password);

        testDataSource(dataSource);
        return dataSource;

    }

    public void testDataSource(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            if (!connection.isValid(1000)) {
                throw new SQLException("Could not establish database connection.");
            }
        }
    }

    //Returns an instance of the plugin.
    public static Main getInstance() {
        return instance;
    }
}
