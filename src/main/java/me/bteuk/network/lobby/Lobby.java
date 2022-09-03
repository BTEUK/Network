package me.bteuk.network.lobby;

/*
This class will handle the functionality of the lobby that are exclusive to the lobby server.

Features of the lobby include but are not limited to:
    Interactive Leaderboards
    Interactive Map
    Portals

The reason the lobby functions have been separated is to prevent unnecessary resource usage on the non-lobby server.
 */

import me.bteuk.network.Network;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

public class Lobby {

    private final Network instance;

    private final ArrayList<Portal> portals;
    private File portalsFile;

    public Lobby(Network instance) {

        this.instance = instance;

    }

    //A public method to reload portals from the portals.yml file.
    //This method is run when using the specific reload command.
    //It saves having to restart the server when portals are edited.
    public void reloadPortals() {

    }

    //Reads the portals from portals.yml
    private void loadPortals() {

        //Create portals.yml if not exists.
        //The data folder should already exist since the plugin will always create config.yml first.
        portalsFile = new File(instance.getDataFolder(), "portals.yml");
        if (!portalsFile.exists()) {
            instance.saveResource("portals.yml", false);
        }

        FileConfiguration portalsConfig = YamlConfiguration.loadConfiguration(portalsFile);

        //Gets all the portal names from the config.
        //This will allow us to query the config for portals.
        Set<String> portals = portalsConfig.getConfigurationSection("portals").getKeys(false);

        //Create the portal from the config.

    }

    private void runPortals() {

    }

    private void runLeaderboards() {

    }

    private void runMap() {

    }
}
