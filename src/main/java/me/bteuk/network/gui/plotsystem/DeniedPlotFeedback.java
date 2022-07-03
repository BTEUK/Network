package me.bteuk.network.gui.plotsystem;

import me.bteuk.network.Network;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.sql.PlotSQL;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;

public class DeniedPlotFeedback extends Gui {

    private final PlotSQL plotSQL;
    private final GlobalSQL globalSQL;

    private final int plotID;

    public DeniedPlotFeedback(int plotID) {

        super(45, Component.text("Plot " + plotID + " feedback", NamedTextColor.AQUA, TextDecoration.BOLD));

        this.plotID = plotID;

        //Get plot sql.
        plotSQL = Network.getInstance().plotSQL;

        //Get global sql.
        globalSQL = Network.getInstance().globalSQL;

        createGui();

    }

    private void createGui() {

        //Get plot owner uuid.
        String uuid = plotSQL.getString("SELECT uuid FROM plot_members WHERE id=" + plotID + " AND is_owner=1;");

        //Get the number of times the plot was denied for the current plot owner.
        int deniedCount = plotSQL.getInt("SELECT COUNT(attempt) FROM deny_data WHERE id=" + plotID + " AND uuid='" + uuid + "';");

        //Slot count.
        int slot = 10;

        //Iterate through the deniedCount inversely.
        //We cap the number at 21, since we'd never expect a player to have more plots denied than that,
        //it also saves us having to create multiple pages.
        for (int i = deniedCount; i > 0; i--) {

            //If the slot is greater than the number that fit in a page, stop.
            if (slot > 34) {

                break;

            }

            //Add player to gui.
            int finalI = i;
            setItem(slot, Utils.createItem(Material.WRITTEN_BOOK, 1,
                            Utils.chat("&b&lFeedback for submission " + i),
                            Utils.chat("&fClick to view feedback for this submission."),
                            Utils.chat("&fReviewed by &7" + globalSQL.getString("SELECT name FROM player_data WHERE uuid='"
                                    + plotSQL.getString("SELECT reviewer FROM deny_data WHERE id=" + plotID + " AND uuid='" + uuid + "' AND attempt=" + i + ";") + "';"))),

                    u ->

                    {

                        //Close inventory.
                        u.player.closeInventory();

                        //Create book.
                        ItemStack writtenBook = new ItemStack(Material.WRITTEN_BOOK);
                        BookMeta bookMeta = (BookMeta) writtenBook.getItemMeta();
                        bookMeta.setTitle(Utils.chat("&b&lPlot " + plotID + " Attempt " + finalI));

                        //Get book author, aka the reviewer.
                        String author = globalSQL.getString("SELECT name FROM player_data WHERE uuid='" +
                                plotSQL.getString("SELECT reviewer FROM deny_data WHERE id=" + plotID + " AND uuid='" + uuid + "' AND attempt=" + finalI + ";") + "';");
                        bookMeta.setAuthor(author);

                        //Get pages of the book.
                        ArrayList<String> pages = plotSQL.getStringList("SELECT contents FROM book_data WHERE id="
                                + plotSQL.getInt("SELECT book_id FROM deny_data WHERE id=" + plotID + " AND uuid='" + uuid + "' AND attempt=" + finalI + ";") + ";");

                        //Set the pages of the book.
                        bookMeta.setPages(pages);
                        writtenBook.setItemMeta(bookMeta);

                        //Open the book.
                        u.player.openBook(writtenBook);

                    });


            //Increase slot accordingly.
            if (slot % 9 == 7) {
                //Increase row, basically add 3.
                slot += 3;
            } else {
                //Increase value by 1.
                slot++;
            }
        }

        //Return to plot info menu.
        setItem(44, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.chat("&b&lReturn"),
                        Utils.chat("&fReturn to the menu of plot " + plotID + ".")),
                u ->

                {

                    //Delete this gui.
                    this.delete();
                    u.deniedPlotFeedback = null;

                    //Switch back to plot menu.
                    u.plotInfo = new PlotInfo(plotID, u.player.getUniqueId().toString());
                    u.plotInfo.open(u);

                });
    }

    public void refresh() {

        this.clearGui();
        createGui();

    }
}
