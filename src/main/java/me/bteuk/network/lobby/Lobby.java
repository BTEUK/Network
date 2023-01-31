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
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;

public class Lobby {

    private final Network instance;

    private final ArrayList<Portal> portals;
    private File portalsFile;
    private int portalTask;

    public Lobby(Network instance) {

        this.instance = instance;
        portals = new ArrayList<>();

    }

    //A public method to reload portals from the portals.yml file.
    //This method is run when using the specific reload command.
    //It saves having to restart the server when portals are edited.
    public void reloadPortals() {

        //Clear the portals arrayList and stop the portals from running.
        if (portalTask != 0) {
            Bukkit.getScheduler().cancelTask(portalTask);
        }
        portals.clear();

        //Load the portals from config.
        loadPortals();

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
        Set<String> portalNames = portalsConfig.getConfigurationSection("portals").getKeys(false);

        //Create the portal from the config.
        for (String portalName: portalNames) {

            //Create new portal with given values from config.
            try {

                portals.add(new Portal(
                        portalsConfig.getInt("portals." + portalName + ".min.x"),
                        portalsConfig.getInt("portals." + portalName + ".min.y"),
                        portalsConfig.getInt("portals." + portalName + ".min.z"),
                        portalsConfig.getInt("portals." + portalName + ".max.x"),
                        portalsConfig.getInt("portals." + portalName + ".max.y"),
                        portalsConfig.getInt("portals." + portalName + ".max.z"),
                        portalsConfig.getString("portals." + portalName + ".executes").split(",")
                ));

            } catch (Exception e) {
                Network.getInstance().getLogger().warning("Portal " + portalName + " configured incorrectly, please check the portals.yml file.");
            }
        }

        //Once the portals have been loaded, start running them.
        runPortals();
    }

    //Runs the events set to the portals.
    //Events will run each second in order of how they are in the portals.yml file.
    private void runPortals() {

        portalTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(Network.getInstance(), () -> {

            for (Portal portal: portals) {

                //Check if any players are in the area of the portal.
                for (Player p: Bukkit.getOnlinePlayers()) {
                    if (portal.in(p.getLocation())) {

                        //If they are run the portal events.
                        portal.event(p);

                    }
                }
            }

        }, 0L, 20L);
    }

    private void runLeaderboards() {

    }

    private void runMap() {

    }
}
