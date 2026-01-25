package com.SwitchBoard.WorkspaceService.utility;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicInteger;

class SlugUtilsTest {

    private final SlugUtils slugUtils = new SlugUtils();

    @Test
    void generateSlug_regularText_returnsSlug() {
        // Arrange
        String text = "Hello World! This is 2025";

        // Act
        String slug = slugUtils.generateSlug(text);

        // Assert
        assertEquals("hello-world-this-is-2025", slug);
    }

    @Test
    void generateSlug_nullOrEmpty_returnsEmpty() {
        assertEquals("", slugUtils.generateSlug(null));
        assertEquals("", slugUtils.generateSlug("   "));
    }

    @Test
    void isValidSlug_positiveAndNegative() {
        assertTrue(slugUtils.isValidSlug("abc-def-123"));
        assertFalse(slugUtils.isValidSlug("Invalid Slug"));
        assertFalse(slugUtils.isValidSlug(null));
    }

    @Test
    void generateUniqueSlug_appendsCounterWhenExists() {
        // Arrange
        AtomicInteger calls = new AtomicInteger(0);
        java.util.function.Function<String, Boolean> existsChecker = slug -> {
            calls.incrementAndGet();
            // return true for base and first attempted unique slug, then false
            return "hello".equals(slug) || "hello-1".equals(slug);
        };

        // Act
        String unique = slugUtils.generateUniqueSlug("Hello", existsChecker);

        // Assert
        assertEquals("hello-2", unique);
        assertTrue(calls.get() >= 2);
    }

    @Test
    void normalizeSlug_variousInputs() {
        assertEquals("abc-def", slugUtils.normalizeSlug("AbC--Def!!"));
        assertEquals("", slugUtils.normalizeSlug(null));
    }
}
