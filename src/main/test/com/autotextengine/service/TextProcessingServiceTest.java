package com.autotextengine.service;

import main.java.com.autotextengine.model.ProcessedResult;
import main.java.com.autotextengine.model.TextData;
import main.java.com.autotextengine.service.TextProcessingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple tests for TextProcessingService
 */
public class TextProcessingServiceTest {

    private TextProcessingService textProcessingService;

    @BeforeEach
    public void setUp() {
        textProcessingService = new TextProcessingService();
    }

    @Test
    public void testCountWords() {
        // Test basic word counting
        TextData text = new TextData("Hello world. This is a test.");
        assertEquals(6, textProcessingService.countWords(text), "Should count 6 words");

        // Test with empty text
        TextData emptyText = new TextData("");
        assertEquals(0, textProcessingService.countWords(emptyText), "Empty text should have 0 words");

        // Test with whitespace only
        TextData whitespaceText = new TextData("   ");
        assertEquals(0, textProcessingService.countWords(whitespaceText), "Whitespace only should have 0 words");

        // Test with special characters
        TextData specialText = new TextData("Hello, world! How are you?");
        assertEquals(5, textProcessingService.countWords(specialText), "Should count 5 words with punctuation");
    }

    @Test
    public void testCountCharacters() {
        // Test basic character counting
        TextData text = new TextData("Hello, world!");
        assertEquals(13, textProcessingService.countCharacters(text), "Should count 13 characters");

        // Test counting without spaces
        assertEquals(12, textProcessingService.countCharacters(text, false), "Should count 12 characters without space");

        // Test with empty text
        TextData emptyText = new TextData("");
        assertEquals(0, textProcessingService.countCharacters(emptyText), "Empty text should have 0 characters");
    }

    @Test
    public void testCountSentences() {
        // Test basic sentence counting
        TextData text = new TextData("Hello world. This is a test! How are you?");
        assertEquals(3, textProcessingService.countSentences(text), "Should count 3 sentences");

        // Test with empty text
        TextData emptyText = new TextData("");
        assertEquals(0, textProcessingService.countSentences(emptyText), "Empty text should have 0 sentences");

        // Test with no sentence terminators
        TextData noTerminatorText = new TextData("Hello world This is a test");
        assertEquals(0, textProcessingService.countSentences(noTerminatorText), "Text without terminators should have 0 sentences");
    }

    @Test
    public void testAnalyzeWordFrequency() {
        // Test basic word frequency analysis
        TextData text = new TextData("Hello world. Hello again. World of wonders.");
        Map<String, Long> frequency = textProcessingService.analyzeWordFrequency(text);

        assertEquals(5, frequency.size(), "Should find 5 unique words");
        assertEquals(2L, frequency.get("hello"), "Hello should appear twice");
        assertEquals(2L, frequency.get("world"), "World should appear twice (case insensitive)");
        assertEquals(1L, frequency.get("again"), "Again should appear once");
        assertEquals(1L, frequency.get("of"), "Of should appear once");
        assertEquals(1L, frequency.get("wonders"), "Wonders should appear once");

        // Test with empty text
        TextData emptyText = new TextData("");
        Map<String, Long> emptyFrequency = textProcessingService.analyzeWordFrequency(emptyText);
        assertTrue(emptyFrequency.isEmpty(), "Empty text should have empty frequency map");
    }

    @Test
    public void testGetTopWords() {
        // Test getting top words
        TextData text = new TextData("Hello world. Hello again. World of wonders. Hello universe.");
        List<Map.Entry<String, Long>> topWords = textProcessingService.getTopWords(text, 2);

        assertEquals(2, topWords.size(), "Should return 2 top words");
        assertEquals("hello", topWords.get(0).getKey(), "Top word should be hello");
        assertEquals(3L, topWords.get(0).getValue(), "Hello should appear 3 times");
        assertEquals("world", topWords.get(1).getKey(), "Second top word should be world");
        assertEquals(2L, topWords.get(1).getValue(), "World should appear 2 times");

        // Test with limit larger than unique word count
        List<Map.Entry<String, Long>> allWords = textProcessingService.getTopWords(text, 10);
        assertEquals(6, allWords.size(), "Should return all 6 unique words");
    }

    @Test
    public void testTransformCase() {
        // Test uppercase transformation
        TextData text = new TextData("Hello World");
        assertEquals("HELLO WORLD", textProcessingService.transformCase(text, "UPPER"), "Should transform to uppercase");

        // Test lowercase transformation
        assertEquals("hello world", textProcessingService.transformCase(text, "LOWER"), "Should transform to lowercase");

        // Test title case transformation
        TextData lowercaseText = new TextData("hello world");
        assertEquals("Hello World", textProcessingService.transformCase(lowercaseText, "TITLE"), "Should transform to title case");

        // Test with unknown case type
        assertEquals("Hello World", textProcessingService.transformCase(text, "UNKNOWN"), "Unknown case type should return original text");
    }

