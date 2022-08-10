package me.bteuk.network.utils;

import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bukkit.ChatColor.COLOR_CHAR;

public class Utils {

    public static String chat(String message) {
        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(message);

        while (matcher.find()) {
            String color = message.substring(matcher.start(), matcher.end());
            message = message.replace(color, ChatColor.valueOf(color) + "");
            matcher = pattern.matcher(message);
        }
        return ChatColor.translateAlternateColorCodes('&', message);
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

    public static boolean isPlayerInGroup(Player player, String group) {
        return player.hasPermission("group." + group);
    }

    public static void spawnFireWork(Player p) {

        Firework f = p.getWorld().spawn(p.getLocation(), Firework.class);
        FireworkMeta fm = f.getFireworkMeta();
        fm.addEffect(FireworkEffect.builder().flicker(true).trail(true).with(FireworkEffect.Type.BALL_LARGE).withColor(Color.RED).withColor(Color.BLUE).withColor(Color.WHITE).build());
        fm.setPower(1);
        f.setFireworkMeta(fm);


    }

    public static int getHighestYAt(World w, int x, int z) {

        for (int i = 255; i >= 0; i--) {
            if (w.getBlockAt(x, i, z).getType() != Material.AIR) {
                return i + 1;
            }
        }
        return 0;
    }
}
