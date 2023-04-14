package me.bteuk.network.commands;

import me.bteuk.network.events.EventManager;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.utils.SwitchServer;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

import static me.bteuk.network.utils.Constants.SERVER_NAME;

public class Home implements CommandExecutor {
    private final GlobalSQL globalSQL;

    public Home(GlobalSQL globalSQL) {
        this.globalSQL = globalSQL;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Check if the sender is a player.
        if (!(sender instanceof Player p)) {
            sender.sendMessage(Utils.error("This command can only be used by players."));
            return true;
        }

        //If no args teleport to default home, if exists.
        //Else try to set homes with specific names.
        //For multiple homes the player needs permission.
        if (args.length == 0) {

            //If a default home is set, teleport to it.
            if (!globalSQL.hasRow("SELECT uuid FROM home WHERE uuid='" + p.getUniqueId() + "' AND name IS NULL;")) {
                p.sendMessage(Utils.error("You do not have a default home set, you can set it typing ")
                        .append(Component.text("/sethome", NamedTextColor.DARK_RED)));
                return true;
            }

            //Get coordinate ID.
            int coordinate_id = globalSQL.getInt("SELECT coordinate_id FROM home WHERE uuid='" + p.getUniqueId() + "' AND name IS NULL;");

            //Get server.
            String server = globalSQL.getString("SELECT server FROM coordinates WHERE id=" + coordinate_id + ";");

            //Check if server is current.
            if (Objects.equals(SERVER_NAME, server)) {

                //Get default home location from the coordinate id.
                Location l = globalSQL.getCoordinate(coordinate_id);

                //Teleport to the location.
                p.teleport(l);
                p.sendMessage(Utils.success("Teleported to your default home."));

            } else {

                //Switch server with join event.
                EventManager.createTeleportEvent(true, p.getUniqueId().toString(), "network",
                        "teleport coordinateID " + coordinate_id,
                        "&aTeleported to your default home.",
                        p.getLocation());

                //Switch server.
                SwitchServer.switchServer(p, server);

            }
        } else {

            //Check for permission.
            if (!p.hasPermission("uknet.navigation.homes")) {
                p.sendMessage(Utils.error("You do not have permission to set multiple homes, you can only use your default home with ")
                        .append(Component.text("/home", NamedTextColor.DARK_RED)));
                return true;
            }

            //Check if a home with this name already exists.
            String name = String.join(" ", Arrays.copyOfRange(args, 0, args.length));

            //Check if home with this name exists.
            if (!globalSQL.hasRow("SELECT uuid FROM home WHERE uuid='" + p.getUniqueId() + "' AND name='" + name + "';")) {
                p.sendMessage(Utils.error("You do not have a home with the name ")
                        .append(Component.text(name, NamedTextColor.DARK_RED)));
                return true;
            }

            //Get coordinate ID.
            int coordinate_id = globalSQL.getInt("SELECT coordinate_id FROM home WHERE uuid='" + p.getUniqueId() + "' AND name='" + name + "';");

            //Get server.
            String server = globalSQL.getString("SELECT server FROM coordinates WHERE id=" + coordinate_id + ";");

            //Check if server is current.
            if (Objects.equals(SERVER_NAME, server)) {

                //Get default home location from the coordinate id.
                Location l = globalSQL.getCoordinate(coordinate_id);

                //Teleport to the location.
                p.teleport(l);
                p.sendMessage(Utils.success("Teleported to your home ")
                        .append(Component.text(name, NamedTextColor.DARK_AQUA)));

            } else {

                //Switch server with join event.
                EventManager.createTeleportEvent(true, p.getUniqueId().toString(), "network",
                        "teleport coordinateID " + coordinate_id,
                        "&aTeleported to your home &3" + name + "&a.",
                        p.getLocation());

                //Switch server.
                SwitchServer.switchServer(p, server);

            }

        }

        return true;

    }
}
