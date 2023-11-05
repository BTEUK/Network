package me.bteuk.network.commands.staff;

import me.bteuk.network.Network;
import me.bteuk.network.commands.AbstractCommand;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.WorldScanner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Command to manage regions on the server.
 */
public class RegionManagerCommand extends AbstractCommand {

    public RegionManagerCommand(Network instance) {
        super(instance, "regionmanager");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (sender instanceof Player p) {
            p.sendMessage(Utils.error("This command can only be run from the console."));
            return true;
        }

        if (args.length < 1) {
            return true;
        }

        if (args[0].equalsIgnoreCase("draw")) {
            WorldScanner scanner = new WorldScanner();
            scanner.loadRegions();
            scanner.drawRegions();
        }

        return true;
    }
}
