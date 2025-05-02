package com.autotextengine.controller;

import main.java.com.autotextengine.controller.FileController;
import main.java.com.autotextengine.model.ProcessedResult;
import main.java.com.autotextengine.model.TextData;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FileController class
 */
@ExtendWith(MockitoExtension.class)
public class FileControllerTest {

    @InjectMocks
    private FileController fileController;

    @Mock
    private TextField mockInputDirectoryField;

    @Mock
    private TextField mockOutputDirectoryField;

    @Mock
    private TextField mockFileExtensionsField;

    @Mock
    private TextField mockFileNamePatternField;

    @Mock
    private CheckBox mockIncludeSubdirectoriesCheckBox;

    @Mock
    private Spinner<Integer> mockMinFileSizeSpinner;

    @Mock
    private Spinner<Integer> mockMaxFileSizeSpinner;

    @Mock
    private TableView<File> mockFilesTableView;

    @Mock
    private ComboBox<String> mockOperationComboBox;

    @Mock
    private ComboBox<String> mockOutputFormatComboBox;

    @Mock
    private Stage mockStage;

    @BeforeEach
    public void setUp() throws Exception {
        // Set the mock stage
        fileController.setStage(mockStage);
    }

    @Test
    public void testProcessFile_CharacterCount() {
        // Create sample text data
        TextData textData = new TextData("Sample text for testing");

        // Process with Character Count operation
        ProcessedResult result = fileController.processFile(textData, "Character Count");

        // Verify result
        assertNotNull(result, "Result should not be null");
        assertEquals(ProcessedResult.ResultType.NUMERIC, result.getResultType(),
                "Character Count should return a numeric result");
        assertEquals(22, result.getNumericResult(),
                "Character count should be correct for sample text");
    }

    @Test
    public void testProcessFile_WordCount() {
        // Create sample text data
        TextData textData = new TextData("Sample text for testing");

        // Process with Word Count operation
        ProcessedResult result = fileController.processFile(textData, "Word Count");

        // Verify result
        assertNotNull(result, "Result should not be null");
        assertEquals(ProcessedResult.ResultType.NUMERIC, result.getResultType(),
                "Word Count should return a numeric result");
        assertEquals(4, result.getNumericResult(),
                "Word count should be correct for sample text");
    }

    @Test
    public void testProcessFile_LineCount() {
        // Create sample text data with multiple lines
        TextData textData = new TextData("Line 1\nLine 2\nLine 3");

        // Process with Line Count operation
        ProcessedResult result = fileController.processFile(textData, "Line Count");

        // Verify result
        assertNotNull(result, "Result should not be null");
        assertEquals(ProcessedResult.ResultType.NUMERIC, result.getResultType(),
                "Line Count should return a numeric result");
        assertEquals(3, result.getNumericResult(),
                "Line count should be correct for sample text");
    }

    @Test
    public void testProcessFile_SentenceCount() {
        // Create sample text data with multiple sentences
        TextData textData = new TextData("First sentence. Second sentence! Third sentence?");

        // Process with Sentence Count operation
        ProcessedResult result = fileController.processFile(textData, "Sentence Count");

        // Verify result
        assertNotNull(result, "Result should not be null");
        assertEquals(ProcessedResult.ResultType.NUMERIC, result.getResultType(),
                "Sentence Count should return a numeric result");
        assertEquals(3, result.getNumericResult(),
                "Sentence count should be correct for sample text");
    }

    @Test
    public void testProcessFile_ConvertToUppercase() {
        // Create sample text data
        TextData textData = new TextData("Sample text");

        // Process with Convert to Uppercase operation
        ProcessedResult result = fileController.processFile(textData, "Convert to Uppercase");

        // Verify result
        assertNotNull(result, "Result should not be null");
        assertEquals(ProcessedResult.ResultType.TEXT, result.getResultType(),
                "Convert to Uppercase should return a text result");
        assertEquals("SAMPLE TEXT", result.getTextResult(),
                "Text should be converted to uppercase");
    }

    @Test
    public void testProcessFile_ConvertToLowercase() {
        // Create sample text data
        TextData textData = new TextData("SAMPLE TEXT");

        // Process with Convert to Lowercase operation
        ProcessedResult result = fileController.processFile(textData, "Convert to Lowercase");

        // Verify result
        assertNotNull(result, "Result should not be null");
        assertEquals(ProcessedResult.ResultType.TEXT, result.getResultType(),
                "Convert to Lowercase should return a text result");
        assertEquals("sample text", result.getTextResult(),
                "Text should be converted to lowercase");
    }

