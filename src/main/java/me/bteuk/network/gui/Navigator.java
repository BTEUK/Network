package me.bteuk.network.gui;

import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

public class Navigator extends Gui {

    public Navigator() {

        super(27, Component.text("Navigator", NamedTextColor.AQUA, TextDecoration.BOLD));

        setItem(12, Utils.createItem(Material.DIAMOND_PICKAXE, 1,
                        Utils.chat("&b&lBuild"),
                        Utils.chat("&fClick to open the build menu.")),
                u -> {

                    //Open the build gui.
                    u.uniqueGui = BuildGui.createBuildGui(u);
                    u.uniqueGui.open(u);

                });

        setItem(14, Utils.createItem(Material.SPRUCE_BOAT, 1,
                        Utils.chat("&b&lVisit"),
                        Utils.chat("&fClick to open the visit menu.")),
                u -> {

                    //Click Action

                });

    }
}
