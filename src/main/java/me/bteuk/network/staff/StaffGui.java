package me.bteuk.network.staff;

import me.bteuk.network.gui.UniqueGui;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

public class StaffGui {

    public static UniqueGui createBuildGui(NetworkUser user) {

        UniqueGui gui = new UniqueGui(27, Component.text("Building Menu", NamedTextColor.AQUA, TextDecoration.BOLD));

        //Teleport to random unclaimed plot.
        gui.setItem(12, Utils.createItem(Material.ENDER_PEARL, 1,
                        Utils.chat("&b&lRandom Plot"),
                        Utils.chat("&fClick teleport to a random claimable plot.")),
                u -> {

                });


        return gui;

    }
}
