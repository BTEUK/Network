package me.bteuk.network.commands;

import me.bteuk.network.Network;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;

import static me.bteuk.network.utils.Constants.LOGGER;

public abstract class Command implements CommandExecutor {

    //Constructor to enable the command.
    public Command(Network instance, String commandName) {

        //Register command.
        PluginCommand command = instance.getCommand(commandName);

        if (command == null) {
            LOGGER.warning(StringUtils.capitalize(commandName) + " command not added to plugin.yml, it will therefore not be enabled.");
            return;
        }

        //Set executor.
        command.setExecutor(this);

    }
}
