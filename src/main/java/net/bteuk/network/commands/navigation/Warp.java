package net.bteuk.network.commands.navigation;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.bteuk.network.Network;
import net.bteuk.network.commands.AbstractCommand;
import net.bteuk.network.commands.tabcompleters.LocationSelector;
import net.bteuk.network.eventing.events.EventManager;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.utils.SwitchServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static net.bteuk.network.utils.Constants.SERVER_NAME;

public class Warp extends AbstractCommand {

    public Warp() {
        setTabCompleter(new LocationSelector());
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {

        //Check if the sender is a player.
        Player player = getPlayer(stack);
        if (player == null) {
            return;
        }

        if (args.length == 0) {
            help(player);
            return;
        }

        //Get location name from all remaining args.
        String location = String.join(" ", Arrays.copyOfRange(args, 0, args.length));

        //Find a location.
        if (Network.getInstance().getGlobalSQL().hasRow("SELECT location FROM location_data WHERE location='" + location + "';")) {

            //Get coordinate id.
            int coordinate_id = Network.getInstance().getGlobalSQL().getInt("SELECT coordinate FROM location_data WHERE location='" + location + "';");

            //Get server, if server is not current server,
            // teleport the player to the correct server with join event to teleport them to the location.
            String server = Network.getInstance().getGlobalSQL().getString("SELECT server FROM coordinates WHERE id=" + coordinate_id + ";");
            if (server.equals(SERVER_NAME)) {
                //Server is equal.

                //Get location from coordinate id.
                Location l = Network.getInstance().getGlobalSQL().getLocation(coordinate_id);

                String worldName = Network.getInstance().getGlobalSQL().getString("SELECT world FROM coordinates WHERE id=" + coordinate_id + ";");

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
                Back.setPreviousCoordinate(player.getUniqueId().toString(), player.getLocation());

                //Teleport to location.
                player.teleport(l);
                player.sendMessage(ChatUtils.success("Teleported to ")
                        .append(Component.text(location, NamedTextColor.DARK_AQUA)));

            } else {

                //Server is different.
                EventManager.createTeleportEvent(true, player.getUniqueId().toString(), "network",
                        "teleport location " + location, player.getLocation());

                SwitchServer.switchServer(player, server);
            }

        } else {
            player.sendMessage(ChatUtils.error("The location ")
                    .append(Component.text(location, NamedTextColor.DARK_RED))
                    .append(ChatUtils.error(" does not exist.")));
        }
    }

    private void help(Player p) {
        p.sendMessage(ChatUtils.error("/warp <location>"));
    }
}
