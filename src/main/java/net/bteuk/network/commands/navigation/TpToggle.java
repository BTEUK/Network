package net.bteuk.network.commands.navigation;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.bteuk.network.Network;
import net.bteuk.network.commands.AbstractCommand;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.utils.NetworkUser;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.bteuk.network.utils.Constants.LOGGER;

public class TpToggle extends AbstractCommand {

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {

        //Check if the sender is a player.
        Player player = getPlayer(stack);
        if (player == null) {
            return;
        }

        NetworkUser user = Network.getInstance().getUser(player);

        //If u is null, cancel.
        if (user == null) {
            LOGGER.severe("User " + player.getName() + " can not be found!");
            player.sendMessage(ChatUtils.error("User can not be found, please relog!"));
            return;
        }

        //Invert status.
        if (Network.getInstance().getGlobalSQL().hasRow("SELECT uuid FROM player_data WHERE uuid='" + player.getUniqueId() + "' AND teleport_enabled=1;")) {
            //Disable teleport.
            user.setTeleportEnabled(false);
            Network.getInstance().getGlobalSQL().update("UPDATE player_data SET teleport_enabled=0 WHERE uuid='" + player.getUniqueId() + "';");

            player.sendMessage(ChatUtils.success("Other players will now no longer be able to teleport to you."));
        } else {
            //Enable teleport.
            user.setTeleportEnabled(true);
            Network.getInstance().getGlobalSQL().update("UPDATE player_data SET teleport_enabled=1 WHERE uuid='" + player.getUniqueId() + "';");

            player.sendMessage(ChatUtils.success("Other players will be now be able to teleport to you."));
        }
    }
}
