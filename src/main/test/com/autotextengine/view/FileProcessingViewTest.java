package com.autotextengine.view;

import main.java.com.autotextengine.view.FileProcessingView;
import main.java.com.autotextengine.service.TextProcessingService;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FileProcessingView class
 */
@ExtendWith(MockitoExtension.class)
public class FileProcessingViewTest {

    private FileProcessingView fileProcessingView;

    @Mock
    private TextArea mockInputTextArea;

    @Mock
    private TextArea mockOutputTextArea;

    @Mock
    private TextField mockFilePathField;

    @Mock
    private Label mockStatusLabel;

    @Mock
    private ComboBox<String> mockOperationComboBox;

    @Mock
    private TextProcessingService mockTextProcessingService;

    @BeforeEach
    public void setUp() throws Exception {
        fileProcessingView = new FileProcessingView();

        // Inject mocks into the FileProcessingView using reflection
        injectMocks();
    }

    private void injectMocks() throws Exception {
        // Inject UI components
        setField("inputTextArea", mockInputTextArea);
        setField("outputTextArea", mockOutputTextArea);
        setField("filePathField", mockFilePathField);
        setField("statusLabel", mockStatusLabel);
        setField("operationComboBox", mockOperationComboBox);

        // Inject service
        Field textProcessingServiceField = FileProcessingView.class.getDeclaredField("textProcessingService");
        textProcessingServiceField.setAccessible(true);
        textProcessingServiceField.set(fileProcessingView, mockTextProcessingService);
    }

    private void setField(String fieldName, Object value) throws Exception {
        Field field = FileProcessingView.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(fileProcessingView, value);
    }

    @Test
    public void testInitialize() throws Exception {
        // Call initialize method
        fileProcessingView.initialize();

        // Verify that status label is set
        verify(mockStatusLabel).setText("Ready");

        // Check that operation ComboBox is initialized if not null
        verify(mockOperationComboBox, times(1)).setItems(any());
    }

    @Test
    public void testHandleClearInput() {
        // Call the method
        fileProcessingView.handleClearInput();

        // Verify that input area is cleared
        verify(mockInputTextArea).clear();

        // Verify status update
        verify(mockStatusLabel).setText("Input cleared");
    }

    @Test
    public void testHandleClearOutput() {
        // Call the method
        fileProcessingView.handleClearOutput();

        // Verify that output area is cleared
        verify(mockOutputTextArea).clear();

        // Verify status update
        verify(mockStatusLabel).setText("Output cleared");
    }

    @Test
    public void testHandleProcessFile_CharacterCount() {
        // Setup mocks
        when(mockInputTextArea.getText()).thenReturn("Hello World");
        when(mockOperationComboBox.getValue()).thenReturn("Character Count");

        // Call the method
        fileProcessingView.handleProcessFile();

        // Verify output text was set correctly
        verify(mockOutputTextArea).setText("Total characters: 11");

        // Verify status update
        verify(mockStatusLabel).setText("Processing complete: Character Count");
    }

    @Test
    public void testHandleProcessFile_WordCount() {
        // Setup mocks
        when(mockInputTextArea.getText()).thenReturn("Hello beautiful world");
        when(mockOperationComboBox.getValue()).thenReturn("Word Count");

        // Call the method
        fileProcessingView.handleProcessFile();

        // Verify output text was set correctly
        verify(mockOutputTextArea).setText("Total words: 3");

        // Verify status update
        verify(mockStatusLabel).setText("Processing complete: Word Count");
    }

    @Test
    public void testHandleProcessFile_EmptyInput() {
        // Setup mocks for empty input
        when(mockInputTextArea.getText()).thenReturn("");

        // Call the method
        fileProcessingView.handleProcessFile();

        // Verify error status
        verify(mockStatusLabel).setText("No input to process");

        // Verify output area was not updated
        verify(mockOutputTextArea, never()).setText(anyString());
    }

    @Test
    public void testHandleProcessFile_Exception() {
        // Setup mocks to throw exception
        when(mockInputTextArea.getText()).thenReturn("Hello World");
        when(mockOperationComboBox.getValue()).thenReturn("Format JSON");

        // This should throw an exception when processing invalid JSON
        fileProcessingView.handleProcessFile();

        // Verify error status
        verify(mockStatusLabel).setText(contains("Error processing"));
    }

    @Test
    public void testSortLines() throws Exception {
        // Get access to the private method using reflection
        Method sortLinesMethod = FileProcessingView.class.getDeclaredMethod("sortLines", String.class);
        sortLinesMethod.setAccessible(true);

        // Test with unsorted text
        String unsortedText = "C\nA\nB";
        String result = (String) sortLinesMethod.invoke(fileProcessingView, unsortedText);

        // Verify lines are sorted alphabetically
        assertEquals("A\nB\nC", result, "Lines should be sorted alphabetically");

        // Test with different line endings
        String mixedEndingsText = "C\r\nA\rB";
        String mixedResult = (String) sortLinesMethod.invoke(fileProcessingView, mixedEndingsText);

        // Lines should still be sorted correctly regardless of line endings
        assertTrue(mixedResult.contains("A"), "Result should contain A");
        assertTrue(mixedResult.contains("B"), "Result should contain B");
        assertTrue(mixedResult.contains("C"), "Result should contain C");
    }

