package com.autotextengine.integration;

import main.java.com.autotextengine.controller.FileController;
import main.java.com.autotextengine.model.ProcessedResult;
import main.java.com.autotextengine.model.TextData;
import main.java.com.autotextengine.service.TextProcessingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the complete text processing workflow
 */
public class EndToEndProcessingTest {

    private TextProcessingService textProcessingService;
    private FileController fileController;

    @TempDir
    Path tempDir;

    @BeforeEach
    public void setUp() {
        textProcessingService = new TextProcessingService();
        fileController = new FileController();
    }

    @Test
    public void testCompleteTextProcessingWorkflow() throws IOException {
        // 1. Create a test file with content
        String testContent = "This is a test file.\nIt has two lines and multiple words.\nThis is a duplicate line.\nThis is a test file.";
        Path testFile = tempDir.resolve("test.txt");
        Files.writeString(testFile, testContent);

        // 2. Load file content
        String fileContent = Files.readString(testFile);
        TextData textData = new TextData(fileContent, testFile.getFileName().toString());

        // 3. Verify basic text properties
        assertEquals(4, textData.getLineCount(),
                "Line count should match the number of lines in the file");
        assertEquals(fileContent.length(), textData.getCharacterCount(),
                "Character count should match the file content length");

        // 4. Process word count operation using FileController
        ProcessedResult wordCountResult = fileController.processFile(textData, "Word Count");
        assertEquals(ProcessedResult.ResultType.NUMERIC, wordCountResult.getResultType(),
                "Word count should return a numeric result");
        assertEquals(21, wordCountResult.getNumericResult(),
                "Word count should match the number of words in the file");

        // 5. Process line count operation using FileController
        ProcessedResult lineCountResult = fileController.processFile(textData, "Line Count");
        assertEquals(4, lineCountResult.getNumericResult(),
                "Line count should match the number of lines in the file");

        // 6. Process duplicate line removal
        ProcessedResult dedupResult = fileController.processFile(textData, "Remove Duplicate Lines");
        assertEquals(ProcessedResult.ResultType.TEXT, dedupResult.getResultType(),
                "Deduplication should return a text result");
        String dedupText = dedupResult.getTextResult();
        assertEquals(3, dedupText.split("\n").length,
                "Deduplication should remove one of the duplicate lines");

        // 7. Process and save the result to another file
        Path outputFile = tempDir.resolve("output.txt");
        Files.writeString(outputFile, dedupText);

        // 8. Verify the output file content
        String outputContent = Files.readString(outputFile);
        assertEquals(dedupText, outputContent,
                "Output file should contain the deduplicated text");
        assertTrue(outputContent.contains("This is a test file."),
                "Output should preserve the content");
        assertEquals(1, countOccurrences(outputContent, "This is a test file."),
                "Duplicate line should be removed");
    }