    @Test
    public void testProcessFile_FormatJSON() {
        // Create sample JSON text data (unformatted)
        TextData textData = new TextData("{\"name\":\"John\",\"age\":30,\"city\":\"New York\"}");

        // Process with Format JSON operation
        ProcessedResult result = fileController.processFile(textData, "Format JSON");

        // Verify result
        assertNotNull(result, "Result should not be null");
        assertEquals(ProcessedResult.ResultType.TEXT, result.getResultType(),
                "Format JSON should return a text result");

        String formattedJson = result.getTextResult();
        assertTrue(formattedJson.contains("{\n"), "Formatted JSON should contain newlines");
        assertTrue(formattedJson.contains("\"name\""), "Formatted JSON should preserve content");
    }

    @Test
    public void testProcessFile_ExtractEmails() {
        // Create sample text data with emails
        TextData textData = new TextData("Contact john@example.com or jane@company.org for more information.");

        // Process with Extract Emails operation
        ProcessedResult result = fileController.processFile(textData, "Extract Emails");

        // Verify result
        assertNotNull(result, "Result should not be null");
        assertEquals(ProcessedResult.ResultType.LIST, result.getResultType(),
                "Extract Emails should return a list result");

        assertEquals(2, result.getListResult().size(),
                "Should extract both email addresses");
        assertTrue(result.getListResult().contains("john@example.com"),
                "Should extract first email address");
        assertTrue(result.getListResult().contains("jane@company.org"),
                "Should extract second email address");
    }

    @Test
    public void testProcessFile_UnknownOperation() {
        // Create sample text data
        TextData textData = new TextData("Sample text");

        // Process with unknown operation
        ProcessedResult result = fileController.processFile(textData, "Unknown Operation");

        // Verify result
        assertNotNull(result, "Result should not be null");
        assertEquals(ProcessedResult.ResultType.TEXT, result.getResultType(),
                "Unknown operation should return a text result");
        assertTrue(result.getTextResult().contains("Unknown operation"),
                "Result should indicate unknown operation");
    }

    @Test
    public void testFormatFileSize() {
        try {
            // Get access to the private method using reflection
            java.lang.reflect.Method formatFileSizeMethod =
                    FileController.class.getDeclaredMethod("formatFileSize", long.class);
            formatFileSizeMethod.setAccessible(true);

            // Test bytes
            String bytesResult = (String) formatFileSizeMethod.invoke(fileController, 500L);
            assertEquals("500 B", bytesResult, "Should format 500 bytes correctly");

            // Test kilobytes
            String kbResult = (String) formatFileSizeMethod.invoke(fileController, 1500L);
            assertTrue(kbResult.contains("KB"), "KB format should contain 'KB'");

            // Test megabytes
            String mbResult = (String) formatFileSizeMethod.invoke(fileController, 1500000L);
            assertTrue(mbResult.contains("MB"), "MB format should contain 'MB'");

            // Test gigabytes
            String gbResult = (String) formatFileSizeMethod.invoke(fileController, 1500000000L);
            assertTrue(gbResult.contains("GB"), "GB format should contain 'GB'");
        } catch (Exception e) {
            fail("Exception occurred testing formatFileSize: " + e.getMessage());
        }
    }

    @Test
    public void testCountCharacters() {
        try {
            // Get access to the private method using reflection
            java.lang.reflect.Method countCharactersMethod =
                    FileController.class.getDeclaredMethod("countCharacters", TextData.class);
            countCharactersMethod.setAccessible(true);

            // Test with regular text
            TextData textData = new TextData("Hello World");
            long result = (long) countCharactersMethod.invoke(fileController, textData);
            assertEquals(11L, result, "Should count all characters correctly");

            // Test with empty text
            TextData emptyData = new TextData("");
            long emptyResult = (long) countCharactersMethod.invoke(fileController, emptyData);
            assertEquals(0L, emptyResult, "Empty text should have zero characters");
        } catch (Exception e) {
            fail("Exception occurred testing countCharacters: " + e.getMessage());
        }
    }

    @Test
    public void testCountWords() {
        try {
            // Get access to the private method using reflection
            java.lang.reflect.Method countWordsMethod =
                    FileController.class.getDeclaredMethod("countWords", TextData.class);
            countWordsMethod.setAccessible(true);

            // Test with regular text
            TextData textData = new TextData("Hello beautiful world");
            long result = (long) countWordsMethod.invoke(fileController, textData);
            assertEquals(3L, result, "Should count words correctly");

            // Test with empty text
            TextData emptyData = new TextData("");
            long emptyResult = (long) countWordsMethod.invoke(fileController, emptyData);
            assertEquals(0L, emptyResult, "Empty text should have zero words");

            // Test with text that has extra whitespace
            TextData spacyData = new TextData("  Hello   beautiful   world  ");
            long spacyResult = (long) countWordsMethod.invoke(fileController, spacyData);
            assertEquals(3L, spacyResult, "Should handle extra whitespace correctly");
        } catch (Exception e) {
            fail("Exception occurred testing countWords: " + e.getMessage());
        }
    }

