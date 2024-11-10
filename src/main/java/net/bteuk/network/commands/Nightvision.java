package net.bteuk.network.commands;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.bteuk.network.Network;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.utils.NetworkUser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import static net.bteuk.network.utils.Constants.LOGGER;

public class Nightvision extends AbstractCommand {

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {

        //Check if the sender is a player.
        Player player = getPlayer(stack);
        if (player == null) {
            return;
        }

        NetworkUser user = Network.getInstance().getUser(player);

        //If u is null, cancel.
        if (user == null) {
            LOGGER.severe("User " + player.getName() + " can not be found!");
            player.sendMessage(ChatUtils.error("User can not be found, please relog!"));
            return;
        }

        toggleNightvision(user);

    }

    public static void toggleNightvision(NetworkUser user) {
        if (user.isNightvisionEnabled()) {
            removeNightvision(user.player);
            user.setNightvisionEnabled(false);
            user.player.sendMessage(ChatUtils.success("Disabled nightvision."));
        } else {
            giveNightvision(user.player);
            user.setNightvisionEnabled(true);
            user.player.sendMessage(ChatUtils.success("Enabled nightvision."));
        }
    }

    public static void giveNightvision(Player player) {
        // Remove any existing night vision first.
        Bukkit.getScheduler().runTask(Network.getInstance(), () -> {
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1, false, false));
        });
    }

    public static void removeNightvision(Player player) {
        Bukkit.getScheduler().runTask(Network.getInstance(), () -> {
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        });
    }
}
