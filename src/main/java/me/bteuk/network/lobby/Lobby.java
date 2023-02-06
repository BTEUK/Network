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
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;

public class Lobby {

    private final Network instance;

    private final ArrayList<Portal> portals;
    private int portalTask;

    private ItemStack rulesBook;

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
        File portalsFile = new File(instance.getDataFolder(), "portals.yml");
        if (!portalsFile.exists()) {
            instance.saveResource("portals.yml", false);
        }

        FileConfiguration portalsConfig = YamlConfiguration.loadConfiguration(portalsFile);

        //Gets all the portal names from the config.
        //This will allow us to query the config for portals.
        ConfigurationSection section = portalsConfig.getConfigurationSection("portals");
        //No portals have yet been added.
        if (section == null) {
            return;
        }

        Set<String> portalNames = portalsConfig.getConfigurationSection("portals").getKeys(false);

        //Create the portal from the config.
        for (String portalName : portalNames) {

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

            //Check if any players are in the area of the portal.
            for (NetworkUser user : instance.getUsers()) {

                user.inPortal = false;

                for (Portal portal : portals) {

                    if (portal.in(user.player.getLocation())) {
                        if (!user.wasInPortal) {

                            //Set user to in portal.
                            user.inPortal = true;
                            user.wasInPortal = true;

                            //If they are run the portal events.
                            portal.event(user.player);
                            break;

                        } else {
                            //User is still in a portal, but don't execute the portal event.
                            user.inPortal = true;
                            break;
                        }
                    }
                }

                if (user.wasInPortal && !user.inPortal) {

                    //Set user to not in portal.
                    user.wasInPortal = false;

                }
            }

        }, 0L, 1L);
    }

    //Load the rules.
    //The rules are stored in rules.yml.
    public void loadRules() {

        //Create rules.yml if not exists.
        //The data folder should already exist since the plugin will always create config.yml first.
        File rulesFile = new File(instance.getDataFolder(), "rules.yml");
        if (!rulesFile.exists()) {
            instance.saveResource("rules.yml", false);
        }

        FileConfiguration rulesConfig = YamlConfiguration.loadConfiguration(rulesFile);

        //Gets all the portal names from the config.
        //This will allow us to query the config for portals.
        ConfigurationSection section = rulesConfig.getConfigurationSection("rules");
        //No portals have yet been added.
        if (section == null) {
            return;
        }

        Set<String> rules = rulesConfig.getConfigurationSection("portals").getKeys(false);

        //Set all the pages of the book.
        rulesBook = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) rulesBook.getItemMeta();
        bookMeta.setTitle(Utils.title("Rules"));

        //Get book author.
        bookMeta.setAuthor(rulesConfig.getString("author"));

        //Get pages of the book.
        ArrayList<String> pages = new ArrayList<>();
        rules.forEach(str -> {
            pages.add(rulesConfig.getString("rules." + str));
        });

        //Set the pages of the book.
        bookMeta.setPages(pages);
        rulesBook.setItemMeta(bookMeta);

    }

    public ItemStack getRules() {
        return rulesBook;
    }

    private void runLeaderboards() {

    }

    private void runMap() {

    }
}
