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

        if (CONFIG.getString("earth_world") == null) {
            //Setting default value.
            EARTH_WORLD = "earth";
        } else {
            EARTH_WORLD = CONFIG.getString("earth_world");
        }
    }
}
