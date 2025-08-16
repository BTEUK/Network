package net.bteuk.network.utils;

import lombok.extern.java.Log;
import net.bteuk.network.Network;
import net.bteuk.network.core.Constants;
import net.bteuk.network.core.ServerType;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Log
public class NetworkConfig {

    public static FileConfiguration CONFIG;

    public NetworkConfig() {

        // Create a config instance.
        CONFIG = Network.getInstance().getConfig();
    }

    // Get old config version.
    private String configVersion() {

        String version = CONFIG.getString("version");
        // If null return default.
        return Objects.requireNonNullElse(version, "1.0.0");
    }

    // Get latest config version.
    private String latestVersion() {
        String version = Objects.requireNonNull(CONFIG.getDefaults()).getString("version");
        // If null return default.
        return Objects.requireNonNullElse(version, "1.8.0");
    }

    // Update config if the version is outdated.
    public void updateConfig() {

        String version = configVersion();

        if (!version.equals(latestVersion())) {
            log.info("Your config version is outdated, updating to latest version!");

            // Get old config values, these are needed to add them back after updating.
            Map<String, Object> values = CONFIG.getValues(true);

            // Generate a new config file from the default config.
            // Copy any values that can be reused.
            // Delete the current config and set the new one.
            File configFile = new File(Network.getInstance().getDataFolder(), "config.yml");

            if (!configFile.delete()) {

                // Something went wrong.
                log.warning("The old config file could not be deleted!");
                return;
            }

            // Copy the default config and get it.
            Network.getInstance().saveDefaultConfig();
            Network.getInstance().reloadConfig();
            CONFIG = Network.getInstance().getConfig();

            for (Map.Entry<String, Object> value : values.entrySet()) {

                if (CONFIG.contains(value.getKey())) {

                    // Check if this is a configuration section, if true skip.
                    if (CONFIG.isConfigurationSection(value.getKey())) {
                        continue;
                    }

                    // Skip the version since that needs to be the latest value.
                    if (value.getKey().equals("version")) {
                        continue;
                    }
                    CONFIG.set(value.getKey(), value.getValue());
                }
            }

            Network.getInstance().saveConfig();

            CONFIG = Network.getInstance().getConfig();

            log.info("Updated config to version " + CONFIG.getString("version"));
        } else {
            log.info("The config is up to date!");
        }
    }

    public net.bteuk.network.core.Constants getConstants() {
        // Set the server name from config.
        String serverName = CONFIG.getString("server_name");

        // Set the server type from config.
        ServerType serverType = ServerType.valueOf(CONFIG.getString("server_type"));

        // Basically indicates that this server is not running in a network.
        boolean standalone = CONFIG.getBoolean("standalone");

        boolean regionsEnabled = CONFIG.getBoolean("regions.enabled");

        // days * 24 hours * 60 minutes * 60 seconds * 1000 milliseconds
        long regionInactivity = CONFIG.getInt("region.inactivity_days") * 24L * 60L * 60L * 1000L;

        boolean tpllEnabled = CONFIG.getBoolean("tpll.enabled");

        int maxY = CONFIG.getInt("tpll.max_y");
        int minY = CONFIG.getInt("tpll.min_y");

        boolean staffChat = CONFIG.getBoolean("staff.chat.enabled");

        boolean tips = CONFIG.getBoolean("chat.tips.enabled");

        boolean tutorials = CONFIG.getBoolean("tutorials.enabled");

        boolean llEnabled = CONFIG.getBoolean("ll_enabled");

        boolean progressMap = CONFIG.getBoolean("ProgressMap.enabled");

        boolean progression = CONFIG.getBoolean("progression.enabled");
        boolean announceOverallLevelUps = CONFIG.getBoolean("progression.announce_level-ups.overall");
        boolean announceSeasonalLevelUps = CONFIG.getBoolean("progression.announce_level-ups.seasonal");

        boolean sidebarEnabled = CONFIG.getBoolean("sidebar.enabled");
        String sidebarTitle = CONFIG.getString("sidebar.title", "");

        List<?> sidebarTextConfig = CONFIG.getList("sidebar.text");
        List<String> sidebarText = new ArrayList<>();
        if (sidebarTextConfig != null && !sidebarTextConfig.isEmpty()) {
            sidebarTextConfig.forEach(listItem -> {
                if (listItem instanceof String listTextItem) {
                    sidebarText.add(listTextItem);
                }
            });
        }

        List<String> sidebarTextList = Collections.unmodifiableList(sidebarText);

        boolean motdEnabled = CONFIG.getBoolean("motd.enabled");
        String motdText = CONFIG.getString("motd.text", "");

        String earthWorld;
        if (CONFIG.getString("regions.earth_world") == null) {
            // Setting default value.
            earthWorld = "earth";
        } else {
            earthWorld = CONFIG.getString("regions.earth_world");
        }

        boolean plotSystemEnabled = CONFIG.getBoolean("plot_system.enabled");

        return new Constants(serverName, serverType, standalone, regionsEnabled, regionInactivity, tpllEnabled, maxY, minY, earthWorld, staffChat, tips,
                tutorials, llEnabled, progressMap, progression, announceOverallLevelUps, announceSeasonalLevelUps, sidebarEnabled, sidebarTitle, sidebarTextList, motdEnabled,
                motdText, plotSystemEnabled);
    }
}
