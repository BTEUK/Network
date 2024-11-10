package net.bteuk.network.commands;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.bteuk.network.Network;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.utils.regions.Region;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.bteuk.network.utils.Constants.EARTH_WORLD;

public class RegionCommand extends AbstractCommand {

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {

        //Check if the sender is a player.
        Player player = getPlayer(stack);
        if (player == null) {
            return;
        }

        if (args.length < 2) {
            player.sendMessage(ChatUtils.error("/region join <region>"));
            return;
        }

        //Check if the first arg is 'join'
        if (!args[0].equals("join")) {
            player.sendMessage(ChatUtils.error("/region join <region>"));
            return;
        }

        //Check if the region exists.
        if (Network.getInstance().getRegionManager().exists(args[1])) {

            //Get the region.
            Region region = Network.getInstance().getRegionManager().getRegion(args[1]);

            //Check if they have an invite for this region.
            if (region.hasInvite(player.getUniqueId().toString())) {

                //Check if the player has permission, else notify the player accordingly.
                if (player.hasPermission("uknet.regions.join")) {

                    //Add server event to join plot.
                    Network.getInstance().getGlobalSQL().update("INSERT INTO server_events(uuid,type,server,event) VALUES('" + player.getUniqueId() + "'," + "'network'" + ",'" +
                            EARTH_WORLD +
                            "','region join " + region.regionName() + "');");

                } else {

                    //Send error.
                    player.sendMessage(ChatUtils.error("You do not have permission to join regions."));
                    player.sendMessage(ChatUtils.error("To join regions you need at least Jr.Builder."));
                    player.sendMessage(ChatUtils.error("For more information type ")
                            .append(Component.text("/help building", NamedTextColor.DARK_RED)));

                }

                //Remove invite.
                region.removeInvite(player.getUniqueId().toString());

            } else {
                player.sendMessage(ChatUtils.error("You have not been invited to join this region."));
            }
        } else {
            player.sendMessage(ChatUtils.error("The region ")
                    .append(Component.text(args[1], NamedTextColor.DARK_RED))
                    .append(ChatUtils.error(" does not exist.")));
        }
    }
}
