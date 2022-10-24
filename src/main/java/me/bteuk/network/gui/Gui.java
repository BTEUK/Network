package me.bteuk.network.gui;

import me.bteuk.network.Network;
import me.bteuk.network.utils.NetworkUser;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class Gui implements GuiInterface {

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

    public void clearGui() {
        inv.clear();
        actions.clear();
    }

    public void open(NetworkUser u) {

        Network.getInstance().getLogger().info("Number of Gui's: " + inventoriesByUUID.size());

        u.player.openInventory(inv);
        openInventories.put(u.player.getUniqueId(), getUuid());

    }

    public void delete() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            UUID u = openInventories.get(p.getUniqueId());
            if (u != null) {
                if (u.equals(getUuid())) {
                    p.closeInventory();
                }
            }
        }
        inventoriesByUUID.remove(getUuid());
    }

    //TODO: Refresh gui method in this class, which is then overwritten all unique guis.

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
