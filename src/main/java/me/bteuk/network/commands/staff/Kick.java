package me.bteuk.network.commands.staff;

import me.bteuk.network.Network;
import me.bteuk.network.utils.staff.Moderation;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static me.bteuk.network.utils.Constants.LOGGER;

public class Kick extends Moderation implements CommandExecutor {

    //Constructor to enable the command.
    public Kick(Network instance) {

        //Register command.
        PluginCommand command = instance.getCommand("kick");

        if (command == null) {
            LOGGER.warning("Kick command not added to plugin.yml, it will therefore not be enabled.");
            return;
        }

        //Set executor.
        command.setExecutor(this);

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Check if sender is player, then check permissions
        if (sender instanceof Player p) {
            if (!p.hasPermission("uknet.kick")) {
                p.sendMessage(Utils.error("You do not have permission to use this command."));
                return true;
            }
        }

        //Check args.
        if (args.length < 2) {
            sender.sendMessage(Utils.error("/kick <player> <reason>"));
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

        //Check if player is online.
        if (!Network.getInstance().globalSQL.hasRow("SELECT uuid FROM online_users WHERE uuid='" + uuid + "';")) {
            sender.sendMessage(Component.text(args[0], NamedTextColor.DARK_RED)
                    .append(Utils.error(" is not online.")));
            return true;
        }

        //Combine all remaining args to create a reason.
        String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        sender.sendMessage(kickPlayer(args[0], uuid, reason));

        return true;
    }

    public Component kickPlayer(String name, String uuid, String reason) {

        kick(uuid, reason);

        return (Utils.success("Kicked ")
                .append(Component.text(name, NamedTextColor.DARK_AQUA))
                .append(Utils.success(" for reason: "))
                .append(Component.text(reason, NamedTextColor.DARK_AQUA)));

    }
}
