package me.bteuk.network.commands;

import me.bteuk.network.Network;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.regions.Region;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RegionCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player p)) {

            sender.sendMessage(Utils.chat("&cYou must be a player to run this command."));
            return true;

        }

        if (args.length < 2) {
            p.sendMessage(Utils.chat("&c/region join <region>"));
            return true;
        }

        //Check if the first arg is 'join'
        if (!args[0].equals("join")) {
            p.sendMessage(Utils.chat("&c/region join <region>"));
            return true;
        }

        //Check if the region exists.
        if (Network.getInstance().getRegionManager().exists(args[1])) {

            //Get the region.
            Region region = Network.getInstance().getRegionManager().getRegion(args[1]);

            //Check if they have an invite for this region.
            if (region.hasInvite(p.getUniqueId().toString())) {

                //Add server event to join plot.
                Network.getInstance().globalSQL.update("INSERT INTO server_events(uuid,type,server,event) VALUES('" + p.getUniqueId() + "'," + "'network'" + ",'" +
                        Network.getInstance().getConfig().getString("earth_world") +
                        "','region join " + region.regionName() + "');");

                //Remove invite.
                region.removeInvite(p.getUniqueId().toString());

                return true;

            } else {
                p.sendMessage(Utils.chat("&cYou have not been invited to join this region."));
                return true;
            }
        } else {

            p.sendMessage(Utils.chat("&cThe region " + args[1] + " does not exist."));
            return true;

        }
    }
}
