package me.bteuk.network.commands;

import me.bteuk.network.Network;
import me.bteuk.network.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class Nightvision implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Check if the sender is a player.
        if (!(sender instanceof Player p)) {

            sender.sendMessage(Utils.chat("&cThis command can only be run by a player."));
            return true;

        }

        toggleNightvision(p);
        return true;

    }

    public static void toggleNightvision(Player p) {

        //If the player currently does not have nightvision enable it, else disable it.
        if (p.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {

            //Disable nightvision.
            p.removePotionEffect(PotionEffectType.NIGHT_VISION);

            //Disable nightvision in database.
            Network.getInstance().globalSQL.update("UPDATE player_data SET nightvision_enabled=0 WHERE uuid='" + p.getUniqueId() + "';");

            p.sendMessage(Utils.chat("&aDisabled nightvision."));

        } else {

            //Enable nightvision.
            p.addPotionEffect(PotionEffectType.NIGHT_VISION.createEffect(Integer.MAX_VALUE,1));

            //Enable nightvision in database.
            Network.getInstance().globalSQL.update("UPDATE player_data SET nightvision_enabled=1 WHERE uuid='" + p.getUniqueId() + "';");

            p.sendMessage(Utils.chat("&aEnabled nightvision."));
        }
    }

    public static void giveNightvision(Player p) {

        //If the player currently does not have nightvision enable it, else disable it.
        if (!p.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {

            //Enable nightvision.
            p.addPotionEffect(PotionEffectType.NIGHT_VISION.createEffect(Integer.MAX_VALUE,1));

        }

    }

    public static void removeNightvision(Player p) {

        //If the player currently does not have nightvision enable it, else disable it.
        if (p.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {

            //Enable nightvision.
            p.removePotionEffect(PotionEffectType.NIGHT_VISION);

        }
    }
}
