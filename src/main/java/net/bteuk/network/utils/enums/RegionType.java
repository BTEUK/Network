package net.bteuk.network.utils.enums;

public enum RegionType {
    REGION("Region"),
    PLOT("Plot"),
    ZONE("Zone");

    public final String label;

    RegionType(String label) {
        this.label = label;
    }
}
