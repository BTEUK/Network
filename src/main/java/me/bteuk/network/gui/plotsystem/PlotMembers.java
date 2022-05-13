package me.bteuk.network.gui.plotsystem;

import me.bteuk.network.gui.UniqueGui;
import me.bteuk.network.utils.NetworkUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class PlotMembers {

    public static UniqueGui createPlotMembers(NetworkUser u, int plotID) {

        UniqueGui gui = new UniqueGui(45, Component.text("Plot Menu", NamedTextColor.AQUA, TextDecoration.BOLD));

        return gui;

    }
}
