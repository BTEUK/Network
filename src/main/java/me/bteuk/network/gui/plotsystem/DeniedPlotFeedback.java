package me.bteuk.network.gui.plotsystem;

import me.bteuk.network.Network;
import me.bteuk.network.gui.UniqueGui;
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

public class DeniedPlotFeedback {

    public static UniqueGui createDeniedPlotFeedback(int plotID) {

        UniqueGui gui = new UniqueGui(45, Component.text("Plot " + plotID + " feedback", NamedTextColor.AQUA, TextDecoration.BOLD));

        //Get plot sql.
        PlotSQL plotSQL = Network.getInstance().plotSQL;

        //Get global sql.
        GlobalSQL globalSQL = Network.getInstance().globalSQL;

        //Get plot owner uuid.
        String uuid = plotSQL.getString("SELEC FROM plot_members WHERE id=" + plotID + " AND is_owner=1;");

        //Get the number of times the plot was denied for the current plot owner.
        int deniedCount = plotSQL.getInt("SELECT COUNT(attempt) FROM deny_data WHERE id=" + plotID + " AND uuid=" + uuid + ";");

        //Slot count.
        int slot = 10;

        //Iterate through the deniedCount inversely.
        //We cap the number at 21, since we'd never expect a player to have more plots denied than that,
        //it also saves us having to create multiple pages.
        for (int i = deniedCount; i > 0; deniedCount--) {

            //If the slot is greater than the number that fit in a page, stop.
            if (slot > 34) {

                break;

            }

            //Add player to gui.
            int finalDeniedCount = deniedCount;
            gui.setItem(slot, Utils.createItem(Material.WRITTEN_BOOK, 1,
                            Utils.chat("&b&lFeedback for submission " + deniedCount),
                            Utils.chat("&fClick to view feedback for this submission."),
                            Utils.chat("&fReviewed by &7" + globalSQL.getString("SELECT name FROM player_data WHERE uuid="
                                    + plotSQL.getString("SELECT reviewer FROM deny_data WHERE id=" + plotID + " AND uuid=" + uuid + " AND attempt=" + deniedCount + ";")))),

                    u ->

                    {

                        //Delete this inventory.
                        u.uniqueGui.delete();
                        u.uniqueGui = null;
                        u.player.closeInventory();

                        //Create book.
                        ItemStack writtenBook = new ItemStack(Material.WRITTEN_BOOK);
                        BookMeta bookMeta = (BookMeta) writtenBook.getItemMeta();
                        bookMeta.setTitle(Utils.chat("&b&lPlot " + plotID + " Attempt " + finalDeniedCount));

                        //Get book author, aka the reviewer.
                        String author = globalSQL.getString("SELECT name FROM player_data WHERE uuid=" +
                                plotSQL.getString("SELECT reviewer FROM deny_data WHERE id=" + plotID + " AND uuid=" + uuid + " AND attempt=" + finalDeniedCount + ";") + ";");
                        bookMeta.setAuthor(author);

                        //Get pages of the book.
                        ArrayList<String> pages = plotSQL.getStringList("SELECT text FROM book_data WHERE id="
                                + plotSQL.getInt("SELECT book_id FROM deny_data WHERE id=" + plotID + " AND uuid=" + uuid + " AND attempt=" + finalDeniedCount + ";") + ";");

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
        gui.setItem(44, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.chat("&b&lReturn"),
                        Utils.chat("&fReturn to the menu of plot " + plotID + ".")),
                u ->

                {

                    //Delete this inventory.
                    u.uniqueGui.delete();
                    u.player.closeInventory();

                    //Return to the plot info menu.
                    u.uniqueGui = PlotInfo.createPlotInfo(plotID);
                    u.uniqueGui.open(u);

                });

        return gui;


    }
}