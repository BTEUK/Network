package me.bteuk.network.utils.navigation;

import me.bteuk.network.Network;
import me.bteuk.network.gui.navigation.AddLocation;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitTask;

public class LocationNameListener implements Listener {

    private final AddLocation gui;
    private final Player p;

    private final BukkitTask task;

    public LocationNameListener(Player p, AddLocation gui) {

        Bukkit.getServer().getPluginManager().registerEvents(this, Network.getInstance());

        this.p = p;
        this.gui = gui;

        //Start timer to automatically close the listener.
        task = Bukkit.getScheduler().runTaskLater(Network.getInstance(), () -> {
            //Send message to player telling them it's been timer out.
            if (p != null) {
                p.sendMessage(Utils.chat("&c'Set Location Name' cancelled."));

                //If AddLocation gui still exists, reopen it.
                //Also check if player is actually still online.
                if (p.isOnline()) {
                    NetworkUser u = Network.getInstance().getUser(p);
                    if (u.mainGui != null) {
                        if (u.mainGui instanceof AddLocation) {
                            u.mainGui.open(u);
                        }
                    }
                }

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
                e.getPlayer().sendMessage(Utils.chat("&cThe location name can't be longer than 64 characters."));
            } else {

                //Set location name.
                gui.setName(e.getMessage());

                //Send message to player.
                p.sendMessage(Utils.chat("&aSet location name to" + e.getMessage()));

                //Unregister listener and task.
                task.cancel();
                unregister();

                //If AddLocation gui still exists, reopen it.
                NetworkUser u = Network.getInstance().getUser(p);
                if (u.mainGui != null) {
                    if (u.mainGui instanceof AddLocation) {
                        u.mainGui.open(u);
                    }
                }

            }
        }
    }

    public void unregister() {
        AsyncPlayerChatEvent.getHandlerList().unregister(this);
    }
}
