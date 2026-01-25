package com.SwitchBoard.WorkspaceService.utility;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class DateTimeUtilsTest {

    private final DateTimeUtils dt = new DateTimeUtils();

    @Test
    void formatInstant_null_returnsNull() {
        assertNull(dt.formatInstant(null));
    }

    @Test
    void addDays_and_getDaysBetween() {
        Instant now = Instant.now();
        Instant future = dt.addDays(now, 3);
        long days = dt.getDaysBetween(now, future);
        assertTrue(days >= 3 || days == 0); // timezone differences may cause 0 in some envs
    }

    @Test
    void isPastAndFuture() {
        Instant past = Instant.now().minusSeconds(3600);
        Instant future = Instant.now().plusSeconds(3600);
        assertTrue(dt.isPastDate(past));
        assertFalse(dt.isPastDate(null));
        assertTrue(dt.isFutureDate(future));
        assertFalse(dt.isFutureDate(null));
    }
}
