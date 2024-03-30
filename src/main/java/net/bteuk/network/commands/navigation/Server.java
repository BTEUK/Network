package net.bteuk.network.commands.navigation;

import net.bteuk.network.Network;
import net.bteuk.network.utils.SwitchServer;
import net.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

import static net.bteuk.network.utils.Constants.SERVER_NAME;

public class Server implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Check if sender is a player and that they have permission.
        if (!(sender instanceof Player p)) {

            sender.sendMessage(Utils.error("This command can only be used by a player."));
            return true;

        }

        //Check if the player has permission.
        if (!p.hasPermission("uknet.navigation.server")) {

            p.sendMessage(Utils.error("You do not have permission to use this command."));
            return true;

        }

        //If no args are given send a clickable list of servers.
        //Sort by online servers and highlight the player's current server.
        if (args.length == 0) {
            ArrayList<String> servers = Network.getInstance().getGlobalSQL().getStringList("SELECT name FROM server_data ORDER BY online DESC");

            Component message = Component.text("Available servers: ", NamedTextColor.GREEN);

            for (int i = 0; i < servers.size(); i++) {

                //If the server is the current server highlight it.
                //If it's offline, grey it out.
                Component server;
                if (Objects.equals(SERVER_NAME, servers.get(i))) {
                    server = Component.text(servers.get(i), TextColor.color(245, 173, 100));
                    server = server.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Utils.line("You are connected to this server.")));
                } else if (Network.getInstance().getGlobalSQL().hasRow("SELECT name FROM server_data WHERE name='" + servers.get(i) + "' AND online=0;")) {
                    server = Component.text(servers.get(i), NamedTextColor.GRAY);
                    server = server.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Utils.line("This server is offline.")));
                }else {
                    server = Component.text(servers.get(i), TextColor.color(245, 221, 100));
                    server = server.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Utils.line("Click to teleport to " + servers.get(i))));
                    server = server.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/server " + servers.get(i)));
                }

                message = message.append(server);

                //If it's the last item, don't add a comma.
                if (i < (servers.size() - 1)) {

                    message = message.append(Utils.line(", "));

                }
            }

            p.sendMessage(message);

            return true;
        } else {

            //Teleport to the server of the first arg.
            //If that is not a server notify the player.
            if (Network.getInstance().getGlobalSQL().hasRow("SELECT name FROM server_data WHERE name='" + args[0] + "';")) {

                //Check if the player is not already on this server.
                if (Objects.equals(SERVER_NAME, args[0])) {

                    p.sendMessage(Utils.error("You are already on this server."));

                } else {

                    SwitchServer.switchServer(p, args[0]);

                }

            } else {

                p.sendMessage(Utils.error("The server ").append(Component.text(args[0], NamedTextColor.DARK_RED).append(Utils.error(" does not exist!"))));

            }

            return true;

        }
    }
}
