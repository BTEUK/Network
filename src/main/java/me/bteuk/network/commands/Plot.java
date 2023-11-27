package me.bteuk.network.commands;

import me.bteuk.network.Network;
import me.bteuk.network.events.EventManager;
import me.bteuk.network.sql.PlotSQL;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Plot extends AbstractCommand {

    private static final Component ERROR = Utils.error("/plot join <plotID>");

    public Plot(Network instance) {
        super(instance, "plot");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        Player p = getPlayer(sender);

        if (p == null) {
            return true;
        }

        if (args.length < 2) {
            p.sendMessage(ERROR);
            return true;
        }

        int plotID;

        //Check if the plotID is an actual number.
        try {

            plotID = Integer.parseInt(args[1]);

        } catch (NumberFormatException e) {
            p.sendMessage(ERROR);
            return true;
        }

        //Check if the first arg is 'join'
        if (!args[0].equals("join")) {
            p.sendMessage(ERROR);
            return true;
        }

        PlotSQL plotSQL = Network.getInstance().plotSQL;

        //Check if they have an invite for this plot.
        if (plotSQL.hasRow("SELECT id FROM plot_invites WHERE id=" + plotID + " AND uuid='" + p.getUniqueId() + "';")) {

            //Add server event to join plot.
            EventManager.createEvent(p.getUniqueId().toString(), "plotsystem", plotSQL.getString("SELECT server FROM location_data WHERE name='" +
                    plotSQL.getString("SELECT location FROM plot_data WHERE id=" + plotID + ";") + "';"), "join plot " + plotID);

            //Remove invite.
            plotSQL.update("DELETE FROM plot_invites WHERE id=" + plotID + " AND uuid='" + p.getUniqueId() + "';");

            return true;

        } else {
            p.sendMessage(Utils.error("You have not been invited to join this plot."));
            return true;
        }
    }
}
