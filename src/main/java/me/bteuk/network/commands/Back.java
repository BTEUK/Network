package me.bteuk.network.commands;

import me.bteuk.network.Network;
import me.bteuk.network.utils.SwitchServer;
import me.bteuk.network.utils.Utils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Back implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Check if the sender is a player.
        if (!(sender instanceof Player p)) {

            sender.sendMessage(Utils.chat("&cThis command can only be used by a player."));
            return true;

        }

        //Get the coordinate ID.
        int coordinateID = Network.getInstance().globalSQL.getInt("SELECT previous_coordinate FROM player_data WHERE uuid='" + p.getUniqueId() + "';");

        //Check if the player has a previous coordinate.
        if (coordinateID == 0) {

            p.sendMessage(Utils.chat("&cYou have not teleported anywhere previously."));
            return true;

        }

        //Check if the server is this server.
        String server = Network.getInstance().globalSQL.getString("SELECT server FROM coordinates WHERE id=" + coordinateID + ";");
        if (Network.SERVER_NAME.equals(server)) {

            //Get location.
            Location l = Network.getInstance().globalSQL.getCoordinate(coordinateID);

            //Teleport player to the coordinate.
            p.teleport(l);
            p.sendMessage(Utils.chat("&aTeleported to previous location."));

        } else {

            //Teleport the player to the correct server with a join event to teleport to the coordinate id.
            Network.getInstance().globalSQL.update("INSERT INTO join_events(uuid,type,event) VALUES('" + p.getUniqueId() + "','network','teleport coordinateID "
                    + coordinateID + "');");

            //Switch server.
            SwitchServer.switchServer(p, server);

        }

        return true;
    }
}

