package net.bteuk.network.commands.navigation;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.bteuk.network.Network;
import net.bteuk.network.commands.AbstractCommand;
import net.bteuk.network.commands.tabcompleters.HomeSelector;
import net.bteuk.network.eventing.events.EventManager;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.sql.GlobalSQL;
import net.bteuk.network.utils.SwitchServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

import static net.bteuk.network.utils.Constants.SERVER_NAME;

public class Home extends AbstractCommand {

    private final GlobalSQL globalSQL;

    // Constructor to enable the command.
    public Home(Network instance) {

        this.globalSQL = instance.getGlobalSQL();

        // Set tab completer.
        setTabCompleter(new HomeSelector());
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {

        // Check if the sender is a player.
        Player player = getPlayer(stack);
        if (player == null) {
            return;
        }

        // If no args teleport to default home, if exists.
        // Else try to set homes with specific names.
        // For multiple homes the player needs permission.
        if (args.length == 0) {

            // If a default home is set, teleport to it.
            if (!globalSQL.hasRow(
                    "SELECT uuid FROM home WHERE uuid='" + player.getUniqueId() + "' AND name IS NULL;")) {
                player.sendMessage(ChatUtils.error("You do not have a default home set, you can set it typing ")
                        .append(Component.text("/sethome", NamedTextColor.DARK_RED)));
                return;
            }

            // Get coordinate ID.
            int coordinate_id =
                    globalSQL.getInt("SELECT coordinate_id FROM home WHERE uuid='" + player.getUniqueId() + "' AND " +
                            "name IS NULL;");

            // Get server.
            String server = globalSQL.getString("SELECT server FROM coordinates WHERE id=" + coordinate_id + ";");

            // Check if server is current.
            if (Objects.equals(SERVER_NAME, server)) {

                // Get default home location from the coordinate id.
                Location l = globalSQL.getLocation(coordinate_id);

                // Teleport to the location.
                player.teleport(l);
                player.sendMessage(ChatUtils.success("Teleported to your default home."));
            } else {

                // Switch server with join event.
                EventManager.createTeleportEvent(true, player.getUniqueId().toString(), "network",
                        "teleport coordinateID " + coordinate_id,
                        "&aTeleported to your default home.",
                        player.getLocation());

                // Switch server.
                SwitchServer.switchServer(player, server);
            }
        } else {

            // Check for permission.
            if (!player.hasPermission("uknet.navigation.homes")) {
                player.sendMessage(ChatUtils.error("You do not have permission to set multiple homes, you can only " +
                                "use your default home with ")
                        .append(Component.text("/home", NamedTextColor.DARK_RED)));
                return;
            }

            // Check if a home with this name already exists.
            String name = String.join(" ", Arrays.copyOfRange(args, 0, args.length));

            // Check if home with this name exists.
            if (!globalSQL.hasRow(
                    "SELECT uuid FROM home WHERE uuid='" + player.getUniqueId() + "' AND name='" + name + "';")) {
                player.sendMessage(ChatUtils.error("You do not have a home with the name ")
                        .append(Component.text(name, NamedTextColor.DARK_RED)));
                return;
            }

            // Get coordinate ID.
            int coordinate_id =
                    globalSQL.getInt("SELECT coordinate_id FROM home WHERE uuid='" + player.getUniqueId() + "' AND " +
                            "name='" + name + "';");

            // Get server.
            String server = globalSQL.getString("SELECT server FROM coordinates WHERE id=" + coordinate_id + ";");

            // Check if server is current.
            if (Objects.equals(SERVER_NAME, server)) {

                // Get default home location from the coordinate id.
                Location l = globalSQL.getLocation(coordinate_id);

                // Teleport to the location.
                player.teleport(l);
                player.sendMessage(ChatUtils.success("Teleported to your home ")
                        .append(Component.text(name, NamedTextColor.DARK_AQUA)));
            } else {

                // Switch server with join event.
                EventManager.createTeleportEvent(true, player.getUniqueId().toString(), "network",
                        "teleport coordinateID " + coordinate_id,
                        "&aTeleported to your home &3" + name + "&a.",
                        player.getLocation());

                // Switch server.
                SwitchServer.switchServer(player, server);
            }
        }
    }
}
