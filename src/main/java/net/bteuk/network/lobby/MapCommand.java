package net.bteuk.network.lobby;

import net.bteuk.network.Network;
import net.bteuk.network.commands.AbstractCommand;
import net.bteuk.network.commands.tabcompleters.LocationAndSubcategorySelector;
import net.bteuk.network.lib.utils.ChatUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Map command class, adds commands to teleport to the map.
 * For players with the relevant permission allows them to add markers to the map.
 */
public class MapCommand extends AbstractCommand {

    private final Map map;

    private static final Component INVALID_USAGE = ChatUtils.error("/map [add/remove] [warp/subcategory]");

    protected MapCommand(Network instance, Map map, String commandName) {
        super(instance, commandName);
        this.map = map;
        command.setTabCompleter(new LocationAndSubcategorySelector(1));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!map.isEnabled())  {
            sender.sendMessage(ChatUtils.error("The map is not enabled."));
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

        switch (args[0]) {
            case "add" -> p.sendMessage(map.addMarker(p.getLocation(), String.join(" ", Arrays.copyOfRange(args, 1, args.length))));
            case "remove" -> p.sendMessage(map.removeMarker(String.join(" ", Arrays.copyOfRange(args, 1, args.length))));
            default -> p.sendMessage(INVALID_USAGE);
        }
        return true;
    }
}
