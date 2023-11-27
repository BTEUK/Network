package me.bteuk.network.commands;

import me.bteuk.network.Network;
import me.bteuk.network.lobby.Lobby;
import me.bteuk.network.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Portals extends AbstractCommand {

    private final Lobby lobby;

    public Portals(Network instance, Lobby lobby) {
        super(instance, "portals");
        this.lobby = lobby;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Check permission if player.
        if (sender instanceof Player p) {
            if (!p.hasPermission("uknet.lobby.portals.reload")) {
                sender.sendMessage(NO_PERMISSION);
                return true;
            }
        }

        if (args.length > 0) {
            if (args[0].equals("reload")) {
                lobby.reloadPortals();
                sender.sendMessage(Utils.success("Reloaded portals"));
            } else {
                error(sender);
            }
        } else {
            error(sender);
        }
        return true;
    }

    private void error(CommandSender sender) {
        sender.sendMessage(Utils.error("/portals reload"));
    }
}
