package net.bteuk.network.commands.navigation;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.bteuk.network.Network;
import net.bteuk.network.commands.AbstractCommand;
import net.bteuk.network.eventing.events.EventManager;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.utils.SwitchServer;
import net.bteuk.network.utils.enums.ServerType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.bteuk.network.utils.Constants.SERVER_TYPE;

public class Spawn extends AbstractCommand {

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {

        // Check if the sender is a player.
        Player player = getPlayer(stack);
        if (player == null) {
            return;
        }

        // Check permission.
        if (!hasPermission(player, "uknet.navigation.spawn")) {
            return;
        }

        // If server is Lobby, teleport to spawn.
        if (SERVER_TYPE == ServerType.LOBBY) {

            Back.setPreviousCoordinate(player.getUniqueId().toString(), player.getLocation());
            player.teleport(Network.getInstance().getLobby().spawn);
            player.sendMessage(ChatUtils.success("Teleported to spawn."));
        } else {

            // Set teleport event to go to spawn.
            EventManager.createTeleportEvent(true, player.getUniqueId().toString(), "network", "teleport spawn",
                    player.getLocation());
            SwitchServer.switchServer(player, Network.getInstance().getGlobalSQL().getString("SELECT name FROM " +
                    "server_data WHERE type='LOBBY';"));
        }
    }
}
