package me.bteuk.network.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import me.bteuk.network.Network;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.profile.PlayerTextures;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static me.bteuk.network.utils.Constants.MAX_Y;
import static me.bteuk.network.utils.Constants.MIN_Y;

public class Utils {

    public static String tabName(String displayName) {
        return tabName(displayName.split(" ")[0], displayName.split(" ")[1]);
    }

    public static String tabName(String prefix, String name) {
        Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + " " + name);
        return GsonComponentSerializer.gson().serialize(component);
    }

    public static Component title(String message) {
        return Component.text(message, NamedTextColor.AQUA, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false);
    }

    public static Component line(String message) {
        return Component.text(message, NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false);
    }

    public static Component error(String message) {
        return Component.text(message, NamedTextColor.RED).decoration(TextDecoration.ITALIC, false);
    }

    public static Component success(String message) {
        return Component.text(message, NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false);
    }

    /**
     * Adds the prefix to a tip message.
     *
     * @param tip the tip to add a prefix to
     *
     * @return Component of the tip with the prefix
     */
    public static Component tip(String tip) {
        return Component.text("[TIP] ", TextColor.color(0x346beb))
                .append(Utils.line(tip));
    }

    public static String toJson(Component component) {
        return GsonComponentSerializer.gson().serialize(component);
    }

    //Adds the chat formatting to the message.
    public static Component chatFormat(Player player, Component message) {
        //Get prefix placeholder and convert from legacy format.
        //Legacy format for RGB is like &#a25981
        Component newMessage = LegacyComponentSerializer.legacyAmpersand().deserialize(PlaceholderAPI.setPlaceholders(player, "%luckperms_prefix% &f%player_name% &7&l> &r&f"));
        return newMessage.append(message);
    }

    public static ItemStack createItem(Material material, int amount, Component displayName, Component... loreString) {

        ItemStack item;

        item = new ItemStack(material);
        item.setAmount(amount);

        ItemMeta meta = item.getItemMeta();
        meta.displayName(displayName);
        List<Component> lore = new ArrayList<>(Arrays.asList(loreString));
        meta.lore(lore);
        item.setItemMeta(meta);

        return item;

    }

    public static ItemStack createCustomSkullWithFallback(String texture, Material fallback, int amount, Component displayName, Component... loreString) {

        ItemStack item;

        try {

            if (texture == null) {
                throw new NullPointerException();
            }

            URL url = new URL("http://textures.minecraft.net/texture/" + texture);
            item = new ItemStack(Material.PLAYER_HEAD);

            SkullMeta meta = (SkullMeta) item.getItemMeta();

            //Create playerprofile.
            PlayerProfile profile = Network.getInstance().getServer().createProfile(UUID.randomUUID());

            PlayerTextures textures = profile.getTextures();
            textures.setSkin(url);

            profile.setTextures(textures);

            meta.setPlayerProfile(profile);

            item.setItemMeta(meta);

        } catch (Exception e) {
            item = new ItemStack(fallback);
        }

        item.setAmount(amount);

        ItemMeta meta = item.getItemMeta();
        meta.displayName(displayName);
        List<Component> lore = new ArrayList<>(Arrays.asList(loreString));
        meta.lore(lore);
        item.setItemMeta(meta);

        return item;

    }

    public static ItemStack createPlayerSkull(String uuid, int amount, Component displayName, Component... loreString) {

        ItemStack item;

        item = new ItemStack(Material.PLAYER_HEAD);
        item.setAmount(amount);

        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.displayName(displayName);
        List<Component> lore = new ArrayList<>(Arrays.asList(loreString));
        meta.lore(lore);
        item.setItemMeta(meta);

        OfflinePlayer p = Bukkit.getOfflinePlayer(UUID.fromString(uuid));

        meta.setOwningPlayer(p);
        item.setItemMeta(meta);

        return item;

    }

    public static ItemStack createPotion(Material material, PotionEffectType effect, int amount, Component displayName, Component... loreString) {

        ItemStack item;

        item = new ItemStack(material);
        item.setAmount(amount);

        PotionMeta meta = (PotionMeta) item.getItemMeta();
        meta.addCustomEffect(new PotionEffect(effect, Integer.MAX_VALUE, 1), true);

        meta.displayName(displayName);
        List<Component> lore = new ArrayList<>(Arrays.asList(loreString));
        meta.lore(lore);
        item.setItemMeta(meta);

        return item;

    }

    public static int getHighestYAt(World w, int x, int z) {

        for (int i = (MAX_Y - 1); i >= MIN_Y; i--) {
            if (w.getBlockAt(x, i, z).getType() != Material.AIR) {
                return i + 1;
            }
        }
        //Return 65 as the default y.
        return 65;
    }

    //Gives a player an item, it will be set in their main hand, if it does not already exist there.

    //If the main hand is empty, set it there.
    //If then main hand is slot 8 and includes the navigator, find the first empty slot available and set it there.
    //If no empty slots are available set it to slot 7.
    //If the main hand has an item swap the current item to an empty slot in the inventory.
    //If no empty slots are available overwrite it.

    public static void giveItem(Player p, ItemStack item, String name) {

        ItemStack currentItem = p.getInventory().getItemInMainHand();

        int emptySlot = getEmptyHotbarSlot(p);

        boolean hasNavigator = (p.getInventory().getHeldItemSlot() == 8 && currentItem.equals(Network.getInstance().navigator));
        boolean hasItemAlready = p.getInventory().containsAtLeast(item, 1);

        //If we already have the item switch to current slot.
        if (hasItemAlready) {

            //Switch item to current slot.
            int slot = p.getInventory().first(item);

            if (hasNavigator) {

                p.getInventory().setItem(slot, p.getInventory().getItem(7));
                p.getInventory().setItem(7, item);
                p.sendMessage(Utils.success("Set ").append(Component.text(name, NamedTextColor.DARK_AQUA).append(Utils.success(" to slot 8"))));

            } else {

                p.getInventory().setItem(slot, p.getInventory().getItemInMainHand());
                p.getInventory().setItemInMainHand(item);
                p.sendMessage(Utils.success("Set ").append(Component.text(name, NamedTextColor.DARK_AQUA).append(Utils.success(" to main hand."))));

            }
        } else if (emptySlot >= 0) {
            //The current slot is empty. This also implies no navigator, and thus the item does not yet exist in the inventory.
            //Set item to empty slot.
            p.getInventory().setItem(emptySlot, item);
            p.sendMessage(Utils.success("Set ").append(Component.text(name, NamedTextColor.DARK_AQUA).append(Utils.success(" to slot " + (emptySlot + 1)))));

        } else {

            //Player has no empty slots and is holding the navigator, set to item to slot 7.
            p.getInventory().setItem(7, item);
            p.sendMessage(Utils.success("Set ").append(Component.text(name, NamedTextColor.DARK_AQUA).append(Utils.success(" to slot 8"))));

        }
    }

    //Return an empty hotbar slot, if no empty slot exists return -1.
    public static int getEmptyHotbarSlot(Player p) {

        //If main hand is empty return that slot.
        ItemStack heldItem = p.getInventory().getItemInMainHand();
        if (heldItem.getType() == Material.AIR) {
            return p.getInventory().getHeldItemSlot();
        }

        //Check if hotbar has an empty slot.
        for (int i = 0; i < 9; i++) {

            ItemStack item = p.getInventory().getItem(i);

            if (item == null) {
                return i;
            }
            if (item.getType() == Material.AIR) {
                return i;
            }
        }

        //No slot could be found, return -1.
        return -1;
    }
}
