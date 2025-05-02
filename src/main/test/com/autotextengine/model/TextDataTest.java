package com.autotextengine.model;

import main.java.com.autotextengine.model.TextData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TextData class
 */
public class TextDataTest {

    private TextData textData;
    private final String sampleText = "Hello world.\nThis is a test.";
    private final String sampleFilePath = "/path/to/file.txt";

    @BeforeEach
    public void setUp() {
        textData = new TextData(sampleText, sampleFilePath);
    }

    @Test
    public void testConstructorWithContentOnly() {
        TextData data = new TextData("Sample content");
        assertEquals("Sample content", data.getContent(), "Content should match constructor parameter");
        assertNull(data.getFilePath(), "FilePath should be null when not provided");
    }

    @Test
    public void testConstructorWithContentAndPath() {
        TextData data = new TextData("Sample content", "sample.txt");
        assertEquals("Sample content", data.getContent(), "Content should match first constructor parameter");
        assertEquals("sample.txt", data.getFilePath(), "FilePath should match second constructor parameter");
    }

    @Test
    public void testGetContent() {
        assertEquals(sampleText, textData.getContent(), "Content getter should return the original text");
    }

    @Test
    public void testSetContent() {
        String newContent = "New content";
        textData.setContent(newContent);
        assertEquals(newContent, textData.getContent(), "Content should be updated after setter call");
    }

    @Test
    public void testGetFilePath() {
        assertEquals(sampleFilePath, textData.getFilePath(), "FilePath getter should return the original path");
    }

    @Test
    public void testSetFilePath() {
        String newPath = "/new/path.txt";
        textData.setFilePath(newPath);
        assertEquals(newPath, textData.getFilePath(), "FilePath should be updated after setter call");
    }

    @Test
    public void testGetCharacterCount() {
        assertEquals(sampleText.length(), textData.getCharacterCount(),
                "Character count should match the content length");

        // Test with empty content
        TextData emptyData = new TextData("");
        assertEquals(0, emptyData.getCharacterCount(), "Empty content should have zero characters");

        // Test with null content
        TextData nullData = new TextData(null);
        assertEquals(0, nullData.getCharacterCount(), "Null content should have zero characters");
    }

    @Test
    public void testGetWordCount() {
        assertEquals(6, textData.getWordCount(), "Word count should be correct for sample text");

        // Test with empty content
        TextData emptyData = new TextData("");
        assertEquals(0, emptyData.getWordCount(), "Empty content should have zero words");

        // Test with null content
        TextData nullData = new TextData(null);
        assertEquals(0, nullData.getWordCount(), "Null content should have zero words");

        // Test with whitespace only
        TextData whitespaceData = new TextData("   ");
        assertEquals(0, whitespaceData.getWordCount(), "Whitespace-only content should have zero words");

        // Test with multiple spaces between words
        TextData spacesData = new TextData("word1   word2     word3");
        assertEquals(3, spacesData.getWordCount(), "Multiple spaces between words should be handled correctly");
    }

    @Test
    public void testGetLineCount() {
        assertEquals(2, textData.getLineCount(), "Line count should be correct for sample text");

        // Test with empty content
        TextData emptyData = new TextData("");
        assertEquals(0, emptyData.getLineCount(), "Empty content should have zero lines");

        // Test with null content
        TextData nullData = new TextData(null);
        assertEquals(0, nullData.getLineCount(), "Null content should have zero lines");

        // Test with different line terminators
        TextData crlfData = new TextData("Line1\r\nLine2\r\nLine3");
        assertEquals(3, crlfData.getLineCount(), "CRLF line terminators should be handled correctly");

        TextData crData = new TextData("Line1\rLine2\rLine3");
        assertEquals(3, crData.getLineCount(), "CR line terminators should be handled correctly");

        TextData lfData = new TextData("Line1\nLine2\nLine3");
        assertEquals(3, lfData.getLineCount(), "LF line terminators should be handled correctly");
    }
}