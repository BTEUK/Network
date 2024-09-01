package net.bteuk.network.commands.navigation;

import net.bteuk.network.Network;
import net.bteuk.network.commands.AbstractCommand;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.utils.NetworkUser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.bteuk.network.utils.Constants.LOGGER;

public class TpToggle extends AbstractCommand {

    public TpToggle(Network instance) {
        super(instance, "teleporttoggle");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Check for player
        Player player = getPlayer(sender);
        
        if (player == null) {
            return true;
        }

        //Get the NetworkUser for this player.
        NetworkUser user = Network.getInstance().getUser(player);

        if (user == null) {
            LOGGER.warning("NetworkUser for player " + player.getName() + " is null!");
            return true;
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

        return true;
    }
}
