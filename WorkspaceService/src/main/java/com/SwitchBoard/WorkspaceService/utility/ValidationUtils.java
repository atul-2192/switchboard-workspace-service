package com.SwitchBoard.WorkspaceService.utility;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Collection;
import java.util.Map;

@Component
@Slf4j
public class ValidationUtils {

    /**
     * Check if string is null or empty
     */
    public boolean isNullOrEmpty(String str) {
        log.debug("ValidationUtils :: isNullOrEmpty :: Checking string :: {}", str);
        return str == null || str.trim().isEmpty();
    }

    /**
     * Check if string is not null and not empty
     */
    public boolean isNotNullOrEmpty(String str) {
        log.debug("ValidationUtils :: isNotNullOrEmpty :: Checking string :: {}", str);
        return !isNullOrEmpty(str);
    }

    /**
     * Check if collection is null or empty
     */
    public boolean isNullOrEmpty(Collection<?> collection) {
        log.debug("ValidationUtils :: isNullOrEmpty :: Checking collection :: size: {}", 
                collection != null ? collection.size() : "null");
        return collection == null || collection.isEmpty();
    }

    /**
     * Check if collection is not null and not empty
     */
    public boolean isNotNullOrEmpty(Collection<?> collection) {
        log.debug("ValidationUtils :: isNotNullOrEmpty :: Checking collection :: size: {}", 
                collection != null ? collection.size() : "null");
        return !isNullOrEmpty(collection);
    }

    /**
     * Check if map is null or empty
     */
    public boolean isNullOrEmpty(Map<?, ?> map) {
        log.debug("ValidationUtils :: isNullOrEmpty :: Checking map :: size: {}", 
                map != null ? map.size() : "null");
        return map == null || map.isEmpty();
    }

    /**
     * Check if object is null
     */
    public boolean isNull(Object obj) {
        log.debug("ValidationUtils :: isNull :: Checking object :: {}", obj);
        return obj == null;
    }

    /**
     * Check if object is not null
     */
    public boolean isNotNull(Object obj) {
        log.debug("ValidationUtils :: isNotNull :: Checking object :: {}", obj);
        return obj != null;
    }

    /**
     * Validate email format
     */
    public boolean isValidEmail(String email) {
        log.debug("ValidationUtils :: isValidEmail :: Validating email :: {}", email);
        
        if (isNullOrEmpty(email)) {
            return false;
        }
        
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    /**
     * Validate URL format
     */
    public boolean isValidUrl(String url) {
        log.debug("ValidationUtils :: isValidUrl :: Validating URL :: {}", url);
        
        if (isNullOrEmpty(url)) {
            return false;
        }
        
        return url.matches("^(https?|ftp)://[^\\s/$.?#].[^\\s]*$");
    }

    /**
     * Validate hex color code
     */
    public boolean isValidHexColor(String color) {
        log.debug("ValidationUtils :: isValidHexColor :: Validating hex color :: {}", color);
        
        if (isNullOrEmpty(color)) {
            return false;
        }
        
        return color.matches("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$");
    }

    /**
     * Validate priority value (1-5)
     */
    public boolean isValidPriority(Integer priority) {
        log.debug("ValidationUtils :: isValidPriority :: Validating priority :: {}", priority);
        
        if (priority == null) {
            return false;
        }
        
        return priority >= 1 && priority <= 5;
    }

    /**
     * Validate that value is positive
     */
    public boolean isPositive(Number number) {
        log.debug("ValidationUtils :: isPositive :: Checking if positive :: {}", number);
        
        if (number == null) {
            return false;
        }
        
        return number.doubleValue() > 0;
    }

    /**
     * Validate that value is non-negative
     */
    public boolean isNonNegative(Number number) {
        log.debug("ValidationUtils :: isNonNegative :: Checking if non-negative :: {}", number);
        
        if (number == null) {
            return false;
        }
        
        return number.doubleValue() >= 0;
    }
}