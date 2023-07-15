package me.bteuk.network.utils;

import me.bteuk.network.Network;
import me.bteuk.network.utils.enums.ServerType;

import java.util.logging.Logger;

import static me.bteuk.network.utils.NetworkConfig.CONFIG;

//This class houses static variables that need to be accessed throughout the plugin.
public class Constants {

    //Logger
    public static final Logger LOGGER;

    //Server Name
    public static final String SERVER_NAME;
    public static final ServerType SERVER_TYPE;

    //Regions.
    public static final boolean REGIONS_ENABLED;

    //Tpll
    public static final boolean TPLL_ENABLED;

    //World height.
    public static final int MAX_Y;
    public static final int MIN_Y;

    //Earth world name.
    public static final String EARTH_WORLD;

    //Is global chat enabled
    public static final boolean GLOBAL_CHAT;

    //Custom join/leave messages.
    public static final boolean CUSTOM_MESSAGES;

    //Is staff chat enabled
    public static final boolean STAFF_CHAT;

    //Custom table
    public static final boolean TAB;

    //Discord chat compatibility
    public static final boolean DISCORD_CHAT;

    //Discord role linking
    public static final boolean DISCORD_LINKING;
    
    //Are tips enabled
    public static final boolean TIPS;

    //Is the tutorial enabled
    public static final boolean TUTORIALS;

    static {

        LOGGER = Network.getInstance().getLogger();

        //Set the server name from config.
        SERVER_NAME = CONFIG.getString("server_name");

        //Set the server type from config.
        SERVER_TYPE = ServerType.valueOf(CONFIG.getString("server_type"));

        REGIONS_ENABLED = CONFIG.getBoolean("regions_enabled");

        TPLL_ENABLED = CONFIG.getBoolean("tpll.enabled");

        MAX_Y = CONFIG.getInt("tpll.max_y");
        MIN_Y = CONFIG.getInt("tpll.min_y");

        GLOBAL_CHAT = CONFIG.getBoolean("chat.global_chat.enabled");
        CUSTOM_MESSAGES = CONFIG.getBoolean("chat.custom_messages.enabled");

        STAFF_CHAT = CONFIG.getBoolean("staff.staff_chat.enabled");

        TAB = CONFIG.getBoolean("chat.global_chat.tab.enabled");

        DISCORD_CHAT = CONFIG.getBoolean("chat.global_chat.discord.chat.enabled");
        DISCORD_LINKING = CONFIG.getBoolean("chat.global_chat.discord.linking.enabled");

        TIPS = CONFIG.getBoolean("chat.tips.enabled");

        TUTORIALS = CONFIG.getBoolean("tutorials.enabled");

        if (CONFIG.getString("earth_world") == null) {
            //Setting default value.
            EARTH_WORLD = "earth";
        } else {
            EARTH_WORLD = CONFIG.getString("earth_world");
        }
    }
}
