package net.bteuk.network.commands.navigation;

import net.bteuk.network.Network;
import net.bteuk.network.eventing.events.EventManager;
import net.bteuk.network.lib.dto.OnlineUser;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.utils.NetworkUser;
import net.bteuk.network.utils.SwitchServer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

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

        // Try and find the player by name.
        Optional<OnlineUser> optionalOnlineUser = Network.getInstance().getOnlineUserByNameIgnoreCase(args[0]);
        if (optionalOnlineUser.isPresent()) {

            OnlineUser onlineUser = optionalOnlineUser.get();

            //Check if the player has teleport enabled/disabled.
            //If disabled cancel teleport.
            if (Network.getInstance().getGlobalSQL().hasRow("SELECT uuid FROM player_data WHERE uuid='" + onlineUser.getUuid() + "' AND teleport_enabled=1;") || p.hasPermission("uknet.navigation.teleport.bypass")) {

                //If the player is on your server teleport.
                //Else switch server and add teleport join event.
                Optional<NetworkUser> optionalNetworkUser = Network.getInstance().getNetworkUserByUuid(onlineUser.getUuid());
                if (optionalNetworkUser.isPresent()) {

                    Player playerToTeleportTo = optionalNetworkUser.get().player;

                    //Set current location for /back
                    Back.setPreviousCoordinate(p.getUniqueId().toString(), p.getLocation());

                    p.teleport(playerToTeleportTo.getLocation());
                    p.sendMessage(ChatUtils.success("Teleported to %s", onlineUser.getName()));

                } else {
                    EventManager.createTeleportEvent(true, p.getUniqueId().toString(), "network", "teleport player " + onlineUser.getUuid(), p.getLocation());
                    SwitchServer.switchServer(p, onlineUser.getServer());
                }
            } else {
                p.sendMessage(ChatUtils.error("%s has teleport disabled.", onlineUser.getName()));
            }

        } else {
            p.sendMessage(ChatUtils.error("%s is not online.", args[0]));
        }

        return true;
    }
}