    @Test
    public void testExtractSentences() {
        // Test basic sentence extraction
        TextData text = new TextData("First sentence. Second sentence! Third sentence?");
        List<String> sentences = textProcessingService.extractSentences(text);

        assertEquals(3, sentences.size(), "Should extract 3 sentences");
        assertEquals("First sentence.", sentences.get(0), "First sentence should match");
        assertEquals("Second sentence!", sentences.get(1), "Second sentence should match");
        assertEquals("Third sentence?", sentences.get(2), "Third sentence should match");

        // Test with empty text
        TextData emptyText = new TextData("");
        List<String> emptySentences = textProcessingService.extractSentences(emptyText);
        assertEquals(1, emptySentences.size(), "Empty text should return a list with one empty string");
        assertEquals("", emptySentences.get(0), "Empty text should return an empty string");
    }

    @Test
    public void testProcessBatch() {
        // Test batch processing with word count
        List<TextData> batch = Arrays.asList(
                new TextData("Hello world."),
                new TextData("Another test.")
        );

        List<ProcessedResult> results = textProcessingService.processBatch(batch, "WORD_COUNT");

        assertEquals(2, results.size(), "Should return 2 results");
        assertEquals(2, results.get(0).getNumericResult(), "First text should have 2 words");
        assertEquals(2, results.get(1).getNumericResult(), "Second text should have 2 words");

        // Test batch processing with character count
        results = textProcessingService.processBatch(batch, "CHARACTER_COUNT");

        assertEquals(2, results.size(), "Should return 2 results");
        assertEquals(12, results.get(0).getNumericResult(), "First text should have 12 characters");
        assertEquals(13, results.get(1).getNumericResult(), "Second text should have 13 characters");

        // Test with empty batch
        List<ProcessedResult> emptyResults = textProcessingService.processBatch(
                Arrays.asList(), "WORD_COUNT");
        assertTrue(emptyResults.isEmpty(), "Empty batch should return empty results");
    }

    @Test
    public void testSummarizeText() {
        // Test summarization with sentence limit
        TextData text = new TextData(
                "First sentence. Second sentence. Third sentence. Fourth sentence. Fifth sentence.");

        String summary = textProcessingService.summarizeText(text, 2);

        assertTrue(summary.contains("First sentence"), "Summary should contain first sentence");
        assertTrue(summary.contains("Second sentence"), "Summary should contain second sentence");
        assertFalse(summary.contains("Third sentence"), "Summary should not contain third sentence");

        // Test with sentence limit higher than available sentences
        String fullSummary = textProcessingService.summarizeText(text, 10);
        assertEquals(text.getContent().trim(), fullSummary, "Summary should be full text when limit exceeds sentence count");

        // Test with empty text
        TextData emptyText = new TextData("");
        String emptySummary = textProcessingService.summarizeText(emptyText, 3);
        assertEquals("", emptySummary, "Empty text should return empty summary");
    }

    @Test
    public void testFilterLines() {
        // Test including matching lines
        TextData text = new TextData("Line with keyword.\nLine without match.\nAnother keyword line.");
        ProcessedResult result = textProcessingService.filterLines(text, "keyword", true);

        assertEquals(2, result.getMatches().size(), "Should match 2 lines");
        assertTrue(result.getProcessedText().contains("Line with keyword"), "Result should contain first matching line");
        assertTrue(result.getProcessedText().contains("Another keyword line"), "Result should contain second matching line");
        assertFalse(result.getProcessedText().contains("Line without match"), "Result should not contain non-matching line");

        // Test excluding matching lines
        ProcessedResult excludeResult = textProcessingService.filterLines(text, "keyword", false);

        assertEquals(1, excludeResult.getMatches().size(), "Should match 1 line");
        assertTrue(excludeResult.getProcessedText().contains("Line without match"), "Result should contain non-matching line");
        assertFalse(excludeResult.getProcessedText().contains("keyword"), "Result should not contain matching lines");

        // Test with empty text
        TextData emptyText = new TextData("");
        ProcessedResult emptyResult = textProcessingService.filterLines(emptyText, "keyword", true);
        assertEquals("", emptyResult.getProcessedText(), "Empty text should return empty result");
    }

    @Test
    public void testExtractStructuredData() {
        // Test extracting email addresses
        TextData text = new TextData("Contact us at support@example.com or sales@company.com");
        ProcessedResult emailResult = textProcessingService.extractStructuredData(text, "email");

        assertEquals(2, emailResult.getMatches().size(), "Should extract 2 email addresses");
        assertTrue(emailResult.getMatches().contains("support@example.com"), "Should extract first email");
        assertTrue(emailResult.getMatches().contains("sales@company.com"), "Should extract second email");

        // Test extracting URLs
        TextData urlText = new TextData("Visit our website at https://example.com or http://company.org");
        ProcessedResult urlResult = textProcessingService.extractStructuredData(urlText, "url");

        assertEquals(2, urlResult.getMatches().size(), "Should extract 2 URLs");
        assertTrue(urlResult.getMatches().contains("https://example.com"), "Should extract HTTPS URL");
        assertTrue(urlResult.getMatches().contains("http://company.org"), "Should extract HTTP URL");

        // Test with unsupported data type
        try {
            textProcessingService.extractStructuredData(text, "unknown");
            fail("Should throw exception for unsupported data type");
        } catch (IllegalArgumentException e) {
            // Expected exception
        }
    }
}