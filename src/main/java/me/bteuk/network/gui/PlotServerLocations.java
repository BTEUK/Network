package me.bteuk.network.gui;

import me.bteuk.network.utils.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class PlotServerLocations {

    public static UniqueGui getPlotServerLocations(User u) {

        UniqueGui gui = new UniqueGui(27, Component.text("Plot Locations", NamedTextColor.AQUA, TextDecoration.BOLD));

        return gui;

    }
}