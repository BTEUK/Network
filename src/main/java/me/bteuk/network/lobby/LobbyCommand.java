package me.bteuk.network.lobby;

import me.bteuk.network.Network;
import me.bteuk.network.commands.AbstractCommand;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.enums.ServerType;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static me.bteuk.network.utils.Constants.SERVER_TYPE;

public class LobbyCommand extends AbstractCommand {

    private final Lobby lobby;

    private static final Component INVALID_FORMAT = Utils.error("/lobby reload portals");

    public LobbyCommand(Network instance, Lobby lobby) {
        super(instance, "lobby");
        this.lobby = lobby;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Check permission if player, or if the server is the lobby.
        if (!hasPermission(sender, "uknet.lobby.reload") || SERVER_TYPE != ServerType.LOBBY) {
            if (sender instanceof Player p) {
                p.performCommand("spawn");
            }
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

        } else {
            sender.sendMessage(INVALID_FORMAT);
        }

        return true;

    }
}
