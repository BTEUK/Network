package net.bteuk.network.building_counter;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.bteuk.network.Network;
import net.bteuk.network.commands.Buildings;
import net.bteuk.network.lib.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ConfirmationListener implements Listener {
    private final Location currentLocation;
    private final Player currentPlayer;
    private final BukkitRunnable timeoutTask;

    public ConfirmationListener(Location l, Player p, Plugin plugin) {
        currentLocation = l;
        currentPlayer = p;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        timeoutTask = new BukkitRunnable() {
            @Override
            public void run() {
                AsyncChatEvent.getHandlerList().unregister(ConfirmationListener.this);
                currentPlayer.sendMessage(ChatUtils.error("Confirmation timed out. No building added."));
            }
        };
        timeoutTask.runTaskLater(plugin, 20 * 120);
    }

    @EventHandler
    public void chatEvent(AsyncChatEvent e) {
        if (e.getPlayer().getUniqueId() == currentPlayer.getUniqueId()) {
            timeoutTask.cancel();
            e.getHandlers().unregister(this);
            e.setCancelled(true);
            if (((net.kyori.adventure.text.TextComponent) e.message()).content().equals("y")) {
                Buildings.addBuildingToDataBase(e.getPlayer(), currentLocation);
            } else {
                e.getPlayer().sendMessage(ChatUtils.error("No building added"));
            }
        }
    }

}
