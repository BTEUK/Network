package me.bteuk.network.commands;

import me.bteuk.network.Network;
import me.bteuk.network.utils.SwitchServer;
import me.bteuk.network.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class Tp implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Sender must be a player.
        if (!(sender instanceof Player p)) {

            sender.sendMessage(Utils.chat("&cYou must be a player to use this command."));
            return true;

        }

        //Check if args exist.
        if (args.length == 0) {

            p.sendMessage(Utils.chat("&cYou must specify a player to teleport to."));
            return true;

        }

        //Check whether the first arg is a valid player.
        if (Network.getInstance().globalSQL.hasRow("SELECT uuid FROM player_data WHERE name='" + args[0] + "';")) {

            //Get the uuid of the player.
            String uuid = Network.getInstance().globalSQL.getString("SELECT uuid FROM player_dat WHERE name='" + args[0] + "';");

            //Check if the player is online.
            if (Network.getInstance().globalSQL.hasRow("SELECT uuid FROM online_users WHERE uuid='" + uuid + "';")) {

                //Check if the player has teleport enabled/disabled.
                //If disabled cancel teleport.
                if (Network.getInstance().globalSQL.hasRow("SELECT uuid FROM player_data WHERE uuid='" + uuid + "' AND teleport_enabled=1;")) {

                    //If the player is on your server teleport.
                    //Else switch server and add teleport join event.
                    if (Network.getInstance().globalSQL.getString("SELECT server FROM online_users WHERE uuid='" + uuid + "';").equals(Network.SERVER_NAME)) {

                        //Get player location.
                        Player player = Bukkit.getPlayer(UUID.fromString(uuid));

                        if (player != null) {

                            p.teleport(player.getLocation());
                            p.sendMessage(Utils.chat("&aTeleported to " +
                                    args[0] + "';"));

                        } else {
                            p.sendMessage(Utils.chat("&c" + args[0] + " is no longer online."));
                        }

                    } else {

                        Network.getInstance().globalSQL.update("INSERT INTO join_events(uuid,type,event) VALUES('" + p.getUniqueId() + "','network'," + "'teleport player " + uuid + "');");
                        SwitchServer.switchServer(p, Network.getInstance().globalSQL.getString("SELECT server FROM online_users WHERE uuid='" + uuid + "';"));

                    }


                } else {
                    p.sendMessage(Utils.chat("&c" + args[0] + " has teleport disabled."));
                }

            } else {
                p.sendMessage(Utils.chat("&c" + args[0] + " is not online."));
            }

        } else {
            p.sendMessage(Utils.chat("&c" + args[0] + " does not exist."));
        }
        return true;
    }
}