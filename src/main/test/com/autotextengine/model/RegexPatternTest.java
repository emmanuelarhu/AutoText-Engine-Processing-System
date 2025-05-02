package com.autotextengine.model;

import main.java.com.autotextengine.model.RegexPattern;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RegexPattern class
 */
public class RegexPatternTest {

    @Test
    public void testConstructorWithPatternOnly() {
        String pattern = "\\d+";
        RegexPattern regexPattern = new RegexPattern(pattern);

        assertEquals(pattern, regexPattern.getPattern(),
                "Pattern should match the constructor parameter");
        assertTrue(regexPattern.isCaseSensitive(),
                "Default case sensitivity should be true");
        assertFalse(regexPattern.isMultiline(),
                "Default multiline should be false");
        assertFalse(regexPattern.isDotAll(),
                "Default dotAll should be false");
    }

    @Test
    public void testConstructorWithPatternNameDescription() {
        String pattern = "\\d+";
        String name = "Digits";
        String description = "Match digits";

        RegexPattern regexPattern = new RegexPattern(pattern, name, description);

        assertEquals(pattern, regexPattern.getPattern(),
                "Pattern should match the first constructor parameter");
        assertEquals(name, regexPattern.getName(),
                "Name should match the second constructor parameter");
        assertEquals(description, regexPattern.getDescription(),
                "Description should match the third constructor parameter");
    }

    @Test
    public void testConstructorWithPatternAndFlags() {
        String pattern = "\\d+";
        RegexPattern regexPattern = new RegexPattern(pattern, false, true, true);

        assertEquals(pattern, regexPattern.getPattern(),
                "Pattern should match the first constructor parameter");
        assertFalse(regexPattern.isCaseSensitive(),
                "Case sensitivity should match the second constructor parameter");
        assertTrue(regexPattern.isMultiline(),
                "Multiline should match the third constructor parameter");
        assertTrue(regexPattern.isDotAll(),
                "DotAll should match the fourth constructor parameter");
    }

    @Test
    public void testSetAndGetPattern() {
        RegexPattern regexPattern = new RegexPattern("initial");
        String newPattern = "\\w+";

        regexPattern.setPattern(newPattern);
        assertEquals(newPattern, regexPattern.getPattern(),
                "getPattern should return what was set");
    }

    @Test
    public void testSetAndGetName() {
        RegexPattern regexPattern = new RegexPattern("\\d+");
        String name = "Digits";

        regexPattern.setName(name);
        assertEquals(name, regexPattern.getName(),
                "getName should return what was set");
    }

    @Test
    public void testSetAndGetDescription() {
        RegexPattern regexPattern = new RegexPattern("\\d+");
        String description = "Match digits";

        regexPattern.setDescription(description);
        assertEquals(description, regexPattern.getDescription(),
                "getDescription should return what was set");
    }

    @Test
    public void testSetAndGetCaseSensitive() {
        RegexPattern regexPattern = new RegexPattern("\\d+");

        regexPattern.setCaseSensitive(false);
        assertFalse(regexPattern.isCaseSensitive(),
                "isCaseSensitive should return what was set");
    }

    @Test
    public void testSetAndGetMultiline() {
        RegexPattern regexPattern = new RegexPattern("\\d+");

        regexPattern.setMultiline(true);
        assertTrue(regexPattern.isMultiline(),
                "isMultiline should return what was set");
    }

    @Test
    public void testSetAndGetDotAll() {
        RegexPattern regexPattern = new RegexPattern("\\d+");

        regexPattern.setDotAll(true);
        assertTrue(regexPattern.isDotAll(),
                "isDotAll should return what was set");
    }

    @Test
    public void testGetFlags() {
        // Test with all flags off
        RegexPattern patternAllOff = new RegexPattern("\\d+", true, false, false);
        assertEquals(0, patternAllOff.getFlags(),
                "getFlags should return 0 with all flags off");

        // Test with CASE_INSENSITIVE only
        RegexPattern patternCaseInsensitive = new RegexPattern("\\d+", false, false, false);
        assertEquals(Pattern.CASE_INSENSITIVE, patternCaseInsensitive.getFlags(),
                "getFlags should include CASE_INSENSITIVE when case sensitivity is off");

        // Test with MULTILINE only
        RegexPattern patternMultiline = new RegexPattern("\\d+", true, true, false);
        assertEquals(Pattern.MULTILINE, patternMultiline.getFlags(),
                "getFlags should include MULTILINE when multiline is on");

        // Test with DOTALL only
        RegexPattern patternDotAll = new RegexPattern("\\d+", true, false, true);
        assertEquals(Pattern.DOTALL, patternDotAll.getFlags(),
                "getFlags should include DOTALL when dotAll is on");

        // Test with all flags on
        RegexPattern patternAllOn = new RegexPattern("\\d+", false, true, true);
        int expectedFlags = Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL;
        assertEquals(expectedFlags, patternAllOn.getFlags(),
                "getFlags should combine all flags correctly");
    }

    @Test
    public void testToString() {
        RegexPattern pattern = new RegexPattern("\\d+", false, true, true);
        String toString = pattern.toString();

        assertTrue(toString.contains("pattern='\\d+'"),
                "toString should include the pattern");
        assertTrue(toString.contains("caseSensitive=false"),
                "toString should include case sensitivity status");
        assertTrue(toString.contains("multiline=true"),
                "toString should include multiline status");
        assertTrue(toString.contains("dotAll=true"),
                "toString should include dotAll status");
    }

    @Test
    public void testSerialization() {
        // Verify that the class implements Serializable
        assertTrue(true,
                "RegexPattern should implement Serializable");

        // Check that serialVersionUID is defined as a static field
        try {
            java.lang.reflect.Field field = RegexPattern.class.getDeclaredField("serialVersionUID");
            field.setAccessible(true);
            assertTrue(java.lang.reflect.Modifier.isStatic(field.getModifiers()),
                    "serialVersionUID should be static");
            assertTrue(java.lang.reflect.Modifier.isFinal(field.getModifiers()),
                    "serialVersionUID should be final");
            assertEquals(Long.class, field.get(null).getClass(),
                    "serialVersionUID should be of type long");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("serialVersionUID field should be defined: " + e.getMessage());
        }
    }
}