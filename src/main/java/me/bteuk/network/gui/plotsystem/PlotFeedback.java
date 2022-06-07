package me.bteuk.network.gui.plotsystem;

import me.bteuk.network.Network;
import me.bteuk.network.gui.UniqueGui;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.sql.PlotSQL;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static jdk.javadoc.internal.doclets.formats.html.markup.HtmlStyle.title;

public class PlotFeedback {

    public static UniqueGui createPlotFeedback(NetworkUser user, int plotID) {

        UniqueGui gui = new UniqueGui(45, Component.text("Plot " + plotID + " Feedback", NamedTextColor.AQUA, TextDecoration.BOLD));

        PlotSQL plotSQL = Network.getInstance().plotSQL;
        GlobalSQL globalSQL = Network.getInstance().globalSQL;

        ArrayList<Integer> books = plotSQL.getIntList("SELECT book_id FROM deny_data WHERE id=" + plotID + " AND uuid=" + user.player.getUniqueId() + ";");

        //Inventory slot.
        int slot = 10;

        //Plot attempt, starting at highest attempt.
        int attempt = books.size();

        for (int bookID : books) {

            //Create book to open feedback.
            int finalAttempt = attempt;
            gui.setItem(slot, Utils.createItem(Material.WRITTEN_BOOK, 1,
                            Utils.chat("&b&lAttempt " + attempt),
                            Utils.chat("&fClick to view plot feedback.")),
                    u -> {

                        //Delete this inventory.
                        u.uniqueGui.delete();
                        u.player.closeInventory();

                        //Create book.
                        ItemStack writtenBook = new ItemStack(Material.WRITTEN_BOOK);
                        BookMeta bookMeta = (BookMeta) writtenBook.getItemMeta();
                        bookMeta.setTitle(Utils.chat("&b&lPlot " + plotID + " Attempt " + finalAttempt));

                        //Get book author, aka the reviewer.
                        String author = globalSQL.getString("SELECT name FROM player_data WHERE uuid=" +
                                plotSQL.getString("SELECT reviewer FROM deny_data WHERE id=" + plotID + " AND attempt=" + finalAttempt + ";") + ";");
                        bookMeta.setAuthor(author);

                        //Get pages of the book.
                        ArrayList<String> pages = plotSQL.getStringList("SELECT text FROM book_data WHERE id=" + bookID + ";");

                        //Set the pages of the book.
                        bookMeta.setPages(pages);
                        writtenBook.setItemMeta(bookMeta);

                        //Open the book.
                        u.player.openBook(writtenBook);

                    });

            //Increase slot accordingly.
            if (slot%9 == 7) {
                //Increase row, basically add 3.
                slot += 3;
            } else if (slot == 34) {
                //Last possible slot, end iteration.
                break;
            } else {
                //Increase value by 1.
                slot++;
            }

            //Decrease attempt count.
            attempt--;

        }


        //Return
        gui.setItem(44, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.chat("&b&lReturn"),
                        Utils.chat("&fOpen the plot info for this plot.")),
                u -> {

                    //Delete this inventory.
                    u.uniqueGui.delete();
                    u.player.closeInventory();

                    //Open the plot menu.
                    u.uniqueGui = PlotInfo.createPlotInfo(plotID);
                    u.uniqueGui.open(u);

                });

        return gui;

    }
}
