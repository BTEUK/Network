package net.bteuk.network.utils.plotsystem;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
public enum ReviewSelection {
    GOOD(Component.text("[Good]", NamedTextColor.GREEN), Component.text("[G]", NamedTextColor.GREEN)),
    OK(Component.text("[Ok]", NamedTextColor.YELLOW), Component.text("[O]", NamedTextColor.YELLOW)),
    POOR(Component.text("[Poor]", NamedTextColor.RED), Component.text("[P]", NamedTextColor.RED)),
    NONE(Component.empty(), Component.empty());

    private final Component displayComponent;

    private final Component abbreviatedComponent;

    ReviewSelection(Component displayComponent, Component abbreviatedComponent) {
        this.displayComponent = displayComponent;
        this.abbreviatedComponent = abbreviatedComponent;
    }
}
