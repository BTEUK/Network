package me.bteuk.network.commands;

import me.bteuk.network.Network;
import me.bteuk.network.lobby.Lobby;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class LobbyCommand extends AbstractCommand {

    private final Lobby lobby;

    private static final Component INVALID_FORMAT = Utils.error("/lobby reload portals|map");

    public LobbyCommand(Network instance, Lobby lobby) {
        super(instance, "lobby");
        this.lobby = lobby;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Check permission if player.
        if (!hasPermission(sender, "uknet.lobby.reload")) {
            return true;
        }

        //Check args.
        if (args.length < 2 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(INVALID_FORMAT);
        } else if (args[1].equalsIgnoreCase("portals")) {

            if (!hasPermission(sender, "uknet.lobby.reload.portals")) {
                return true;
            }

            lobby.reloadPortals();
            sender.sendMessage(Utils.success("Reloaded portals"));

        } else if (args[1].equalsIgnoreCase("map")) {

            if (!hasPermission(sender, "uknet.lobby.reload.map")) {
                return true;
            }

            lobby.reloadMap();
            sender.sendMessage(Utils.success("Reloaded map"));

        } else {
            sender.sendMessage(INVALID_FORMAT);
        }

        return true;

    }
}
