package me.bteuk.network.staff;

import me.bteuk.network.commands.staff.Ban;
import me.bteuk.network.exceptions.DurationFormatException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModerationTest {

    @Test
    void checkDurationStringFormatting() throws DurationFormatException {

        //Created an instance of Ban since Moderation is an abstract class.
        Moderation moderation = new Ban();

        //Check each value separately.
        assertEquals(1000L * 60 * 60 * 24 * 365 * 2, moderation.getDuration("2y"));
        assertEquals(1000L * 60 * 60 * 24 * 30 * 3, moderation.getDuration("3m"));
        assertEquals(1000L * 60 * 60 * 24 * 7, moderation.getDuration("7d"));
        assertEquals(1000L * 60 * 60 * 14, moderation.getDuration("14h"));

        //Test a few combinations to make sure multiple values can be parsed in the same string.
        assertEquals(1000L * 60 * 60 * (3 + 5*24), moderation.getDuration("5d3h"));
        assertEquals(1000L * 60 * 60 * 24 * (30 * 3 + 365 * 5), moderation.getDuration("5y3m"));
        assertEquals(1000L * 60 * 60 * (3 + 5*24*365), moderation.getDuration("5y3h"));
        assertEquals(1000L * 60 * 60 * (15 + 24* (5 + 30* 3 + 365 * 7)), moderation.getDuration("7y3m5d15h"));

        //Test some wrong inputs.
        assertThrows(DurationFormatException.class,
                () -> moderation.getDuration("test"));
        assertThrows(DurationFormatException.class,
                () -> moderation.getDuration("1y5m2t"));
        assertThrows(DurationFormatException.class,
                () -> moderation.getDuration("q"));
        assertThrows(DurationFormatException.class,
                () -> moderation.getDuration("xy"));
        assertThrows(DurationFormatException.class,
                () -> moderation.getDuration("dum"));
        assertThrows(DurationFormatException.class,
                () -> moderation.getDuration("bad"));
        assertThrows(DurationFormatException.class,
                () -> moderation.getDuration("oh"));

        //Check for null.
        assertThrows(NullPointerException.class,
                () -> moderation.getDuration(null));
    }
}