package net.bteuk.network.commands;

import net.bteuk.network.Network;
import net.bteuk.network.utils.NetworkUser;
import net.bteuk.network.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.bteuk.network.utils.Constants.LOGGER;

public class TipsToggle implements CommandExecutor {

    //Constructor to enable the command.
    public TipsToggle(Network instance) {

        //Register command.
        PluginCommand command = instance.getCommand("tips");

        if (command == null) {
            LOGGER.warning("Tips command not added to plugin.yml, it will therefore not be enabled.");
            return;
        }

        //Set executor.
        command.setExecutor(this);

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Check if the sender is a player.
        if (!(sender instanceof Player p)) {

            sender.sendMessage(Utils.error("This command can only be run by a player."));
            return true;

        }

        toggleTips(p);
        return true;

    }

    public static void toggleTips(Player p) {

        //Get the NetworkUser for this player.
        NetworkUser user = Network.getInstance().getUser(p);

        if (user == null) {
            LOGGER.warning("NetworkUser for player " + p.getName() + " is null!");
            return;
        }

        //If tips is enabled, disable it, else enable.
        if (user.isTips_enabled()) {

            //Disable tips.
            user.setTips_enabled(false);

            //Update the database value.
            Network.getInstance().getGlobalSQL().update("UPDATE player_data SET tips_enabled=0 WHERE uuid='" + p.getUniqueId() + "';");

            p.sendMessage(Utils.success("Disabled tips in chat."));

        } else {

            //Enable tips.
            user.setTips_enabled(true);

            //Update the database value.
            Network.getInstance().getGlobalSQL().update("UPDATE player_data SET tips_enabled=1 WHERE uuid='" + p.getUniqueId() + "';");

            p.sendMessage(Utils.success("Enabled tips in chat."));

        }
    }
}