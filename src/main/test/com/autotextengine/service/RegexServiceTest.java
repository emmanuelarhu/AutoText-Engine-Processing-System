package com.autotextengine.service;

import main.java.com.autotextengine.service.RegexService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple tests for RegexService
 */
public class RegexServiceTest {

    private RegexService regexService;

    @BeforeEach
    public void setUp() {
        regexService = new RegexService();
    }

    @Test
    public void testValidatePattern() {
        // Test valid patterns
        assertTrue(regexService.validatePattern("\\d+"), "Digits pattern should be valid");
        assertTrue(regexService.validatePattern("[a-zA-Z]+"), "Letters pattern should be valid");
        assertTrue(regexService.validatePattern("\\w+@\\w+\\.\\w+"), "Email pattern should be valid");

        // Test invalid patterns
        assertFalse(regexService.validatePattern("[unclosed"), "Unclosed bracket should be invalid");
        assertFalse(regexService.validatePattern("(mismatched]"), "Mismatched brackets should be invalid");
        assertFalse(regexService.validatePattern("\\"), "Standalone escape should be invalid");
        assertFalse(regexService.validatePattern(null), "Null pattern should be invalid");
        assertFalse(regexService.validatePattern(""), "Empty pattern should be invalid");
    }

    @Test
    public void testFindMatches() {
        // Test basic matching
        List<String> matches = regexService.findMatches("Hello 123 World", "\\d+");
        assertEquals(1, matches.size(), "Should find one match");
        assertEquals("123", matches.getFirst(), "Should match the digits");

        // Test multiple matches
        matches = regexService.findMatches("abc123def456ghi", "\\d+");
        assertEquals(2, matches.size(), "Should find two matches");
        assertEquals("123", matches.get(0), "First match should be 123");
        assertEquals("456", matches.get(1), "Second match should be 456");

        // Test no matches
        matches = regexService.findMatches("Hello World", "\\d+");
        assertTrue(matches.isEmpty(), "Should find no matches");

        // Test empty input
        matches = regexService.findMatches("", "\\d+");
        assertTrue(matches.isEmpty(), "Empty string should have no matches");

        // Test null input
        matches = regexService.findMatches(null, "\\d+");
        assertTrue(matches.isEmpty(), "Null input should return empty list");
    }

    @Test
    public void testFindMatchesWithGroups() {
        // Test with capture groups
        Map<String, List<String>> groupMatches = regexService.findMatchesWithGroups(
                "John Doe: john@example.com",
                "(\\w+)\\s+(\\w+):\\s+([\\w@.]+)");

        assertEquals(1, groupMatches.get("Full match").size(), "Should have one full match");
        assertEquals("John", groupMatches.get("Group 1").getFirst(), "First group should be John");
        assertEquals("Doe", groupMatches.get("Group 2").getFirst(), "Second group should be Doe");
        assertEquals("john@example.com", groupMatches.get("Group 3").getFirst(), "Third group should be the email");

        // Test with no matches
        Map<String, List<String>> noMatches = regexService.findMatchesWithGroups(
                "No match here",
                "(\\d+)-(\\d+)");

        assertTrue(noMatches.isEmpty(), "Should return empty map for no matches");
    }

    @Test
    public void testReplacePattern() {
        // Test basic replacement
        String result = regexService.replacePattern("Hello 123 World", "\\d+", "456");
        assertEquals("Hello 456 World", result, "Should replace digits");

        // Test with capture groups
        result = regexService.replacePattern("Phone: 123-456-7890", "(\\d{3})-(\\d{3})-(\\d{4})", "($1) $2-$3");
        assertEquals("Phone: (123) 456-7890", result, "Should format phone number");

        // Test with no matches
        result = regexService.replacePattern("Hello World", "\\d+", "456");
        assertEquals("Hello World", result, "Should not change text with no matches");

        // Test empty and null inputs
        assertEquals("", regexService.replacePattern("", "\\d+", "456"), "Empty string should remain empty");
        assertNull(regexService.replacePattern(null, "\\d+", "456"), "Null input should return null");
    }

    @Test
    public void testCreateHighlightedHtml() {
        // Test basic highlighting
        String html = regexService.createHighlightedHtml("Hello 123 World", "\\d+");
        assertTrue(html.contains("<span class=\"match\">123</span>"), "Should highlight digits");

        // Test with special characters
        html = regexService.createHighlightedHtml("Text with <special> & characters", "<.*?>");
        assertTrue(html.contains("<span class=\"match\">&lt;special&gt;</span>"), "Should escape HTML characters");

        // Test invalid pattern
        try {
            regexService.createHighlightedHtml("Some text", "[invalid");
            fail("Should throw exception for invalid pattern");
        } catch (PatternSyntaxException e) {
            // Expected exception
        }
    }
}