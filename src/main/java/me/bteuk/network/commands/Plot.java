package me.bteuk.network.commands;

import me.bteuk.network.Network;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.sql.PlotSQL;
import me.bteuk.network.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Plot implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player p)) {

            sender.sendMessage(Utils.error("You must be a player to run this command."));
            return true;

        }

        if (args.length < 2) {
            p.sendMessage(Utils.error("/plot join <plotID>"));
            return true;
        }

        int plotID;

        //Check if the plotID is an actual number.
        try {

            plotID = Integer.parseInt(args[1]);

        } catch (NumberFormatException e) {
            p.sendMessage(Utils.error("/plot join <plotID>"));
            return true;
        }

        //Check if the first arg is 'join'
        if (!args[0].equals("join")) {
            p.sendMessage(Utils.error("/plot join <plotID>"));
            return true;
        }

        GlobalSQL globalSQL = Network.getInstance().globalSQL;
        PlotSQL plotSQL = Network.getInstance().plotSQL;

        //Check if they have an invite for this plot.
        if (plotSQL.hasRow("SELECT id FROM plot_invites WHERE id=" + plotID + " AND uuid='" + p.getUniqueId() + "';")) {

            //Add server event to join plot.
            globalSQL.update("INSERT INTO server_events(uuid,type,server,event) VALUES('" + p.getUniqueId() + "'," + "'plotsystem'" + ",'" +
                    plotSQL.getString("SELECT server FROM location_data WHERE name='" +
                            plotSQL.getString("SELECT location FROM plot_data WHERE id=" + plotID + ";") + "';") +
                    "','join plot " + plotID + "');");

            //Remove invite.
            plotSQL.update("DELETE FROM plot_invites WHERE id=" + plotID + " AND uuid='" + p.getUniqueId() + "';");

            return true;

        } else {
            p.sendMessage(Utils.error("You have not been invited to join this plot."));
            return true;
        }
    }
}
