package me.bteuk.network.gui.navigation;

import me.bteuk.network.gui.Gui;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.enums.Categories;
import me.bteuk.network.utils.enums.Counties;
import me.bteuk.network.utils.navigation.LocationNameListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

public class AddLocation extends Gui {

    private String name = null;
    private Categories category = null;
    private Counties county = null;

    private final NetworkUser u;

    private LocationNameListener locationNameListener;

    public AddLocation(NetworkUser u) {

        super(27, Component.text("Add Location", NamedTextColor.AQUA, TextDecoration.BOLD));

        this.u = u;

    }

    public void setName(String name) {
        this.name = name;
    }

    private void createGui() {

        //Set/edit name.
        if (name != null) {
            setItem(11, Utils.createItem(Material.SPRUCE_SIGN, 1,
                            Utils.chat("&b&lUpdate Location Name"),
                            Utils.chat("&fEdit the location name."),
                            Utils.chat("&fThe current name is '" + name + "'."),
                            Utils.chat("&fYou can type the name in chat.")),

                    u -> {

                        if (locationNameListener != null) {
                            locationNameListener.unregister();
                        }

                        locationNameListener = new LocationNameListener(u.player, this);
                        u.player.sendMessage(Utils.chat("&aWrite the location name in chat, the first message counts. You can include spaces in the name."));
                        u.player.closeInventory();

                    });
        } else {
            setItem(11, Utils.createItem(Material.SPRUCE_SIGN, 1,
                            Utils.chat("&b&lSet Location Name"),
                            Utils.chat("&fAdd the location name."),
                            Utils.chat("&fYou can type the name in chat.")),

                    u -> {

                        if (locationNameListener != null) {
                            locationNameListener.unregister();
                        }

                        locationNameListener = new LocationNameListener(u.player, this);
                        u.player.sendMessage(Utils.chat("&aWrite the location name in chat, the first message counts. You can include spaces in the name."));
                        u.player.closeInventory();

                    });
        }


        //Select category.


        //If category is England, select county.


        //Return
        setItem(26, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.chat("&b&lReturn"),
                        Utils.chat("&fOpen the explore menu.")),
                u ->

                {

                    //Delete this gui.
                    this.delete();
                    u.addLocation = null;

                    //Switch to navigation menu.
                    u.exploreGui = new ExploreGui(u);
                    u.exploreGui.open(u);

                });
    }

    public void refresh() {

        this.clearGui();
        createGui();

    }
}
