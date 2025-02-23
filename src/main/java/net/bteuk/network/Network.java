package net.bteuk.network;

import lombok.Getter;
import net.bteuk.network.commands.navigation.Tpll;
import net.bteuk.network.commands.staff.Ban;
import net.bteuk.network.commands.staff.Kick;
import net.bteuk.network.commands.staff.Mute;
import net.bteuk.network.commands.staff.Unban;
import net.bteuk.network.commands.staff.Unmute;
import net.bteuk.network.eventing.listeners.CommandPreProcess;
import net.bteuk.network.eventing.listeners.Connect;
import net.bteuk.network.eventing.listeners.GuiListener;
import net.bteuk.network.eventing.listeners.PlayerInteract;
import net.bteuk.network.eventing.listeners.PreJoinServer;
import net.bteuk.network.eventing.listeners.global_teleport.MoveListener;
import net.bteuk.network.eventing.listeners.global_teleport.TeleportListener;
import net.bteuk.network.gui.NavigatorGui;
import net.bteuk.network.lib.dto.OnlineUser;
import net.bteuk.network.lib.dto.OnlineUserAdd;
import net.bteuk.network.lib.dto.OnlineUserRemove;
import net.bteuk.network.lib.dto.OnlineUsersReply;
import net.bteuk.network.lib.dto.ServerStartup;
import net.bteuk.network.lobby.Lobby;
import net.bteuk.network.services.NetworkPromotionService;
import net.bteuk.network.sql.DatabaseInit;
import net.bteuk.network.sql.GlobalSQL;
import net.bteuk.network.sql.PlotSQL;
import net.bteuk.network.sql.RegionSQL;
import net.bteuk.network.utils.NetworkConfig;
import net.bteuk.network.utils.NetworkUser;
import net.bteuk.network.utils.Time;
import net.bteuk.network.utils.Tips;
import net.bteuk.network.utils.Utils;
import net.bteuk.network.utils.enums.ServerType;
import net.bteuk.network.utils.regions.RegionManager;
import net.bteuk.teachingtutorials.services.PromotionService;
import net.buildtheearth.terraminusminus.TerraConfig;
import org.apache.commons.dbcp2.BasicDataSource;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import teachingtutorials.utils.DBConnection;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;

import static net.bteuk.network.utils.Constants.LOGGER;
import static net.bteuk.network.utils.Constants.REGIONS_ENABLED;
import static net.bteuk.network.utils.Constants.SERVER_NAME;
import static net.bteuk.network.utils.Constants.SERVER_TYPE;
import static net.bteuk.network.utils.Constants.TIPS;
import static net.bteuk.network.utils.Constants.TPLL_ENABLED;
import static net.bteuk.network.utils.Constants.TUTORIALS;
import static net.bteuk.network.utils.NetworkConfig.CONFIG;

public final class Network extends JavaPlugin {

    //Returns an instance of the plugin.
    @Getter
    private static Network instance;

    //If the server can shutdown.
    public boolean allow_shutdown;

    //Return an instance of the regionManager.
    //RegionManager
    @Getter
    private RegionManager regionManager;

    // List of users connected to the network.
    @Getter
    private HashSet<OnlineUser> onlineUsers;

    // Server User List
    private ArrayList<NetworkUser> networkUsers;

    //Guis
    public NavigatorGui navigatorGui;
    public ItemStack navigator;

    //SQL
    @Getter
    private PlotSQL plotSQL;
    @Getter
    private GlobalSQL globalSQL;
    public RegionSQL regionSQL;

    //Chat
    @Getter
    private CustomChat chat;

    //Timers
    @Getter
    private Timers timers;

    //Get lobby.
    //Lobby
    @Getter
    private Lobby lobby;

    //Listener and manager of server connects.
    @Getter
    private Connect connect;

    //Movement listeners.
    public MoveListener moveListener;
    public TeleportListener teleportListener;

    //Tab
    @Getter
    private TabManager tab;

