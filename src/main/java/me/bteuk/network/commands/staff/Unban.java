package me.bteuk.network.commands.staff;

import me.bteuk.network.Network;
import me.bteuk.network.utils.staff.Moderation;
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

public class Unban extends Moderation implements CommandExecutor {

    //Constructor to enable the command.
    public Unban(Network instance) {

        //Register command.
        PluginCommand command = instance.getCommand("unban");

        if (command == null) {
            LOGGER.warning("Unban command not added to plugin.yml, it will therefore not be enabled.");
            return;
        }

        //Set executor.
        command.setExecutor(this);

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Check if sender is player, then check permissions
        if (sender instanceof Player p) {
            if (!p.hasPermission("uknet.ban")) {
                p.sendMessage(Utils.error("You do not have permission to use this command."));
                return true;
            }
        }

        //Check args.
        if (args.length < 1) {
            sender.sendMessage(Utils.error("/unban <player>"));
            return true;
        }

        //Check player.
        //If uuid exists for name.
        if (!Network.getInstance().getGlobalSQL().hasRow("SELECT uuid FROM player_data WHERE name='" + args[0] + "';")) {
            sender.sendMessage(Component.text(args[0], NamedTextColor.DARK_RED)
                    .append(Utils.error(" is not a valid player.")));
            return true;
        }

        String uuid = Network.getInstance().getGlobalSQL().getString("SELECT uuid FROM player_data WHERE name='" + args[0] + "';");

        sender.sendMessage(unbanPlayer(args[0], uuid));

        return false;
    }

    /**
     * Unban the player and return the feedback so the executor can be notified of success/failure.
     *
     * @param name
     * Name of the banned player.
     * @param uuid
     * Uuid of the banned player.
     * @return
     * The Component to display to the executor.
     */
    public Component unbanPlayer(String name, String uuid) {

        //Check if the player is currently banned.
        if (isBanned(uuid)) {

            //Unban the player.
            unban(uuid);

            //Send feedback.
            return (Utils.success("Unbanned ")
                    .append(Component.text(name, NamedTextColor.DARK_AQUA)));

        } else {
            return (Utils.error(name + " is not currently banned."));
        }
    }
}
