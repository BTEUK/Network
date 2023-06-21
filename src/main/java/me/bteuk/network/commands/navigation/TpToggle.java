package me.bteuk.network.commands.navigation;

import me.bteuk.network.Network;
import me.bteuk.network.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TpToggle implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Check for player
        if (!(sender instanceof Player p)) {

            sender.sendMessage(Utils.error("This command can only be run by a player!"));
            return true;

        }

        //Invert status.
        if (Network.getInstance().globalSQL.hasRow("SELECT uuid FROM player_data WHERE uuid='" + p.getUniqueId() + "' AND teleport_enabled=1;")) {

            //Disable teleport.
            Network.getInstance().globalSQL.update("UPDATE player_data SET teleport_enabled=0 WHERE uuid='" + p.getUniqueId() + "';");

            p.sendMessage(Utils.success("Other players will now no longer be able to teleport to you."));

        } else {

            //Enable teleport.
            Network.getInstance().globalSQL.update("UPDATE player_data SET teleport_enabled=1 WHERE uuid='" + p.getUniqueId() + "';");

            p.sendMessage(Utils.success("Other players will be now be able to teleport to you."));

        }

        return true;
    }
}
