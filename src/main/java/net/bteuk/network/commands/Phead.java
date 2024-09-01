package net.bteuk.network.commands;

import net.bteuk.network.Network;
import net.bteuk.network.commands.tabcompleters.PlayerSelector;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.bteuk.network.utils.Constants.LOGGER;

public class Phead implements CommandExecutor {

    //Constructor to enable the command.
    public Phead(Network instance) {

        //Register command.
        PluginCommand command = instance.getCommand("phead");

        if (command == null) {
            LOGGER.warning("Phead command not added to plugin.yml, it will therefore not be enabled.");
            return;
        }

        //Set executor.
        command.setExecutor(this);

        //Set tab completer.
        command.setTabCompleter(new PlayerSelector());

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Check if sender is a player and that they have permission.
        if (!(sender instanceof Player p)) {

            sender.sendMessage(ChatUtils.error("This command can only be used by a player."));
            return true;

        }

        //Check if the player has permission.
        if (!p.hasPermission("uknet.phead")) {

            p.sendMessage(ChatUtils.error("You do not have permission to use this command."));
            return true;

        }

        //Check if there is at least 1 arg.
        if (args.length < 1) {

            p.sendMessage(ChatUtils.error("/phead <name>"));
            return true;

        }

        //Check if player exists in the database.
        if (Network.getInstance().getGlobalSQL().hasRow("SELECT uuid FROM player_data WHERE name='" + args[0] + "';")) {

            //Get uuid.
            String uuid = Network.getInstance().getGlobalSQL().getString("SELECT uuid FROM player_data WHERE name='" + args[0] + "';");
            String name = Network.getInstance().getGlobalSQL().getString("SELECT name FROM player_data WHERE name='" + args[0] + "';");

            //Give item to player.
            Utils.giveItem(p, Utils.createPlayerSkull(uuid, 1, Component.text(name + "'s head", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false)), name + "'s head");

        } else {

            p.sendMessage(Component.text(args[0], NamedTextColor.DARK_RED).append(ChatUtils.error(" could not be found.")));

        }

        return true;
    }
}
