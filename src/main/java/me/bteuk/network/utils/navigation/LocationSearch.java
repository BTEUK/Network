package me.bteuk.network.utils.navigation;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.bteuk.network.Network;
import me.bteuk.network.gui.navigation.LocationMenu;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

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
    public void ChatEvent(AsyncChatEvent e) {

        //Check if this is the correct player.
        if (e.getPlayer().equals(u.player)) {

            e.setCancelled(true);

            //Check if message is under 64 character.
            if (PlainTextComponentSerializer.plainText().serialize(e.message()).length() > 64) {
                e.getPlayer().sendMessage(Utils.error("The phrase can't be longer than 64 characters."));
            } else {

                LocationMenu gui = new LocationMenu("Search: " + PlainTextComponentSerializer.plainText().serialize(e.message()), u, PlainTextComponentSerializer.plainText().serialize(e.message()), "Explore");


                //If there are no locations notify the user.
                if (gui.isEmpty()) {

                    gui.delete();
                    u.player.sendMessage(Utils.error("No locations have been found."));

                } else {
                    //Open the location menu with these locations.
                    Bukkit.getScheduler().runTask(Network.getInstance(), () -> {

                        u.mainGui.delete();
                        u.mainGui = gui;
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
        AsyncChatEvent.getHandlerList().unregister(this);
    }
}
