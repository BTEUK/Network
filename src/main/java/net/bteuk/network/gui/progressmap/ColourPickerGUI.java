package net.bteuk.network.gui.progressmap;

import me.bteuk.progressmapper.guis.ColourPicker;
import net.bteuk.network.Network;
import net.bteuk.network.gui.Gui;
import org.bukkit.inventory.Inventory;

public class ColourPickerGUI extends Gui
{
    private ColourPicker colourPicker;
    private Network plugin;
    private FeaturePageGUI parentFeatureMenu;

    public ColourPickerGUI(ColourPicker colourPicker, Network plugin, FeaturePageGUI parentFeatureMenu)
    {
        super(colourPicker.getGUI());
        this.colourPicker = colourPicker;
        this.plugin = plugin;
        this.parentFeatureMenu = parentFeatureMenu;

        setActions();
    }

    private void setActions()
    {
        //Slots here are 0 indexed

        //----------------------------------------------------
        //----------- Top Row - Predefined colours -----------
        //----------------------------------------------------

        //Default 1
        setAction(0, u ->
        {
            colourPicker.updateColour(ColourPicker.Dark_Red);
            refresh();
        });

        //Default 2
        setAction(1, u ->
        {
            colourPicker.updateColour(ColourPicker.Red);
            refresh();
        });

        //Default 3
        setAction(2, u ->
        {
            colourPicker.updateColour(ColourPicker.Dark_Orange);
            refresh();
        });

        //Default 4
        setAction(3, u ->
        {
            colourPicker.updateColour(ColourPicker.Orange);
            refresh();
        });

        //Default 5
        setAction(4, u ->
        {
            colourPicker.updateColour(ColourPicker.Light_Orange);
            refresh();
        });

        //Default 6
        setAction(5, u ->
        {
            colourPicker.updateColour(ColourPicker.Yellow);
            refresh();
        });

        //Default 7
        setAction(6, u ->
        {
            colourPicker.updateColour(ColourPicker.Bright_Yellow);
            refresh();
        });

        //Default 8
        setAction(7, u ->
        {
            colourPicker.updateColour(ColourPicker.Sick_Green);
            refresh();
        });

        //Default 9
        setAction(8, u ->
        {
            colourPicker.updateColour(ColourPicker.Complete_Green);
            refresh();
        });

        //-----------------------------------------------------
        //---------------- Line 2 - Red Editor ----------------
        //-----------------------------------------------------

        //Lower red 16
        setAction(11, u ->
        {
            colourPicker.lowerRed16();
            refresh();
        });

        //Lower red 1
        setAction(12, u ->
        {
            colourPicker.lowerRed1();
            refresh();
        });

        //Raise red 1
        setAction(14, u ->
        {
            colourPicker.raiseRed1();
            refresh();
        });

        //Raise red 16
        setAction(15, u ->
        {
            colourPicker.raiseRed16();
            refresh();
        });

        //-----------------------------------------------------
        //--------------- Line 3 - Green Editor ---------------
        //-----------------------------------------------------

        //Lower green 16
        setAction(20, u ->
        {
            colourPicker.lowerGreen16();
            refresh();
        });

        //Lower green 1
        setAction(21, u ->
        {
            colourPicker.lowerGreen1();
            refresh();
        });

        //Raise green 1
        setAction(23, u ->
        {
            colourPicker.raiseGreen1();
            refresh();
        });

        //Raise green 16
        setAction(24, u ->
        {
            colourPicker.raiseGreen16();
            refresh();
        });

        //----------------------------------------------------
        //--------------- Line 4 - Blue Editor ---------------
        //----------------------------------------------------

        //Lower blue 16
        setAction(29, u ->
        {
            colourPicker.lowerBlue16();
            refresh();
        });

        //Lower blue 1
        setAction(30, u ->
        {
            colourPicker.lowerBlue1();
            refresh();
        });

        //Raise blue 1
        setAction(32, u ->
        {
            colourPicker.raiseBlue1();
            refresh();
        });

        //Raise blue 16
        setAction(33, u ->
        {
            colourPicker.raiseBlue16();
            refresh();
        });

        //-----------------------------------------------------
        //-------------- Line 5 - Colour display --------------
        //-----------------------------------------------------

        //Return/confirm
        setAction(40, u ->
        {
            //Saves the selected colour to the feature
            colourPicker.confirmColour();

            //Delete this gui.
            this.delete();
            u.mainGui = null;

            //Switch to feature list menu.
            parentFeatureMenu.refresh();
            u.mainGui = parentFeatureMenu;
            u.mainGui.open(u);
        });

    }

    @Override
    public void refresh()
    {
        //Refresh icons
        this.clearGui(); //Clears actions as well
        Inventory inventory = colourPicker.getGUI();
        this.getInventory().setContents(inventory.getContents());

        //Refresh actions
        setActions();
    }
}
