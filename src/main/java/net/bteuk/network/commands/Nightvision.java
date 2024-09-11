package net.bteuk.network.commands;

import net.bteuk.network.Network;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.utils.NetworkUser;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import static net.bteuk.network.utils.Constants.LOGGER;

public class Nightvision extends AbstractCommand {

    public Nightvision(Network instance) {
        super(instance, "nightvision");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Check for player
        Player player = getPlayer(sender);

        if (player == null) {
            return true;
        }

        //Get the NetworkUser for this player.
        NetworkUser user = Network.getInstance().getUser(player);

        if (user == null) {
            LOGGER.warning("NetworkUser for player " + player.getName() + " is null!");
            return true;
        }

        toggleNightvision(user);
        return true;

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
