package me.bteuk.network.lobby;

import me.bteuk.network.Network;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import static me.bteuk.network.utils.Constants.LOGGER;

public class HologramClickEvent implements Listener {

    private final Network instance;

    private final Map map;

    public HologramClickEvent(Network instance, Map map) {
        this.instance = instance;
        this.map = map;

        Bukkit.getServer().getPluginManager().registerEvents(this, instance);
    }

    public void unregister() {
        eu.decentsoftware.holograms.event.HologramClickEvent.getHandlerList().unregister(this);
    }

    @EventHandler
    public void onHologramClick(eu.decentsoftware.holograms.event.HologramClickEvent e) {

        // Get the user.
        NetworkUser u = instance.getUser(e.getPlayer());
        if (u == null) {
            e.getPlayer().sendMessage(Utils.error("An error occurred, please rejoin."));
            return;
        }

        // Get the hologram click event.
        Map.HologramClickAction action = map.getHologramClickAction(e.getHologram());

        // Handle click event on hologram.
        if (action != null) {
            action.click(u);
        }
    }
}
