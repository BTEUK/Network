package net.bteuk.network.commands.navigation;

import net.bteuk.network.Network;
import net.bteuk.network.commands.AbstractCommand;
import net.bteuk.network.utils.SwitchServer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BTEUK extends AbstractCommand {

    public BTEUK(Network instance) {
        super(instance, "bteuk");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        Player player = getPlayer(sender);

        if (player == null) {
            return true;
        }

        SwitchServer.switchToExternalServer(player);

        return true;
    }
}
