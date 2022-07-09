package me.bteuk.network.gui.regions;

import me.bteuk.network.gui.Gui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class RegionInfo extends Gui {

    private final String region;
    private final String uuid;

    public RegionInfo(String region, String uuid) {

        super(27, Component.text("Region " + region, NamedTextColor.AQUA, TextDecoration.BOLD));

        this.region = region;
        this.uuid = uuid;

        createGui();

    }

    private void createGui() {






    }

    public void refresh() {

        this.clearGui();
        createGui();

    }
}
