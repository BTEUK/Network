package me.bteuk.network.commands;

import me.bteuk.network.Network;
import me.bteuk.network.events.EventManager;
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

            //Set current location to previous location.
            setPreviousCoordinate(p.getUniqueId().toString(), p.getLocation());

            //Teleport player to the coordinate.
            p.teleport(l);
            p.sendMessage(Utils.chat("&aTeleported to previous location."));

        } else {

            //Teleport the player to the correct server with a join event to teleport to the coordinate id.
            EventManager.createTeleportEvent(true, p.getUniqueId().toString(), "network", "teleport coordinateID " + coordinateID, p.getLocation());

            //Switch server.
            SwitchServer.switchServer(p, server);

        }

        return true;
    }

    //Sets the location as the previous location in the database.
    public static void setPreviousCoordinate(String uuid, Location l) {

        //Set previous location for /back.
        if (Network.getInstance().globalSQL.getInt("SELECT previous_coordinate FROM player_data WHERE uuid='" + uuid + "';") == 0) {

            //No coordinate exists, create new.
            int coordinateID = Network.getInstance().globalSQL.addCoordinate(Network.SERVER_NAME, l);

            //Set coordinate id in player data.
            Network.getInstance().globalSQL.update("UPDATE player_data SET previous_coordinate=" + coordinateID + " WHERE uuid='" + uuid + "';");

        } else {

            //Get coordinate id.
            int coordinateID = Network.getInstance().globalSQL.getInt("SELECT previous_coordinate FROM player_data WHERE uuid='" + uuid + "';");

            //Update existing coordinate.
            Network.getInstance().globalSQL.updateCoordinate(coordinateID, Network.SERVER_NAME, l);

        }
    }
}