    @Test
    public void testCountLines() {
        try {
            // Get access to the private method using reflection
            java.lang.reflect.Method countLinesMethod =
                    FileController.class.getDeclaredMethod("countLines", TextData.class);
            countLinesMethod.setAccessible(true);

            // Test with multi-line text using different line endings
            TextData textData = new TextData("Line 1\nLine 2\r\nLine 3\rLine 4");
            long result = (long) countLinesMethod.invoke(fileController, textData);
            assertEquals(4L, result, "Should count lines with different line endings");

            // Test with empty text
            TextData emptyData = new TextData("");
            long emptyResult = (long) countLinesMethod.invoke(fileController, emptyData);
            assertEquals(0L, emptyResult, "Empty text should have zero lines");
        } catch (Exception e) {
            fail("Exception occurred testing countLines: " + e.getMessage());
        }
    }

    @Test
    public void testCountSentences() {
        try {
            // Get access to the private method using reflection
            java.lang.reflect.Method countSentencesMethod =
                    FileController.class.getDeclaredMethod("countSentences", TextData.class);
            countSentencesMethod.setAccessible(true);

            // Test with multiple sentences using different endings
            TextData textData = new TextData("This is sentence one. This is sentence two! Is this sentence three?");
            long result = (long) countSentencesMethod.invoke(fileController, textData);
            assertEquals(3L, result, "Should count sentences with different endings");

            // Test with empty text
            TextData emptyData = new TextData("");
            long emptyResult = (long) countSentencesMethod.invoke(fileController, emptyData);
            assertEquals(0L, emptyResult, "Empty text should have zero sentences");
        } catch (Exception e) {
            fail("Exception occurred testing countSentences: " + e.getMessage());
        }
    }

    @Test
    public void testFormatJson() {
        try {
            // Get access to the private method using reflection
            java.lang.reflect.Method formatJsonMethod =
                    FileController.class.getDeclaredMethod("formatJson", String.class);
            formatJsonMethod.setAccessible(true);

            // Test with simple JSON
            String json = "{\"name\":\"John\",\"age\":30}";
            String result = (String) formatJsonMethod.invoke(fileController, json);

            // Check for formatting elements
            assertTrue(result.contains("\n"), "Formatted JSON should contain newlines");
            assertTrue(result.contains("  "), "Formatted JSON should contain indentation");
            assertTrue(result.contains("\"name\""), "Formatted JSON should preserve content");
        } catch (Exception e) {
            fail("Exception occurred testing formatJson: " + e.getMessage());
        }
    }

    @Test
    public void testFormatXml() {
        try {
            // Get access to the private method using reflection
            java.lang.reflect.Method formatXmlMethod =
                    FileController.class.getDeclaredMethod("formatXml", String.class);
            formatXmlMethod.setAccessible(true);

            // Test with simple XML
            String xml = "<root><element>content</element><self-closing/></root>";
            String result = (String) formatXmlMethod.invoke(fileController, xml);

            // Check for formatting elements
            assertTrue(result.contains("\n"), "Formatted XML should contain newlines");
            assertTrue(result.contains("  "), "Formatted XML should contain indentation");
            assertTrue(result.contains("<root"), "Formatted XML should preserve content");
        } catch (Exception e) {
            fail("Exception occurred testing formatXml: " + e.getMessage());
        }
    }

    @Test
    public void testExtractEmails() {
        try {
            // Get access to the private method using reflection
            java.lang.reflect.Method extractEmailsMethod =
                    FileController.class.getDeclaredMethod("extractEmails", String.class);
            extractEmailsMethod.setAccessible(true);

            // Test with text containing emails
            String text = "Contact john.doe@example.com or jane_smith@company.co.uk for more information.";
            List<String> result = (List<String>) extractEmailsMethod.invoke(fileController, text);

            assertEquals(2, result.size(), "Should extract all email addresses");
            assertTrue(result.contains("john.doe@example.com"), "Should extract first email");
            assertTrue(result.contains("jane_smith@company.co.uk"), "Should extract second email");

            // Test with no emails
            String noEmailText = "This text has no email addresses.";
            List<String> noEmailResult = (List<String>) extractEmailsMethod.invoke(fileController, noEmailText);
            assertTrue(noEmailResult.isEmpty(), "Should return empty list for text with no emails");
        } catch (Exception e) {
            fail("Exception occurred testing extractEmails: " + e.getMessage());
        }
    }