    //Kick Command
    @Getter
    private Kick kick;

    //Mute Command
    @Getter
    private Mute mute;

    //Unmute Command
    @Getter
    private Unmute unmute;

    //Ban Command
    @Getter
    private Ban ban;

    //Unban Command
    @Getter
    private Unban unban;

    //Tpll Command
    @Getter
    private Tpll tpll;

    //Tutorials DB connection
    @Getter
    private DBConnection tutorialsDBConnection;

    @Override
    public void onEnable() {

        //Config Setup
        Network.instance = this;

        allow_shutdown = true;

        //Sets the config if the file has not yet been created.
        ConfigurationSerialization.registerClass(ConfigurationSerializable.class);
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

            DatabaseInit init = new DatabaseInit();

            //Global Database
            String global_database = CONFIG.getString("database.global");
            BasicDataSource global_dataSource = init.mysqlSetup(global_database);
            globalSQL = new GlobalSQL(global_dataSource);

            //Region Database
            String region_database = CONFIG.getString("database.region");
            BasicDataSource region_dataSource = init.mysqlSetup(region_database);
            regionSQL = new RegionSQL(region_dataSource);

            //Plot Database
            String plot_database = CONFIG.getString("database.plot");
            BasicDataSource plot_dataSource = init.mysqlSetup(plot_database);
            plotSQL = new PlotSQL(plot_dataSource);


        } catch (SQLException | RuntimeException e) {
            getLogger().severe("Failed to connect to the database, please check that you have set the config values correctly.");
            getLogger().severe("Disabling Network");
            return;
        }

        //Setup tutorials DB connection and connect
        if (TUTORIALS) {
            //Initialise the DBConnection object
            tutorialsDBConnection = new DBConnection();

            //Extract database details from the config
            String szHost = CONFIG.getString("tutorials.database.host");
            int iPort = CONFIG.getInt("tutorials.database.port");
            String szDBName = CONFIG.getString("tutorials.database.name");
            String szUsername = CONFIG.getString("tutorials.database.username");
            String szPassword = CONFIG.getString("tutorials.database.password");

            //Set up the DBConnection object with details
            tutorialsDBConnection.externalMySQLSetup(szHost, iPort, szDBName, szUsername, szPassword);

            //Attempt to connect to the DB
            if (!tutorialsDBConnection.connect()) {
                getLogger().severe("Failed to connect to the Tutorials database, please check that you have set the config values correctly.");
                getLogger().severe("Disabling Network");
                return;
            }
        }

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

        // Create user list.
        networkUsers = new ArrayList<>();
        onlineUsers = new HashSet<>();

        //Enable tab.
        tab = new TabManager(this);

        // Enabled chat, both global and normal chat are handled through this.
        chat = new CustomChat(this);

        //Setup connect, this handles all connections to the server.
        connect = new Connect(this);

        //Create navigator.
        navigatorGui = new NavigatorGui();
        navigator = Utils.createItem(Material.NETHER_STAR, 1, Utils.title("Navigator"), Utils.line("Click to open the navigator."));

        //Register events.
        new PreJoinServer(this);

        new GuiListener(this);
        new PlayerInteract(this);

        //Create regionManager if enabled.
        if (REGIONS_ENABLED) {
            regionManager = new RegionManager(regionSQL);
        }

        moveListener = new MoveListener(this);
        teleportListener = new TeleportListener(this);

        //Setup Timers
        timers = new Timers(this, globalSQL);
        timers.startTimers();

        //Setup the lobby, most features are only enabled in the lobby server.
        lobby = new Lobby(this);
        //Create the rules book.
        lobby.loadRules();
        if (SERVER_TYPE == ServerType.LOBBY) {

            // Set spawn location and enable auto-spawn teleport when falling in the void.
            lobby.setSpawn();
            lobby.enableVoidTeleport();

            lobby.reloadPortals();

            //Set the rules lectern.
            lobby.setLectern();
        }

        // Set up the map.
        lobby.reloadMap();

