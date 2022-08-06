package me.bteuk.network.gui.navigation;

import me.bteuk.network.Network;
import me.bteuk.network.gui.BuildGui;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

public class ExploreGui extends Gui {

    private final NetworkUser u;

    public ExploreGui(NetworkUser u) {

        super(27, Component.text("Exploration Menu", NamedTextColor.AQUA, TextDecoration.BOLD));

        this.u = u;

        createGui();

    }

    private void createGui() {

        //If the player is Jr.Builder+ show the 'add location' button.
        if (u.player.hasPermission("group.jrbuilder")) {

            setItem(8, Utils.createItem(Material.GREEN_CONCRETE, 1,
                            Utils.chat("&b&lAdd Location"),
                            Utils.chat("&fRequest a new location to add"),
                            Utils.chat("&fto the exploration menu.")),
                    u -> {

                        this.delete();
                        u.exploreGui = null;

                        //Switch to the location add menu.
                        //u.addLocation = new AddLocation(u);
                        //u.addLocation.open(u);

                    });


        }

        /*
        Create a button for each main category, if the category only has 1 location then show the location directly.

        The main categories are:

        - England
        - Scotland
        - Wales
        - Northern Ireland
        - Overseas Territories & Crown Dependencies

        - Suggested Locations
        - Nearby Locations

        England will also have sub-categories due to it being by far the largest category.

        The sub categories are the regions of England:

        - London: Greater London an City of London
        - North East: Northumberland, Tyne and Wear and Durham
        - North West: Cumbria, Lancashire, Merseyside, Cheshire and Greater Manchester
        - Yorkshire: North Yorkshire, South Yorkshire, West Yorkshire and the East Riding of Yorkshire
        - East Midlands: Derbyshire, Nottinghamshire, Leicestershire, Northamptonshire, Rutland and Lincolnshire
        - West Midlands: West Midlands, Warwickshire, Staffordshire, Shropshire, Worcestershire and Herefordshire
        - South East: West Sussex, East Sussex, Hampshire, Surrey, Kent, Oxfordshire, Berkshire, Buckinghamshire and the Isle of Wight
        - East of England: Essex, Cambridgeshire, Norfolk, Suffolk, Bedfordshire, and Hertfordshire
        - South West: Bristol, Devon, Cornwall, Dorset, Somerset, Wiltshire and Gloucestershire

        Locations and categories are sorted based on number of available locations.
        Suggested and nearby locations are separate and will always be displayed at the end of the list.

         */


        //Return
        setItem(26, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.chat("&b&lReturn"),
                        Utils.chat("&fOpen the navigator main menu.")),
                u ->

                {

                    //Delete this gui.
                    this.delete();
                    u.exploreGui = null;

                    //Switch to navigation menu.
                    Network.getInstance().navigatorGui.open(u);

                });
    }

    public void refresh() {

        this.clearGui();
        createGui();

    }
}