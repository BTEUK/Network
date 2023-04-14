package me.bteuk.network.gui.navigation;

import me.bteuk.network.gui.Gui;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

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
                u -> openLocation("Yorkshire", u, "Yorkshire"));

        //West Midlands: Herefordshire, Shropshire, Staffordshire, Warwickshire, West Midlands and Worcestershire
        setItem(2, Utils.createItem(Material.PURPLE_CONCRETE_POWDER, 1,
                        Utils.title("West Midlands"),
                        Utils.line("Click to pick from"),
                        Utils.line("locations in Herefordshire,"),
                        Utils.line("Shropshire, Staffordshire,"),
                        Utils.line("Warwickshire, the West Midlands"),
                        Utils.line("and Worcestershire.")),
                u -> openLocation("West Midlands", u, "West Midlands"));

        //London: City of London and Greater London
        setItem(4, Utils.createItem(Material.BLUE_CONCRETE_POWDER, 1,
                        Utils.title("London"),
                        Utils.line("Click to pick from"),
                        Utils.line("locations in the City of London"),
                        Utils.line("and Greater London.")),
                u -> openLocation("London", u, "London"));

        //East Midlands: Derbyshire, Leicestershire, Lincolnshire, Northamptonshire, Nottinghamshire and Rutland
        setItem(6, Utils.createItem(Material.CYAN_CONCRETE_POWDER, 1,
                        Utils.title("East Midlands"),
                        Utils.line("Click to pick from"),
                        Utils.line("locations in Derbyshire,"),
                        Utils.line("Leicestershire, Lincolnshire,"),
                        Utils.line("Northamptonshire, Nottinghamshire"),
                        Utils.line("and Rutland.")),
                u -> openLocation("East Midlands", u, "East Midlands"));

        //East of England: Bedfordshire, Cambridgeshire, Essex, Hertfordshire, Norfolk and Suffolk
        setItem(7, Utils.createItem(Material.YELLOW_CONCRETE_POWDER, 1,
                        Utils.title("East of England"),
                        Utils.line("Click to pick from"),
                        Utils.line("locations in Bedfordshire,"),
                        Utils.line("Cambridgeshire, Essex,"),
                        Utils.line("Hertfordshire, Norfork and Suffolk.")),
                u -> openLocation("East of England", u, "East of England"));

        //North West: Cheshire, Cumbria, Greater Manchester, Merseyside and Lancashire
        setItem(11, Utils.createItem(Material.RED_CONCRETE_POWDER, 1,
                        Utils.title("North West"),
                        Utils.line("Click to pick from"),
                        Utils.line("locations in Cheshire,"),
                        Utils.line("Cumbria, Greater Manchester,"),
                        Utils.line("Merseyside and Lancashire.")),
                u -> openLocation("North West", u, "North West"));

        //North East: Durham, Northumberland and Tyne and Wear
        setItem(12, Utils.createItem(Material.ORANGE_CONCRETE_POWDER, 1,
                        Utils.title("North East"),
                        Utils.line("Click to pick from"),
                        Utils.line("locations in Durham,"),
                        Utils.line("Northumberland and Tyne and Wear.")),
                u -> openLocation("North East", u, "North East"));

        //South West: Bristol, Cornwall, Devon, Dorset, Gloucestershire, Somerset and Wiltshire
        setItem(14, Utils.createItem(Material.LIGHT_BLUE_CONCRETE_POWDER, 1,
                        Utils.title("South West"),
                        Utils.line("Click to pick from"),
                        Utils.line("locations in Bristol, Cornwall,"),
                        Utils.line("Devon, Dorset, Gloucestershire,"),
                        Utils.line("Somerset and Wiltshire.")),
                u -> openLocation("South West", u, "South West"));

        //South East: Berkshire, Buckinghamshire, East Sussex, Hampshire, the Isle of Wight, Kent, Oxfordshire, Surrey and West Sussex
        setItem(15, Utils.createItem(Material.LIME_CONCRETE_POWDER, 1,
                        Utils.title("South East"),
                        Utils.line("Click to pick from"),
                        Utils.line("locations in Berkshire,"),
                        Utils.line("Buckinghamshire, East Sussex,"),
                        Utils.line("Hampshire, the Isle of Wight,"),
                        Utils.line("Kent, Oxfordshire, Surrey"),
                        Utils.line("and West Sussex.")),
                u -> openLocation("South East", u, "South East"));

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

    private void openLocation(String name, NetworkUser u, String type) {

        LocationMenu gui = new LocationMenu(name, u, type, "England");

        if (gui.isEmpty()) {

            gui.delete();
            u.player.sendMessage(Utils.error("No locations added to the menu in ")
                    .append(Component.text(name, NamedTextColor.DARK_RED)));

        } else {

            //Switch to location menu with all scotland locations.
            this.delete();
            u.mainGui = new LocationMenu(name, u, type, "England");
            u.mainGui.open(u);

        }
    }
}
