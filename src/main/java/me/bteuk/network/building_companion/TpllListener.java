package me.bteuk.network.building_companion;

import me.bteuk.network.Network;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class TpllListener implements Listener {

    private final BuildingCompanion companion;

    public TpllListener(BuildingCompanion companion) {
        this.companion = companion;
        Bukkit.getServer().getPluginManager().registerEvents(this, Network.getInstance());
    }

    @EventHandler
    public void onCommandPreProcess(PlayerCommandPreprocessEvent e) {
        if (!companion.playerEquals(e.getPlayer())) {
            return;
        }

        if (e.getMessage().startsWith("/tpll")) {

        } else {
            return;
        }
    }
}
