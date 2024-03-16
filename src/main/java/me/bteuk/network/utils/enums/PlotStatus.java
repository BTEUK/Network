package me.bteuk.network.utils.enums;

public enum PlotStatus {
    UNCLAIMED("unclaimed"),
    CLAIMED("claimed"),
    SUBMITTED("submitted"),
    REVIEWING("reviewing"),
    COMPLETED("completed"),
    DELETED("deleted");

    public final String database_value;

    PlotStatus(String database_value) {
        this.database_value = database_value;
    }

    /**
     * Get the {@link PlotStatus} from the database value.
     * @param value the database value
     * @return the PlotStatus, or null if none match
     */
    public static PlotStatus fromDatabaseValue(String value) {
        for (PlotStatus status : PlotStatus.values()) {
            if (status.database_value.equals(value)) {
                return status;
            }
        }
        return null;
    }
}