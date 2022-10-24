package me.bteuk.network.utils.enums;

public enum Categories {
    ENGLAND("England"),
    SCOTLAND("Scotland"),
    WALES("Wales"),
    NORTHERN_IRELAND("Northern Ireland"),
    OTHER("Other");

    public final String label;

    Categories(String label) {
        this.label = label;
    }
}
