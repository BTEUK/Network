package net.bteuk.network.commands;

import net.bteuk.network.Network;
import net.bteuk.network.gui.BuildGui;
import net.bteuk.network.gui.tutorials.TutorialsGui;
import net.bteuk.network.gui.navigation.ExploreGui;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.utils.NetworkUser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.bteuk.network.utils.Constants.SERVER_TYPE;
import static net.bteuk.network.utils.Constants.TUTORIALS;
import static net.bteuk.network.utils.enums.ServerType.TUTORIAL;

public class Navigator implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {

        //Check if the sender is a player.
        if (!(sender instanceof Player p)) {

            sender.sendMessage(ChatUtils.error("This command can only be used by a player."));
            return true;

        }

        //Get user.
        NetworkUser u = Network.getInstance().getUser(p);

        if (u == null) {return true;}

        //Check args, allows the player to open a specific menu directly.
        if (args.length > 0) {
            switch (args[0]) {

                case "explore" -> openExplore(u);
                case "building" -> openBuilding(u);
                case "tutorials" -> openTutorials(u);
                default -> openNavigator(u);

            }
        } else {

            //If the player has a previous gui, open that.
            openNavigator(u);

        }

        return true;
    }

    public static void openNavigator(NetworkUser u) {

        //Check if the mainGui is not null.
        //If not then open it after refreshing its contents.
        //If no gui exists open the navigator.

        if (u.mainGui != null) {

            u.mainGui.refresh();
            u.mainGui.open(u);

        } else {

            Network.getInstance().navigatorGui.open(u);

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

    //Only if tutorials is enabled and the server is not already tutorials.
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
