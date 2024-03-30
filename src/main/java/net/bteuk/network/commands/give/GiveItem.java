package net.bteuk.network.commands.give;

import net.bteuk.network.Network;
import net.bteuk.network.commands.AbstractCommand;
import net.bteuk.network.utils.NetworkUser;
import net.bteuk.network.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static net.bteuk.network.utils.Constants.LOGGER;

/**
 * Abstract class for giving an item to a player.
 * Is used for all commands that give items to a player.
 */
public abstract class GiveItem extends AbstractCommand {
    public GiveItem(Network instance, String commandName) {
        super(instance, commandName);
    }

    public boolean onCommand(CommandSender sender, String permission, ItemStack item, String itemName) {

        //Check if the sender is a player.
        if (!(sender instanceof Player p)) {

            sender.sendMessage(Utils.error("You must be a player to use this command."));
            return true;

        }

        //Check if the user has permission.
        if (!p.hasPermission(permission)) {

            p.sendMessage(Utils.error("You do not have permission to use this command."));
            return true;

        }

        //Get user.
        NetworkUser u = Network.getInstance().getUser(p);

        //If u is null, cancel.
        if (u == null) {
            LOGGER.severe("User " + p.getName() + " can not be found!");
            p.sendMessage(Utils.error("User can not be found, please relog!"));
            return true;
        }

        //Add debug stick to inventory.
        Utils.giveItem(p, item, itemName);

        return true;
    }
}
