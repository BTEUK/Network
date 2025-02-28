package net.bteuk.network.gui.tutorials;

import net.bteuk.network.Network;
import net.bteuk.network.gui.Gui;
import net.bteuk.network.utils.NetworkUser;
import net.bteuk.network.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import teachingtutorials.guis.Event;
import teachingtutorials.guis.EventType;
import teachingtutorials.guis.LibraryMenu;
import teachingtutorials.tutorialobjects.Tutorial;
import teachingtutorials.tutorialplaythrough.Lesson;
import teachingtutorials.utils.User;

import static net.bteuk.network.utils.Constants.LOGGER;

public class TutorialLibraryGui extends Gui {

    private final NetworkUser user;

    private Tutorial[] inUseTutorials;

    public TutorialLibraryGui(NetworkUser user, Tutorial[] inUseTutorials) {

        //Initialises the Gui with the menu icons already set
        super(LibraryMenu.getGUI(inUseTutorials));

        this.user = user;
        this.inUseTutorials = inUseTutorials;

        //Sets the actions of the menu
        setActions();

    }

    /**
     * Sets up the actions for this menu
     */
    private void setActions() {
        //Declare variables
        int i;
        int iTutorials;
        int iDiv;
        int iMod;
        int iRows;

        //Works out how many rows in the inventory are needed
        iTutorials = inUseTutorials.length;
        iDiv = iTutorials / 9;
        iMod = iTutorials % 9;

        if (iMod != 0 || iDiv == 0) {
            iDiv = iDiv + 1;
        }

        //Enables an empty row and then a row for the back button
        iRows = iDiv + 2;

        //Adds back button
        setAction((iRows * 9) - 1, u -> {
            delete();
            u.mainGui = new TutorialsGui(user);
            u.mainGui.open(u);
        });

        //Gets the information about the Tutorials User
        User tutorialsUser = new User(this.user.player);
        tutorialsUser.fetchDetailsByUUID(Network.getInstance().getTutorialsDBConnection(), LOGGER);

        if (tutorialsUser.hasIncompleteLessons(Network.getInstance().getTutorialsDBConnection(), LOGGER)) {
            //Get current lesson's tutorial ID and sets up the tutorial object from this
            int iTutorialIDCurrentLesson = Lesson.getTutorialOfCurrentLessonOfPlayer(user.player.getUniqueId(), Network.getInstance().getTutorialsDBConnection(), LOGGER);
            if (iTutorialIDCurrentLesson == -1) {
                Bukkit.getConsoleSender().sendMessage(Utils.error("An error occurred. Player is in lesson but has no lesson in the database"));
            }
            Tutorial currentTutorial = Tutorial.fetchByTutorialID(iTutorialIDCurrentLesson, Network.getInstance().getTutorialsDBConnection(), LOGGER);

            //Inv slot 0 = the first one
            //Adds the actions of each slot
            for (i = 0; i < inUseTutorials.length; i++) {
                int iSlot = i;
                setAction(iSlot, user -> {
                    Bukkit.getConsoleSender().sendMessage("Current TutorialID: " + currentTutorial.getTutorialID());
                    Bukkit.getConsoleSender().sendMessage("TutorialID of slot: " + inUseTutorials[iSlot].getTutorialID());
                    if (tutorialsUser.hasIncompleteLessons(Network.getInstance().getTutorialsDBConnection(), LOGGER) && (currentTutorial.getTutorialID() != inUseTutorials[iSlot].getTutorialID())) {
                        user.sendMessage(Utils.error("You cannot start a new tutorial before you finish your current one"));
                    } else {
                        if (Event.addEvent(EventType.LIBRARY, user.player.getUniqueId(), inUseTutorials[iSlot].getTutorialID(), Network.getInstance().getTutorialsDBConnection(), LOGGER))
                            TutorialsGui.switchServer(user);
                        else
                            user.sendMessage(Utils.error("A problem occurred, please let staff know"));
                    }
                });
            }
        }
    }

    @Override
    public void refresh() {
        //Refresh List of available tutorials
        this.inUseTutorials = Tutorial.getInUseTutorialsWithLocations(Network.getInstance().getTutorialsDBConnection(), LOGGER);

        //Refresh icons
        this.clearGui();
        Inventory inventory = LibraryMenu.getGUI(this.inUseTutorials);
        this.getInventory().setContents(inventory.getContents());

        //Refresh actions
        setActions();
    }
}
