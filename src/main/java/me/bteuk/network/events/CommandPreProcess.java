package me.bteuk.network.events;

import me.bteuk.network.Network;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandPreProcess implements Listener {

    public CommandPreProcess(Network instance) {
        Bukkit.getServer().getPluginManager().registerEvents(this, instance);
    }

    @EventHandler
    public void onCommandPreProcess(PlayerCommandPreprocessEvent e) {
        //Replace /region with /network:region
        if (e.getMessage().startsWith("/region")) {
            e.setMessage(e.getMessage().replace("/region", "/network:region"));
        }
    }
}
