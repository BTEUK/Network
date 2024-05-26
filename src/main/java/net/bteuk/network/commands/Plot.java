package net.bteuk.network.commands;

import net.bteuk.network.Network;
import net.bteuk.network.commands.tabcompleters.FixedArgSelector;
import net.bteuk.network.eventing.events.EventManager;
import net.bteuk.network.gui.plotsystem.PlotInfo;
import net.bteuk.network.gui.plotsystem.PlotMenu;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.sql.PlotSQL;
import net.bteuk.network.utils.NetworkUser;
import net.bteuk.network.utils.enums.PlotStatus;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static net.bteuk.network.utils.Constants.LOGGER;

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

        if (args.length < 1) {
            error(p);
            return true;
        }

        int plotID = -1;
        if (args.length > 1) {
            //Check if the plotID is an actual number.
            try {
                plotID = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                error(p);
                return true;
            }
        }

        switch (args[0]) {
            case "menu" -> menu(p);
            case "info" -> info(p, plotID);
            case "join" -> join(p, plotID);
            default -> error(p);
        }
        return true;
    }

    private void menu(Player p) {
        // Get the user.
        NetworkUser u = instance.getUser(p);
        if (u == null) {
            p.sendMessage(ChatUtils.error("An error occurred, please rejoin!"));
            LOGGER.severe("No user exists for player " + p.getName());
            return;
        }
        // Open the plot menu.
        if (u.mainGui != null) {
            u.mainGui.delete();
        }
        u.mainGui = new PlotMenu(u);
        u.mainGui.open(u);
    }

    private void info(Player p, int plot) {
        if (plot == -1) {
            error(p);
            return;
        }
        // Check if the plot exists and is not deleted.
        PlotStatus status = PlotStatus.fromDatabaseValue(plotSQL.getString("SELECT status FROM plot_data WHERE id=" + plot + ";"));
        if (status == null || status == PlotStatus.DELETED) {
            p.sendMessage(ChatUtils.error("This plot does not exist."));
            return;
        }
        // Get the user.
        NetworkUser u = instance.getUser(p);
        if (u == null) {
            p.sendMessage(ChatUtils.error("An error occurred, please rejoin!"));
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
        if (plot == -1) {
            error(p);
            return;
        }

        //Check if they have an invite for this plot.
        if (plotSQL.hasRow("SELECT id FROM plot_invites WHERE id=" + plot + " AND uuid='" + p.getUniqueId() + "';")) {

            //Add server event to join plot.
            EventManager.createEvent(p.getUniqueId().toString(), "plotsystem", plotSQL.getString("SELECT server FROM location_data WHERE name='" +
                    plotSQL.getString("SELECT location FROM plot_data WHERE id=" + plot + ";") + "';"), "join plot " + plot);

            //Remove invite.
            plotSQL.update("DELETE FROM plot_invites WHERE id=" + plot + " AND uuid='" + p.getUniqueId() + "';");
        } else {
            p.sendMessage(ChatUtils.error("You have not been invited to join this plot."));
        }
    }

    private void error(Player p) {
        p.sendMessage(ChatUtils.error("/plot menu"));
        p.sendMessage(ChatUtils.error("/plot info <plotID>"));
        p.sendMessage(ChatUtils.error("/plot join <plotID>"));
    }
}
