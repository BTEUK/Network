package me.bteuk.network;

import me.bteuk.network.commands.*;
import me.bteuk.network.commands.staff.Ban;
import me.bteuk.network.commands.staff.Database;
import me.bteuk.network.commands.staff.Mute;
import me.bteuk.network.commands.staff.Staff;
import me.bteuk.network.commands.tabcompleter.LocationSelector;
import me.bteuk.network.commands.tabcompleter.PlayerSelector;
import me.bteuk.network.commands.tabcompleter.ServerSelector;
import me.bteuk.network.gui.NavigatorGui;
import me.bteuk.network.listeners.*;
import me.bteuk.network.listeners.global_teleport.MoveListener;
import me.bteuk.network.listeners.global_teleport.TeleportListener;
import me.bteuk.network.lobby.Lobby;
import me.bteuk.network.sql.DatabaseUpdates;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.sql.PlotSQL;
import me.bteuk.network.sql.RegionSQL;
import me.bteuk.network.utils.*;
import me.bteuk.network.utils.enums.ServerType;
import me.bteuk.network.utils.regions.RegionManager;
import net.kyori.adventure.text.Component;
import org.apache.commons.dbcp2.BasicDataSource;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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

import static me.bteuk.network.utils.Constants.*;
import static me.bteuk.network.utils.NetworkConfig.CONFIG;

public final class Network extends JavaPlugin {

    private static Network instance;

    //If the server can shutdown.
    public boolean allow_shutdown;

    //RegionManager
    private RegionManager regionManager;

    //User List
    private ArrayList<NetworkUser> networkUsers;

    //Guis
    public NavigatorGui navigatorGui;
    public ItemStack navigator;

    //SQL
    public PlotSQL plotSQL;
    public GlobalSQL globalSQL;
    public RegionSQL regionSQL;

    //Chat
    public CustomChat chat;
    public String socketIP;
    public int socketPort;

    //Timers
    public Timers timers;

    //Lobby
    private Lobby lobby;

    //Leave Server listener.
    public LeaveServer leaveServer;

    //Movement listeners.
    public MoveListener moveListener;
    public TeleportListener teleportListener;

    //Tab
    public TabManager tab;

    @Override
    public void onEnable() {

        //Config Setup
        Network.instance = this;

        allow_shutdown = true;

        //Sets the config if the file has not yet been created.
        saveDefaultConfig();

        //Update the config to the latest version if it's outdated.
        //It will copy over any keys that remain the same.
        //This will also set the status variable to access the config project-wide.
        NetworkConfig networkConfig = new NetworkConfig();
        networkConfig.updateConfig();

        if (!CONFIG.getBoolean("enabled")) {

            getLogger().warning("The config must be configured before the plugin can be enabled!");
            getLogger().warning("Please edit the database values in the config, give the server a unique name and then set 'enabled: true'");
            getLogger().warning("Also make sure to set the server to the correct type.");
            return;

        }

        //Setup MySQL
        try {

            //Global Database
            String global_database = CONFIG.getString("database.global");
            BasicDataSource global_dataSource = mysqlSetup(global_database);
            globalSQL = new GlobalSQL(global_dataSource);
            initDb("dbsetup_global.sql", global_dataSource);

            //Region Database
            String region_database = CONFIG.getString("database.region");
            BasicDataSource region_dataSource = mysqlSetup(region_database);
            regionSQL = new RegionSQL(region_dataSource);
            initDb("dbsetup_regions.sql", region_dataSource);

            //Plot Database
            String plot_database = CONFIG.getString("database.plot");
            BasicDataSource plot_dataSource = mysqlSetup(plot_database);
            plotSQL = new PlotSQL(plot_dataSource);
            initDb("dbsetup_plots.sql", plot_dataSource);

        } catch (SQLException | IOException e) {
            e.printStackTrace();
            getLogger().severe("Failed to connect to the database, please check that you have set the config values correctly.");
            return;
        }

        //Update database.
        new DatabaseUpdates().updateDatabase();

        if (!globalSQL.hasRow("SELECT name FROM server_data WHERE name='" + SERVER_NAME + "';")) {

            //Add server to database and enable server.
            if (globalSQL.update(
                    "INSERT INTO server_data(name,type) VALUES('" + SERVER_NAME + "','" + SERVER_TYPE + "');"
            )) {

                //Enable plugin.
                getLogger().info("Server added to database with name " + SERVER_NAME + " and type " + SERVER_TYPE);
                getLogger().info("Enabling Plugin");
                enablePlugin();

            } else {

                //If the server is not in the database, shut down plugin.
                getLogger().severe("Failed to add server to database, disabling plugin!");

            }

        } else {

            //Enable plugin.
            getLogger().info("Enabling Plugin");
            enablePlugin();

        }
    }

