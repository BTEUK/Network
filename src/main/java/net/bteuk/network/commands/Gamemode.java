package net.bteuk.network.commands;

import net.bteuk.network.Network;
import net.bteuk.network.utils.Utils;
import net.bteuk.network.utils.enums.ServerType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.GameMode;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.bteuk.network.utils.Constants.LOGGER;
import static net.bteuk.network.utils.Constants.SERVER_TYPE;

public class Gamemode implements CommandExecutor, TabCompleter {

    //Gamemodes.
    private final ArrayList<String> gamemodes;

    //Constructor to enable the command.
    public Gamemode(Network instance) {

        gamemodes = new ArrayList<>(Arrays.asList("creative", "spectator", "adventure", "survival"));

        //Register command.
        PluginCommand command = instance.getCommand("gamemode");

        if (command == null) {
            LOGGER.warning("Gamemode command not added to plugin.yml, it will therefore not be enabled.");
            return;
        }

        //Set executor.
        command.setExecutor(this);

        //Set tab completer.
        command.setTabCompleter(this);

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Check if sender is a player and that they have permission.
        if (!(sender instanceof Player p)) {

            sender.sendMessage(Utils.error("This command can only be used by a player."));
            return true;

        }

        //Check allowed server type.
        if (SERVER_TYPE != ServerType.PLOT && SERVER_TYPE != ServerType.EARTH) {

            p.sendMessage(Utils.error("You do not have permission to use this command here."));
            return true;

        }

        //Check if the player has permission.
        if (!p.hasPermission("uknet.gamemode")) {

            p.sendMessage(Utils.error("You do not have permission to use this command."));
            return true;

        }

        //If no args are given, send a list of clickable gamemodes.
        //Else switch to the given gamemode in arg[0], if it exists.
        if (args.length == 0) {

            Component message = Component.text("Available gamemodes: ", NamedTextColor.GREEN);

            for (int i = 0; i < gamemodes.size(); i++) {

                Component gamemode;

                //If the player is in the gamemode, highlight it.
                if (p.getGameMode() == GameMode.valueOf(gamemodes.get(i).toUpperCase(Locale.ROOT))) {
                    gamemode = Component.text(StringUtils.capitalize(gamemodes.get(i)), TextColor.color(245, 173, 100));
                    gamemode = gamemode.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Utils.line("You are already in this gamemode.")));
                } else {
                    gamemode = Component.text(StringUtils.capitalize(gamemodes.get(i)), TextColor.color(245, 221, 100));
                    gamemode = gamemode.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/gamemode " + gamemodes.get(i)));
                    gamemode = gamemode.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Utils.line("Click to switch to " + StringUtils.capitalize(gamemodes.get(i)))));
                }

                message = message.append(gamemode);

                //Add comma is not the list value.
                if (i < (gamemodes.size() - 1)) {
                    message = message.append(Utils.line(", "));
                }

            }

            p.sendMessage(message);

        } else {

            //Check if the gamemode exists.
            if (!gamemodes.contains(args[0].toLowerCase(Locale.ROOT))) {

                Component error = Component.text(args[0], NamedTextColor.DARK_RED);
                error = error.append(Utils.error(" is not a valid gamemode"));

                p.sendMessage(error);
                return true;

            }

            //Set the player to this gamemode.
            p.setGameMode(GameMode.valueOf(args[0].toUpperCase(Locale.ROOT)));
            p.sendMessage(Utils.success("Set gamemode to ").append(Component.text(StringUtils.capitalize(args[0].toLowerCase(Locale.ROOT)), NamedTextColor.DARK_AQUA)));

        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command
            command, @NotNull String label, @NotNull String[] args) {

        List<String> returns = new ArrayList<>();

        if (args.length == 0) {

            return gamemodes;

        } else if (args.length == 1) {

            StringUtil.copyPartialMatches(args[0].toLowerCase(Locale.ROOT), gamemodes, returns);
            return returns;

        } else {

            return null;

        }
    }

}
