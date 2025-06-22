package net.bteuk.network.gui.plotsystem;

import net.bteuk.network.Network;
import net.bteuk.network.eventing.events.EventManager;
import net.bteuk.network.gui.Gui;
import net.bteuk.network.sql.GlobalSQL;
import net.bteuk.network.sql.PlotSQL;
import net.bteuk.network.utils.SwitchServer;
import net.bteuk.network.utils.Utils;
import net.bteuk.network.utils.plotsystem.ReviewFeedback;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

import static net.bteuk.network.utils.Constants.SERVER_NAME;

public class VerificationInfo extends Gui {

    private final int verificationId;

    private final PlotSQL plotSQL;
    private final GlobalSQL globalSQL;

    public VerificationInfo(int verificationId) {

        // Create the menu.
        super(27, Component.text("Verification " + verificationId, NamedTextColor.AQUA, TextDecoration.BOLD));

        this.verificationId = verificationId;

        // Get plot sql.
        plotSQL = Network.getInstance().getPlotSQL();

        // Get global sql.
        globalSQL = Network.getInstance().getGlobalSQL();

        createGui();
    }

    public void createGui() {

        // Return
        setItem(26, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.title("Return"),
                        Utils.line("Open the plot menu.")),
                u -> {
                    this.delete();
                    u.mainGui = new PlotMenu(u);
                    u.mainGui.open(u);
                }
        );

        String verifierUuid =
                plotSQL.getString("SELECT verifier FROM plot_verification WHERE id=" + verificationId + ";");
        String verifier = globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + verifierUuid + "';");

        int plotId = plotSQL.getInt("SELECT plot_id FROM plot_review WHERE id=(SELECT review_id FROM " +
                "plot_verification WHERE id=" + verificationId + ");");
        boolean feedbackChanged =
                plotSQL.hasRow("SELECT 1 FROM plot_verification_category WHERE verification_id=" + verificationId +
                        " AND book_id_old <> book_id_new;");
        boolean selectionChanged =
                plotSQL.hasRow("SELECT 1 FROM plot_verification_category WHERE verification_id=" + verificationId +
                        " AND selection_old <> selection_old;");

        String outcomeOld =
                plotSQL.getBoolean("SELECT accepted_old FROM plot_verification WHERE id=" + verificationId + ";") ?
                        "Accepted" : "Denied";
        String outcomeNew =
                plotSQL.getBoolean("SELECT accepted_new FROM plot_verification WHERE id=" + verificationId + ";") ?
                        "Accepted" : "Denied";

        Component[] description;
        if (!outcomeOld.equals(outcomeNew)) {
            description = new Component[]{
                    Utils.line("Verified by " + verifier),
                    Utils.line("The outcome of the review"),
                    Utils.line("was altered from:"),
                    Utils.line(outcomeOld + " -> " + outcomeNew)
            };
        } else if (selectionChanged) {
            description = new Component[]{
                    Utils.line("Verified by " + verifier),
                    Utils.line("The selection of at least one"),
                    Utils.line("category was altered, check"),
                    Utils.line("the books to see the changes.")
            };
        } else if (feedbackChanged) {
            description = new Component[]{
                    Utils.line("Verified by " + verifier),
                    Utils.line("The feedback of at least one"),
                    Utils.line("category was altered, check"),
                    Utils.line("the books to see the changes.")
            };
        } else {
            description = new Component[]{
                    Utils.line("Verified by " + verifier)
            };
        }

        // Verification Info
        setItem(4, Utils.createItem(Material.BOOK, 1,
                Utils.title("Plot " + plotId),
                description));

        // If the selection or feedback was changed show the before and after books.
        setItem(20, Utils.createItem(Material.WRITABLE_BOOK, 1,
                        Utils.title("Initial Feedback"),
                        Utils.line("Click to show initial feedback"),
                        Utils.line("for categories that were"),
                        Utils.line("altered by the verifier.")),
                u -> {
                    // Open the feedback book.
                    u.player.openBook(ReviewFeedback.createVerificationFeedbackBook(verificationId, true));
                });

        // If the selection or feedback was changed show the before and after books.
        setItem(24, Utils.createItem(Material.WRITABLE_BOOK, 1,
                        Utils.title("Altered Feedback"),
                        Utils.line("Click to show altered feedback"),
                        Utils.line("for categories that were"),
                        Utils.line("altered by the verifier.")),
                u -> {
                    // Open the feedback book.
                    u.player.openBook(ReviewFeedback.createVerificationFeedbackBook(verificationId, false));
                });

        // Teleport to the plot.
        setItem(22, Utils.createItem(Material.ENDER_PEARL, 1,
                        Utils.title("Teleport to Plot"),
                        Utils.line("Click to teleport to this plot.")),
                u -> {
                    u.player.closeInventory();

                    // Get the server of the plot.
                    String server = plotSQL.getString("SELECT server FROM location_data WHERE name='"
                            + plotSQL.getString("SELECT location FROM plot_data WHERE id=" + plotId + ";")
                            + "';");

                    // If the plot is on the current server teleport them directly.
                    // Else teleport them to the correct server and them teleport them to the plot.
                    if (server.equals(SERVER_NAME)) {
                        EventManager.createTeleportEvent(false, u.player.getUniqueId().toString(), "plotsystem",
                                "teleport plot " + plotId, u.player.getLocation());
                    } else {
                        // Set the server join event.
                        EventManager.createTeleportEvent(true, u.player.getUniqueId().toString(), "plotsystem",
                                "teleport plot " + plotId, u.player.getLocation());

                        // Teleport them to another server.
                        SwitchServer.switchServer(u.player, server);
                    }
                });
    }

    public void refresh() {
        this.clearGui();
        createGui();
    }
}

