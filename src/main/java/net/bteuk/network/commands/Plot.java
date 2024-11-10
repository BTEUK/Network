package net.bteuk.network.commands;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.bteuk.network.Network;
import net.bteuk.network.commands.tabcompleters.FixedArgSelector;
import net.bteuk.network.eventing.events.EventManager;
import net.bteuk.network.gui.plotsystem.PlotInfo;
import net.bteuk.network.gui.plotsystem.PlotMenu;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.sql.PlotSQL;
import net.bteuk.network.utils.NetworkUser;
import net.bteuk.network.utils.Utils;
import net.bteuk.network.utils.enums.PlotStatus;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

import static net.bteuk.network.utils.Constants.LOGGER;

public class Plot extends AbstractCommand {

    private final Network instance;
    private final PlotSQL plotSQL;

    public Plot(Network instance) {
        this.instance = instance;
        plotSQL = Network.getInstance().getPlotSQL();
        setTabCompleter(new FixedArgSelector(Arrays.asList("info", "join"), 0));
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {

        //Check if the sender is a player.
        Player player = getPlayer(stack);
        if (player == null) {
            return;
        }

        if (args.length < 1) {
            error(player);
            return;
        }

        int plotID = -1;
        if (args.length > 1) {
            //Check if the plotID is an actual number.
            try {
                plotID = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                error(player);
                return;
            }
        }

        switch (args[0]) {
            case "menu" -> menu(player);
            case "info" -> info(player, plotID);
            case "join" -> join(player, plotID);
            case "feedback" -> feedback(player, plotID);
            default -> error(player);
        }
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

    private void feedback(Player player, int plot) {
        if (plot == -1) {
            error(player);
            return;
        }
        // Check if the player is the owner of a member of the plot.
        // Then open the latest feedback.
        // And set their Main gui to the plot info of this plot.
        if (!plotSQL.hasRow("SELECT id FROM plot_members WHERE id=" + plot + " AND uuid='" + player.getUniqueId() + "';")) {
            player.sendMessage(ChatUtils.error("You are no longer the owner or a member of this plot."));
            return;
        }

        // Find the latest attempt.
        String uuid = plotSQL.getString("SELECT uuid FROM plot_members WHERE id=" + plot + " AND is_owner=1;");
        int latestAttempt = plotSQL.getInt("SELECT MAX(attempt) FROM deny_data WHERE id=" + plot + " AND uuid='" + uuid + "';");

        if (latestAttempt == 0) {
            player.sendMessage(Utils.error("There is no feedback available for this plot."));
            return;
        }

        NetworkUser user = instance.getUser(player);
        if (user != null) {
            user.mainGui = new PlotInfo(user, plot);
        }

        // Open the feedback, if it exists.
        //Create book.
        Component title = Component.text("Plot " + plot + " Attempt " + latestAttempt, NamedTextColor.AQUA, TextDecoration.BOLD);
        Component author = Component.text(instance.getGlobalSQL().getString("SELECT name FROM player_data WHERE uuid='" +
                plotSQL.getString("SELECT reviewer FROM deny_data WHERE id=" + plot + " AND uuid='" + uuid + "' AND attempt=" + latestAttempt + ";") + "';"));

        //Get pages of the book.
        ArrayList<String> sPages = plotSQL.getStringList("SELECT contents FROM book_data WHERE id="
                + plotSQL.getInt("SELECT book_id FROM deny_data WHERE id=" + plot + " AND uuid='" + uuid + "' AND attempt=" + latestAttempt + ";") + ";");

        //Create a list of components from the list of strings.
        ArrayList<Component> pages = new ArrayList<>();
        for (String page : sPages) {
            pages.add(Component.text(page));
        }

        Book book = Book.book(title, author, pages);

        //Open the book.
        player.openBook(book);
    }

    private void error(Player p) {
        p.sendMessage(ChatUtils.error("/plot menu"));
        p.sendMessage(ChatUtils.error("/plot info <plotID>"));
        p.sendMessage(ChatUtils.error("/plot join <plotID>"));
        p.sendMessage(ChatUtils.error("/plot feedback <plotID>"));
    }
}
