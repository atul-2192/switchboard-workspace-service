package com.SwitchBoard.WorkspaceService.utility;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.regex.Pattern;

@Component
@Slf4j
public class SlugUtils {

    private static final Pattern VALID_SLUG_PATTERN = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");

    /**
     * Generate a slug from a given text
     */
    public String generateSlug(String text) {
        log.debug("SlugUtils :: generateSlug :: Generating slug from :: {}", text);
        
        if (text == null || text.trim().isEmpty()) {
            log.warn("SlugUtils :: generateSlug :: Text is null or empty");
            return "";
        }
        
        String slug = text.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\s-]", "") // Remove special characters
                .replaceAll("\\s+", "-") // Replace spaces with hyphens
                .replaceAll("-+", "-") // Replace multiple hyphens with single hyphen
                .replaceAll("^-|-$", ""); // Remove leading/trailing hyphens
        
        log.debug("SlugUtils :: generateSlug :: Generated slug :: {}", slug);
        return slug;
    }

    /**
     * Validate if a string is a valid slug
     */
    public boolean isValidSlug(String slug) {
        log.debug("SlugUtils :: isValidSlug :: Validating slug :: {}", slug);
        
        if (slug == null || slug.trim().isEmpty()) {
            return false;
        }
        
        boolean isValid = VALID_SLUG_PATTERN.matcher(slug).matches();
        log.debug("SlugUtils :: isValidSlug :: Slug validation result :: {}", isValid);
        
        return isValid;
    }

    /**
     * Generate a unique slug by appending a number if necessary
     */
    public String generateUniqueSlug(String text, java.util.function.Function<String, Boolean> existsChecker) {
        log.debug("SlugUtils :: generateUniqueSlug :: Generating unique slug from :: {}", text);
        
        String baseSlug = generateSlug(text);
        
        if (baseSlug.isEmpty()) {
            log.warn("SlugUtils :: generateUniqueSlug :: Base slug is empty");
            return "";
        }
        
        String uniqueSlug = baseSlug;
        int counter = 1;
        
        while (existsChecker.apply(uniqueSlug)) {
            uniqueSlug = baseSlug + "-" + counter;
            counter++;
            log.debug("SlugUtils :: generateUniqueSlug :: Trying slug :: {}", uniqueSlug);
        }
        
        log.debug("SlugUtils :: generateUniqueSlug :: Generated unique slug :: {}", uniqueSlug);
        return uniqueSlug;
    }

    /**
     * Clean and normalize a slug
     */
    public String normalizeSlug(String slug) {
        log.debug("SlugUtils :: normalizeSlug :: Normalizing slug :: {}", slug);
        
        if (slug == null) {
            return "";
        }
        
        String normalized = slug.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9-]", "") // Keep only alphanumeric and hyphens
                .replaceAll("-+", "-") // Replace multiple hyphens with single hyphen
                .replaceAll("^-|-$", ""); // Remove leading/trailing hyphens
        
        log.debug("SlugUtils :: normalizeSlug :: Normalized slug :: {}", normalized);
        return normalized;
    }
}