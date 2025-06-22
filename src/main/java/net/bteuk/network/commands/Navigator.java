package net.bteuk.network.commands;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.bteuk.network.Network;
import net.bteuk.network.gui.BuildGui;
import net.bteuk.network.gui.navigation.ExploreGui;
import net.bteuk.network.gui.tutorials.TutorialsGui;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.utils.NetworkUser;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.bteuk.network.utils.Constants.LOGGER;
import static net.bteuk.network.utils.Constants.SERVER_TYPE;
import static net.bteuk.network.utils.Constants.TUTORIALS;
import static net.bteuk.network.utils.enums.ServerType.TUTORIAL;

public class Navigator extends AbstractCommand {

    public static void openNavigator(NetworkUser u) {

        // Check if the mainGui is not null.
        // If not then open it after refreshing its contents.
        // If no gui exists open the navigator.

        if (u.mainGui != null) {

            u.mainGui.refresh();
            u.mainGui.open(u);
        } else {

            Network.getInstance().navigatorGui.open(u);
        }
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {

        // Check if the sender is a player.
        Player player = getPlayer(stack);
        if (player == null) {
            return;
        }

        NetworkUser user = Network.getInstance().getUser(player);

        // If u is null, cancel.
        if (user == null) {
            LOGGER.severe("User " + player.getName() + " can not be found!");
            player.sendMessage(ChatUtils.error("User can not be found, please relog!"));
            return;
        }

        // Check args, allows the player to open a specific menu directly.
        if (args.length > 0) {
            switch (args[0]) {

                case "explore" -> openExplore(user);
                case "building" -> openBuilding(user);
                case "tutorials" -> openTutorials(user);
                default -> openNavigator(user);
            }
        } else {

            // If the player has a previous gui, open that.
            openNavigator(user);
        }
    }

    private void openExplore(NetworkUser u) {

        if (u.mainGui != null) {
            u.mainGui.delete();
            u.mainGui = null;
        }

        u.mainGui = new ExploreGui(u);
        u.mainGui.open(u);
    }

    private void openBuilding(NetworkUser u) {

        if (u.mainGui != null) {
            u.mainGui.delete();
            u.mainGui = null;
        }

        u.mainGui = new BuildGui(u);
        u.mainGui.open(u);
    }

    // Only if tutorials is enabled and the server is not already tutorials.
    private void openTutorials(NetworkUser u) {

        if (SERVER_TYPE != TUTORIAL && TUTORIALS) {

            if (u.mainGui != null) {
                u.mainGui.delete();
                u.mainGui = null;
            }

            u.mainGui = new TutorialsGui(u);
            u.mainGui.open(u);
        } else {

            openNavigator(u);
        }
    }
}
