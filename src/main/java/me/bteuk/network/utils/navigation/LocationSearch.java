package me.bteuk.network.utils.navigation;

import me.bteuk.network.Network;
import me.bteuk.network.gui.navigation.LocationMenu;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.enums.Categories;
import me.bteuk.network.utils.enums.Counties;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashSet;

public class LocationSearch implements Listener {

    private final NetworkUser u;

    private final BukkitTask task;

    public LocationSearch(NetworkUser u) {

        Bukkit.getServer().getPluginManager().registerEvents(this, Network.getInstance());

        this.u = u;

        //Start timer to automatically close the listener.
        task = Bukkit.getScheduler().runTaskLater(Network.getInstance(), () -> {
            //Send message to player telling them it's been timer out.
            if (u.player != null) {
                u.player.sendMessage(Utils.error("'Find Location' cancelled."));
            }
            unregister();
        }, 1200L);

    }

    @EventHandler
    public void ChatEvent(AsyncPlayerChatEvent e) {

        //Check if this is the correct player.
        if (e.getPlayer().equals(u.player)) {

            e.setCancelled(true);

            //Check if message is under 64 character.
            if (e.getMessage().length() > 64) {
                e.getPlayer().sendMessage(Utils.error("The phrase can't be longer than 64 characters."));
            } else {

                //Search for locations that include this phrase.
                ArrayList<String> locations = Network.getInstance().globalSQL.getStringList("SELECT location FROM location_data WHERE location LIKE '%" + e.getMessage() + "%';");

                //Also search for any regions or counties.
                for (Counties county : Counties.values()) {
                    if (StringUtils.containsIgnoreCase(county.label, e.getMessage())) {

                        locations.addAll(Network.getInstance().globalSQL.getStringList("SELECT location FROM location_data WHERE subcategory='" + county.region + "';"));

                    }
                }

                for (Categories category : Categories.values()) {
                    if (StringUtils.containsIgnoreCase(category.label, e.getMessage())) {

                        locations.addAll(Network.getInstance().globalSQL.getStringList("SELECT location FROM location_data WHERE category='" + category + "';"));

                    }
                }

                HashSet<String> searchLocations = new HashSet<>(locations);


                //If there are no locations notify the user.
                if (searchLocations.size() == 0) {

                    u.player.sendMessage(Utils.error("No locations have been found."));

                } else {
                    //Open the location menu with these locations.
                    Bukkit.getScheduler().runTask(Network.getInstance(), () -> {

                        u.mainGui.delete();
                        u.mainGui = new LocationMenu("Search: " + e.getMessage(), searchLocations, false, false, u);
                        u.mainGui.open(u);

                    });
                }

                //Unregister listener and task.
                task.cancel();
                unregister();

            }
        }
    }

    public void unregister() {
        AsyncPlayerChatEvent.getHandlerList().unregister(this);
    }
}
