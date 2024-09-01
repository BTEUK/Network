package net.bteuk.network.commands;

import net.bteuk.network.Network;
import net.bteuk.network.utils.NetworkUser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.bteuk.network.utils.Constants.LOGGER;

/**
 * Command to enable/disable focus mode.
 */
public class Focus extends AbstractCommand {

    private final Network instance;

    public Focus(Network instance) {
        super(instance, "focus");
        this.instance = instance;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        Player player = getPlayer(sender);

        if (player == null) {
            return true;
        }

        NetworkUser user = instance.getUser(player);

        if (user == null) {
            LOGGER.warning("NetworkUser for player " + player.getName() + " is null!");
            return true;
        }

        user.toggleFocus();
        return true;
    }
}
