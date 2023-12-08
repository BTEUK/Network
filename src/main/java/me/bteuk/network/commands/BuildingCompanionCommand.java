package me.bteuk.network.commands;

import me.bteuk.network.Network;
import me.bteuk.network.building_companion.BuildingCompanion;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static me.bteuk.network.utils.Constants.LOGGER;

public class BuildingCompanionCommand extends AbstractCommand {

    private final Network instance;

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
        } else if (args.length == 1) {
            if (args[0].equals("drawoutlines")) {
                drawOutlines(user);
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

    public void drawOutlines(NetworkUser user) {
        if (user.getCompanion() != null) {
            user.getCompanion().drawOutlines();
        }
    }
}
