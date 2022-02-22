package me.bteuk.network.gui;

import me.bteuk.network.utils.User;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

public class BuildGui {

    //Create a build gui for user u.
    public static UniqueGui createBuildGui(User user) {

        UniqueGui gui = new UniqueGui(27, Component.text("Build Menu", NamedTextColor.AQUA, TextDecoration.BOLD));

        return gui;

    }
}
