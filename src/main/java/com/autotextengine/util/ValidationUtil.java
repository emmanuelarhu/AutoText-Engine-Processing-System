package main.java.com.autotextengine.util;

import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Utility class for validation operations
 */
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern FILENAME_PATTERN = Pattern.compile(
            "^[^<>:\"/\\\\|?*]+\\.[A-Za-z0-9]+$");

    /**
     * Check if a string is not null and not empty
     *
     * @param text The string to check
     * @return true if the string is not null and not empty
     */
    public static boolean isNonEmptyText(String text) {
        return text != null && !text.trim().isEmpty();
    }

    /**
     * Check if a string is a valid regex pattern
     *
     * @param pattern The regex pattern to validate
     * @return true if the pattern is valid
     */
    public static boolean isValidRegexPattern(String pattern) {
        if (!isNonEmptyText(pattern)) {
            return false;
        }

        try {
            Pattern.compile(pattern);
            return true;
        } catch (PatternSyntaxException e) {
            return false;
        }
    }

    /**
     * Check if a file path is valid
     *
     * @param path The file path to validate
     * @return true if the path is valid
     */
    public static boolean isValidFilePath(String path) {
        if (!isNonEmptyText(path)) {
            return false;
        }

        try {
            Paths.get(path);
            return true;
        } catch (InvalidPathException e) {
            return false;
        }
    }

    /**
     * Check if a string is a valid email address
     *
     * @param email The email address to validate
     * @return true if the email is valid
     */
    public static boolean isValidEmail(String email) {
        if (!isNonEmptyText(email)) {
            return false;
        }

        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Check if a number is within a specified range
     *
     * @param value The number to check
     * @param min The minimum allowed value (inclusive)
     * @param max The maximum allowed value (inclusive)
     * @return true if the number is within the range
     */
    public static boolean isValidNumberInRange(int value, int min, int max) {
        return value >= min && value <= max;
    }

    /**
     * Check if a string is a valid filename
     *
     * @param filename The filename to validate
     * @return true if the filename is valid
     */
    public static boolean isValidFileName(String filename) {
        if (!isNonEmptyText(filename)) {
            return false;
        }

        return FILENAME_PATTERN.matcher(filename).matches();
    }

    /**
     * Sanitize input to prevent security issues
     *
     * @param input The input to sanitize
     * @return The sanitized input
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return "";
        }

        // Remove potentially dangerous characters
        return input.replaceAll("[<>\"\'&]", "");
    }
}