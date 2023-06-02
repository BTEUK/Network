package me.bteuk.network.staff.commands;

import me.bteuk.network.Network;
import me.bteuk.network.staff.Moderation;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static me.bteuk.network.utils.Constants.LOGGER;

public class Unmute extends Moderation implements CommandExecutor {

    //Constructor to enable the command.
    public Unmute(Network instance) {

        //Register command.
        PluginCommand command = instance.getCommand("unmute");

        if (command == null) {
            LOGGER.warning("Home command not added to plugin.yml, it will therefore not be enabled.");
            return;
        }

        //Set executor.
        command.setExecutor(this);

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Check if sender is player, then check permissions
        if (sender instanceof Player p) {
            if (!p.hasPermission("uknet.mute")) {
                p.sendMessage(Utils.error("You do not have permission to use this command."));
                return true;
            }
        }

        //Check args.
        if (args.length < 1) {
            sender.sendMessage(Utils.error("/unmute <player>"));
            return true;
        }

        //Check player.
        //If uuid exists for name.
        if (!Network.getInstance().globalSQL.hasRow("SELECT uuid FROM player_data WHERE name='" + args[0] + "';")) {
            sender.sendMessage(Component.text(args[0], NamedTextColor.DARK_RED)
                    .append(Utils.error(" is not a valid player.")));
            return true;
        }

        String uuid = Network.getInstance().globalSQL.getString("SELECT uuid FROM player_data WHERE name='" + args[0] + "';");

        //Check if the player is currently banned.
        if (isMuted(uuid)) {

            //Unban the player.
            unmute(uuid);

            //Send feedback.
            sender.sendMessage(Utils.success("Unmuted ")
                    .append(Component.text(args[0], NamedTextColor.DARK_AQUA)));

        } else {
            sender.sendMessage(Utils.error(args[0] + " is not currently muted."));
            return true;
        }

        return false;
    }
}
