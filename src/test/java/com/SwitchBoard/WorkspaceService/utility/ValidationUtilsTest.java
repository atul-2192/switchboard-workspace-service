package com.SwitchBoard.WorkspaceService.utility;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilsTest {

    private final ValidationUtils vu = new ValidationUtils();

    @Test
    void stringChecks() {
        assertTrue(vu.isNullOrEmpty((String) null));
        assertTrue(vu.isNullOrEmpty(""));
        assertTrue(vu.isNotNullOrEmpty("ok"));
    }

    @Test
    void collectionAndMapChecks() {
        assertTrue(vu.isNullOrEmpty((List<?>) null));
        assertTrue(vu.isNullOrEmpty(List.of()));
        assertTrue(vu.isNotNullOrEmpty(List.of(1)));

        assertTrue(vu.isNullOrEmpty((Map<?, ?>) null));
        assertTrue(vu.isNullOrEmpty(Map.of()));
    }

    @Test
    void emailUrlHexPriorityAndNumbers() {
        assertTrue(vu.isValidEmail("test@example.com"));
        assertFalse(vu.isValidEmail("bad@@example"));

        assertTrue(vu.isValidUrl("https://example.com"));
        assertFalse(vu.isValidUrl("notaurl"));

        assertTrue(vu.isValidHexColor("#FFF"));
        assertTrue(vu.isValidHexColor("#abcdef"));
        assertFalse(vu.isValidHexColor("red"));

        assertTrue(vu.isValidPriority(3));
        assertFalse(vu.isValidPriority(null));

        assertTrue(vu.isPositive(1));
        assertFalse(vu.isPositive(0));
        assertTrue(vu.isNonNegative(0));
    }
}
