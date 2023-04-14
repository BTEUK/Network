package me.bteuk.network.commands;

import me.bteuk.network.Network;
import me.bteuk.network.events.EventManager;
import me.bteuk.network.utils.SwitchServer;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static me.bteuk.network.utils.Constants.SERVER_NAME;

public class Tp implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Sender must be a player.
        if (!(sender instanceof Player p)) {

            sender.sendMessage(Utils.error("You must be a player to use this command."));
            return true;

        }

        //Check if args exist.
        if (args.length == 0) {

            p.sendMessage(Utils.error("You must specify a player to teleport to."));
            return true;

        }

        //Check whether the first arg is a valid player.
        if (Network.getInstance().globalSQL.hasRow("SELECT uuid FROM player_data WHERE name='" + args[0] + "';")) {

            //Get the uuid of the player.
            String uuid = Network.getInstance().globalSQL.getString("SELECT uuid FROM player_data WHERE name='" + args[0] + "';");

            //Check if the player is online.
            if (Network.getInstance().globalSQL.hasRow("SELECT uuid FROM online_users WHERE uuid='" + uuid + "';")) {

                //Check if the player has teleport enabled/disabled.
                //If disabled cancel teleport.
                //TODO add method to bypass teleport blocking for mods.
                if (Network.getInstance().globalSQL.hasRow("SELECT uuid FROM player_data WHERE uuid='" + uuid + "' AND teleport_enabled=1;") || p.hasPermission("uknet.navigation.teleport.bypass")) {

                    //If the player is on your server teleport.
                    //Else switch server and add teleport join event.
                    if (Network.getInstance().globalSQL.getString("SELECT server FROM online_users WHERE uuid='" + uuid + "';").equals(SERVER_NAME)) {

                        //Get player location.
                        Player player = Bukkit.getPlayer(UUID.fromString(uuid));

                        if (player != null) {

                            //Set current location for /back
                            Back.setPreviousCoordinate(p.getUniqueId().toString(), p.getLocation());

                            p.teleport(player.getLocation());
                            p.sendMessage(Utils.success("Teleported to ")
                                    .append(Component.text(args[0], NamedTextColor.DARK_AQUA)));

                        } else {
                            p.sendMessage(Component.text(args[0], NamedTextColor.DARK_RED)
                                    .append(Utils.error(" is no longer online.")));
                        }

                    } else {

                        EventManager.createTeleportEvent(true, p.getUniqueId().toString(), "network", "teleport player " + uuid, p.getLocation());
                        SwitchServer.switchServer(p, Network.getInstance().globalSQL.getString("SELECT server FROM online_users WHERE uuid='" + uuid + "';"));

                    }


                } else {
                    p.sendMessage(Component.text(args[0], NamedTextColor.DARK_RED)
                            .append(Utils.error(" has teleport disabled.")));
                }

            } else {
                p.sendMessage(Component.text(args[0], NamedTextColor.DARK_RED)
                        .append(Utils.error(" is not online.")));
            }

        } else {
            p.sendMessage(Component.text(args[0], NamedTextColor.DARK_RED)
                    .append(Utils.error(" does not exist.")));
        }
        return true;
    }
}
