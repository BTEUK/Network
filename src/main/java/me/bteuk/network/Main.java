package me.bteuk.network;

import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;
import com.mysql.cj.jdbc.MysqlDataSource;
import me.bteuk.network.gui.BuildGui;
import me.bteuk.network.gui.Navigator;
import me.bteuk.network.listeners.GuiListener;
import me.bteuk.network.listeners.JoinServer;
import me.bteuk.network.listeners.LeaveServer;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.sql.NavigationSQL;
import me.bteuk.network.sql.PlotSQL;
import me.bteuk.network.utils.ServerType;
import me.bteuk.network.utils.User;
import me.bteuk.network.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.units.qual.N;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public final class Main extends JavaPlugin {

    //Server Name
    public static String SERVER_NAME;
    public static ServerType SERVER_TYPE;

    private static Main instance;
    private static FileConfiguration config;

    //User List
    private ArrayList<User> users;

    //Guis
    public Navigator navigator;

    //SQL
    public NavigationSQL navigationSQL;
    public PlotSQL plotSQL;
    public GlobalSQL globalSQL;

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

        //Global Database
        String global_database;
        DataSource global_dataSource;

        //Plot Database
        String plot_database;
        DataSource plot_dataSource;

        //Navigation Database
        String navigation_database;
        DataSource navigation_dataSource;

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

        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().severe(Utils.chat("&cFailed to connect to the database, please check that you have set the config values correctly."));
            return;
        }

        //Set the server name and type from config.
        SERVER_NAME = config.getString("server_name");

        if (!navigationSQL.hasRow("SELECT name FROM server_data WHERE name=" + SERVER_NAME + ";")) {

            //If the server is not in the database, shut down plugin.
            Bukkit.getLogger().severe(Utils.chat("&cServer not in database, disabling plugin!"));
            return;

        } else {

            //Try to get the server type.
            try {

                SERVER_TYPE = ServerType.valueOf(
                        navigationSQL.getString("SELECT type FROM server_data WHERE name=" + SERVER_NAME + ";").toUpperCase());

            } catch (NullPointerException | IllegalArgumentException e) {

                Bukkit.getLogger().severe(Utils.chat("&cServer type in database is not valid!"));
                Bukkit.getLogger().severe(Utils.chat("&cPlease make sure the server is configured correctly."));

            }
        }

        //Create user list.
        users = new ArrayList<>();

        //Register events.
        new JoinServer(this);
        new LeaveServer(this);

        new GuiListener(this);

        //Create Guis.
        navigator = new Navigator();

        //Create bungeecord channel
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

    }

    @Override
    public void onDisable() {

        //Disable bungeecord channel.
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);

    }

    //Creates the mysql connection.
    private DataSource mysqlSetup(String database) throws SQLException {

        String host = config.getString("host");
        int port = config.getInt("port");
        String username = config.getString("username");
        String password = config.getString("password");

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

    //Get user from player.
    public User getUser(Player p) {

        for (User u : users) {

            if (u.player == p) {

                return u;

            }
        }

        return null;
    }

    //Add user to list.
    public void addUser(User u) {

        users.add(u);

    }

    //Get user from player.
    public void removeUser(User u) {

        users.remove(u);

    }
}
