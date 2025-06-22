package net.bteuk.network.utils.staff;

import net.bteuk.network.exceptions.DurationFormatException;
import org.junit.jupiter.api.Test;

import static net.bteuk.network.utils.staff.Moderation.getDuration;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModerationTest {

    @Test
    void checkDurationStringFormatting() throws DurationFormatException {

        // Check each value separately.
        assertEquals(1000L * 60 * 60 * 24 * 365 * 2, getDuration("2y"));
        assertEquals(1000L * 60 * 60 * 24 * 30 * 3, getDuration("3m"));
        assertEquals(1000L * 60 * 60 * 24 * 7, getDuration("7d"));
        assertEquals(1000L * 60 * 60 * 14, getDuration("14h"));

        // Test a few combinations to make sure multiple values can be parsed in the same string.
        assertEquals(1000L * 60 * 60 * (3 + 5 * 24), getDuration("5d3h"));
        assertEquals(1000L * 60 * 60 * 24 * (30 * 3 + 365 * 5), getDuration("5y3m"));
        assertEquals(1000L * 60 * 60 * (3 + 5 * 24 * 365), getDuration("5y3h"));
        assertEquals(1000L * 60 * 60 * (15 + 24 * (5 + 30 * 3 + 365 * 7)), getDuration("7y3m5d15h"));

        // Test some wrong inputs.
        assertThrows(DurationFormatException.class,
                () -> getDuration("test"));
        assertThrows(DurationFormatException.class,
                () -> getDuration("1y5m2t"));
        assertThrows(DurationFormatException.class,
                () -> getDuration("q"));
        assertThrows(DurationFormatException.class,
                () -> getDuration("xy"));
        assertThrows(DurationFormatException.class,
                () -> getDuration("dum"));
        assertThrows(DurationFormatException.class,
                () -> getDuration("bad"));
        assertThrows(DurationFormatException.class,
                () -> getDuration("oh"));

        // Check for null.
        assertThrows(NullPointerException.class,
                () -> getDuration(null));
    }
}