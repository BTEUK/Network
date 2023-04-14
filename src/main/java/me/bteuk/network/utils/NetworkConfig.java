package me.bteuk.network.utils;

import me.bteuk.network.Network;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.Objects;
import java.util.Set;

import static me.bteuk.network.utils.Constants.LOGGER;

public class NetworkConfig {

    public static FileConfiguration CONFIG;

    public NetworkConfig() {

        //Create a config instance.
        CONFIG = Network.getInstance().getConfig();

    }

    //Check for latest config version.
    private boolean latestConfigVersion() {
        return (Objects.equals(CONFIG.getString("version"), Objects.requireNonNull(CONFIG.getDefaults()).getString("version")));
    }

    //Update config if the version is outdated.
    public void updateConfig() {
        if (!latestConfigVersion()) {
            LOGGER.info("Your config version is outdated, updating to latest version!");

            //Generate a new config file from the default config.
            //Copy any values that can be reused.
            //Delete the current config and set the new one.
            File configFile = new File(Network.getInstance().getDataFolder(), "config.yml");

            if (!configFile.delete()) {

                //Something went wrong.
                LOGGER.warning("The old config file could not be deleted!");
                return;

            }

            //Copy the default config and get it.
            Network.getInstance().saveDefaultConfig();
            FileConfiguration newConfig = Network.getInstance().getConfig();

            //Iterate through the old config and add any values that are reusable.
            Set<String> keys = CONFIG.getKeys(true);
            for (String key : keys) {

                if (newConfig.contains(key)) {
                    newConfig.set(key, CONFIG.get(key));
                }
            }

            //Set the new config.
            CONFIG = newConfig;

            //Update the database.
            updateDatabase();

            LOGGER.info("Updated config to version " + newConfig.getString("version"));

        } else {
            LOGGER.info("The config is up to date!");
        }
    }

    //Update database if the config was outdated, this implies the database is also outdated.
    private void updateDatabase() {

        //Check for specific table columns that could be missing,
        //All changes have to be tested from 1.0.0.

        //Version 1.1.0.
        //Add skin texture id column.
        Network.getInstance().globalSQL.update("ALTER TABLE player_data ADD COLUMN player_skin TEXT NULL DEFAULT NULL;");

    }

    //Reload the config.
    public void reloadConfig() {
        CONFIG = Network.getInstance().getConfig();
    }
}
