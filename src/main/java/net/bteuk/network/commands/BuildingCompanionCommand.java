package net.bteuk.network.commands;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.bteuk.network.Network;
import net.bteuk.network.building_companion.BuildingCompanion;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.utils.NetworkUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.bteuk.network.utils.Constants.LOGGER;

public class BuildingCompanionCommand extends AbstractCommand {

    private static final Component ERROR = ChatUtils.error("/buildingcompanion <clear>");

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {

        //Check if the sender is a player.
        Player player = getPlayer(stack);
        if (player == null) {
            return;
        }

        if (!hasPermission(player, "uknet.companion")) {
            return;
        }

        NetworkUser user = Network.getInstance().getUser(player);

        //If u is null, cancel.
        if (user == null) {
            LOGGER.severe("User " + player.getName() + " can not be found!");
            player.sendMessage(ChatUtils.error("User can not be found, please relog!"));
            return;
        }

        // Without args, toggle companion.
        if (args.length == 0) {
            toggleCompanion(user);
        } else {
            switch (args[0]) {
                case "drawoutlines" -> drawOutlines(user);
                case "clear" -> clearOutlines(user);
                case "save" -> saveOutlines(user, args);
                case "remove" -> removeOutlines(user, args);
                case "walls" -> createWalls(user, args);
                default -> user.player.sendMessage(ERROR);
            }
        }
    }

    public static void toggleCompanion(NetworkUser user) {
        // Toggle the building companion.
        BuildingCompanion companion = user.getCompanion();
        if (companion == null) {
            user.setCompanion(new BuildingCompanion(user));
            user.player.sendMessage(ChatUtils.success("Building Companion enabled"));
        } else {
            // Disable the building companion.
            companion.disable();
            user.setCompanion(null);
            user.player.sendMessage(ChatUtils.success("Building Companion disabled"));
        }
    }

    private void drawOutlines(NetworkUser user) {
        if (user.getCompanion() != null) {
            user.player.sendMessage(Component.text("Drawing outlines...", NamedTextColor.YELLOW));
            user.getCompanion().drawOutlines();
        }
    }

    private void clearOutlines(NetworkUser user) {
        if (user.getCompanion() != null) {
            user.getCompanion().clearSelection();
        } else {
            user.player.sendMessage(ChatUtils.error("Your building companion is not enabled"));
        }
    }

    private void saveOutlines(NetworkUser user, String[] args) {
        if (args.length > 1 && user.getCompanion() != null) {
            if (user.getCompanion().saveOutlines(args[1], Material.ORANGE_CONCRETE.createBlockData(), true)) {
                user.player.sendMessage(ChatUtils.success("Saved outlines"));
                // Also clear the outlines.
                user.getCompanion().clearSelection();
            }
        }
    }

    private void removeOutlines(NetworkUser user, String[] args) {
        if (args.length > 1 && user.getCompanion() != null) {
            if (user.getCompanion().saveOutlines(args[1], Material.AIR.createBlockData(), false)) {
                user.player.sendMessage(ChatUtils.success("Removed outlines"));
            }
        }
    }

    private void createWalls(NetworkUser user, String[] args) {

    }
}