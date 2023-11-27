package me.bteuk.network.commands;

import me.bteuk.network.Network;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Rules extends AbstractCommand {

    public Rules(Network instance) {
        super(instance, "rules");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        Player p = getPlayer(sender);
        if (p != null) {
            //Open rules book.
            p.openBook(Network.getInstance().getLobby().getRules());
        }
        return true;
    }
}
