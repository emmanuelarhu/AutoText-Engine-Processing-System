package com.autotextengine.util;

import main.java.com.autotextengine.util.ValidationUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple tests for ValidationUtil
 */
public class ValidationUtilTest {

    @Test
    public void testIsNonEmptyText() {
        // Test valid texts
        assertTrue(ValidationUtil.isNonEmptyText("Hello"), "Non-empty text should be valid");
        assertTrue(ValidationUtil.isNonEmptyText(" Hello "), "Text with whitespace should be valid");
        assertTrue(ValidationUtil.isNonEmptyText("123"), "Numeric text should be valid");

        // Test invalid texts
        assertFalse(ValidationUtil.isNonEmptyText(""), "Empty string should be invalid");
        assertFalse(ValidationUtil.isNonEmptyText(null), "Null should be invalid");
        assertFalse(ValidationUtil.isNonEmptyText("   "), "Whitespace only should be invalid");
    }

    @Test
    public void testIsValidRegexPattern() {
        // Test valid patterns
        assertTrue(ValidationUtil.isValidRegexPattern("\\d+"), "Digits pattern should be valid");
        assertTrue(ValidationUtil.isValidRegexPattern("[a-z]*"), "Character class should be valid");
        assertTrue(ValidationUtil.isValidRegexPattern("(abc|def)"), "Alternation should be valid");

        // Test invalid patterns
        assertFalse(ValidationUtil.isValidRegexPattern("[unclosed"), "Unclosed bracket should be invalid");
        assertFalse(ValidationUtil.isValidRegexPattern("*starts-with-quantifier"), "Invalid quantifier usage should be invalid");
        assertFalse(ValidationUtil.isValidRegexPattern(null), "Null pattern should be invalid");
        assertFalse(ValidationUtil.isValidRegexPattern(""), "Empty pattern should be invalid");
    }

    @Test
    public void testIsValidFilePath() {
        // Test valid paths - using system-independent paths
        String tempDir = System.getProperty("java.io.tmpdir");
        assertTrue(ValidationUtil.isValidFilePath(tempDir), "Temp directory should be valid");
        assertTrue(ValidationUtil.isValidFilePath(tempDir + "test.txt"), "File in temp directory should be valid");

        // Test invalid paths
        assertFalse(ValidationUtil.isValidFilePath(""), "Empty path should be invalid");
        assertFalse(ValidationUtil.isValidFilePath(null), "Null path should be invalid");

        // Test platform-specific invalid paths (might work differently on different OSes)
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            assertFalse(ValidationUtil.isValidFilePath("C:\\invalid\\path\\with\\:illegal\\chars"),
                    "Path with illegal characters should be invalid on Windows");
        } else {
            assertFalse(ValidationUtil.isValidFilePath("/invalid/path/with/\0illegal/chars"),
                    "Path with null character should be invalid on Unix");
        }
    }

    @Test
    public void testIsValidEmail() {
        // Test valid emails
        assertTrue(ValidationUtil.isValidEmail("user@example.com"), "Standard email should be valid");
        assertTrue(ValidationUtil.isValidEmail("user.name@domain.co.uk"), "Email with subdomain should be valid");
        assertTrue(ValidationUtil.isValidEmail("user+tag@example.com"), "Email with plus should be valid");

        // Test invalid emails
        assertFalse(ValidationUtil.isValidEmail("invalid@"), "Incomplete email should be invalid");
        assertFalse(ValidationUtil.isValidEmail("@domain.com"), "Missing username should be invalid");
        assertFalse(ValidationUtil.isValidEmail("user@.com"), "Missing domain should be invalid");
        assertFalse(ValidationUtil.isValidEmail("user@domain"), "Missing TLD should be invalid");
        assertFalse(ValidationUtil.isValidEmail(""), "Empty email should be invalid");
        assertFalse(ValidationUtil.isValidEmail(null), "Null email should be invalid");
    }

    @Test
    public void testIsValidNumberInRange() {
        // Test valid ranges
        assertTrue(ValidationUtil.isValidNumberInRange(5, 1, 10), "Number within range should be valid");
        assertTrue(ValidationUtil.isValidNumberInRange(1, 1, 10), "Lower bound should be valid");
        assertTrue(ValidationUtil.isValidNumberInRange(10, 1, 10), "Upper bound should be valid");

        // Test invalid ranges
        assertFalse(ValidationUtil.isValidNumberInRange(0, 1, 10), "Below range should be invalid");
        assertFalse(ValidationUtil.isValidNumberInRange(11, 1, 10), "Above range should be invalid");
        assertFalse(ValidationUtil.isValidNumberInRange(5, 10, 1), "Inverted range should be invalid");
    }

    @Test
    public void testIsValidFileName() {
        // Test valid filenames
        assertTrue(ValidationUtil.isValidFileName("document.txt"), "Simple filename should be valid");
        assertTrue(ValidationUtil.isValidFileName("file_name-123.pdf"), "Filename with special chars should be valid");
        assertTrue(ValidationUtil.isValidFileName("long.filename.with.dots.txt"), "Filename with multiple dots should be valid");

        // Test invalid filenames
        assertFalse(ValidationUtil.isValidFileName(""), "Empty filename should be invalid");
        assertFalse(ValidationUtil.isValidFileName(null), "Null filename should be invalid");
        assertFalse(ValidationUtil.isValidFileName("file/path.txt"), "Filename with path separator should be invalid");
        assertFalse(ValidationUtil.isValidFileName("file:colon.txt"), "Filename with colon should be invalid");
        assertFalse(ValidationUtil.isValidFileName("file*.txt"), "Filename with asterisk should be invalid");
    }

    @Test
    public void testSanitizeInput() {
        // Test sanitization
        assertEquals("Hello world", ValidationUtil.sanitizeInput("Hello world"), "Plain text should be unchanged");
        assertEquals("Hello world", ValidationUtil.sanitizeInput("<script>Hello world</script>"), "HTML tags should be removed");
        assertEquals("Quote marks", ValidationUtil.sanitizeInput("\"Quote marks\""), "Quote marks should be handled");
        assertEquals("", ValidationUtil.sanitizeInput(null), "Null should return empty string");
    }
}