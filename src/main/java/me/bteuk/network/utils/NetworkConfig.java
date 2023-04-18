package me.bteuk.network.utils;

import me.bteuk.network.Network;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.Map;
import java.util.Objects;

import static me.bteuk.network.utils.Constants.LOGGER;

public class NetworkConfig {

    public static FileConfiguration CONFIG;

    public NetworkConfig() {

        //Create a config instance.
        CONFIG = Network.getInstance().getConfig();

    }

    //Get old config version.
    private String configVersion() {
        String version = Objects.requireNonNull(CONFIG.getString("version"));
        //If null return default.
        return Objects.requireNonNullElse(version, "1.0.0");
    }

    //Get latest config version.
    private String latestVersion() {
        String version = Objects.requireNonNull(CONFIG.getDefaults()).getString("version");
        //If null return default.
        return Objects.requireNonNullElse(version, "1.0.0");
    }

    //Update config if the version is outdated.
    public String updateConfig() {

        String version = configVersion();

        if (!Objects.equals(version, latestVersion())) {
            LOGGER.info("Your config version is outdated, updating to latest version!");

            //Get old config values, these are needed to add them back after updating.
            Map<String, Object> values = CONFIG.getValues(true);

            //Generate a new config file from the default config.
            //Copy any values that can be reused.
            //Delete the current config and set the new one.
            File configFile = new File(Network.getInstance().getDataFolder(), "config.yml");

            if (!configFile.delete()) {

                //Something went wrong.
                LOGGER.warning("The old config file could not be deleted!");
                return version;

            }

            //Copy the default config and get it.
            Network.getInstance().saveDefaultConfig();
            Network.getInstance().reloadConfig();
            CONFIG = Network.getInstance().getConfig();

            for (Map.Entry<String, Object> value : values.entrySet()) {

                if (CONFIG.contains(value.getKey())) {
                    //Skip the version since that needs to be the latest value.
                    if (value.getKey().equals("version")) {
                        continue;
                    }
                    CONFIG.set(value.getKey(), value.getValue());

                }
            }

            Network.getInstance().saveConfig();
            Network.getInstance().reloadConfig();

            CONFIG = Network.getInstance().getConfig();

            LOGGER.info("Updated config to version " + CONFIG.getString("version"));

        } else {
            LOGGER.info("The config is up to date!");
        }

        return version;
    }

    //Update database if the config was outdated, this implies the database is also outdated.
    public void updateDatabase(String oldVersion) {

        //Check for specific table columns that could be missing,
        //All changes have to be tested from 1.0.0.
        //We update 1 version at a time.

        //Convert config version to integer, so we can easily use them.
        int oldVersionInt = getVersionInt(oldVersion);

        //Update sequentially.

        //1.0.0 -> 1.1.0
        if (oldVersionInt <= 1) {
            update1_2();
        }
    }

    private int getVersionInt(String version) {

        switch(version) {

            //1.1.0 = 2
            case "1.1.0" -> {
                return 2;
            }

            //Default is 1.0.0 = 1;
            default -> {
                return 1;
            }

        }

    }

    private void update1_2() {

        LOGGER.info("Updating database from 1.0.0 to 1.1.0");

        //Version 1.1.0.
        //Add skin texture id column.
        Network.getInstance().globalSQL.update("ALTER TABLE player_data ADD COLUMN player_skin TEXT NULL DEFAULT NULL;");

    }
}
