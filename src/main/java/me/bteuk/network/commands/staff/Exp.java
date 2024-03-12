package me.bteuk.network.commands.staff;

import me.bteuk.network.Network;
import me.bteuk.network.commands.AbstractCommand;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.progression.Progression;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Exp extends AbstractCommand {

    public Exp(Network instance) {
        super(instance, "exp");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (sender instanceof Player) {
            return true;
        }

        if (args.length < 3) {
            return true;
        }

        if (args[0].equals("give")) {
            String uuid = Network.getInstance().getGlobalSQL().getString("SELECT uuid FROM player_data WHERE name='" + args[1] + "';");
            if (uuid == null) {
                sender.sendMessage(Utils.error("Player " + args[1] + " could not be found."));
            } else {
                try {
                    int val = Integer.parseInt(args[2]);
                    Progression.addExp(uuid, val);
                } catch (NumberFormatException ignored) {
                    return true;
                }
            }
        }

        return true;
    }
}
