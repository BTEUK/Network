package net.bteuk.network.commands.navigation;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.bteuk.network.Network;
import net.bteuk.network.commands.AbstractCommand;
import net.bteuk.network.commands.tabcompleters.PlayerSelector;
import net.bteuk.network.eventing.events.EventManager;
import net.bteuk.network.lib.dto.OnlineUser;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.utils.NetworkUser;
import net.bteuk.network.utils.SwitchServer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class Tp extends AbstractCommand {

    public Tp() {
        setTabCompleter(new PlayerSelector());
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {

        //Check if the sender is a player.
        Player player = getPlayer(stack);
        if (player == null) {
            return;
        }

        //Check if args exist.
        if (args.length == 0) {
            player.sendMessage(ChatUtils.error("You must specify a player to teleport to."));
            return;
        }

        // Try and find the player by name.
        Optional<OnlineUser> optionalOnlineUser = Network.getInstance().getOnlineUserByNameIgnoreCase(args[0]);
        if (optionalOnlineUser.isPresent()) {

            OnlineUser onlineUser = optionalOnlineUser.get();

            //Check if the player has teleport enabled/disabled.
            //If disabled cancel teleport.
            if (Network.getInstance().getGlobalSQL().hasRow("SELECT uuid FROM player_data WHERE uuid='" + onlineUser.getUuid() + "' AND teleport_enabled=1;") || player.hasPermission("uknet.navigation.teleport.bypass")) {

                //If the player is on your server teleport.
                //Else switch server and add teleport join event.
                Optional<NetworkUser> optionalNetworkUser = Network.getInstance().getNetworkUserByUuid(onlineUser.getUuid());
                if (optionalNetworkUser.isPresent()) {

                    Player playerToTeleportTo = optionalNetworkUser.get().player;

                    //Set current location for /back
                    Back.setPreviousCoordinate(player.getUniqueId().toString(), player.getLocation());

                    player.teleport(playerToTeleportTo.getLocation());
                    player.sendMessage(ChatUtils.success("Teleported to %s", onlineUser.getName()));

                } else {
                    EventManager.createTeleportEvent(true, player.getUniqueId().toString(), "network", "teleport player " + onlineUser.getUuid(), player.getLocation());
                    SwitchServer.switchServer(player, onlineUser.getServer());
                }
            } else {
                player.sendMessage(ChatUtils.error("%s has teleport disabled.", onlineUser.getName()));
            }

        } else {
            player.sendMessage(ChatUtils.error("%s is not online.", args[0]));
        }
    }
}
