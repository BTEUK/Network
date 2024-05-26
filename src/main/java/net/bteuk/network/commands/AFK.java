package net.bteuk.network.commands;

import net.bteuk.network.Network;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.utils.NetworkUser;
import net.bteuk.network.utils.Statistics;
import net.bteuk.network.utils.Time;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.bteuk.network.utils.Constants.LOGGER;

public class AFK implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Check if the sender is a player.
        if (!(sender instanceof Player p)) {

            sender.sendMessage(ChatUtils.error("This command can only be run by a player."));
            return true;

        }

        //Get user
        NetworkUser u = Network.getInstance().getUser(p);

        //If u is null, cancel.
        if (u == null) {
            LOGGER.severe("User " + p.getName() + " can not be found!");
            p.sendMessage(ChatUtils.error("User can not be found, please relog!"));
            return true;
        }

        //Switch afk status.
        if (u.afk) {

            //Reset last logged time.
            u.last_time_log = u.last_movement = Time.currentTime();
            u.afk = false;
            Network.getInstance().getChat().broadcastAFK(u.player, false);

        } else {

            long time = Time.currentTime();

            //Update playtime, and pause it.
            Statistics.save(u, Time.getDate(time), time);

            u.afk = true;
            Network.getInstance().getChat().broadcastAFK(u.player, true);

        }

        return true;

    }
}