    //Server enabling procedure when the config has been set up.
    public void enablePlugin() {

        //Create user list.
        networkUsers = new ArrayList<>();

        //Setup custom chat.
        socketIP = CONFIG.getString("socket.IP");
        socketPort = CONFIG.getInt("socket.port");

        chat = new CustomChat(this, socketIP, socketPort);

        //Setup connect.
        //Network connect
        Connect connect = new Connect(this, globalSQL, plotSQL);

        //Create navigator.
        navigatorGui = new NavigatorGui();
        navigator = Utils.createItem(Material.NETHER_STAR, 1, Utils.title("Navigator"), Utils.line("Click to open the navigator."));

        //Register events.
        new JoinServer(this, globalSQL, connect);
        leaveServer = new LeaveServer(this, globalSQL, connect);

        new GuiListener(this);
        new PlayerInteract(this);

        //Create regionManager if enabled.
        if (REGIONS_ENABLED) {
            regionManager = new RegionManager(regionSQL);
        }

        moveListener = new MoveListener(this);
        teleportListener = new TeleportListener(this);

        //Setup Timers
        timers = new Timers(this, globalSQL, connect);
        timers.startTimers();

        //Create bungeecord channel
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        //Setup the lobby, most features are only enabled in the lobby server.
        lobby = new Lobby(this);
        //Create the rules book.
        lobby.loadRules();
        //Command to view the rules.
        getCommand("rules").setExecutor(new Rules());
        if (SERVER_TYPE == ServerType.LOBBY) {

            //Set spawn location and enable auto-spawn teleport when falling in the void.
            lobby.setSpawn();
            lobby.enableVoidTeleport();

            lobby.reloadPortals();

            //Create portals reload command.
            getCommand("portals").setExecutor(new Portals(lobby));

            //Set the rules lectern.
            lobby.setLectern();
        }


        //Setup tpll if enabled in config.
        if (TPLL_ENABLED) {

            getCommand("tpll").setExecutor(new Tpll(CONFIG.getBoolean("requires_permission")));
        }

        //Enable commands.
        getCommand("plot").setExecutor(new Plot());
        getCommand("zone").setExecutor(new Zone());

        getCommand("navigator").setExecutor(new Navigator());

        getCommand("staff").setExecutor(new Staff());
        getCommand("ban").setExecutor(new Ban());
        getCommand("mute").setExecutor(new Mute());

        getCommand("server").setExecutor(new Server());
        getCommand("server").setTabCompleter(new ServerSelector());

        getCommand("teleport").setExecutor(new Tp());
        getCommand("teleport").setTabCompleter(new PlayerSelector());
        getCommand("teleporttoggle").setExecutor(new TpToggle());

        getCommand("back").setExecutor(new Back());

        //Modpack will no longer be used.
        //getCommand("modpack").setExecutor(new Modpack());
        getCommand("discord").setExecutor(new Discord());

        getCommand("ll").setExecutor(new ll());

        getCommand("nightvision").setExecutor(new Nightvision());
        getCommand("speed").setExecutor(new Speed());

        getCommand("help").setExecutor(new Help());

        getCommand("warp").setExecutor(new Warp());
        getCommand("warp").setTabCompleter(new LocationSelector());
        getCommand("warps").setExecutor(new Warps());

        getCommand("navigation").setExecutor(new Navigation());

        getCommand("database").setExecutor(new Database());

        getCommand("afk").setExecutor(new AFK());

        getCommand("sethome").setExecutor(new Sethome(globalSQL));
        getCommand("home").setExecutor(new Home(globalSQL));
        getCommand("delhome").setExecutor(new Delhome(globalSQL));

        getCommand("clear").setExecutor(new Clear());
        getCommand("debugstick").setExecutor(new DebugStick());

        getCommand("spawn").setExecutor(new Spawn());

        //Gamemode command.
        new Gamemode(this);

        //Phead command.
        new Phead(this);

        //Register commandpreprocess to make sure /network:region runs and not that of another plugin.
        new CommandPreProcess(this);
        getCommand("region").setExecutor(new RegionCommand());

        //Enable tab.
        tab = new TabManager(this);

        //Enable server in server table.
        globalSQL.update("UPDATE server_data SET online=1 WHERE name='" + SERVER_NAME + "';");

    }

