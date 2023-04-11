package me.bteuk.network.utils;

import me.bteuk.network.Network;
import org.bukkit.configuration.file.FileConfiguration;

import static me.bteuk.network.utils.Constants.LOGGER;

public class NetworkConfig {

    private final FileConfiguration config;

    public NetworkConfig() {

        //Create a config instance.
        config = Network.getInstance().getConfig();

    }

    //Getter
    public FileConfiguration getConfig() {
        return config;
    }

    //Check for latest config version.
    private boolean latestConfigVersion() {
        return (config.getString("version").equals(config.getDefaults().getString("version")));
    }

    //Update config if the version is outdated.
    public void updateConfig() {
        if (!latestConfigVersion()) {
            LOGGER.info("Your config version is outdated, updating to latest version!");

            //Update config

        } else {
            LOGGER.info("The config is up to date!");
        }
    }
}
