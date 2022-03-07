package me.bteuk.network.gui;

import me.bteuk.network.utils.NetworkUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class PlotMenu {

    public static UniqueGui getPlotMenu(NetworkUser u) {

        UniqueGui gui = new UniqueGui(27, Component.text("Plot Locations", NamedTextColor.AQUA, TextDecoration.BOLD));

        return gui;

    }
}