        //Enable commands
        if (TPLL_ENABLED) {
            TerraConfig.reducedConsoleMessages = true;
            tpll = new Tpll(instance, CONFIG.getBoolean("requires_permission"));
        }
        kick = new Kick();
        mute = new Mute();
        unmute = new Unmute();
        ban = new Ban();
        unban = new Unban();
        CommandManager.registerCommands(this);

        //Register commandpreprocess to make sure /network:region runs and not that of another plugin.
        new CommandPreProcess(this);

        //Enable tips.
        if (TIPS) {
            //Enable tips in chat.
            new Tips();
        }

        //Create default season if not exists.
        if (!globalSQL.hasRow("SELECT id FROM seasons WHERE id='default';")) {
            globalSQL.update("INSERT INTO seasons(id,active) VALUES('default',1);");
        }

        // Register Promotion Service.
        try {
            Class.forName("net.bteuk.teachingtutorials.services.PromotionService");
            PromotionService promotionService = new NetworkPromotionService();
            this.getServer().getServicesManager().register(PromotionService.class, promotionService, this, ServicePriority.High);
            LOGGER.info("Registered Network Promotion Service");
        } catch (ClassNotFoundException e) {
            // Only load the PromotionService is the class exists.
        }

        // Let the Proxy know that the server is enabled.
        instance.getChat().sendSocketMesage(new ServerStartup(SERVER_NAME));

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

                //Reset last logged time.
                if (u.afk) {
                    u.last_movement = Time.currentTime();
                    u.afk = false;
                }
            }
        }

        //Disconnect from tutorials
        if (TUTORIALS) {
            tutorialsDBConnection.disconnect();
        }
    }

    //Get user from player.
    public NetworkUser getUser(Player p) {
        return networkUsers.stream().filter(user -> user.player.equals(p)).findFirst().orElse(null);
    }

    public Optional<NetworkUser> getNetworkUserByUuid(String uuid) {
        return networkUsers.stream().filter(user -> user.player.getUniqueId().toString().equals(uuid)).findFirst();
    }

    //Get users.
    public ArrayList<NetworkUser> getUsers() {
        return networkUsers;
    }

    //Add user to list.
    public void addUser(NetworkUser u) {

        networkUsers.add(u);

    }

    public void removeUser(NetworkUser u) {
        networkUsers.remove(u);
    }

    public void handleOnlineUsersReply(OnlineUsersReply onlineUsersReply) {
        onlineUsers.addAll(onlineUsersReply.getOnlineUsers());
    }

    public void handleOnlineUserAdd(OnlineUserAdd onlineUserAdd) {
        onlineUsers.remove(onlineUserAdd.getUser());
        onlineUsers.add(onlineUserAdd.getUser());
    }

    public void handleOnlineUserRemove(OnlineUserRemove onlineUserRemove) {
        Optional<OnlineUser> optionalOnlineUser = onlineUsers.stream().filter(onlineUser -> onlineUser.getUuid().equals(onlineUserRemove.getUuid())).findFirst();
        optionalOnlineUser.ifPresent(onlineUser -> onlineUsers.remove(onlineUser));
    }

    public boolean isOnlineOnNetwork(String uuid) {
        return onlineUsers.stream().anyMatch(onlineUser -> onlineUser.getUuid().equals(uuid));
    }

    public Optional<OnlineUser> getOnlineUserByUuid(String uuid) {
        return onlineUsers.stream().filter(onlineUser -> onlineUser.getUuid().equals(uuid)).findFirst();
    }

    public Optional<OnlineUser> getOnlineUserByNameIgnoreCase(String name) {
        return onlineUsers.stream().filter(onlineUser -> onlineUser.getName().equalsIgnoreCase(name)).findFirst();
    }

    // Check if user is on this server.
    public boolean hasPlayer(String uuid) {
        for (NetworkUser u : getUsers()) {
            if (u.player.getUniqueId().toString().equals(uuid)) {
                return true;
            }
        }
        return false;
    }
}
