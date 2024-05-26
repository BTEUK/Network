package net.bteuk.network.commands.navigation;

import net.bteuk.network.Network;
import net.bteuk.network.eventing.events.EventManager;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.utils.SwitchServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static net.bteuk.network.utils.Constants.SERVER_NAME;

public class Tp implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Sender must be a player.
        if (!(sender instanceof Player p)) {

            sender.sendMessage(ChatUtils.error("You must be a player to use this command."));
            return true;

        }

        //Check if args exist.
        if (args.length == 0) {

            p.sendMessage(ChatUtils.error("You must specify a player to teleport to."));
            return true;

        }

        //Check whether the first arg is a valid player.
        if (Network.getInstance().getGlobalSQL().hasRow("SELECT uuid FROM player_data WHERE name='" + args[0] + "';")) {

            //Get the uuid of the player.
            String uuid = Network.getInstance().getGlobalSQL().getString("SELECT uuid FROM player_data WHERE name='" + args[0] + "';");

            //Check if the player is online.
            if (Network.getInstance().getGlobalSQL().hasRow("SELECT uuid FROM online_users WHERE uuid='" + uuid + "';")) {

                //Check if the player has teleport enabled/disabled.
                //If disabled cancel teleport.
                if (Network.getInstance().getGlobalSQL().hasRow("SELECT uuid FROM player_data WHERE uuid='" + uuid + "' AND teleport_enabled=1;") || p.hasPermission("uknet.navigation.teleport.bypass")) {

                    //If the player is on your server teleport.
                    //Else switch server and add teleport join event.
                    if (Network.getInstance().getGlobalSQL().getString("SELECT server FROM online_users WHERE uuid='" + uuid + "';").equals(SERVER_NAME)) {

                        //Get player location.
                        Player player = Bukkit.getPlayer(UUID.fromString(uuid));

                        if (player != null) {

                            //Set current location for /back
                            Back.setPreviousCoordinate(p.getUniqueId().toString(), p.getLocation());

                            p.teleport(player.getLocation());
                            p.sendMessage(ChatUtils.success("Teleported to ")
                                    .append(Component.text(args[0], NamedTextColor.DARK_AQUA)));

                        } else {
                            p.sendMessage(Component.text(args[0], NamedTextColor.DARK_RED)
                                    .append(ChatUtils.error(" is no longer online.")));
                        }

                    } else {

                        EventManager.createTeleportEvent(true, p.getUniqueId().toString(), "network", "teleport player " + uuid, p.getLocation());
                        SwitchServer.switchServer(p, Network.getInstance().getGlobalSQL().getString("SELECT server FROM online_users WHERE uuid='" + uuid + "';"));

                    }


                } else {
                    p.sendMessage(Component.text(args[0], NamedTextColor.DARK_RED)
                            .append(ChatUtils.error(" has teleport disabled.")));
                }

            } else {
                p.sendMessage(Component.text(args[0], NamedTextColor.DARK_RED)
                        .append(ChatUtils.error(" is not online.")));
            }

        } else {
            p.sendMessage(Component.text(args[0], NamedTextColor.DARK_RED)
                    .append(ChatUtils.error(" does not exist.")));
        }
        return true;
    }
}
