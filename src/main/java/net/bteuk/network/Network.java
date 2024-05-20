package net.bteuk.network;

import lombok.Getter;
import net.bteuk.network.commands.AFK;
import net.bteuk.network.commands.BuildingCompanionCommand;
import net.bteuk.network.commands.Clear;
import net.bteuk.network.commands.Discord;
import net.bteuk.network.commands.Gamemode;
import net.bteuk.network.lobby.LobbyCommand;
import net.bteuk.network.commands.Ptime;
import net.bteuk.network.commands.Season;
import net.bteuk.network.commands.give.GiveBarrier;
import net.bteuk.network.commands.give.GiveDebugStick;
import net.bteuk.network.commands.give.GiveLight;
import net.bteuk.network.commands.Hdb;
import net.bteuk.network.commands.Help;
import net.bteuk.network.commands.Navigator;
import net.bteuk.network.commands.Nightvision;
import net.bteuk.network.commands.Phead;
import net.bteuk.network.commands.Plot;
import net.bteuk.network.commands.RegionCommand;
import net.bteuk.network.commands.Rules;
import net.bteuk.network.commands.Speed;
import net.bteuk.network.commands.TipsToggle;
import net.bteuk.network.commands.Zone;
import net.bteuk.network.commands.ll;
import net.bteuk.network.commands.ProgressMap;
import net.bteuk.network.commands.navigation.Back;
import net.bteuk.network.commands.navigation.Delhome;
import net.bteuk.network.commands.navigation.Home;
import net.bteuk.network.commands.navigation.Homes;
import net.bteuk.network.commands.navigation.Navigation;
import net.bteuk.network.commands.navigation.Server;
import net.bteuk.network.commands.navigation.Sethome;
import net.bteuk.network.commands.navigation.Spawn;
import net.bteuk.network.commands.navigation.Tp;
import net.bteuk.network.commands.navigation.TpToggle;
import net.bteuk.network.commands.navigation.Tpll;
import net.bteuk.network.commands.navigation.Warp;
import net.bteuk.network.commands.navigation.Warps;
import net.bteuk.network.commands.staff.Ban;
import net.bteuk.network.commands.staff.Exp;
import net.bteuk.network.commands.staff.Kick;
import net.bteuk.network.commands.staff.Mute;
import net.bteuk.network.commands.staff.Staff;
import net.bteuk.network.commands.staff.Unban;
import net.bteuk.network.commands.staff.Unmute;
import net.bteuk.network.eventing.listeners.CommandPreProcess;
import net.bteuk.network.eventing.listeners.Connect;
import net.bteuk.network.eventing.listeners.GuiListener;
import net.bteuk.network.eventing.listeners.PlayerInteract;
import net.bteuk.network.eventing.listeners.PreJoinServer;
import net.bteuk.network.sql.DatabaseInit;
import net.bteuk.network.commands.tabcompleters.LocationSelector;
import net.bteuk.network.commands.tabcompleters.PlayerSelector;
import net.bteuk.network.commands.tabcompleters.ServerSelector;
import net.bteuk.network.gui.NavigatorGui;
import net.bteuk.network.eventing.listeners.global_teleport.MoveListener;
import net.bteuk.network.eventing.listeners.global_teleport.TeleportListener;
import net.bteuk.network.lobby.Lobby;
import net.bteuk.network.sql.DatabaseUpdates;
import net.bteuk.network.sql.GlobalSQL;
import net.bteuk.network.sql.PlotSQL;
import net.bteuk.network.sql.RegionSQL;
import net.bteuk.network.utils.NetworkConfig;
import net.bteuk.network.utils.NetworkUser;
import net.bteuk.network.utils.Statistics;
import net.bteuk.network.utils.Time;
import net.bteuk.network.utils.Tips;
import net.bteuk.network.utils.Utils;
import net.bteuk.network.sql.Tutorials;
import net.bteuk.network.utils.enums.ServerType;
import net.bteuk.network.utils.regions.RegionManager;
import net.kyori.adventure.text.Component;
import org.apache.commons.dbcp2.BasicDataSource;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.ArrayList;

