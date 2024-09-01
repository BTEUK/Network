package net.bteuk.network.gui.progressmap;

import me.bteuk.progressmapper.guis.FeatureMenu;
import me.bteuk.progressmapper.guis.Field;
import net.bteuk.network.Network;
import net.bteuk.network.eventing.listeners.progressmap.FeatureGeometryEditorListener;
import net.bteuk.network.eventing.listeners.progressmap.FeaturePropertiesBookListener;
import net.bteuk.network.gui.Gui;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.utils.NetworkUser;
import net.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

//The page for editing a feature
//Should we have two different ones for edit and create (and then a shared parent) or just have both within one?
public class FeaturePageGUI extends Gui
{
    private FeatureMenu featureMenu;
    private Network plugin;
    private FeaturePropertiesBookListener bookListener;
    private LocalFeatureListGUI parentLocalFeatureListGUI;

    public FeaturePageGUI(FeatureMenu featureMenu, Network plugin, LocalFeatureListGUI parentLocalFeatureListGUI)
    {
        super(featureMenu.getGUI());
        this.featureMenu = featureMenu;
        this.plugin = plugin;
        this.parentLocalFeatureListGUI = parentLocalFeatureListGUI;
        setActions();
    }

    public FeatureMenu getFeatureMenu()
    {
        return featureMenu;
    }

    private void setActions()
    {
        //Slots here are 0 indexed

        //Title edit
        setAction(0, u ->
        {
            openFieldEditor(u, featureMenu.getTitleBook(), Field.Title);
        });

        //Description edit
        setAction(2, u ->
        {
            openFieldEditor(u, featureMenu.getDescriptionBook(), Field.Description);
        });

        //Fill/stroke edit
        setAction(4, u ->
        {
            //Delete this gui.
            // this.delete(); - NO. I do not want to do this
            // u.mainGui = null; - NO. u.mainGui here refers to this gui, so we never want to make it null because it makes this gui null

            //Switch to colour picker menu.
            u.mainGui = new ColourPickerGUI(featureMenu.getColourPicker(), plugin, this);
            u.mainGui.open(u);
        });

        //Media_url edit
        setAction(6, u ->
        {
            openFieldEditor(u, featureMenu.getMedialURLBook(), Field.Media_url);
        });

        //Geometry
        setAction(8, u ->
        {
            //Gives the blaze rod
            ItemStack blazeRod = new ItemStack(Material.BLAZE_ROD, 1);
            Utils.giveItem(u.player, blazeRod, "Area selection tool");
            u.player.sendMessage(Component.text("Use the blaze rod like a WorldEdit wand to make a selection. When you have made your selection, reopen the nether star menu and click confirm selection", Style.style(NamedTextColor.AQUA)));

            FeatureGeometryEditorListener geometryListener = new FeatureGeometryEditorListener(plugin, this, featureMenu.getGeometryEditor(), u, blazeRod);
            geometryListener.register();
            u.player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
            u.mainGui = geometryListener;
        });

        //Send update
        setAction(22, u ->
        {
            if (featureMenu.isNew())
                featureMenu.sendAppend(plugin.getConfig().getString("ProgressMap.MapHubAPIKey"));
            else
                featureMenu.sendUpdate(plugin.getConfig().getString("ProgressMap.MapHubAPIKey"));

            u.player.sendMessage(ChatUtils.success("The progress map has been updated !"));

            //Return to the local feature list
            //Delete this gui.
            this.delete();
            u.mainGui = null;

            //Switch to feature list menu.
            parentLocalFeatureListGUI.refresh();
            u.mainGui = parentLocalFeatureListGUI;
            u.mainGui.open(u);
        });

        //Return
        setAction(26, u ->
        {
            //Delete this gui.
            this.delete();
            u.mainGui = null;

            //Switch to feature list menu.
            parentLocalFeatureListGUI.refresh();
            u.mainGui = parentLocalFeatureListGUI;
            u.mainGui.open(u);
        });

    }

    private void openFieldEditor(NetworkUser u, ItemStack book, Field fieldType)
    {
    //    this.delete();
    //    u.mainGui = null;

        //Need a book menu sort of thing, and then when that detects a closure it updates the values in the gson and then refreshes
        //u.player.openBook(book);

        switch (fieldType)
        {
            case Title:
                Utils.giveItem(u.player, book, "Title editor book");
                u.player.sendMessage(ChatUtils.success("Use the title editor book to change the title"));
                break;
            case Description:
                Utils.giveItem(u.player, book, "Description editor book");
                u.player.sendMessage(ChatUtils.success("Use the description editor book to change the description"));
                break;
            case Media_url:
                Utils.giveItem(u.player, book, "Media url editor book");
                u.player.sendMessage(ChatUtils.success("Use the media url editor book to change the media url"));
                break;
        }

        //Closes the current inventory
        u.player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);

        //Sets up the book listener and registers it
        bookListener = new FeaturePropertiesBookListener(plugin, this, fieldType, u);
        bookListener.register();
    }

    @Override
    public void refresh()
    {
        //Refresh icons
        this.clearGui();
        Inventory inventory = featureMenu.getGUI();
        this.getInventory().setContents(inventory.getContents());

        //Refresh actions
        setActions();
    }
}
