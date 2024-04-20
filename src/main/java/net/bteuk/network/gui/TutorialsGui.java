package net.bteuk.network.gui;

import net.bteuk.network.Network;
import net.bteuk.network.utils.NetworkUser;
import net.bteuk.network.utils.SwitchServer;
import net.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

import static net.bteuk.network.utils.NetworkConfig.CONFIG;

public class TutorialsGui extends Gui {

    private final NetworkUser user;

    public TutorialsGui(NetworkUser user) {

        super(27, Component.text("Tutorials Menu", NamedTextColor.AQUA, TextDecoration.BOLD));

        this.user = user;

        createGui();

    }

    private void createGui() {

        boolean inLesson = Network.getInstance().getTutorials().hasRow("SELECT InLesson FROM Players WHERE UUID='" + user.player.getUniqueId() + "' AND InLesson=1;");

        //Add the compulsory tutorial button if enabled.
        if (CONFIG.getBoolean("tutorials.compulsory_tutorial")) {

            boolean completedCompulsory = Network.getInstance().getTutorials().hasRow("SELECT CompletedCompulsory FROM Players WHERE UUID='" + user.player.getUniqueId() + "' AND CompletedCompulsory=1;");

            //If the player has already completed the compulsory tutorial.
            if (completedCompulsory) {

                setItem(13, Utils.createItem(Material.WRITABLE_BOOK, 1,
                                Utils.title("Continue Learning"),
                                Utils.line(inLesson ? "Continue your lesson" : "Start the next tutorial")),
                        u -> {

                            //Switch to the tutorial.
                            clickContinue();

                        });

                setItem(18, Utils.createItem(Material.ENCHANTED_BOOK, 1,
                                Utils.title("Restart Compulsory Tutorial")),
                        u -> {

                            //Switch to the tutorial.
                            clickCompulsory();

                        });

            } else {

                setItem(13, Utils.createItem(Material.BOOK, 1,
                                Utils.title(inLesson ? "Continue Compulsory Tutorial" : "Start Compulsory Tutorial"),
                                Utils.line("Gain the applicant rank!")),
                        u -> {

                            //Switch to the tutorial.
                            clickCompulsory();

                        });

            }
        } else {

            setItem(13, Utils.createItem(Material.WRITABLE_BOOK, 1,
                            Utils.title("Continue Learning"),
                            Utils.line(inLesson ? "Continue your lesson" : "Start the next tutorial")),
                    u -> {

                        //Switch to the tutorial.
                        clickContinue();

                    });

        }

        //Return
        setItem(26, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.title("Return"),
                        Utils.line("Open the navigator main menu.")),
                u ->

                {

                    //Delete this gui.
                    this.delete();
                    u.mainGui = null;

                    //Switch to navigation menu.
                    Network.getInstance().navigatorGui.open(u);

                });
    }

    public void refresh() {

        this.clearGui();
        createGui();

    }

    private void clickContinue() {
        //Switch to the tutorial.
        Network.getInstance().getTutorials().update("INSERT INTO Events(UUID,EventName) VALUES('" + user.player.getUniqueId() + "','CONTINUE');");
        switchServer();
    }

    private void clickCompulsory() {
        //Switch to the tutorial.
        Network.getInstance().getTutorials().update("INSERT INTO Events(UUID,EventName) VALUES('" + user.player.getUniqueId() + "','COMPULSORY');");
        switchServer();
    }

    private void switchServer() {
        SwitchServer.switchServer(user.player, Network.getInstance().getGlobalSQL().getString("SELECT name FROM server_data WHERE type='TUTORIAL';"));
        user.player.closeInventory();
    }
}