    @Test
    public void testRemoveDuplicateLines() throws Exception {
        // Get access to the private method using reflection
        Method removeDuplicateLinesMethod =
                FileProcessingView.class.getDeclaredMethod("removeDuplicateLines", String.class);
        removeDuplicateLinesMethod.setAccessible(true);

        // Test with duplicate lines
        String textWithDuplicates = "A\nB\nA\nC\nB";
        String result = (String) removeDuplicateLinesMethod.invoke(fileProcessingView, textWithDuplicates);

        // Count the number of lines in the result
        int lineCount = result.split("\n").length;
        assertEquals(3, lineCount, "Result should have 3 unique lines");

        // Verify order preservation
        assertTrue(result.startsWith("A"), "First line should still be A");

        // Test with all unique lines
        String uniqueText = "A\nB\nC";
        String uniqueResult = (String) removeDuplicateLinesMethod.invoke(fileProcessingView, uniqueText);
        assertEquals(uniqueText, uniqueResult, "Text with all unique lines should remain unchanged");
    }

    @Test
    public void testRemoveExtraWhitespace() throws Exception {
        // Get access to the private method using reflection
        Method removeExtraWhitespaceMethod =
                FileProcessingView.class.getDeclaredMethod("removeExtraWhitespace", String.class);
        removeExtraWhitespaceMethod.setAccessible(true);

        // Test with extra whitespace
        String textWithExtraSpace = "  Hello   world  with    extra spaces  ";
        String result = (String) removeExtraWhitespaceMethod.invoke(fileProcessingView, textWithExtraSpace);

        // Verify whitespace is normalized
        assertEquals("Hello world with extra spaces", result,
                "Extra whitespace should be removed, keeping single spaces between words");

        // Test with multiple blank lines
        String textWithBlankLines = "Line 1\n\n\nLine 2\n\n";
        String blankResult = (String) removeExtraWhitespaceMethod.invoke(fileProcessingView, textWithBlankLines);

        // Verify multiple blank lines are normalized
        assertFalse(blankResult.contains("\n\n\n"),
                "Multiple consecutive newlines should be normalized");
    }

    @Test
    public void testExtractPatterns() throws Exception {
        // Get access to the private method using reflection
        Method extractPatternsMethod =
                FileProcessingView.class.getDeclaredMethod("extractPatterns", String.class, String.class);
        extractPatternsMethod.setAccessible(true);

        // Test extracting email addresses
        String textWithEmails = "Contact john@example.com or jane@company.org for more info.";
        String emailRegex = "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}";
        String emailResult = (String) extractPatternsMethod.invoke(fileProcessingView, textWithEmails, emailRegex);

        // Verify the result format
        assertTrue(emailResult.contains("Found 2 matches"),
                "Result should indicate the number of matches");
        assertTrue(emailResult.contains("john@example.com"),
                "Result should contain the first email");
        assertTrue(emailResult.contains("jane@company.org"),
                "Result should contain the second email");

        // Test with no matches
        String textWithoutMatches = "This text has no email addresses.";
        String noMatchResult = (String) extractPatternsMethod.invoke(fileProcessingView, textWithoutMatches, emailRegex);

        assertTrue(noMatchResult.contains("Found 0 matches"),
                "Result should indicate zero matches");
    }

    @Test
    public void testHandleSaveFile() throws Exception {
        // Mock FileChooser and File using a spy on FileProcessingView
        FileProcessingView spy = spy(fileProcessingView);

        // Setup output content
        when(mockOutputTextArea.getText()).thenReturn("Test content to save");

        // Setup FileChooser to return a temp file
        File tempFile = File.createTempFile("test", ".txt");
        tempFile.deleteOnExit();

        // Create a FileChooser spy that returns our temp file
        FileChooser mockChooser = mock(FileChooser.class);
        when(mockChooser.showSaveDialog(any())).thenReturn(tempFile);

        // Use doReturn to avoid actual FileChooser creation
        doReturn(mockChooser).when(spy).handleClearInput();

        // Call the method
        spy.handleSaveFile();

        // Verify file was written with correct content
        String fileContent = Files.readString(tempFile.toPath());
        assertEquals("Test content to save", fileContent,
                "File should be saved with the content from output text area");

        // Verify status update
        verify(mockStatusLabel).setText("File saved successfully");
    }

    @Test
    public void testHandleSaveFile_EmptyContent() {
        // Setup empty output content
        when(mockOutputTextArea.getText()).thenReturn("");

        // Call the method
        fileProcessingView.handleSaveFile();

        // Verify error status
        verify(mockStatusLabel).setText("No content to save");
    }

    @Test
    public void testUpdateStatus() throws Exception {
        // Call the private method using reflection
        Method updateStatusMethod = FileProcessingView.class.getDeclaredMethod("updateStatus", String.class);
        updateStatusMethod.setAccessible(true);

        // Test with a status message
        String statusMessage = "Test status message";
        updateStatusMethod.invoke(fileProcessingView, statusMessage);

        // Verify status label was updated
        verify(mockStatusLabel).setText(statusMessage);
    }
}