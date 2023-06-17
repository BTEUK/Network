package me.bteuk.network.commands;

import me.bteuk.network.Network;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;

import static me.bteuk.network.utils.Constants.LOGGER;

public class Hdb implements CommandExecutor {

    //Constructor to enable the command.
    public Hdb(Network instance) {

        //Register command.
        PluginCommand command = instance.getCommand("hdb");

        if (command == null) {
            LOGGER.warning("Hdb command not added to plugin.yml, it will therefore not be enabled.");
            return;
        }

        //Set executor.
        command.setExecutor(this);

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Do nothing.
        return true;

    }
}