    @Test
    public void testTextAnalysisWorkflow() throws IOException {
        // 1. Create a file with a longer text sample
        StringBuilder textBuilder = new StringBuilder();
        textBuilder.append("This is a sample text for analysis. It contains multiple sentences.\n");
        textBuilder.append("The text has several words that repeat. Words like text, analysis, and words.\n");
        textBuilder.append("Email addresses like test@example.com and admin@company.org should be detected.\n");
        textBuilder.append("URLs such as https://example.com and http://test.org should also be found.\n");
        textBuilder.append("This is a sample text that repeats for testing duplicate removal.");

        String testContent = textBuilder.toString();
        Path testFile = tempDir.resolve("analysis.txt");
        Files.writeString(testFile, testContent);

        // 2. Load and create TextData
        TextData textData = new TextData(Files.readString(testFile), testFile.getFileName().toString());

        // 3. Analyze word frequency
        Map<String, Long> wordFrequency = textProcessingService.analyzeWordFrequency(textData);
        assertTrue(wordFrequency.containsKey("text"),
                "Word frequency should contain 'text'");
        assertTrue(wordFrequency.get("text") > 1,
                "'text' should appear multiple times");

        // 4. Get top words
        List<Map.Entry<String, Long>> topWords = textProcessingService.getTopWords(textData, 5);
        assertTrue(topWords.size() <= 5,
                "Should return at most 5 top words");
        assertTrue(topWords.get(0).getValue() >= topWords.get(topWords.size()-1).getValue(),
                "Words should be ordered by frequency (descending)");

        // 5. Process with extracting emails
        ProcessedResult emailResult = fileController.processFile(textData, "Extract Emails");
        assertEquals(ProcessedResult.ResultType.LIST, emailResult.getResultType(),
                "Email extraction should return a list result");
        assertEquals(2, emailResult.getListResult().size(),
                "Should extract both email addresses");
        assertTrue(emailResult.getListResult().contains("test@example.com"),
                "Should extract the first email address");
        assertTrue(emailResult.getListResult().contains("admin@company.org"),
                "Should extract the second email address");

        // 6. Process with extracting URLs
        ProcessedResult urlResult = fileController.processFile(textData, "Extract URLs");
        assertEquals(ProcessedResult.ResultType.LIST, urlResult.getResultType(),
                "URL extraction should return a list result");
        assertEquals(2, urlResult.getListResult().size(),
                "Should extract both URLs");
        assertTrue(urlResult.getListResult().contains("https://example.com"),
                "Should extract the HTTPS URL");
        assertTrue(urlResult.getListResult().contains("http://test.org"),
                "Should extract the HTTP URL");

        // 7. Test case conversion
        ProcessedResult upperResult = fileController.processFile(textData, "Convert to Uppercase");
        assertTrue(upperResult.getTextResult().equals(testContent.toUpperCase()),
                "Text should be converted to uppercase");

        ProcessedResult lowerResult = fileController.processFile(textData, "Convert to Lowercase");
        assertTrue(lowerResult.getTextResult().equals(testContent.toLowerCase()),
                "Text should be converted to lowercase");
    }

    @Test
    public void testPerformanceWithLargeFile() throws IOException {
        // Create a large file (100KB+)
        StringBuilder largeContent = new StringBuilder();
        for (int i = 0; i < 5000; i++) {
            largeContent.append("Line ").append(i).append(" of the large test file with some repeated content.\n");
        }

        Path largeFile = tempDir.resolve("large.txt");
        Files.writeString(largeFile, largeContent.toString());

        // Verify file size
        assertTrue(Files.size(largeFile) > 100 * 1024,
                "Test file should be larger than 100KB");

        // Measure processing time for word count
        TextData largeTextData = new TextData(Files.readString(largeFile));

        long startTime = System.currentTimeMillis();
        ProcessedResult wordCountResult = fileController.processFile(largeTextData, "Word Count");
        long duration = System.currentTimeMillis() - startTime;

        // Assert reasonable processing time (adjust threshold as needed for your environment)
        assertTrue(duration < 5000,
                "Processing large file should complete within 5 seconds, took: " + duration + "ms");

        // Verify correct result
        assertEquals(55000, wordCountResult.getNumericResult(),
                "Word count should be correct for large file (5000 lines × 11 words)");
    }

    @Test
    public void testErrorHandling() {
        // Test with null data
        Exception nullDataException = assertThrows(NullPointerException.class, () -> {
            fileController.processFile(null, "Word Count");
        }, "Should throw NullPointerException when processing null data");

        // Test with invalid operation
        TextData sampleData = new TextData("Sample text");
        ProcessedResult invalidResult = fileController.processFile(sampleData, "Invalid Operation");

        assertTrue(invalidResult.getTextResult().contains("Unknown operation"),
                "Result should indicate unknown operation");

        // Test with malformed JSON
        TextData invalidJson = new TextData("{bad json}");
        ProcessedResult jsonResult = fileController.processFile(invalidJson, "Format JSON");

        assertTrue(jsonResult.getTextResult().contains("{bad json}"),
                "Should handle invalid JSON gracefully");
    }

    // Helper method to count string occurrences
    private int countOccurrences(String text, String searchString) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(searchString, index)) != -1) {
            count++;
            index += searchString.length();
        }
        return count;
    }
}
