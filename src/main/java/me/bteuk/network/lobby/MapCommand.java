package me.bteuk.network.lobby;

import me.bteuk.network.Network;
import me.bteuk.network.commands.AbstractCommand;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Map command class, adds commands to teleport to the map.
 * For players with the relevant permission allows them to add markers to the map.
 */
public class MapCommand extends AbstractCommand {

    private final Map map;

    private static final Component INVALID_USAGE = Utils.error("/map [add/remove] [warp/subcategory]");

    protected MapCommand(Network instance, Map map, String commandName) {
        super(instance, commandName);
        this.map = map;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!map.isEnabled())  {
            sender.sendMessage(Utils.error("The map is not enabled."));
            return true;
        }

        Player p = getPlayer(sender);
        if (p == null) {
            return true;
        }

        /*
        Default command teleports the player to the map.
        Will be executed if the player does not have sufficient permissions to use the subcommands,
        or if no arguments are given.
         */
        if (!p.hasPermission("uknet.navigation.map") || args.length == 0) {
            map.teleport(p);
            return true;
        } else if (args.length < 2) {
            p.sendMessage(INVALID_USAGE);
            return true;
        }

        switch (args[1]) {
            case "add" -> map.addMarker(p, args[2]);
            case "remove" -> map.removeMarker(p, args[2]);
            default -> p.sendMessage(INVALID_USAGE);
        }
        return true;
    }
}
