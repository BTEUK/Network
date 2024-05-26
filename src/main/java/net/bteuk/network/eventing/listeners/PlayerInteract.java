package net.bteuk.network.eventing.listeners;

import net.bteuk.network.Network;
import net.bteuk.network.commands.Navigator;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.utils.NetworkUser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import static net.bteuk.network.utils.Constants.LOGGER;

public class PlayerInteract implements Listener {

    private final Network instance;

    public PlayerInteract(Network instance) {

        this.instance = instance;
        Bukkit.getServer().getPluginManager().registerEvents(this, instance);

    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {

        NetworkUser u = instance.getUser(e.getPlayer());

        //If u is null, cancel.
        if (u == null) {
            LOGGER.severe("User " + e.getPlayer().getName() + " can not be found!");
            e.getPlayer().sendMessage(ChatUtils.error("User can not be found, please relog!"));
            return;
        }

        if (e.getItem() != null) {
            if (e.getItem().equals(instance.navigator)) {
                e.setCancelled(true);
                //Open navigator.
                Navigator.openNavigator(u);
            }
        }
    }

    //If the player clicks on the navigator in their inventory, open the gui.
    @EventHandler
    public void onClick(InventoryClickEvent e) {

        if (e.getCurrentItem() == null) {
            return;
        }

        NetworkUser u = instance.getUser((Player) e.getWhoClicked());

        //If u is null, cancel.
        if (u == null) {
            LOGGER.severe("User " + e.getWhoClicked().getName() + " can not be found!");
            e.getWhoClicked().sendMessage(ChatUtils.error("User can not be found, please relog!"));
            return;
        }

        //If item is navigator then open it.
        if (e.getCurrentItem().equals(instance.navigator)) {
            e.setCancelled(true);

            //If item is not in slot 8, delete it.
            if (e.getSlot() != 8) {
                u.player.getInventory().clear(e.getSlot());
                return;
            }

            u.player.closeInventory();
            Bukkit.getScheduler().runTaskLater(instance, () -> Navigator.openNavigator(u), 1);
        }
    }


    /*

    The following events are to prevent the navigator being moved in the inventory,
    causing duplicate items which are difficult to remove.

     */

    @EventHandler
    public void swapHands(PlayerSwapHandItemsEvent e) {

        if (e.getOffHandItem() == null) {
            return;
        }

        if (e.getOffHandItem().equals(instance.navigator)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void dropItem(PlayerDropItemEvent e) {

        if (e.getItemDrop().getItemStack().equals(instance.navigator)) {
            e.setCancelled(true);
        }

    }

    @EventHandler
    public void moveItem(InventoryMoveItemEvent e) {
        if (e.getItem().equals(instance.navigator)) {
            e.setCancelled(true);
        }

    }

    @EventHandler
    public void moveItem(InventoryDragEvent e) {
        if (e.getOldCursor().equals(instance.navigator)) {
            e.setCancelled(true);
        }

        if (e.getCursor() == null) {
            return;
        }

        if (e.getCursor().equals(instance.navigator)) {
            e.setCancelled(true);
        }
    }
}
