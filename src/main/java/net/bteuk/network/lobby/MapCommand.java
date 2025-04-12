package net.bteuk.network.lobby;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.bteuk.network.commands.AbstractCommand;
import net.bteuk.network.commands.tabcompleters.LocationAndSubcategorySelector;
import net.bteuk.network.lib.utils.ChatUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

import static net.bteuk.network.utils.Constants.SERVER_NAME;

/**
 * Map command class, adds commands to teleport to the map.
 * For players with the relevant permission allows them to add markers to the map.
 */
public class MapCommand extends AbstractCommand {

    private static final Component INVALID_USAGE = ChatUtils.error("/map [add/remove] [warp/subcategory]");
    private final Map map;
    private final String server;

    protected MapCommand(Map map, String server) {
        this.map = map;
        this.server = server;
        setTabCompleter(new LocationAndSubcategorySelector(1));
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {

        if (!map.isEnabled()) {
            stack.getSender().sendMessage(ChatUtils.error("The map is not enabled."));
            return;
        }

        // Check if the sender is a player.
        Player player = getPlayer(stack);
        if (player == null) {
            return;
        }

        /*
        Default command teleports the player to the map.
        Will be executed if the player does not have sufficient permissions to use the subcommands,
        or if no arguments are given.
         */
        if (!player.hasPermission("uknet.navigation.map") || args.length == 0) {
            map.teleport(player);
            return;
        } else if (args.length < 2) {
            player.sendMessage(INVALID_USAGE);
            return;
        }

        if (Objects.equals(server, SERVER_NAME)) {
            switch (args[0]) {
                case "add" -> player.sendMessage(map.addMarker(player.getLocation(), String.join(" ",
                        Arrays.copyOfRange(args, 1, args.length))));
                case "remove" -> player.sendMessage(map.removeMarker(String.join(" ",
                        Arrays.copyOfRange(args, 1, args.length))));
                default -> player.sendMessage(INVALID_USAGE);
            }
        } else {
            player.sendMessage(ChatUtils.error("Map markers can only be added/removed in the same server as the map."));
        }
    }
}
