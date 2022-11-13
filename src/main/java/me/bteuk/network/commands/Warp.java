package me.bteuk.network.commands;

import me.bteuk.network.Network;
import me.bteuk.network.events.EventManager;
import me.bteuk.network.utils.SwitchServer;
import me.bteuk.network.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class Warp implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Check if sender is player.
        if (!(sender instanceof Player p)) {
            sender.sendMessage(Utils.error("You must be a player to use this command."));
            return true;
        }

        if (args.length == 0) {
            help(p);
            return true;
        }

        //Check if first arg is list.
        if (args[0].equalsIgnoreCase("list")) {
            //TODO /warp list
        } else {

            //Get location name from all remaining args.
            String location = String.join(" ", Arrays.copyOfRange(args, 0, args.length));

            //Find a location.
            if (Network.getInstance().globalSQL.hasRow("SELECT location FROM location_data WHERE location='" + location + "';")) {

                //Get coordinate id.
                int coordinate_id = Network.getInstance().globalSQL.getInt("SELECT coordinate FROM location_data WHERE location='" + location + "';");

                //Get server, if server is not current server,
                // teleport the player to the correct server with join event to teleport them to the location.
                String server = Network.getInstance().globalSQL.getString("SELECT server FROM coordinates WHERE id=" + coordinate_id + ";");
                if (server.equals(Network.SERVER_NAME)) {
                    //Server is equal.
                    //Teleport to location.
                    p.teleport(Network.getInstance().globalSQL.getCoordinate(coordinate_id));
                    p.sendMessage(Utils.success("Teleported to &3" + location));
                } else {
                    //Server is different.
                    EventManager.createJoinEvent(p.getUniqueId().toString(),"network",
                            "teleport location " + location + " " + Network.SERVER_NAME);
                    SwitchServer.switchServer(p, server);
                }

            } else {
                p.sendMessage(Utils.error("The location &4" + location + " &cdoes not exist."));
            }
        }
        return true;
    }

    private void help(Player p) {
        p.sendMessage(Utils.error("/warp <location>/list"));
    }
}
