package me.bteuk.network.gui.plotsystem;

import me.bteuk.network.Network;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.sql.PlotSQL;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.PlotValues;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

import java.util.ArrayList;

public class AcceptedPlotFeedback extends Gui {

    private final NetworkUser user;

    private int page;

    private final GlobalSQL globalSQL;
    private final PlotSQL plotSQL;

    public AcceptedPlotFeedback(NetworkUser user) {

        super(45, Component.text("Accepted Plots", NamedTextColor.AQUA, TextDecoration.BOLD));

        this.user = user;

        page = 1;

        //Get global sql.
        globalSQL = Network.getInstance().globalSQL;

        //Get plot sql.
        plotSQL = Network.getInstance().plotSQL;

        createGui();

    }

    private void createGui() {

        //Get all accepted plots sorted by most recently accepted.
        ArrayList<Integer> plots = plotSQL.getIntList("SELECT id FROM accept_data WHERE uuid='" + user.player.getUniqueId() + "' ORDER BY accept_time DESC;");

        //Slot count.
        int slot = 10;

        //Skip count.
        int skip = 21 * (page - 1);

        //If page is greater than 1 add a previous page button.
        if (page > 1) {
            setItem(18, Utils.createItem(Material.ARROW, 1,
                            Utils.title("Previous Page"),
                            Utils.line("Open the previous page of accepted plots.")),
                    u ->

                    {

                        //Update the gui.
                        page--;
                        this.refresh();
                        u.player.getOpenInventory().getTopInventory().setContents(this.getInventory().getContents());

                    });
        }

        //Iterate through all accepted plots.
        for (int plot : plots) {

            //If the slot is greater than the number that fit in a page, create a new page.
            if (slot > 34) {

                setItem(26, Utils.createItem(Material.ARROW, 1,
                                Utils.title("Next Page"),
                                Utils.line("Open the next page of online users.")),
                        u ->

                        {

                            //Update the gui.
                            page++;
                            this.refresh();
                            u.player.getOpenInventory().getTopInventory().setContents(this.getInventory().getContents());

                        });

                //Stop iterating.
                break;

            }

            //If skip is greater than 0, skip this iteration.
            if (skip > 0) {
                skip--;
                continue;
            }

            //Add plot to gui.
            //If there is feedback add click event to view feedback, else not.
            if (plotSQL.hasRow("SELECT id FROM accept_data WHERE id=" + plot + " AND book_id=0;")) {

                //No feedback
                setItem(slot, Utils.createItem(Material.BOOK, 1,
                        Utils.title("Plot " + plot),
                        Utils.line("Accepted by &7" + globalSQL.getString("SELECT name FROM player_data WHERE uuid='"
                                + plotSQL.getString("SELECT reviewer FROM accept_data WHERE id=" + plot + ";") + "';")),
                        Utils.line("Difficulty: &7" + PlotValues.difficultyName(plotSQL.getInt("SELECT difficulty FROM plot_data WHERE id=" + plot + ";"))),
                        Utils.line("Size: &7" + PlotValues.sizeName(plotSQL.getInt("SELECT size FROM plot_data WHERE id=" + plot + ";"))),
                        Utils.line("Accuracy: &7" + plotSQL.getInt("SELECT accuracy FROM accept_data WHERE id=" + plot + ";") + "&f/&75"),
                        Utils.line("Quality: &7" + plotSQL.getInt("SELECT quality FROM accept_data WHERE id=" + plot + ";") + "&f/&75")));

            } else {

                //There is feedback
                setItem(slot, Utils.createItem(Material.WRITTEN_BOOK, 1,
                                Utils.title("Plot " + plot),
                                Utils.line("Accepted by &7" + globalSQL.getString("SELECT name FROM player_data WHERE uuid='"
                                        + plotSQL.getString("SELECT reviewer FROM accept_data WHERE id=" + plot + ";") + "';")),
                                Utils.line("Difficulty: &7" + PlotValues.difficultyName(plotSQL.getInt("SELECT difficulty FROM plot_data WHERE id=" + plot + ";"))),
                                Utils.line("Size: &7" + PlotValues.sizeName(plotSQL.getInt("SELECT size FROM plot_data WHERE id=" + plot + ";"))),
                                Utils.line("Accuracy: &7" + plotSQL.getInt("SELECT accuracy FROM accept_data WHERE id=" + plot + ";") + "&f/&75"),
                                Utils.line("Quality: &7" + plotSQL.getInt("SELECT quality FROM accept_data WHERE id=" + plot + ";") + "&f/&75"),
                                Utils.line("Click to view feedback for this plot.")),
                        u ->

                        {

                            //Close the inventory.
                            u.player.closeInventory();

                            //Create book.
                            Component title = Component.text("Plot " + plot, NamedTextColor.AQUA, TextDecoration.BOLD);
                            Component author = Component.text(globalSQL.getString("SELECT name FROM player_data WHERE uuid='" +
                                    plotSQL.getString("SELECT reviewer FROM accept_data WHERE id=" + plot + ";") + "';"));

                            //Get pages of the book.
                            ArrayList<String> sPages = plotSQL.getStringList("SELECT contents FROM book_data WHERE id="
                                    + plotSQL.getInt("SELECT book_id FROM accept_data WHERE id=" + plot + ";") + ";");

                            //Create a list of components from the list of strings.
                            ArrayList<Component> pages = new ArrayList<>();
                            for (String page : sPages) {
                                pages.add(Component.text(page));
                            }

                            Book book = Book.book(title, author, pages);

                            //Open the book.
                            u.player.openBook(book);

                        });
            }


            //Increase slot accordingly.
            if (slot % 9 == 7) {
                //Increase row, basically add 3.
                slot += 3;
            } else {
                //Increase value by 1.
                slot++;
            }
        }

        //Return to plot menu.
        setItem(44, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.title("Return"),
                        Utils.line("Open the plot menu.")),
                u ->

                {

                    //Delete this gui.
                    this.delete();
                    u.mainGui = null;

                    //Switch tback the plot menu.
                    u.mainGui = new PlotMenu(u);
                    u.mainGui.open(u);

                });
    }

    public void refresh() {

        this.clearGui();
        createGui();

    }
}
