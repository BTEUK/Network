package me.bteuk.network.commands;

import me.bteuk.network.Network;
import me.bteuk.network.building_companion.BuildingCompanion;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static me.bteuk.network.utils.Constants.LOGGER;

public class BuildingCompanionCommand extends AbstractCommand {

    private final Network instance;

    private static final Component ERROR = Utils.error("/buildingcompanion <clear>");

    public BuildingCompanionCommand(Network instance) {
        super(instance, "companion");
        this.instance = instance;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        Player p = getPlayer(sender);

        if (p == null) {
            sender.sendMessage(COMMAND_ONLY_BY_PLAYER);
            return true;
        }

        // Permission check.
        if (!p.hasPermission("uknet.companion")) {
            p.sendMessage(NO_PERMISSION);
            return true;
        }

        // Get the user, toggle the companion.
        NetworkUser user = instance.getUser(p);

        // If u is null, cancel.
        if (user == null) {
            LOGGER.severe("User " + p.getName() + " can not be found!");
            p.sendMessage(Utils.error("User can not be found, please relog!"));
            return true;
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
                default -> user.player.sendMessage(ERROR);
            }
        }
        return true;
    }

    public static void toggleCompanion(NetworkUser user) {
        // Toggle the building companion.
        BuildingCompanion companion = user.getCompanion();
        if (companion == null) {
            user.setCompanion(new BuildingCompanion(user));
            user.player.sendMessage(Utils.success("Building Companion enabled"));
        } else {
            // Disable the building companion.
            companion.disable();
            user.setCompanion(null);
            user.player.sendMessage(Utils.success("Building Companion disabled"));
        }
    }

    private void drawOutlines(NetworkUser user) {
        if (user.getCompanion() != null) {
            user.player.sendMessage(Utils.line("Drawing outlines..."));
            user.getCompanion().drawOutlines();
        }
    }

    private void clearOutlines(NetworkUser user) {
        if (user.getCompanion() != null) {
            user.getCompanion().clearSelection();
        } else {
            user.player.sendMessage(Utils.error("Your building companion is not enabled"));
        }
    }

    private void saveOutlines(NetworkUser user, String[] args) {
        if (args.length > 1 && user.getCompanion() != null) {
            if (user.getCompanion().saveOutlines(args[1], Material.ORANGE_CONCRETE.createBlockData(), true)) {
                user.player.sendMessage(Utils.success("Saved outlines"));
                // Also clear the outlines.
                user.getCompanion().clearSelection();
            }
        }
    }

    private void removeOutlines(NetworkUser user, String[] args) {
        if (args.length > 1 && user.getCompanion() != null) {
            if (user.getCompanion().saveOutlines(args[1], Material.AIR.createBlockData(), false)) {
                user.player.sendMessage(Utils.success("Removed outlines"));
            }
        }
    }
}