import static net.bteuk.network.utils.Constants.REGIONS_ENABLED;
import static net.bteuk.network.utils.Constants.SERVER_NAME;
import static net.bteuk.network.utils.Constants.SERVER_TYPE;
import static net.bteuk.network.utils.Constants.TAB;
import static net.bteuk.network.utils.Constants.TIPS;
import static net.bteuk.network.utils.Constants.TPLL_ENABLED;
import static net.bteuk.network.utils.Constants.TUTORIALS;
import static net.bteuk.network.utils.Constants.PROGRESS_MAP;
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

    //User List
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

    //Tutorials
    @Getter
    private Tutorials tutorials;

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

            DatabaseInit init = new DatabaseInit();
            boolean success;

            //Global Database
            String global_database = CONFIG.getString("database.global");
            BasicDataSource global_dataSource = init.mysqlSetup(global_database);
            globalSQL = new GlobalSQL(global_dataSource);
            success = init.initDb(getClassLoader(), "dbsetup_global.sql", global_dataSource);

            //Region Database
            String region_database = CONFIG.getString("database.region");
            BasicDataSource region_dataSource = init.mysqlSetup(region_database);
            regionSQL = new RegionSQL(region_dataSource);
            success = success && init.initDb(getClassLoader(), "dbsetup_regions.sql", region_dataSource);

            //Plot Database
            String plot_database = CONFIG.getString("database.plot");
            BasicDataSource plot_dataSource = init.mysqlSetup(plot_database);
            plotSQL = new PlotSQL(plot_dataSource);
            success = success && init.initDb(getClassLoader(), "dbsetup_plots.sql", plot_dataSource);

            if (!success) {
                throw new RuntimeException("Error in database setup!");
            }

        } catch (SQLException | RuntimeException e) {
            getLogger().severe("Failed to connect to the database, please check that you have set the config values correctly.");
            getLogger().severe("Disabling Network");
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

        //Enabled chat, both global and normal chat are handled through this.
        chat = new CustomChat(this);

        //Setup connect, this handles all connections to the server.
        connect = new Connect(this, globalSQL, plotSQL, regionSQL);

        //Enable the tutorial if enabled.
        if (TUTORIALS) {
            tutorials = new Tutorials();
        }

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

            //Setup the map.
            lobby.reloadMap();

            //Set the rules lectern.
            lobby.setLectern();
        }

        //Create lobby command, will run /spawn if not in the lobby server.
        new LobbyCommand(this, lobby);


        //Setup tpll if enabled in config.
        if (TPLL_ENABLED) {

            tpll = new Tpll(this, CONFIG.getBoolean("requires_permission"));

        }

        //Enable commands.
        new Plot(this);
        getCommand("zone").setExecutor(new Zone());

        getCommand("navigator").setExecutor(new Navigator());

        getCommand("server").setExecutor(new Server());
        getCommand("server").setTabCompleter(new ServerSelector());

        getCommand("teleport").setExecutor(new Tp());
        getCommand("teleport").setTabCompleter(new PlayerSelector());
        getCommand("teleporttoggle").setExecutor(new TpToggle());

        getCommand("back").setExecutor(new Back());

        getCommand("discord").setExecutor(new Discord());

        new ll(this);

        getCommand("nightvision").setExecutor(new Nightvision());
        getCommand("speed").setExecutor(new Speed());

        getCommand("help").setExecutor(new Help());

        getCommand("warp").setExecutor(new Warp());
        getCommand("warp").setTabCompleter(new LocationSelector());
        getCommand("warps").setExecutor(new Warps());

        new Navigation(this);

        getCommand("afk").setExecutor(new AFK());

        getCommand("clear").setExecutor(new Clear());

        new GiveDebugStick(this);
        new GiveLight(this);
        new GiveBarrier(this);

        getCommand("spawn").setExecutor(new Spawn());

        //Enabled the progress map command if enabled
        if (PROGRESS_MAP) {
            getCommand("progressmap").setExecutor(new ProgressMap());
            getLogger().info("Enabled Progress map support");
        }

        //Gamemode command.
        new Gamemode(this);

        //Phead command.
        new Phead(this);

        //Homes commands.
        if (CONFIG.getBoolean("homes.enabled")) {
            new Sethome(this, globalSQL);
            new Home(this, globalSQL);
            new Delhome(this, globalSQL);
            new Homes(this);
        }

        //Staff command to open the staff gui and use staff chat.
        new Staff(this);

        //Moderation commands.
        if (CONFIG.getBoolean("staff.moderation.enabled")) {

            ban = new Ban(this);
            unban = new Unban(this);

            mute = new Mute(this);
            unmute = new Unmute(this);

            kick = new Kick(this);

        }

        //Enable ptime.
        new Ptime(this);

        //Route /hdb to /skulls
        new Hdb(this);

        //Register commandpreprocess to make sure /network:region runs and not that of another plugin.
        new CommandPreProcess(this);
        getCommand("region").setExecutor(new RegionCommand());

        //Enable tab.
        if (TAB) {
            tab = new TabManager(this);
        }

        //Enable tips.
        if (TIPS) {

            //Enable the tips command.
            new TipsToggle(this);

            //Enable tips in chat.
            new Tips();
        }

        new Season(this);
        //Create default season if not exists.
        if (!globalSQL.hasRow("SELECT id FROM seasons WHERE id='default';")) {
            globalSQL.update("INSERT INTO seasons(id,active) VALUES('default',1);");
        }

        new Exp(this);

        new BuildingCompanionCommand(this);

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

                if (TAB) {
                    //Update tab for all players.
                    //This is done with the tab chat channel.
                    instance.chat.broadcastMessage(Component.text("remove " + uuid), "uknet:tab");
                }

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
}
