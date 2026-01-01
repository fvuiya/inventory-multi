package com.bsoft.inventorymanager.utils;

import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Pattern;

public class InputValidator {

    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Patterns.EMAIL_ADDRESS;
    
    // Phone number pattern (basic validation for common formats)
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^\\+?[1-9]\\d{1,14}$|^[0-9\\-\\s\\(\\)]{7,15}$"
    );
    
    // Price/quantity validation pattern (positive numbers with optional decimal)
    private static final Pattern PRICE_PATTERN = Pattern.compile(
        "^\\d+(\\.\\d{1,2})?$"
    );

    // Name validation pattern (letters, spaces, hyphens, apostrophes)
    private static final Pattern NAME_PATTERN = Pattern.compile(
        "^[a-zA-Z\\s\\-'\u00C0-\u017F]{2,50}$"
    );

    // Barcode validation pattern (alphanumeric, common barcode formats)
    private static final Pattern BARCODE_PATTERN = Pattern.compile(
        "^[0-9A-Za-z\\-]{1,20}$"
    );

    /**
     * Validates an email address
     * @param email The email to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validates a phone number
     * @param phone The phone number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPhone(String phone) {
        if (TextUtils.isEmpty(phone)) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * Validates a name (2-50 characters, letters, spaces, hyphens, apostrophes)
     * @param name The name to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidName(String name) {
        if (TextUtils.isEmpty(name)) {
            return false;
        }
        return NAME_PATTERN.matcher(name.trim()).matches();
    }

    /**
     * Validates a price (positive decimal number)
     * @param price The price to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPrice(String price) {
        if (TextUtils.isEmpty(price)) {
            return false;
        }
        return PRICE_PATTERN.matcher(price.trim()).matches();
    }

    /**
     * Validates a quantity (positive integer)
     * @param quantity The quantity to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidQuantity(String quantity) {
        if (TextUtils.isEmpty(quantity)) {
            return false;
        }
        
        try {
            int qty = Integer.parseInt(quantity.trim());
            return qty >= 0; // Quantity should be non-negative
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validates a barcode (alphanumeric, hyphens)
     * @param barcode The barcode to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidBarcode(String barcode) {
        if (TextUtils.isEmpty(barcode)) {
            return false;
        }
        return BARCODE_PATTERN.matcher(barcode.trim()).matches();
    }

    /**
     * Sanitizes user input by removing potentially dangerous characters
     * @param input The input to sanitize
     * @return Sanitized input
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        
        // Remove potentially dangerous characters
        return input.replaceAll("[<>'\"&;]", "").trim();
    }

    /**
     * Validates and sanitizes a name
     * @param name The name to validate and sanitize
     * @return Sanitized name if valid, null otherwise
     */
    public static String validateAndSanitizeName(String name) {
        if (isValidName(name)) {
            return sanitizeInput(name);
        }
        return null;
    }

    /**
     * Validates and sanitizes an email
     * @param email The email to validate and sanitize
     * @return Sanitized email if valid, null otherwise
     */
    public static String validateAndSanitizeEmail(String email) {
        if (isValidEmail(email)) {
            return sanitizeInput(email).toLowerCase();
        }
        return null;
    }

    /**
     * Validates and sanitizes a phone number
     * @param phone The phone number to validate and sanitize
     * @return Sanitized phone number if valid, null otherwise
     */
    public static String validateAndSanitizePhone(String phone) {
        if (isValidPhone(phone)) {
            return sanitizeInput(phone);
        }
        return null;
    }

    /**
     * Validates and sanitizes a price
     * @param price The price to validate and sanitize
     * @return Parsed price as double if valid, -1 otherwise
     */
    public static double validateAndSanitizePrice(String price) {
        if (isValidPrice(price)) {
            try {
                return Double.parseDouble(price.trim());
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }

    /**
     * Validates and sanitizes a quantity
     * @param quantity The quantity to validate and sanitize
     * @return Parsed quantity as int if valid, -1 otherwise
     */
    public static int validateAndSanitizeQuantity(String quantity) {
        if (isValidQuantity(quantity)) {
            try {
                return Integer.parseInt(quantity.trim());
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }

    /**
     * Validates and sanitizes a barcode
     * @param barcode The barcode to validate and sanitize
     * @return Sanitized barcode if valid, null otherwise
     */
    public static String validateAndSanitizeBarcode(String barcode) {
        if (isValidBarcode(barcode)) {
            return sanitizeInput(barcode);
        }
        return null;
    }
}