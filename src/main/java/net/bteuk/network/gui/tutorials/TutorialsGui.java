package net.bteuk.network.gui.tutorials;

import net.bteuk.network.Network;
import net.bteuk.network.gui.Gui;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.utils.NetworkUser;
import net.bteuk.network.utils.Role;
import net.bteuk.network.utils.Roles;
import net.bteuk.network.utils.SwitchServer;
import net.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import teachingtutorials.guis.Event;
import teachingtutorials.guis.EventType;
import teachingtutorials.tutorialobjects.Tutorial;
import teachingtutorials.tutorialplaythrough.Lesson;
import teachingtutorials.utils.User;

import static net.bteuk.network.utils.Constants.LOGGER;
import static net.bteuk.network.utils.NetworkConfig.CONFIG;

public class TutorialsGui extends Gui {

    private final NetworkUser user;

    public TutorialsGui(NetworkUser user) {

        super(27, Component.text("Tutorials Menu", NamedTextColor.AQUA, TextDecoration.BOLD));

        this.user = user;

        createGui();
    }

    protected static void switchServer(NetworkUser user) {
        SwitchServer.switchServer(user.player, Network.getInstance().getGlobalSQL().getString("SELECT name FROM " +
                "server_data WHERE type='TUTORIAL';"));
        user.player.closeInventory();
    }

