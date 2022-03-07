package me.bteuk.network.listeners;

import me.bteuk.network.Network;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.utils.NetworkUser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.UUID;

public class GuiListener implements Listener {

    Network instance;

    public GuiListener(Network instance) {

        this.instance = instance;
        Bukkit.getServer().getPluginManager().registerEvents(this, instance);

    }

    @EventHandler
    public void onClick(InventoryClickEvent e){

        if (!(e.getWhoClicked() instanceof Player)){

            return;

        }

        NetworkUser u = instance.getUser((Player) e.getWhoClicked());
        UUID playerUUID = u.player.getUniqueId();

        UUID inventoryUUID = Gui.openInventories.get(playerUUID);

        if (inventoryUUID != null){

            e.setCancelled(true);
            Gui gui = Gui.getInventoriesByUUID().get(inventoryUUID);
            Gui.guiAction action = gui.getActions().get(e.getSlot());

            if (action != null){

                action.click(u);

            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {

        NetworkUser u = instance.getUser((Player) e.getPlayer());
        UUID playerUUID = u.player.getUniqueId();

        //Remove the player from the list of open inventories.
        Gui.openInventories.remove(playerUUID);

        //Remove the unique gui, if it exists.
        if (u.uniqueGui != null) {

            u.uniqueGui.delete();

        }

        u.uniqueGui = null;

    }
}
