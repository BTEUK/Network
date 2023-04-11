package me.bteuk.network.utils;

import me.bteuk.network.Network;
import me.bteuk.network.utils.enums.ServerType;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.logging.Logger;

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

    static {

        LOGGER = Network.getInstance().getLogger();

        //Get the config, this is used to set the static variables.
        FileConfiguration config = Network.getInstance().getConfig();

        //Set the server name from config.
        SERVER_NAME = config.getString("server_name");

        //Set the server type from config.
        SERVER_TYPE = ServerType.valueOf(config.getString("server_type"));

        REGIONS_ENABLED = config.getBoolean("regions_enabled");

        TPLL_ENABLED = config.getBoolean("tpll.enabled");

    }
}
