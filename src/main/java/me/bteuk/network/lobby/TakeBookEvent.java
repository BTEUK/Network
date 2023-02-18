package me.bteuk.network.lobby;

import me.bteuk.network.Network;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;

public class TakeBookEvent implements Listener {

    Lobby lobby;

    public TakeBookEvent(Network instance, Lobby lobby) {

        this.lobby = lobby;
        Bukkit.getServer().getPluginManager().registerEvents(this, instance);

    }

    @EventHandler
    public void playerTakeLecternBook(PlayerTakeLecternBookEvent e) {
        //If book in lectern is the same as the rules book, cancel the event.
        if (e.getBook().equals(lobby.getRules())) {
            e.setCancelled(true);
        }
    }
}
