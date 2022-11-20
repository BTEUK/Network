package me.bteuk.network.commands;

import me.bteuk.network.Network;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Statistics;
import me.bteuk.network.utils.Time;
import me.bteuk.network.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AFK implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Check if the sender is a player.
        if (!(sender instanceof Player p)) {

            sender.sendMessage(Utils.chat("&cThis command can only be run by a player."));
            return true;

        }

        //Get user
        NetworkUser u = Network.getInstance().getUser(p);

        //Switch afk status.
        if (u.afk) {

            //Reset last logged time.
            u.last_time_log = u.last_movement = Time.currentTime();
            u.afk = false;
            Network.getInstance().chat.broadcastMessage("&7" + u.player.getName() + " is no longer afk.", "uknet:global");

        } else {

            long time = Time.currentTime();

            //Update playtime, and pause it.
            Statistics.save(u, Time.getDate(time), time);

            u.afk = true;
            Network.getInstance().chat.broadcastMessage("&7" + u.player.getName() + " is now afk.", "uknet:global");

        }

        return true;

    }
}