    @Override
    public void onDisable() {

        //Shut down chat.
        if (chat != null) {
            chat.onDisable();
        }

        //Shut down tab.
        if (tab != null) {
            tab.closeTab();
        }

        //Close timers.
        if (timers != null) {
            timers.close();
        }

        if (getUsers() != null) {
            for (NetworkUser u : getUsers()) {

                String uuid = u.player.getUniqueId().toString();

                //Remove any outstanding invites that this player has sent.
                plotSQL.update("DELETE FROM plot_invites WHERE owner='" + uuid + "';");
                plotSQL.update("DELETE FROM zone_invites WHERE owner='" + uuid + "';");

                //Remove any outstanding invites that this player has received.
                plotSQL.update("DELETE FROM plot_invites WHERE uuid='" + uuid + "';");
                plotSQL.update("DELETE FROM zone_invites WHERE uuid='" + uuid + "';");

                //Set last_online time in playerdata.
                instance.globalSQL.update("UPDATE player_data SET last_online=" + Time.currentTime() + " WHERE UUID='" + uuid + "';");

                //Remove player from online_users.
                instance.globalSQL.update("DELETE FROM online_users WHERE uuid='" + uuid + "';");

                //Update tab for all players.
                //This is done with the tab chat channel.
                instance.chat.broadcastMessage(Component.text("remove " + uuid), "uknet:tab");

                //Log playercount in database
                instance.globalSQL.update("INSERT INTO player_count(log_time,players) VALUES(" + Time.currentTime() + "," +
                        instance.globalSQL.getInt("SELECT count(uuid) FROM online_users;") + ");");

                //Reset last logged time.
                if (u.afk) {
                    u.last_time_log = u.last_movement = Time.currentTime();
                    u.afk = false;
                }

                //Update statistics
                long time = Time.currentTime();
                Statistics.save(u, Time.getDate(time), time);

            }
        }

        //Disable bungeecord channel.
        instance.getServer().getMessenger().unregisterOutgoingPluginChannel(instance);

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
        getLogger().info("Database setup complete for " + fileName);
    }

    //Creates the mysql connection.
    private BasicDataSource mysqlSetup(String database) throws SQLException {

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

    //Return an instance of the regionManager.
    public RegionManager getRegionManager() {
        return regionManager;
    }

    //Get user from player.
    public NetworkUser getUser(Player p) {

        for (NetworkUser u : networkUsers) {

            if (u.player.equals(p)) {
                return u;

            }
        }

        return null;
    }

    //Get users.
    public ArrayList<NetworkUser> getUsers() {
        return networkUsers;
    }

    //Add user to list.
    public void addUser(NetworkUser u) {

        networkUsers.add(u);

    }

    //Get user from player.
    public void removeUser(NetworkUser u) {

        networkUsers.remove(u);

    }

    //Check if user is on the server.
    public boolean hasPlayer(String uuid) {
        for (NetworkUser u : getUsers()) {
            if (u.player.getUniqueId().toString().equals(uuid)) {
                return true;
            }
        }
        return false;
    }

    //Get lobby.
    public Lobby getLobby() {
        return lobby;
    }
}
