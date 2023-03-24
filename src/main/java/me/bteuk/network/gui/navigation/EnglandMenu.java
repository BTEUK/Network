package me.bteuk.network.gui.navigation;

import me.bteuk.network.Network;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashSet;

public class EnglandMenu extends Gui {

    public EnglandMenu() {

        super(27, Component.text("England", NamedTextColor.AQUA, TextDecoration.BOLD));

        createGui();

    }

    private void createGui() {

        //Yorkshire: East Riding of Yorkshire, North Yorkshire, South Yorkshire and West Yorkshire
        setItem(1, Utils.createItem(Material.PINK_CONCRETE_POWDER, 1,
                        Utils.title("Yorkshire"),
                        Utils.line("Click to pick from"),
                        Utils.line("locations in the East Riding of Yorkshire,"),
                        Utils.line("North Yorkshire, South Yorkshire"),
                        Utils.line("and West Yorkshire.")),
                u -> openLocation(u, "Yorkshire",
                        Network.getInstance().globalSQL.getStringList("SELECT location FROM location_data WHERE category='ENGLAND' AND subcategory='YORKSHIRE';"))

        );

        //West Midlands: Herefordshire, Shropshire, Staffordshire, Warwickshire, West Midlands and Worcestershire
        setItem(2, Utils.createItem(Material.PURPLE_CONCRETE_POWDER, 1,
                        Utils.title("West Midlands"),
                        Utils.line("Click to pick from"),
                        Utils.line("locations in Herefordshire,"),
                        Utils.line("Shropshire, Staffordshire,"),
                        Utils.line("Warwickshire, the West Midlands"),
                        Utils.line("and Worcestershire.")),
                u -> openLocation(u, "West Midlands",
                        Network.getInstance().globalSQL.getStringList("SELECT location FROM location_data WHERE category='ENGLAND' AND subcategory='WEST_MIDLANDS';"))

        );

        //London: City of London and Greater London
        setItem(4, Utils.createItem(Material.BLUE_CONCRETE_POWDER, 1,
                        Utils.title("London"),
                        Utils.line("Click to pick from"),
                        Utils.line("locations in the City of London"),
                        Utils.line("and Greater London.")),
                u -> openLocation(u, "London",
                        Network.getInstance().globalSQL.getStringList("SELECT location FROM location_data WHERE category='ENGLAND' AND subcategory='LONDON';"))

        );

        //East Midlands: Derbyshire, Leicestershire, Lincolnshire, Northamptonshire, Nottinghamshire and Rutland
        setItem(6, Utils.createItem(Material.CYAN_CONCRETE_POWDER, 1,
                        Utils.title("East Midlands"),
                        Utils.line("Click to pick from"),
                        Utils.line("locations in Derbyshire,"),
                        Utils.line("Leicestershire, Lincolnshire,"),
                        Utils.line("Northamptonshire, Nottinghamshire"),
                        Utils.line("and Rutland.")),
                u -> openLocation(u, "East Midlands",
                        Network.getInstance().globalSQL.getStringList("SELECT location FROM location_data WHERE category='ENGLAND' AND subcategory='EAST_MIDLANDS';"))

        );

        //East of England: Bedfordshire, Cambridgeshire, Essex, Hertfordshire, Norfolk and Suffolk
        setItem(7, Utils.createItem(Material.YELLOW_CONCRETE_POWDER, 1,
                        Utils.title("East of England"),
                        Utils.line("Click to pick from"),
                        Utils.line("locations in Bedfordshire,"),
                        Utils.line("Cambridgeshire, Essex,"),
                        Utils.line("Hertfordshire, Norfork and Suffolk.")),
                u -> openLocation(u, "East of England",
                        Network.getInstance().globalSQL.getStringList("SELECT location FROM location_data WHERE category='ENGLAND' AND subcategory='EAST_OF_ENGLAND';"))

        );

        //North West: Cheshire, Cumbria, Greater Manchester, Merseyside and Lancashire
        setItem(11, Utils.createItem(Material.RED_CONCRETE_POWDER, 1,
                        Utils.title("North West"),
                        Utils.line("Click to pick from"),
                        Utils.line("locations in Cheshire,"),
                        Utils.line("Cumbria, Greater Manchester,"),
                        Utils.line("Merseyside and Lancashire.")),
                u -> openLocation(u, "North West",
                        Network.getInstance().globalSQL.getStringList("SELECT location FROM location_data WHERE category='ENGLAND' AND subcategory='NORTH_WEST';"))

        );

        //North East: Durham, Northumberland and Tyne and Wear
        setItem(12, Utils.createItem(Material.ORANGE_CONCRETE_POWDER, 1,
                        Utils.title("North East"),
                        Utils.line("Click to pick from"),
                        Utils.line("locations in Durham,"),
                        Utils.line("Northumberland and Tyne and Wear.")),
                u -> openLocation(u, "North East",
                        Network.getInstance().globalSQL.getStringList("SELECT location FROM location_data WHERE category='ENGLAND' AND subcategory='NORTH_EAST';"))

        );

        //South West: Bristol, Cornwall, Devon, Dorset, Gloucestershire, Somerset and Wiltshire
        setItem(14, Utils.createItem(Material.LIGHT_BLUE_CONCRETE_POWDER, 1,
                        Utils.title("South West"),
                        Utils.line("Click to pick from"),
                        Utils.line("locations in Bristol, Cornwall,"),
                        Utils.line("Devon, Dorset, Gloucestershire,"),
                        Utils.line("Somerset and Wiltshire.")),
                u -> openLocation(u, "South West",
                        Network.getInstance().globalSQL.getStringList("SELECT location FROM location_data WHERE category='ENGLAND' AND subcategory='SOUTH_WEST';"))

        );

        //South East: Berkshire, Buckinghamshire, East Sussex, Hampshire, the Isle of Wight, Kent, Oxfordshire, Surrey and West Sussex
        setItem(15, Utils.createItem(Material.LIME_CONCRETE_POWDER, 1,
                        Utils.title("South East"),
                        Utils.line("Click to pick from"),
                        Utils.line("locations in Berkshire,"),
                        Utils.line("Buckinghamshire, East Sussex,"),
                        Utils.line("Hampshire, the Isle of Wight,"),
                        Utils.line("Kent, Oxfordshire, Surrey"),
                        Utils.line("and West Sussex.")),
                u -> openLocation(u, "South East",
                        Network.getInstance().globalSQL.getStringList("SELECT location FROM location_data WHERE category='ENGLAND' AND subcategory='SOUTH_EAST';"))

        );

        //Return
        setItem(26, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.title("Return"),
                        Utils.line("Open the exploration menu.")),
                u ->

                {

                    //Delete this gui.
                    this.delete();

                    //Switch to exploration menu.
                    u.mainGui = new ExploreGui(u);
                    u.mainGui.open(u);

                });
    }

    public void refresh() {

        this.clearGui();
        createGui();

    }

    private void openLocation(NetworkUser u, String name, ArrayList<String> locations) {

        if (locations.isEmpty()) {
            u.player.sendMessage(Utils.error("No locations added to the menu in &4" + name + "&c."));
            return;
        }

        //Switch to location menu with all scotland locations.
        this.delete();
        u.mainGui = new LocationMenu(name, new HashSet<>(locations), true, false, u);
        u.mainGui.open(u);

    }
}
