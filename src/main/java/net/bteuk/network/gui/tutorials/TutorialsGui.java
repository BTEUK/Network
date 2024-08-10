package net.bteuk.network.gui.tutorials;

import net.bteuk.network.Network;
import net.bteuk.network.gui.Gui;
import net.bteuk.network.utils.NetworkUser;
import net.bteuk.network.utils.SwitchServer;
import net.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import teachingtutorials.guis.Event;
import teachingtutorials.guis.EventType;
import teachingtutorials.tutorials.Lesson;
import teachingtutorials.tutorials.Tutorial;
import teachingtutorials.utils.User;

import static net.bteuk.network.utils.NetworkConfig.CONFIG;

public class TutorialsGui extends Gui {

    private final NetworkUser user;

    public TutorialsGui(NetworkUser user) {

        super(27, Component.text("Tutorials Menu", NamedTextColor.AQUA, TextDecoration.BOLD));

        this.user = user;

        createGui();

    }

    /**
     * Creates the icons and actions for this menu
     */
    private void createGui() {

        //Retrieves the critical information for the menu
        boolean bCompulsoryTutorialEnabled = CONFIG.getBoolean("tutorials.compulsory_tutorial");

        //Gets the information about the Tutorials User
        User tutorialsUser = new User(this.user.player);
        tutorialsUser.fetchDetailsByUUID(Network.getInstance().getTutorialsDBConnection());
        tutorialsUser.calculateRatings(Network.getInstance().getTutorialsDBConnection());

        //Initiates the current tutorial object
        Tutorial currentTutorial = new Tutorial();

        //Get compulsory tutorial ID
        int iCompulsoryTutorialID;
        Tutorial[] compulsoryTutorials = Tutorial.fetchAll(true, true, Network.getInstance().getTutorialsDBConnection());
        if (compulsoryTutorials.length == 0)
            iCompulsoryTutorialID = -1;
        else
            iCompulsoryTutorialID = compulsoryTutorials[0].getTutorialID();


        //Designs the 'Continue' menu button
        ItemStack continueLearning_CompulsoryComplete;
        if (tutorialsUser.bInLesson)
        {
            //Get current lesson's tutorial ID and sets up the tutorial object from this
            int iTutorialIDCurrentLesson = Lesson.getTutorialOfCurrentLessonOfPlayer(user.player.getUniqueId(), Network.getInstance().getTutorialsDBConnection());
            if (iTutorialIDCurrentLesson == -1)
            {
                Bukkit.getConsoleSender().sendMessage(Utils.error("An error occurred. Player is in lesson but has no lesson in the database"));
            }
            currentTutorial.setTutorialID(iTutorialIDCurrentLesson);
            currentTutorial.fetchByTutorialID(Network.getInstance().getTutorialsDBConnection());

            //Sets up the menu icon with the name of the current tutorial
            continueLearning_CompulsoryComplete = Utils.createItem(Material.WRITABLE_BOOK, 1,
                    Utils.title("Resume Your Lesson"),
                    Utils.line(currentTutorial.szTutorialName));
        }
        else
        {
            //Sets up the menu icon with the new tutorial's name

            //Decides the next tutorial
            Tutorial nextTutorial = Lesson.decideTutorial(tutorialsUser, Network.getInstance().getTutorialsDBConnection());
            continueLearning_CompulsoryComplete = Utils.createItem(Material.WRITABLE_BOOK, 1,
                    Utils.title("Start a new Tutorial:"),
                    Utils.line(nextTutorial.szTutorialName));
        }

        //Designs the tutorials library menu option
        ItemStack tutorialLibrary = Utils.createItem(Material.BOOKSHELF, 1,
                Utils.title("Tutorial Library"),
                Utils.line("Browse all of our available tutorials"));

        //------------------------------------------------------------
        //-------- Begin arranging the menu icons and actions --------
        //------------------------------------------------------------

        //Add the compulsory tutorial button if enabled.
        if (bCompulsoryTutorialEnabled)
        {
            //If the player has already completed the compulsory tutorial.
            if (tutorialsUser.bHasCompletedCompulsory) {
                //------ Compulsory Tutorial Option ------
                ItemStack compulsory;

                //User is currently redoing the compulsory tutorial
                if (tutorialsUser.bInLesson && (currentTutorial.getTutorialID() == iCompulsoryTutorialID))
                {
                    compulsory = Utils.createItem(Material.BOOK, 1,
                            Utils.title("Restart the Starter Tutorial"),
                            Utils.line("Restart the starter tutorial again"));

                    super.setItem(11 - 1, compulsory, new guiAction() {
                        @Override
                        public void click(NetworkUser user) {
                            clickCompulsory();
                            //Deletes this gui
                            delete();
                        }
                    });
                }

                //User is currently in a different tutorial
                else if (tutorialsUser.bInLesson)
                {
                    compulsory = Utils.createItem(Material.ENCHANTED_BOOK, 1,
                            Utils.title("Restart the Starter Tutorial"),
                            Utils.line("Finish your current lesson first!"));
                    super.setItem(11 - 1, compulsory, new guiAction() {
                        @Override
                        public void click(NetworkUser user)
                        {
                            //Do nothing
                        }
                    });

                }
                //User is not in any tutorial
                else
                {
                    compulsory = Utils.createItem(Material.BOOK, 1,
                            Utils.title("Restart the Starter Tutorial"),
                            Utils.line("Refresh your essential knowledge"));

                    super.setItem(11 - 1, compulsory, new guiAction() {
                        @Override
                        public void click(NetworkUser user) {
                            clickCompulsory();
                            //Deletes this gui
                            delete();
                        }
                    });
                }

                //---------- Library Option ----------
                super.setItem(14 - 1, tutorialLibrary, new guiAction() {
                    @Override
                    public void click(NetworkUser user) {
                        clickLibrary();
                        //Deletes this gui
                        delete();
                    }
                });

                //----- Continue Learning Option -----
                super.setItem(17 - 1, continueLearning_CompulsoryComplete, new guiAction() {
                    @Override
                    public void click(NetworkUser user) {
                        clickContinue();
                        //Deletes this gui
                        delete();
                    }
                });


            } else {

                ItemStack resumeCompulsory;
                ItemStack restartCompulsory;
                ItemStack beginCompulsory;

                //They are currently in the compulsory tutorial for the first time
                //OR: The user was in another lesson and someone added a compulsory tutorial to the system
                if (tutorialsUser.bInLesson)
                {
                    //Player is in a lesson other than the compulsory but hasn't started the compulsory (compulsory added to system)
                    if (currentTutorial.getTutorialID() != iCompulsoryTutorialID)
                    {
                        ItemStack erroneousTutorial = Utils.createItem(Material.BOOK, 1,
                                Utils.title("Continue Your Tutorial"),
                                Utils.line("You must then complete the starter tutorial"));

                        super.setItem(14 - 1, erroneousTutorial, new guiAction() {

                            @Override
                            public void click(NetworkUser user) {
                                clickContinue();
                                //Deletes this gui
                                delete();
                            }
                        });
                    }

                    //Player is half way through the compulsory tutorial but hasn't ever finished it
                    else
                    {
                        restartCompulsory = Utils.createItem(Material.BOOK, 1,
                                Utils.title("Restart the Starter Tutorial"),
                                Utils.line("Gain the applicant rank"));

                        resumeCompulsory = Utils.createItem(Material.WRITABLE_BOOK, 1,
                                Utils.title("Resume the Starter Tutorial"),
                                Utils.line("Gain the applicant rank"));

                        super.setItem(12 - 1, restartCompulsory, new guiAction() {

                            @Override
                            public void click(NetworkUser user) {
                                clickCompulsory();
                                //Deletes this gui
                                delete();
                            }
                        });

                        super.setItem(16 - 1, resumeCompulsory, new guiAction() {

                            @Override
                            public void click(NetworkUser user) {
                                clickContinue();
                                //Deletes this gui
                                delete();
                            }
                        });
                    }
                }
                else // They have never started the compulsory tutorial
                {
                    beginCompulsory = Utils.createItem(Material.BOOK, 1,
                            Utils.title("Begin the Starter Tutorial"),
                            Utils.line("Gain the applicant rank"));

                    super.setItem(14 - 1, beginCompulsory, new guiAction() {
                        @Override
                        public void click(NetworkUser user) {
                            clickCompulsory();
                            //Deletes this gui
                            delete();
                        }
                    });
                }

            }
        }

        //There is no compulsory tutorial
        else {

            //Continue learning, compulsory complete
            super.setItem(16 - 1, continueLearning_CompulsoryComplete, new guiAction() {
                @Override
                public void click(NetworkUser user)
                {
                    clickContinue();
                    //Deletes this gui
                    delete();
                }
            });

            //Library
            super.setItem(12 - 1, tutorialLibrary, new guiAction() {

                @Override
                public void click(NetworkUser user) {
                    clickLibrary();
                    //Deletes this gui
                    delete();
                }
            });

        }

        //Return
        setItem(26, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.title("Return"),
                        Utils.line("Open the navigator main menu.")),
                user ->

                {

                    //Delete this gui.
                    this.delete();
                    user.mainGui = null;

                    //Switch to navigation menu.
                    Network.getInstance().navigatorGui.open(user);

                });
    }

    public void refresh() {

        this.clearGui();
        createGui();

    }

    private void clickContinue() {
        //Switch to the tutorial.
        if (Event.addEvent(EventType.CONTINUE, user.player.getUniqueId(), 0, Network.getInstance().getTutorialsDBConnection()))
            switchServer(user);
        else
           user.sendMessage(Utils.error("A problem occurred, please let staff know"));
    }

    private void clickLibrary() {
        //Delete this gui.
        this.delete();

        //Switch to tutorial library menu.
        user.mainGui = new TutorialLibraryGui(user, Tutorial.getInUseTutorialsWithLocations(Network.getInstance().getTutorialsDBConnection()));
        user.mainGui.open(user);
    }

    private void clickCompulsory() {
        //Switch to the tutorial.
        if (Event.addEvent(EventType.COMPULSORY, user.player.getUniqueId(), 0, Network.getInstance().getTutorialsDBConnection()))
            switchServer(user);
        else
            user.sendMessage(Utils.error("A problem occurred, please let staff know"));
    }

    protected static void switchServer(NetworkUser user) {
        SwitchServer.switchServer(user.player, Network.getInstance().getGlobalSQL().getString("SELECT name FROM server_data WHERE type='TUTORIAL';"));
        user.player.closeInventory();
    }
}
