package net.bteuk.network.lobby;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.bteuk.network.Network;
import net.bteuk.network.commands.AbstractCommand;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.utils.enums.ServerType;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.bteuk.network.utils.Constants.SERVER_TYPE;

public class LobbyCommand extends AbstractCommand {

    private static final Component INVALID_FORMAT = ChatUtils.error("/lobby reload portals");
    private final Lobby lobby;

    public LobbyCommand(Network instance) {
        this.lobby = instance.getLobby();
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {

        // Check permission if player, or if the server is the lobby.
        CommandSender sender = stack.getSender();
        if (!sender.hasPermission("uknet.lobby.reload") || SERVER_TYPE != ServerType.LOBBY) {
            if (sender instanceof Player p) {
                p.performCommand("spawn");
            }
            return;
        }

        // Check args.
        if (args.length < 2 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(INVALID_FORMAT);
        } else if (args[1].equalsIgnoreCase("portals")) {

            if (!hasPermission(sender, "uknet.lobby.reload.portals")) {
                return;
            }

            lobby.reloadPortals();
            sender.sendMessage(ChatUtils.success("Reloaded portals"));
        } else {
            sender.sendMessage(INVALID_FORMAT);
        }
    }
}
