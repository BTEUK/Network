package me.bteuk.network.gui.progressmap;

import me.bteuk.network.Network;
import me.bteuk.network.gui.BuildGui;
import me.bteuk.network.gui.Gui;
import me.bteuk.progressmapper.guis.LocalFeaturesMenu;
import org.bukkit.inventory.Inventory;

public class LocalFeatureListGUI extends Gui
{
    //Contains all of the relevant information for each feature in the list
    LocalFeaturesMenu features;
    Network plugin;

    public LocalFeatureListGUI(LocalFeaturesMenu features, Network plugin)
    {
        //Need a list of things created really. I think this has to be before this one is created. This then holds all of the Features
        //Each feature would have a feature menu (not a feature page)
        super(features.getGUI());
        this.features = features;
        this.plugin = plugin;
        setActions();
    }

    private void setActions()
    {
        int i, iFeatures;
        iFeatures = features.getNumFeatures();

        //Creates all of the actions
        for (i = 0 ; i < iFeatures ; i++)
        {
            final int iFinalSlot = i;
            setAction(i, u ->
            {
                //When a feature is clicked on it needs to open a FeaturePageGUI
                u.mainGui = new FeaturePageGUI(features.getFeatureMenu(iFinalSlot), plugin, this);
                u.mainGui.open(u);
            });
        }

        //Back button
        setAction(getInventory().getSize() - 1, u ->
        {
            //Delete this gui.
            this.delete();
            u.mainGui = null;

            //Switch to plot info.
            u.mainGui = new BuildGui(u);
            u.mainGui.open(u);
        });
    }
    @Override
    public void refresh()
    {
        //Reloads the features (with a blank one at the end)
        features.loadFeatures(plugin.getConfig().getString("ProgressMap.MapHubAPIKey"));

        //Refresh icons
        this.clearGui();
        Inventory inventory = features.getGUI();
        this.getInventory().setContents(inventory.getContents());

        //Refresh actions
        setActions();
    }
}
