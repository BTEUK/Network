package net.bteuk.network.commands.give;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.bteuk.network.Network;
import net.bteuk.network.commands.AbstractCommand;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.utils.NetworkUser;
import net.bteuk.network.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static net.bteuk.network.utils.Constants.LOGGER;

/**
 * Abstract class for giving an item to a player.
 * Is used for all commands that give items to a player.
 */
public abstract class GiveItem extends AbstractCommand {

    public void onCommand(CommandSourceStack stack, String permission, ItemStack item, String itemName) {

        // Check if the sender is a player.
        Player player = getPlayer(stack);
        if (player == null) {
            return;
        }

        // Check if the user has permission.
        if (!hasPermission(player, permission)) {
            return;
        }

        NetworkUser user = Network.getInstance().getUser(player);

        // If u is null, cancel.
        if (user == null) {
            LOGGER.severe("User " + player.getName() + " can not be found!");
            player.sendMessage(ChatUtils.error("User can not be found, please relog!"));
            return;
        }

        // Add debug stick to inventory.
        Utils.giveItem(player, item, itemName);
    }
}
