package net.bteuk.network.utils.plotsystem;

import lombok.Getter;

@Getter
public enum ReviewCategory {

    OUTLINES("Outlines"),
    FEATURES("Features"),
    ROOF("Roof"),
    GARDEN("Garden"),
    TEXTURES("Textures"),
    DETAILS("Details"),
    GENERAL("General");

    private final String displayName;

    ReviewCategory(String displayName) {
        this.displayName = displayName;
    }
}
