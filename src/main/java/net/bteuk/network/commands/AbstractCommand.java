package net.bteuk.network.commands;

import net.bteuk.network.lib.utils.ChatUtils;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import static net.bteuk.network.utils.Constants.LOGGER;

/**
 * Abstract class for registering a command.
 * The implementation of the commandExecutor happens in the extending class.
 */
public abstract class AbstractCommand implements CommandExecutor {

    protected static final Component COMMAND_ONLY_BY_PLAYER = ChatUtils.error("This command can only be run by a player.");
    protected static final Component NO_PERMISSION = ChatUtils.error("You do not have permission to use this command.");

    protected PluginCommand command;

    //Constructor to enable the command.
    protected AbstractCommand(JavaPlugin instance, String commandName) {

        //Register command.
        command = instance.getCommand(commandName);

        if (command == null) {
            LOGGER.warning(StringUtils.capitalize(commandName) + " command not added to plugin.yml, it will therefore not be enabled.");
            return;
        }

        //Set executor.
        command.setExecutor(this);

    }

    /**
     * Gets the {@link Player} from the server, if the sender is not a player send them a warning with
     * 'This command can only be run by a player.'
     *
     * @param sender the command sender
     * @return the {@link Player} instance, or null if not a player
     */
    protected Player getPlayer(CommandSender sender) {

        //Check if the sender is a player.
        if (!(sender instanceof Player p)) {

            sender.sendMessage(COMMAND_ONLY_BY_PLAYER);
            return null;

        }

        return p;
    }

    /**
     * Checks whether the sender has permission, if the sender is not a {@link Player} then this will always return true.
     * If the player doesn't have the permission send a no permission message.
     *
     * @param sender the command sender
     * @param permission the permission to check
     * @return whether the sender has the permission
     */
    protected boolean hasPermission(CommandSender sender, String permission) {
        if (sender instanceof Player p) {
            if (!p.hasPermission(permission)) {
                sender.sendMessage(NO_PERMISSION);
                return false;
            }
        }
        return true;
    }
}
