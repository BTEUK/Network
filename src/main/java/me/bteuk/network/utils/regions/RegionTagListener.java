package me.bteuk.network.utils.regions;

import me.bteuk.network.Network;
import me.bteuk.network.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitTask;

public class RegionTagListener implements Listener {

    private final Player p;
    private final Region region;

    private final BukkitTask task;

    public RegionTagListener(Player p, Region region) {

        Bukkit.getServer().getPluginManager().registerEvents(this, Network.getInstance());

        this.p = p;
        this.region = region;

        //Start timer to automatically close the listener.
        task = Bukkit.getScheduler().runTaskLater(Network.getInstance(), () -> {
            //Send message to player telling them it's been timer out.
            if (p != null) {
                p.sendMessage(Utils.chat("&c'Set Region Tag' cancelled."));
            }
            unregister();
        }, 1200L);

    }

    @EventHandler
    public void ChatEvent(AsyncPlayerChatEvent e) {

        //Check if this is the correct player.
        if (e.getPlayer().equals(p)) {

            e.setCancelled(true);

            //Check if message is under 64 character.
            if (e.getMessage().length() > 64) {
                e.getPlayer().sendMessage(Utils.chat("&cThe region tag can't be longer than 64 characters."));
            } else {

                //Set region tag.
                region.setTag(p.getUniqueId().toString(), e.getMessage());

                //Send message to player.
                p.sendMessage(Utils.chat("&aSet tag for region " + region.getName() + " to " + e.getMessage()));

                //Unregister listener and task.
                task.cancel();
                unregister();

            }
        }
    }

    private void unregister() {
        AsyncPlayerChatEvent.getHandlerList().unregister(this);
    }
}
