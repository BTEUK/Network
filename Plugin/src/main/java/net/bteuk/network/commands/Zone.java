package net.bteuk.network.commands;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.bteuk.network.Network;
import net.bteuk.network.eventing.events.EventManager;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.sql.PlotSQL;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Zone extends AbstractCommand {

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {

        // Check if the sender is a player.
        Player player = getPlayer(stack);
        if (player == null) {
            return;
        }

        if (args.length < 2) {
            player.sendMessage(ChatUtils.error("/zone join <zoneID>"));
            return;
        }

        int zoneID;

        // Check if the zoneID is an actual number.
        try {

            zoneID = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatUtils.error("/zone join <plotID>"));
            return;
        }

        // Check if the first arg is 'join'
        if (!args[0].equals("join")) {
            player.sendMessage(ChatUtils.error("/zone join <plotID>"));
            return;
        }

        PlotSQL plotSQL = Network.getInstance().getPlotSQL();

        // Check if they have an invite for this plot.
        if (plotSQL.hasRow("SELECT id FROM zone_invites WHERE id=" + zoneID + " AND uuid='" + player.getUniqueId() +
                "';")) {

            // Add server event to join plot.
            EventManager.createEvent(player.getUniqueId().toString(), "plotsystem", plotSQL.getString("SELECT server " +
                            "FROM location_data WHERE name='" +
                            plotSQL.getString("SELECT location FROM zones WHERE id=" + zoneID + ";") + "';"),
                    "join zone " + zoneID);

            // Remove invite.
            plotSQL.update("DELETE FROM plot_invites WHERE id=" + zoneID + " AND uuid='" + player.getUniqueId() + "';");
        } else {
            player.sendMessage(ChatUtils.error("You have not been invited to join this Zone."));
        }
    }
}