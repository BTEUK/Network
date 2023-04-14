package me.bteuk.network.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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
        return Component.text(message, NamedTextColor.AQUA, TextDecoration.BOLD);
    }

    public static Component line(String message) {
        return Component.text(message, NamedTextColor.WHITE);
    }

    public static Component error(String message) {
        return Component.text(message, NamedTextColor.RED);
    }

    public static Component success(String message) {
        return Component.text(message, NamedTextColor.GREEN);
    }

    public static String toJson(Component component) {
        return GsonComponentSerializer.gson().serialize(component);
    }

    //Adds the chat formatting to the message.
    public static Component chatFormat(Player player, Component message) {
        //Get prefix placeholder and convert from legacy format.
        //Legacy format for RGB is like §#a25981
        Component newMessage = LegacyComponentSerializer.legacyAmpersand().deserialize(PlaceholderAPI.setPlaceholders(player, "%luckperms_prefix% §f%player_name% §7§l> §r§f"));
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
        return Integer.MIN_VALUE;
    }
}
