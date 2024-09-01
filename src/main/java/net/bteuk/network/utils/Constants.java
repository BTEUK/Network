package net.bteuk.network.utils;

import net.bteuk.network.Network;
import net.bteuk.network.utils.enums.ServerType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import static net.bteuk.network.utils.NetworkConfig.CONFIG;

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

    //Custom join/leave messages.
    public static final boolean CUSTOM_MESSAGES;

    //Is staff chat enabled
    public static final boolean STAFF_CHAT;
    
    //Are tips enabled
    public static final boolean TIPS;

    //Is the tutorial enabled
    public static final boolean TUTORIALS;

    //Is /ll enabled
    public static final boolean LL;

    //Is the progress map enabled
    public static final boolean PROGRESS_MAP;

    //Is progression enabled
    public static final boolean PROGRESSION;
    public static final boolean ANNOUNCE_OVERALL_LEVELUPS;
    public static final boolean ANNOUNCE_SEASONAL_LEVELUPS;

    public static final boolean SIDEBAR_ENABLED;
    public static final String SIDEBAR_TITLE;
    public static final List<String> SIDEBAR_CONTENT;

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

        CUSTOM_MESSAGES = CONFIG.getBoolean("chat.custom_messages.enabled");

        STAFF_CHAT = CONFIG.getBoolean("staff.staff_chat.enabled");

        TIPS = CONFIG.getBoolean("chat.tips.enabled");

        TUTORIALS = CONFIG.getBoolean("tutorials.enabled");

        LL = CONFIG.getBoolean("ll_enabled");

        PROGRESS_MAP = CONFIG.getBoolean("ProgressMap.enabled");

        PROGRESSION = CONFIG.getBoolean("progression.enabled");
        ANNOUNCE_OVERALL_LEVELUPS = CONFIG.getBoolean("progression.announce_level-ups.overall");
        ANNOUNCE_SEASONAL_LEVELUPS = CONFIG.getBoolean("progression.announce_level-ups.seasonal");

        SIDEBAR_ENABLED = CONFIG.getBoolean("sidebar.enabled");
        SIDEBAR_TITLE = CONFIG.getString("sidebar.title", "");

        List<?> sidebarTextConfig = CONFIG.getList("sidebar.text");
        List<String> sidebarText = new ArrayList<>();
        if (sidebarTextConfig != null && !sidebarTextConfig.isEmpty()) {
            sidebarTextConfig.forEach(listItem -> {
                if (listItem instanceof String listTextItem) {
                    sidebarText.add(listTextItem);
                }
            });
        }

        SIDEBAR_CONTENT = Collections.unmodifiableList(sidebarText);

        if (CONFIG.getString("earth_world") == null) {
            //Setting default value.
            EARTH_WORLD = "earth";
        } else {
            EARTH_WORLD = CONFIG.getString("earth_world");
        }
    }
}
