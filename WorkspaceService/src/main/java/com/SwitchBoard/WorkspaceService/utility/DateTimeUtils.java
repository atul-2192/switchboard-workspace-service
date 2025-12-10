package com.SwitchBoard.WorkspaceService.utility;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class DateTimeUtils {

    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Convert Instant to formatted string
     */
    public String formatInstant(Instant instant) {
        log.debug("DateTimeUtils :: formatInstant :: Converting instant to string :: {}", instant);
        
        if (instant == null) {
            return null;
        }
        
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return localDateTime.format(DEFAULT_FORMATTER);
    }

    /**
     * Get current timestamp as Instant
     */
    public Instant getCurrentInstant() {
        log.debug("DateTimeUtils :: getCurrentInstant :: Getting current timestamp");
        return Instant.now();
    }

    /**
     * Check if a date is in the past
     */
    public boolean isPastDate(Instant instant) {
        log.debug("DateTimeUtils :: isPastDate :: Checking if date is past :: {}", instant);
        
        if (instant == null) {
            return false;
        }
        
        return instant.isBefore(Instant.now());
    }

    /**
     * Check if a date is in the future
     */
    public boolean isFutureDate(Instant instant) {
        log.debug("DateTimeUtils :: isFutureDate :: Checking if date is future :: {}", instant);
        
        if (instant == null) {
            return false;
        }
        
        return instant.isAfter(Instant.now());
    }

    /**
     * Get days difference between two instants
     */
    public long getDaysBetween(Instant start, Instant end) {
        log.debug("DateTimeUtils :: getDaysBetween :: Calculating days between :: start: {}, end: {}", start, end);
        
        if (start == null || end == null) {
            return 0;
        }
        
        return java.time.Duration.between(start, end).toDays();
    }

    /**
     * Add days to an instant
     */
    public Instant addDays(Instant instant, long days) {
        log.debug("DateTimeUtils :: addDays :: Adding {} days to :: {}", days, instant);
        
        if (instant == null) {
            return null;
        }
        
        return instant.plus(java.time.Duration.ofDays(days));
    }
}