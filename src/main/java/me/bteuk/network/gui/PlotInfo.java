package me.bteuk.network.gui;

import me.bteuk.network.Network;
import me.bteuk.network.sql.PlotSQL;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

import java.util.ArrayList;

public class PlotInfo {

    public static UniqueGui createPlotInfo(NetworkUser user, int plotID) {

        UniqueGui gui = new UniqueGui(45, Component.text("Plot Menu", NamedTextColor.AQUA, TextDecoration.BOLD));

        return gui;

    }
}
