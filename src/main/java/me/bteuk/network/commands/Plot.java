package me.bteuk.network.commands;

import me.bteuk.network.Network;
import me.bteuk.network.events.EventManager;
import me.bteuk.network.gui.plotsystem.PlotInfo;
import me.bteuk.network.sql.PlotSQL;
import me.bteuk.network.tabcompleters.FixedArgSelector;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.enums.PlotStatus;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static me.bteuk.network.utils.Constants.LOGGER;

public class Plot extends AbstractCommand {

    private final Network instance;
    private final PlotSQL plotSQL;

    public Plot(Network instance) {
        super(instance, "plot");
        this.instance = instance;
        plotSQL = Network.getInstance().getPlotSQL();
        command.setTabCompleter(new FixedArgSelector(Arrays.asList("info", "join"), 0));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        Player p = getPlayer(sender);
        if (p == null) {
            sender.sendMessage(COMMAND_ONLY_BY_PLAYER);
            return true;
        }

        if (args.length < 2) {
            error(p);
            return true;
        }

        int plotID;
        //Check if the plotID is an actual number.
        try {
            plotID = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            error(p);
            return true;
        }

        switch (args[0]) {
            case "info" -> info(p, plotID);
            case "join" -> join(p, plotID);
            default -> error(p);
        }
        return true;
    }

    private void info(Player p, int plot) {
        // Check if the plot exists and is not deleted.
        PlotStatus status = PlotStatus.fromDatabaseValue(plotSQL.getString("SELECT status FROM plot_data WHERE id=" + plot + ";"));
        if (status == null || status == PlotStatus.DELETED) {
            p.sendMessage(Utils.error("This plot does not exist."));
            return;
        }
        // Get the user.
        NetworkUser u = instance.getUser(p);
        if (u == null) {
            p.sendMessage(Utils.error("An error occurred, please rejoin!"));
            LOGGER.severe("No user exists for player " + p.getName());
            return;
        }
        // Open the plot info menu.
        if (u.mainGui != null) {
            u.mainGui.delete();
        }
        u.mainGui = new PlotInfo(u, plot);
        u.mainGui.open(u);
    }

    private void join(Player p, int plot) {


        //Check if they have an invite for this plot.
        if (plotSQL.hasRow("SELECT id FROM plot_invites WHERE id=" + plot + " AND uuid='" + p.getUniqueId() + "';")) {

            //Add server event to join plot.
            EventManager.createEvent(p.getUniqueId().toString(), "plotsystem", plotSQL.getString("SELECT server FROM location_data WHERE name='" +
                    plotSQL.getString("SELECT location FROM plot_data WHERE id=" + plot + ";") + "';"), "join plot " + plot);

            //Remove invite.
            plotSQL.update("DELETE FROM plot_invites WHERE id=" + plot + " AND uuid='" + p.getUniqueId() + "';");
        } else {
            p.sendMessage(Utils.error("You have not been invited to join this plot."));
        }
    }

    private void error(Player p) {
        p.sendMessage(Utils.error("/plot info <plotID>"));
        p.sendMessage(Utils.error("/plot join <plotID>"));
    }
}
