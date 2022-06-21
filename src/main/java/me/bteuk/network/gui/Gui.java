package me.bteuk.network.gui;

import me.bteuk.network.utils.NetworkUser;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class Gui {

    public static Map<UUID, Gui> inventoriesByUUID = new HashMap<>();
    public static Map<UUID, UUID> openInventories = new HashMap<>();

    //Information about the gui.
    private final UUID uuid;
    private final Inventory inv;
    private final Map<Integer, guiAction> actions;

    public Gui(int invSize, Component invName) {

        uuid = UUID.randomUUID();
        inv = Bukkit.createInventory(null, invSize, invName);
        actions = new HashMap<>();
        inventoriesByUUID.put(getUuid(), this);

    }

    public Inventory getInventory() {
        return inv;
    }

    public interface guiAction {
        void click(NetworkUser u);
    }

    public void setItem(int slot, ItemStack stack, guiAction action) {

        inv.setItem(slot, stack);
        if (action != null) {
            actions.put(slot, action);
        }

    }

    public void setItem(int slot, ItemStack stack) {

        setItem(slot, stack, null);

    }

    public void open(NetworkUser u) {

        u.player.openInventory(inv);
        openInventories.put(u.player.getUniqueId(), getUuid());

    }

    //Update the gui the user is in by updating the current menu.
    public void update(NetworkUser u, UniqueGui uniqueGui) {

        //Remove player from openInventories.
        openInventories.remove(u.player.getUniqueId());

        //Remove current gui without closing inventory.
        inventoriesByUUID.remove(getUuid());

        //Set the new gui as open inventory.
        openInventories.put(u.player.getUniqueId(), uniqueGui.getUuid());

        //Set the contents of the players inventory.
        u.player.getOpenInventory().getTopInventory().setContents(uniqueGui.getInventory().getContents());

        //Set the uniqueGui for user.
        u.uniqueGui = uniqueGui;

    }

    //Switch from current gui to a new one by closing and opening.
    public void switchGui(NetworkUser u, UniqueGui uniqueGui) {

        //Delete the current gui.
        delete();

        //Set the new gui and open it.
        u.uniqueGui = uniqueGui;
        u.uniqueGui.open(u);

    }

    public void delete(){
        for (Player p : Bukkit.getOnlinePlayers()){
            UUID u = openInventories.get(p.getUniqueId());
            if (u != null) {
                if (u.equals(getUuid())) {
                    p.closeInventory();
                }
            }
        }
        inventoriesByUUID.remove(getUuid());
    }

    public UUID getUuid() {
        return uuid;
    }

    public static Map<UUID, Gui> getInventoriesByUUID() {
        return inventoriesByUUID;
    }

    public static Map<UUID, UUID> getOpenInventories() {
        return openInventories;
    }

    public Map<Integer, guiAction> getActions() {
        return actions;
    }
}
