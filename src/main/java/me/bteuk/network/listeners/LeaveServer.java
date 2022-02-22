package me.bteuk.network.listeners;

import me.bteuk.network.Main;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.utils.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class LeaveServer implements Listener {

    Main instance;

    public LeaveServer(Main instance) {

        this.instance = instance;
        Bukkit.getServer().getPluginManager().registerEvents(this, instance);

    }

    @EventHandler
    public void leaveServerEvent(PlayerQuitEvent e) {

        User u = instance.getUser(e.getPlayer());

        //Remove user from list.
        instance.removeUser(u);

        //Get player uuid.
        UUID playerUUID = u.player.getUniqueId();

        //If they are currently in an inventory, remove them from the list of open inventories.
        Gui.openInventories.remove(playerUUID);

    }
}
