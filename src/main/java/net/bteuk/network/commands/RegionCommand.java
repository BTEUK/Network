package net.bteuk.network.commands;

import net.bteuk.network.Network;
import net.bteuk.network.utils.Utils;
import net.bteuk.network.utils.regions.Region;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.bteuk.network.utils.Constants.EARTH_WORLD;

public class RegionCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player p)) {

            sender.sendMessage(Utils.error("You must be a player to run this command."));
            return true;

        }

        if (args.length < 2) {
            p.sendMessage(Utils.error("/region join <region>"));
            return true;
        }

        //Check if the first arg is 'join'
        if (!args[0].equals("join")) {
            p.sendMessage(Utils.error("/region join <region>"));
            return true;
        }

        //Check if the region exists.
        if (Network.getInstance().getRegionManager().exists(args[1])) {

            //Get the region.
            Region region = Network.getInstance().getRegionManager().getRegion(args[1]);

            //Check if they have an invite for this region.
            if (region.hasInvite(p.getUniqueId().toString())) {

                //Check if the player has permission, else notify the player accordingly.
                if (p.hasPermission("uknet.regions.join")) {

                    //Add server event to join plot.
                    Network.getInstance().getGlobalSQL().update("INSERT INTO server_events(uuid,type,server,event) VALUES('" + p.getUniqueId() + "'," + "'network'" + ",'" +
                            EARTH_WORLD +
                            "','region join " + region.regionName() + "');");

                } else {

                    //Send error.
                    p.sendMessage(Utils.error("You do not have permission to join regions."));
                    p.sendMessage(Utils.error("To join regions you need at least Jr.Builder."));
                    p.sendMessage(Utils.error("For more information type ")
                            .append(Component.text("/help building", NamedTextColor.DARK_RED)));

                }

                //Remove invite.
                region.removeInvite(p.getUniqueId().toString());

            } else {
                p.sendMessage(Utils.error("You have not been invited to join this region."));
            }
            return true;
        } else {

            p.sendMessage(Utils.error("The region ")
                    .append(Component.text(args[1], NamedTextColor.DARK_RED))
                    .append(Utils.error(" does not exist.")));
            return true;

        }
    }
}
