package me.bteuk.network.utils.enums;

public enum Regions {
    LONDON("London"),
    NORTH_EAST("North Earth"),
    NORTH_WEST("North West"),
    YORKSHIRE("Yorkshire"),
    EAST_MIDLANDS("East Midlands"),
    WEST_MIDLANDS("West Midlands"),
    SOUTH_EAST("South East"),
    EAST_OF_ENGLAND("East of England"),
    SOUTH_WEST("South West");

    public final String label;

    Regions(String label) {
        this.label = label;
    }
}
