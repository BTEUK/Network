package me.bteuk.network.utils;

import me.bteuk.network.Network;
import me.clip.placeholderapi.libs.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.bteuk.network.utils.Constants.MAX_Y;
import static me.bteuk.network.utils.Constants.MIN_Y;

public class Utils {

    public static String chat(String message) {
        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(message);

        while (matcher.find()) {
            String color = message.substring(matcher.start(), matcher.end());
            message = message.replace(color, ChatColor.of(color) + "");
            matcher = pattern.matcher(message);
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String tabName(String displayName) {
        return tabName(displayName.split(" ")[0], displayName.split(" ")[1]);
    }

    public static String tabName(String prefix, String name) {
        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(prefix);

        //Replace &r in string.
        prefix = prefix.replace("&r", "");

        TextComponent text;
        if (matcher.find()) {

            String color = prefix.substring(matcher.start(), matcher.end());
            prefix = prefix.replace(color, "");

            text = Component.text(prefix, TextColor.fromHexString(color))
                    .append(Component.text(" " + name, NamedTextColor.WHITE));

        } else {

            text = Component.text(prefix, NamedTextColor.WHITE)
                    .append(Component.text(" " + name, NamedTextColor.WHITE));
        }

        return GsonComponentSerializer.gson().serialize(text);
    }

    public static String title(String message) {
        return chat("&b&l" + message);
    }

    public static String line(String message) {
        return chat("&f" + message);
    }

    public static String error(String message) {
        return chat("&c" + message);
    }

    public static String success(String message) {
        return chat("&a" + message);
    }

    public static ItemStack createItem(Material material, int amount, String displayName, String... loreString) {

        ItemStack item;

        List<String> lore = new ArrayList<String>();

        item = new ItemStack(material);
        item.setAmount(amount);

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Utils.chat(displayName));
        for (String s : loreString) {
            lore.add(Utils.chat(s));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;

    }

    public static ItemStack createPlayerSkull(String uuid, int amount, String displayName, String... loreString) {

        ItemStack item;

        List<String> lore = new ArrayList<>();

        item = new ItemStack(Material.PLAYER_HEAD);
        item.setAmount(amount);

        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setDisplayName(Utils.chat(displayName));
        for (String s : loreString) {
            lore.add(Utils.chat(s));
        }
        meta.setLore(lore);

        OfflinePlayer p = Bukkit.getOfflinePlayer(UUID.fromString(uuid));

        meta.setOwningPlayer(p);
        item.setItemMeta(meta);

        return item;

    }

    public static ItemStack createPotion(Material material, PotionEffectType effect, int amount, String displayName, String... loreString) {

        ItemStack item;

        List<String> lore = new ArrayList<String>();

        item = new ItemStack(material);
        item.setAmount(amount);

        PotionMeta meta = (PotionMeta) item.getItemMeta();
        meta.addCustomEffect(new PotionEffect(effect, Integer.MAX_VALUE, 1), true);

        meta.setDisplayName(Utils.chat(displayName));
        for (String s : loreString) {
            lore.add(Utils.chat(s));
        }
        meta.setLore(lore);
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