    /**
     * Creates the icons and actions for this menu
     */
    private void createGui() {

        // Retrieves the critical information for the menu
        boolean bCompulsoryTutorialEnabled = CONFIG.getBoolean("tutorials.compulsory_tutorial");

        // Gets the information about the Tutorials User
        User tutorialsUser = new User(this.user.player);
        tutorialsUser.fetchDetailsByUUID(Network.getInstance().getTutorialsDBConnection(), LOGGER);
        tutorialsUser.calculateRatings(Network.getInstance().getTutorialsDBConnection());

        // Initiates the current tutorial object
        Tutorial currentTutorial = null;

        // Initiates the next tutorial object - decides the next tutorial
        Tutorial nextTutorial = Lesson.decideTutorial(tutorialsUser, Network.getInstance().getTutorialsDBConnection()
                , LOGGER);
        if (nextTutorial == null)
            return;

        // Get compulsory tutorial ID
        int iCompulsoryTutorialID;
        Tutorial[] compulsoryTutorials = Tutorial.fetchAll(true, true, null,
                Network.getInstance().getTutorialsDBConnection(), LOGGER);
        if (compulsoryTutorials.length == 0)
            iCompulsoryTutorialID = -1;
        else
            iCompulsoryTutorialID = compulsoryTutorials[0].getTutorialID();

        // Designs the 'Continue' menu button
        ItemStack continueLearning_CompulsoryComplete;
        if (tutorialsUser.hasIncompleteLessons(Network.getInstance().getTutorialsDBConnection(), LOGGER)) {
            // Get current lesson's tutorial ID and sets up the tutorial object from this
            int iTutorialIDCurrentLesson = Lesson.getTutorialOfCurrentLessonOfPlayer(user.player.getUniqueId(),
                    Network.getInstance().getTutorialsDBConnection(), LOGGER);
            currentTutorial = Tutorial.fetchByTutorialID(iTutorialIDCurrentLesson,
                    Network.getInstance().getTutorialsDBConnection(), LOGGER);

            // Sets up the menu icon with the name of the current tutorial
            continueLearning_CompulsoryComplete = Utils.createItem(Material.WRITABLE_BOOK, 1,
                    Utils.title("Resume Your Lesson"),
                    Utils.line(currentTutorial.getTutorialName()));
        } else {
            // Sets up the menu icon with the new tutorial's name
            continueLearning_CompulsoryComplete = Utils.createItem(Material.WRITABLE_BOOK, 1,
                    Utils.title("Start a new Tutorial:"),
                    Utils.line(nextTutorial.getTutorialName()));
        }

        // Designs the tutorials library menu option
        ItemStack tutorialLibrary = Utils.createItem(Material.BOOKSHELF, 1,
                Utils.title("Tutorial Library"),
                Utils.line("Browse all of our available tutorials"));

        //------------------------------------------------------------
        //-------- Begin arranging the menu icons and actions --------
        //------------------------------------------------------------

        // Add the compulsory tutorial button if enabled.
        if (bCompulsoryTutorialEnabled && iCompulsoryTutorialID >= 0) {
            // Fetches the details of the compulsory tutorial
            Tutorial compulsoryTutorial = Tutorial.fetchByTutorialID(iCompulsoryTutorialID,
                    Network.getInstance().getTutorialsDBConnection(), LOGGER);

            // If the player has already completed the compulsory tutorial.
            if (tutorialsUser.bHasCompletedCompulsory) {
                //------ Compulsory Tutorial Option ------
                ItemStack compulsory;

                boolean bUserRedoingCompulsoryTutorial;
                if (tutorialsUser.hasIncompleteLessons(Network.getInstance().getTutorialsDBConnection(), LOGGER))
                    bUserRedoingCompulsoryTutorial = currentTutorial.getTutorialID() == iCompulsoryTutorialID;
                else
                    bUserRedoingCompulsoryTutorial = false;

                // User is currently redoing the compulsory tutorial
                if (bUserRedoingCompulsoryTutorial) {
                    compulsory = Utils.createItem(Material.BOOK, 1,
                            Utils.title("Restart the Starter Tutorial"),
                            Utils.line("Restart the starter tutorial again"));

                    super.setItem(11 - 1, compulsory, new guiAction() {
                        @Override
                        public void click(NetworkUser user) {
                            clickRestart(compulsoryTutorial);
                        }
                    });
                }

                // User is currently in a different tutorial
                else if (tutorialsUser.hasIncompleteLessons(Network.getInstance().getTutorialsDBConnection(), LOGGER)) {
                    compulsory = Utils.createItem(Material.ENCHANTED_BOOK, 1,
                            Utils.title("Restart the Starter Tutorial"),
                            Utils.line("Finish your current lesson first!"));
                    super.setItem(11 - 1, compulsory, null);
                }
                // User is not in any tutorial
                else {
                    compulsory = Utils.createItem(Material.BOOK, 1,
                            Utils.title("Restart the Starter Tutorial"),
                            Utils.line("Refresh your essential knowledge"));

                    super.setItem(11 - 1, compulsory, user -> {
                        clickRestart(compulsoryTutorial);
                        // Deletes this gui
                        delete();
                    });
                }

                //---------- Library Option ----------
                super.setItem(14 - 1, tutorialLibrary, user -> {
                    clickLibrary();
                    // Deletes this gui
                    delete();
                });

                // Decide on continue option
                Tutorial tutorialForContinue;
                if (tutorialsUser.hasIncompleteLessons(Network.getInstance().getTutorialsDBConnection(), LOGGER))
                    tutorialForContinue = currentTutorial;
                else
                    tutorialForContinue = nextTutorial;

                //----- Continue Learning Option -----
                super.setItem(17 - 1, continueLearning_CompulsoryComplete, user -> {
                    clickContinue(tutorialForContinue);
                    // Deletes this gui
                    delete();
                });
            } else {

                ItemStack resumeCompulsory;
                ItemStack restartCompulsory;
                ItemStack beginCompulsory;

                Role applicant = Roles.getRoleById("applicant");

                // They are currently in the compulsory tutorial for the first time
                // OR: The user was in another lesson and someone added a compulsory tutorial to the system
                if (tutorialsUser.hasIncompleteLessons(Network.getInstance().getTutorialsDBConnection(), LOGGER)) {
                    // Player is in a lesson other than the compulsory but hasn't started the compulsory (compulsory
                    // added to system)
                    if (currentTutorial.getTutorialID() != iCompulsoryTutorialID) {
                        ItemStack erroneousTutorial = Utils.createItem(Material.BOOK, 1,
                                Utils.title("Continue Your Tutorial"),
                                Utils.line("You must then complete the starter tutorial"));

                        Tutorial finalCurrentTutorial = currentTutorial;

                        super.setItem(14 - 1, erroneousTutorial, new guiAction() {

                            @Override
                            public void click(NetworkUser user) {
                                clickContinue(finalCurrentTutorial);
                                // Deletes this gui
                                delete();
                            }
                        });
                    }

                    // Player is half way through the compulsory tutorial but hasn't ever finished it
                    else {
                        restartCompulsory = Utils.createItem(Material.BOOK, 1,
                                Utils.title("Restart the Starter Tutorial"),
                                Utils.line("Gain the ")
                                        .append(applicant == null ? Utils.line("Applicant") :
                                                applicant.getColouredRoleName())
                                        .append(ChatUtils.line("rank!")));

                        resumeCompulsory = Utils.createItem(Material.WRITABLE_BOOK, 1,
                                Utils.title("Resume the Starter Tutorial"),
                                Utils.line("Gain the ")
                                        .append(applicant == null ? Utils.line("Applicant") :
                                                applicant.getColouredRoleName())
                                        .append(ChatUtils.line("rank!")));

                        super.setItem(12 - 1, restartCompulsory, user -> {
                            clickRestart(compulsoryTutorial);
                            // Deletes this gui
                            delete();
                        });

                        super.setItem(16 - 1, resumeCompulsory, user -> {
                            clickContinue(compulsoryTutorial);
                            // Deletes this gui
                            delete();
                        });
                    }
                } else // They have never started the compulsory tutorial
                {
                    beginCompulsory = Utils.createItem(Material.BOOK, 1,
                            Utils.title("Begin the Starter Tutorial"),
                            Utils.line("Gain the ")
                                    .append(applicant == null ? Utils.line("Applicant") :
                                            applicant.getColouredRoleName())
                                    .append(ChatUtils.line("rank!")));

                    super.setItem(14 - 1, beginCompulsory, user -> {
                        clickRestart(compulsoryTutorial);
                        // Deletes this gui
                        delete();
                    });
                }
            }
        }

        // There is no compulsory tutorial
        else {
            // Decide on continue option
            Tutorial tutorialForContinue;
            if (tutorialsUser.hasIncompleteLessons(Network.getInstance().getTutorialsDBConnection(), LOGGER))
                tutorialForContinue = currentTutorial;
            else
                tutorialForContinue = nextTutorial;

            // Continue learning, compulsory complete
            super.setItem(16 - 1, continueLearning_CompulsoryComplete, user -> {
                clickContinue(tutorialForContinue);
                // Deletes this gui
                delete();
            });

            // Library
            super.setItem(12 - 1, tutorialLibrary, user -> {
                clickLibrary();
                // Deletes this gui
                delete();
            });
        }

        // Admin and creator menu
        if (tutorialsUser.player.hasPermission("TeachingTutorials.Admin") || tutorialsUser.player.hasPermission(
                "TeachingTutorials.Creator")) {
            // Admin and creator menu
            super.setItem(19 - 1, teachingtutorials.utils.Utils.createItem(Material.LECTERN, 1,
                    Utils.title("Admin Menu"), Utils.line("Teleport to the tutorials server")), user ->
            {
                switchServer(user);
            });
        }

        // Return
        setItem(26, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.title("Return"),
                        Utils.line("Open the navigator main menu.")),
                user ->

                {

                    // Delete this gui.
                    this.delete();
                    user.mainGui = null;

                    // Switch to navigation menu.
                    Network.getInstance().navigatorGui.open(user);
                });
    }

    public void refresh() {

        this.clearGui();
        createGui();
    }

    private void clickContinue(Tutorial tutorial) {
        // Switch to the tutorial.
        if (Event.addEvent(EventType.CONTINUE, user.player.getUniqueId(), tutorial.getTutorialID(),
                Network.getInstance().getTutorialsDBConnection(), LOGGER))
            switchServer(user);
        else
            user.sendMessage(Utils.error("A problem occurred, please let staff know"));
    }

    private void clickLibrary() {
        // Delete this gui.
        this.delete();

        // Switch to tutorial library menu.
        user.mainGui = new TutorialLibraryGui(user,
                Tutorial.getInUseTutorialsWithLocations(Network.getInstance().getTutorialsDBConnection(), LOGGER));
        user.mainGui.open(user);
    }

    private void clickRestart(Tutorial tutorial) {
        // Switch to the tutorial.
        if (Event.addEvent(EventType.RESTART, user.player.getUniqueId(), tutorial.getTutorialID(),
                Network.getInstance().getTutorialsDBConnection(), LOGGER))
            switchServer(user);
        else
            user.sendMessage(Utils.error("A problem occurred, please let staff know"));
    }
}
