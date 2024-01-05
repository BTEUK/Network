package me.bteuk.network.commands.navigation;

import me.bteuk.network.Network;
import me.bteuk.network.commands.navigation.Back;
import me.bteuk.network.events.EventManager;
import me.bteuk.network.utils.SwitchServer;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static me.bteuk.network.utils.Constants.SERVER_NAME;

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

        //Get location name from all remaining args.
        String location = String.join(" ", Arrays.copyOfRange(args, 0, args.length));

        //Find a location.
        if (Network.getInstance().globalSQL.hasRow("SELECT location FROM location_data WHERE location='" + location + "';")) {

            //Get coordinate id.
            int coordinate_id = Network.getInstance().globalSQL.getInt("SELECT coordinate FROM location_data WHERE location='" + location + "';");

            //Get server, if server is not current server,
            // teleport the player to the correct server with join event to teleport them to the location.
            String server = Network.getInstance().globalSQL.getString("SELECT server FROM coordinates WHERE id=" + coordinate_id + ";");
            if (server.equals(SERVER_NAME)) {
                //Server is equal.

                //Get location from coordinate id.
                Location l = Network.getInstance().globalSQL.getCoordinate(coordinate_id);

                String worldName = Network.getInstance().globalSQL.getString("SELECT world FROM coordinates WHERE id=" + coordinate_id + ";");

                //Check if world is in plotsystem.
                if (Network.getInstance().getPlotSQL().hasRow("SELECT name FROM location_data WHERE name='" + worldName + "';")) {

                    //Add coordinate transformation.
                    l = new Location(
                            Bukkit.getWorld(worldName),
                            l.getX() + Network.getInstance().getPlotSQL().getInt("SELECT xTransform FROM location_data WHERE name='" + worldName + "';"),
                            l.getY(),
                            l.getZ() + Network.getInstance().getPlotSQL().getInt("SELECT zTransform FROM location_data WHERE name='" + worldName + "';"),
                            l.getYaw(),
                            l.getPitch()
                    );

                }

                //Set current location for /back
                Back.setPreviousCoordinate(p.getUniqueId().toString(), p.getLocation());

                //Teleport to location.
                p.teleport(l);
                p.sendMessage(Utils.success("Teleported to ")
                        .append(Component.text(location, NamedTextColor.DARK_AQUA)));

            } else {

                //Server is different.
                EventManager.createTeleportEvent(true, p.getUniqueId().toString(), "network",
                        "teleport location " + location, p.getLocation());

                SwitchServer.switchServer(p, server);
            }

        } else {
            p.sendMessage(Utils.error("The location ")
                    .append(Component.text(location, NamedTextColor.DARK_RED))
                    .append(Utils.error(" does not exist.")));
        }

        return true;
    }

    private void help(Player p) {
        p.sendMessage(Utils.error("/warp <location>"));
    }
}