    @Test
    public void testExtractUrls() {
        try {
            // Get access to the private method using reflection
            java.lang.reflect.Method extractUrlsMethod =
                    FileController.class.getDeclaredMethod("extractUrls", String.class);
            extractUrlsMethod.setAccessible(true);

            // Test with text containing URLs
            String text = "Visit our website at https://example.com or http://test.org/page for more information.";
            List<String> result = (List<String>) extractUrlsMethod.invoke(fileController, text);

            assertEquals(2, result.size(), "Should extract all URLs");
            assertTrue(result.contains("https://example.com"), "Should extract HTTPS URL");
            assertTrue(result.contains("http://test.org/page"), "Should extract HTTP URL with path");

            // Test with no URLs
            String noUrlText = "This text has no web addresses.";
            List<String> noUrlResult = (List<String>) extractUrlsMethod.invoke(fileController, noUrlText);
            assertTrue(noUrlResult.isEmpty(), "Should return empty list for text with no URLs");
        } catch (Exception e) {
            fail("Exception occurred testing extractUrls: " + e.getMessage());
        }
    }

    @Test
    public void testRemoveDuplicateLines() {
        try {
            // Get access to the private method using reflection
            java.lang.reflect.Method removeDuplicateLinesMethod =
                    FileController.class.getDeclaredMethod("removeDuplicateLines", String.class);
            removeDuplicateLinesMethod.setAccessible(true);

            // Test with duplicate lines
            String text = "Line 1\nLine 2\nLine 1\nLine 3\nLine 2\nLine 4";
            String result = (String) removeDuplicateLinesMethod.invoke(fileController, text);

            String[] lines = result.split("\n");
            assertEquals(4, lines.length, "Should have removed duplicates");

            // Verify content and order preservation
            assertEquals("Line 1", lines[0], "First unique line should be preserved");
            assertEquals("Line 2", lines[1], "Second unique line should be preserved");
            assertEquals("Line 3", lines[2], "Third unique line should be preserved");
            assertEquals("Line 4", lines[3], "Fourth unique line should be preserved");
        } catch (Exception e) {
            fail("Exception occurred testing removeDuplicateLines: " + e.getMessage());
        }
    }

    @Test
    public void testSortLines() {
        try {
            // Get access to the private method using reflection
            java.lang.reflect.Method sortLinesMethod =
                    FileController.class.getDeclaredMethod("sortLines", String.class);
            sortLinesMethod.setAccessible(true);

            // Test with unsorted lines
            String text = "Zebra\nApple\nBanana\nCherry";
            String result = (String) sortLinesMethod.invoke(fileController, text);

            String[] lines = result.split("\n");
            assertEquals(4, lines.length, "Should preserve all lines");

            // Verify alphabetical order
            assertEquals("Apple", lines[0], "First sorted line should be Apple");
            assertEquals("Banana", lines[1], "Second sorted line should be Banana");
            assertEquals("Cherry", lines[2], "Third sorted line should be Cherry");
            assertEquals("Zebra", lines[3], "Fourth sorted line should be Zebra");
        } catch (Exception e) {
            fail("Exception occurred testing sortLines: " + e.getMessage());
        }
    }

    @Test
    public void testRemoveExtraWhitespace() {
        try {
            // Get access to the private method using reflection
            java.lang.reflect.Method removeExtraWhitespaceMethod =
                    FileController.class.getDeclaredMethod("removeExtraWhitespace", String.class);
            removeExtraWhitespaceMethod.setAccessible(true);

            // Test with extra whitespace
            String text = "  This   has  extra   spaces  \n and\nmultiple\n\n\nlines  ";
            String result = (String) removeExtraWhitespaceMethod.invoke(fileController, text);

            // Check for normalized whitespace
            assertFalse(result.contains("  "), "Result should not contain double spaces");
            assertTrue(result.trim().startsWith("This"), "Leading spaces should be removed");
            assertTrue(result.trim().endsWith("lines"), "Trailing spaces should be removed");
            assertFalse(result.contains("\n\n\n"), "Multiple consecutive newlines should be normalized");
        } catch (Exception e) {
            fail("Exception occurred testing removeExtraWhitespace: " + e.getMessage());
        }
    }
}