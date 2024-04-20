package net.bteuk.network.utils.enums;

public enum RegionStatus {
    DEFAULT ("Default"),
    PUBLIC ("Public"),
    LOCKED ("Locked"),
    OPEN ("Open"),
    BLOCKED ("Blocked"),
    PLOT ("Plots"),
    INACTIVE ("Inactive");

    public final String label;

    RegionStatus(String label) {
        this.label = label;
    }
}
