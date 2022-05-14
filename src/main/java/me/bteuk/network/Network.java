package me.bteuk.network;

import me.bteuk.network.gui.Navigator;
import me.bteuk.network.listeners.GuiListener;
import me.bteuk.network.listeners.JoinServer;
import me.bteuk.network.listeners.LeaveServer;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.sql.NavigationSQL;
import me.bteuk.network.sql.PlotSQL;
import me.bteuk.network.utils.enums.ServerType;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import org.apache.commons.dbcp2.BasicDataSource;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.stream.Collectors;

public final class Network extends JavaPlugin {

    //Server Name
    public static String SERVER_NAME;
    public static ServerType SERVER_TYPE;

    private static Network instance;
    private static FileConfiguration config;

    //User List
    private ArrayList<NetworkUser> networkUsers;

    //Guis
    public Navigator navigator;

    //SQL
    public NavigationSQL navigationSQL;
    public PlotSQL plotSQL;
    public GlobalSQL globalSQL;

    //Chat
    private CustomChat chat;
    public String socketIP;
    public int socketPort;

    @Override
    public void onEnable() {

        //Config Setup
        Network.instance = this;
        Network.config = this.getConfig();

        saveDefaultConfig();

        if (!config.getBoolean("enabled")) {

            Bukkit.getLogger().warning(Utils.chat("&cThe config must be configured before the plugin can be enabled!"));
            Bukkit.getLogger().warning(Utils.chat("&cPlease edit the database values in the config, give the server a unique name and then set 'enabled: true'"));
            Bukkit.getLogger().warning(Utils.chat("&cAlso make sure to set the server to the correct type."));
            return;

        }

        //Setup MySQL
        try {

            //Global Database
            String global_database = config.getString("database.global");
            BasicDataSource global_dataSource = mysqlSetup(global_database);
            globalSQL = new GlobalSQL(global_dataSource);
            initDb("dbsetup_global.sql", global_dataSource);

            //Navigation Database
            String navigation_database = config.getString("database.navigation");
            BasicDataSource navigation_dataSource = mysqlSetup(navigation_database);
            navigationSQL = new NavigationSQL(navigation_dataSource);
            initDb("dbsetup_navigation.sql", navigation_dataSource);

            //Plot Database
            String plot_database = config.getString("database.plot");
            BasicDataSource plot_dataSource = mysqlSetup(plot_database);
            plotSQL = new PlotSQL(plot_dataSource);
            initDb("dbsetup_plot.sql", plot_dataSource);

        } catch (SQLException | IOException e) {
            e.printStackTrace();
            Bukkit.getLogger().severe(Utils.chat("&cFailed to connect to the database, please check that you have set the config values correctly."));
            return;
        }

        //Set the server name from config.
        SERVER_NAME = config.getString("server_name");

        //Set the server type from config.
        SERVER_TYPE = ServerType.valueOf(config.getString("server_type"));

        if (!navigationSQL.hasRow("SELECT name FROM server_data WHERE name=" + SERVER_NAME + ";")) {

            //Add server to database and enable server.
            if (navigationSQL.update(
                    "INSERT INTO server_data(name,type) VALUES(" + SERVER_NAME + "," + SERVER_TYPE.toString() + ");"
            )) {

                //Enable plugin.
                Bukkit.getLogger().info(Utils.chat("&aServer added to database with name " + SERVER_NAME + " and type " + SERVER_TYPE));
                Bukkit.getLogger().info(Utils.chat("&cEnabling Plugin"));
                enablePlugin();

            } else {

                //If the server is not in the database, shut down plugin.
                Bukkit.getLogger().severe(Utils.chat("&cFailed to add server to database, disabling plugin!"));

            }

        } else {

            //Enable plugin.
            Bukkit.getLogger().info(Utils.chat("&cEnabling Plugin"));
            enablePlugin();

        }
    }

    //Server enabling procedure when the config has been set up.
    public void enablePlugin() {

        //Create user list.
        networkUsers = new ArrayList<>();

        //Register events.
        new JoinServer(this);
        new LeaveServer(this);

        new GuiListener(this);

        //Create Guis.
        navigator = new Navigator();

        //Create bungeecord channel
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        //Setup custom chat.
        chat = new CustomChat(this);
        socketIP = config.getString("socket.IP");
        socketPort = config.getInt("socket.port");

    }

    @Override
    public void onDisable() {

        //Disable bungeecord channel.
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);

        chat.onDisable();

    }

    //Setup the tables for the database.
    private void initDb(String fileName, BasicDataSource dataSource) throws SQLException, IOException {
        // first lets read our setup file.
        // This file contains statements to create our inital tables.
        // it is located in the resources.
        String setup;
        try (InputStream in = getClassLoader().getResourceAsStream(fileName)) {
            // Legacy way
            setup = new BufferedReader(new InputStreamReader(in)).lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not read db setup file.", e);
            throw e;
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
            }
        }
        getLogger().info("§2Database setup complete for " + fileName);
    }

    //Creates the mysql connection.
    private BasicDataSource mysqlSetup(String database) throws SQLException {

        String host = config.getString("host");
        int port = config.getInt("port");
        String username = config.getString("username");
        String password = config.getString("password");

        BasicDataSource dataSource = new BasicDataSource();

        dataSource.setUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?&useSSL=false&");
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        testDataSource(dataSource);
        return dataSource;

    }

    public void testDataSource(BasicDataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            if (!connection.isValid(1000)) {
                throw new SQLException("Could not establish database connection.");
            }
        }
    }

    //Returns an instance of the plugin.
    public static Network getInstance() {
        return instance;
    }

    //Get user from player.
    public NetworkUser getUser(Player p) {

        for (NetworkUser u : networkUsers) {

            if (u.player == p) {

                return u;

            }
        }

        return null;
    }

    //Add user to list.
    public void addUser(NetworkUser u) {

        networkUsers.add(u);

    }

    //Get user from player.
    public void removeUser(NetworkUser u) {

        networkUsers.remove(u);

    }
}
