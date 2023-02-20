package me.bteuk.network.commands;

import me.bteuk.network.Network;
import me.bteuk.network.events.EventManager;
import me.bteuk.network.utils.SwitchServer;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.enums.ServerType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Spawn implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Check if the sender is a player.
        if (!(sender instanceof Player p)) {

            sender.sendMessage(Utils.error("This command can only be run by a player."));
            return true;

        }

        //Check permission.
        if (!p.hasPermission("uknet.navigation.spawn")) {

            p.sendMessage(Utils.error("You do not have permission to use this command."));
            return true;

        }

        //If server is Lobby, teleport to spawn.
        if (Network.SERVER_TYPE == ServerType.LOBBY) {

            Back.setPreviousCoordinate(p.getUniqueId().toString(), p.getLocation());
            p.teleport(Network.getInstance().getLobby().spawn);
            p.sendMessage(Utils.success("Teleported to spawn."));

        } else {

            //Set teleport event to go to spawn.
            EventManager.createTeleportEvent(true, p.getUniqueId().toString(), "network", "teleport spawn", p.getLocation());
            SwitchServer.switchServer(p, Network.getInstance().globalSQL.getString("SELECT name FROM server_data WHERE type='LOBBY';"));

        }

        return true;

    }
}
