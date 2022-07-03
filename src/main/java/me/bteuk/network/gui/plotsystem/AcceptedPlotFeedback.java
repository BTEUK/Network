package me.bteuk.network.gui.plotsystem;

import me.bteuk.network.Network;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.sql.PlotSQL;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.PlotValues;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

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
                            Utils.chat("&b&lPrevious Page"),
                            Utils.chat("&fOpen the previous page of accepted plots.")),
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
                                Utils.chat("&b&lNext Page"),
                                Utils.chat("&fOpen the next page of online users.")),
                        u ->

                        {

                            //Update the gui.
                            page++;
                            this.refresh();
                            u.player.getOpenInventory().getTopInventory().setContents(this.getInventory().getContents());

                        });

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
                        Utils.chat("&b&lPlot " + plot),
                        Utils.chat("&fAccepted by &7" + globalSQL.getString("SELECT name FROM player_data WHERE uuid='"
                                + plotSQL.getString("SELECT reviewer FROM accept_data WHERE id=" + plot + ";") + "';")),
                        Utils.chat("&fDifficulty: &7" + PlotValues.difficultyName(plotSQL.getInt("SELECT difficulty FROM plot_data WHERE id=" + plot + ";"))),
                        Utils.chat("&fSize: &7" + PlotValues.sizeName(plotSQL.getInt("SELECT size FROM plot_data WHERE id=" + plot + ";"))),
                        Utils.chat("&fAccuracy: &7" + plotSQL.getInt("SELECT accuracy FROM accept_data WHERE id=" + plot + ";") + "&f/&75"),
                        Utils.chat("&fQuality: &7" + plotSQL.getInt("SELECT quality FROM accept_data WHERE id=" + plot + ";") + "&f/&75")));

            } else {

                //There is feedback
                setItem(slot, Utils.createItem(Material.WRITTEN_BOOK, 1,
                                Utils.chat("&b&lPlot " + plot),
                                Utils.chat("&fAccepted by &7" + globalSQL.getString("SELECT name FROM player_data WHERE uuid='"
                                        + plotSQL.getString("SELECT reviewer FROM accept_data WHERE id=" + plot + ";") + "';")),
                                Utils.chat("&fDifficulty: &7" + PlotValues.difficultyName(plotSQL.getInt("SELECT difficulty FROM plot_data WHERE id=" + plot + ";"))),
                                Utils.chat("&fSize: &7" + PlotValues.sizeName(plotSQL.getInt("SELECT size FROM plot_data WHERE id=" + plot + ";"))),
                                Utils.chat("&fAccuracy: &7" + plotSQL.getInt("SELECT accuracy FROM accept_data WHERE id=" + plot + ";") + "&f/&75"),
                                Utils.chat("&fQuality: &7" + plotSQL.getInt("SELECT quality FROM accept_data WHERE id=" + plot + ";") + "&f/&75"),
                                Utils.chat("&fClick to view feedback for this plot.")),
                        u ->

                        {

                            //Close the inventory.
                            u.player.closeInventory();

                            //Create book.
                            ItemStack writtenBook = new ItemStack(Material.WRITTEN_BOOK);
                            BookMeta bookMeta = (BookMeta) writtenBook.getItemMeta();
                            bookMeta.setTitle(Utils.chat("&b&lPlot " + plot));

                            //Get book author, aka the reviewer.
                            String author = globalSQL.getString("SELECT name FROM player_data WHERE uuid='" +
                                    plotSQL.getString("SELECT reviewer FROM accept_data WHERE id=" + plot + ";") + "';");
                            bookMeta.setAuthor(author);

                            //Get pages of the book.
                            ArrayList<String> pages = plotSQL.getStringList("SELECT contents FROM book_data WHERE id="
                                    + plotSQL.getInt("SELECT book_id FROM accept_data WHERE id=" + plot + ";") + ";");

                            //Set the pages of the book.
                            bookMeta.setPages(pages);
                            writtenBook.setItemMeta(bookMeta);

                            //Open the book.
                            u.player.openBook(writtenBook);

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
                        Utils.chat("&b&lReturn"),
                        Utils.chat("&fOpen the plot menu.")),
                u ->

                {

                    //Delete this gui.
                    this.delete();
                    u.acceptedPlotFeedback = null;

                    //Switch tback the plot menu.
                    u.plotMenu = new PlotMenu(u);
                    u.plotMenu.open(u);

                });
    }

    public void refresh() {

        this.clearGui();
        createGui();

    }
}
