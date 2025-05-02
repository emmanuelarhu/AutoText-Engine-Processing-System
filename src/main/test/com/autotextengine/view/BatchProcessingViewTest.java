package com.autotextengine.view;

import main.java.com.autotextengine.model.ProcessedResult;
import main.java.com.autotextengine.view.BatchProcessingView;
import main.java.com.autotextengine.controller.FileController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BatchProcessingView class
 */
@ExtendWith(MockitoExtension.class)
public class BatchProcessingViewTest {

    private BatchProcessingView batchProcessingView;

    @Mock
    private TableView<BatchProcessingView.FileItem> mockTableView;

    @Mock
    private TextField mockInputDirectoryField;

    @Mock
    private TextField mockOutputDirectoryField;

    @Mock
    private ComboBox<String> mockOperationComboBox;

    @Mock
    private ComboBox<String> mockOutputFormatComboBox;

    @Mock
    private CheckBox mockCreateSeparateFilesCheckBox;

    @Mock
    private Button mockProcessButton;

    @Mock
    private Button mockStopButton;

    @Mock
    private Label mockStatusLabel;

    @Mock
    private Label mockFilesProcessedLabel;

    @Mock
    private ProgressBar mockProgressBar;

    @Mock
    private FileController mockFileController;

    @BeforeEach
    public void setUp() throws Exception {
        batchProcessingView = new BatchProcessingView();

        // Inject mocks into the BatchProcessingView using reflection
        injectMocks();
    }

    private void injectMocks() throws Exception {
        // Inject all mock UI components
        setField("filesTableView", mockTableView);
        setField("inputDirectoryField", mockInputDirectoryField);
        setField("outputDirectoryField", mockOutputDirectoryField);
        setField("operationComboBox", mockOperationComboBox);
        setField("outputFormatComboBox", mockOutputFormatComboBox);
        setField("createSeparateFilesCheckBox", mockCreateSeparateFilesCheckBox);
        setField("processButton", mockProcessButton);
        setField("stopButton", mockStopButton);
        setField("statusLabel", mockStatusLabel);
        setField("filesProcessedLabel", mockFilesProcessedLabel);
        setField("processingProgressBar", mockProgressBar);

        // Mock the FileController using reflection
        Field fileControllerField = BatchProcessingView.class.getDeclaredField("fileController");
        fileControllerField.setAccessible(true);
        fileControllerField.set(batchProcessingView, mockFileController);
    }

    private void setField(String fieldName, Object value) throws Exception {
        Field field = BatchProcessingView.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(batchProcessingView, value);
    }

    @Test
    public void testFormatFileSize() throws Exception {
        // Get access to the private method using reflection
        Method formatFileSizeMethod = BatchProcessingView.class.getDeclaredMethod("formatFileSize", long.class);
        formatFileSizeMethod.setAccessible(true);

        // Test bytes format
        String bytesResult = (String) formatFileSizeMethod.invoke(batchProcessingView, 500L);
        assertEquals("500 B", bytesResult, "Should format 500 bytes correctly");

        // Test KB format
        String kbResult = (String) formatFileSizeMethod.invoke(batchProcessingView, 1500L);
        assertEquals("1.5 KB", kbResult, "Should format 1.5 KB correctly");

        // Test MB format
        String mbResult = (String) formatFileSizeMethod.invoke(batchProcessingView, 1500000L);
        assertEquals("1.4 MB", mbResult, "Should format 1.4 MB correctly");

        // Test GB format
        String gbResult = (String) formatFileSizeMethod.invoke(batchProcessingView, 1500000000L);
        assertEquals("1.4 GB", gbResult, "Should format 1.4 GB correctly");
    }

    @Test
    public void testGenerateOutputFilename() throws Exception {
        // Get access to the private method using reflection
        Method generateOutputFilenameMethod = BatchProcessingView.class.getDeclaredMethod(
                "generateOutputFilename", String.class, String.class);
        generateOutputFilenameMethod.setAccessible(true);

        // Setup test
        when(mockOutputFormatComboBox.getValue()).thenReturn("Same as Input");
        String result1 = (String) generateOutputFilenameMethod.invoke(
                batchProcessingView, "test.txt", "Character Count");
        assertEquals("test.txt", result1, "Should return same filename for 'Same as Input' format");

        // Test append operation format
        when(mockOutputFormatComboBox.getValue()).thenReturn("Append Operation");
        String result2 = (String) generateOutputFilenameMethod.invoke(
                batchProcessingView, "test.txt", "Character Count");
        assertEquals("test_character_count.txt", result2,
                "Should append operation name for 'Append Operation' format");

        // Test without extension
        String result3 = (String) generateOutputFilenameMethod.invoke(
                batchProcessingView, "test", "Character Count");
        assertEquals("test_character_count", result3,
                "Should handle filenames without extensions");
    }

    @Test
    public void testFileItemClass() {
        // Test FileItem constructor and getters
        String name = "test.txt";
        String size = "1.5 KB";
        String path = "/path/to/test.txt";
        String status = "Ready";

        BatchProcessingView.FileItem fileItem = new BatchProcessingView.FileItem(name, size, path, status);

        assertEquals(name, fileItem.getName(), "Name should match the constructor parameter");
        assertEquals(size, fileItem.getSize(), "Size should match the constructor parameter");
        assertEquals(path, fileItem.getPath(), "Path should match the constructor parameter");
        assertEquals(status, fileItem.getStatus(), "Status should match the constructor parameter");

        // Test status setter
        String newStatus = "Processing";
        fileItem.setStatus(newStatus);
        assertEquals(newStatus, fileItem.getStatus(), "Status should be updated after calling setter");
    }

    @Test
    public void testFormatResult() throws Exception {
        // Get access to the private method using reflection
        Method formatResultMethod = BatchProcessingView.class.getDeclaredMethod("formatResult", ProcessedResult.class);
        formatResultMethod.setAccessible(true);

        // Test TEXT result type
        ProcessedResult textResult = new ProcessedResult("Sample text result");
        String formattedText = (String) formatResultMethod.invoke(batchProcessingView, textResult);
        assertEquals("Sample text result", formattedText, "Should return text result directly");

        // Test NUMERIC result type
        ProcessedResult numericResult = new ProcessedResult(123L);
        String formattedNumeric = (String) formatResultMethod.invoke(batchProcessingView, numericResult);
        assertEquals("123", formattedNumeric, "Should convert numeric result to string");

        // Test LIST result type
        List<String> list = new ArrayList<>();
        list.add("item1");
        list.add("item2");
        ProcessedResult listResult = new ProcessedResult(list);
        String formattedList = (String) formatResultMethod.invoke(batchProcessingView, listResult);
        assertEquals("item1, item2", formattedList, "Should join list items with commas");

        // Test null result
        String nullResult = (String) formatResultMethod.invoke(batchProcessingView, null);
        assertEquals("No result", nullResult, "Should handle null result");
    }

    @Test
    public void testHandleClearFiles() {
        // Setup ObservableList and mock behavior
        ObservableList<BatchProcessingView.FileItem> items = FXCollections.observableArrayList();
        when(mockTableView.getItems()).thenReturn(items);

        // Execute the method
        batchProcessingView.handleClearFiles();

        // Verify interactions
        verify(mockTableView, times(1)).getItems();
        verify(mockStatusLabel, times(1)).setText("Cleared file list");
    }

    @Test
    public void testUpdateControlsState() throws Exception {
        // Get access to the private method using reflection
        Method updateControlsStateMethod = BatchProcessingView.class.getDeclaredMethod("updateControlsState");
        updateControlsStateMethod.setAccessible(true);

        // Setup test with empty table
        ObservableList<BatchProcessingView.FileItem> emptyItems = FXCollections.observableArrayList();
        when(mockTableView.getItems()).thenReturn(emptyItems);

        // Call the method
        updateControlsStateMethod.invoke(batchProcessingView);

        // Verify disable state for empty table
        verify(mockProcessButton).setDisable(true);

        // Setup test with non-empty table
        ObservableList<BatchProcessingView.FileItem> nonEmptyItems = FXCollections.observableArrayList(
                new BatchProcessingView.FileItem("test.txt", "1KB", "/path/to/test.txt", "Ready")
        );
        when(mockTableView.getItems()).thenReturn(nonEmptyItems);

        // Reset mocks
        reset(mockProcessButton);

        // Call the method again
        updateControlsStateMethod.invoke(batchProcessingView);

        // Verify disable state for non-empty table
        verify(mockProcessButton).setDisable(false);
    }

    @Test
    public void testHandleAddFileToTable() throws Exception {
        // Get access to the private method using reflection
        Method addFileToTableMethod = BatchProcessingView.class.getDeclaredMethod("addFileToTable", File.class);
        addFileToTableMethod.setAccessible(true);

        // Setup mock table and items
        ObservableList<BatchProcessingView.FileItem> items = FXCollections.observableArrayList();
        when(mockTableView.getItems()).thenReturn(items);

        // Create a temporary file for testing
        File tempFile = File.createTempFile("test", ".txt");
        tempFile.deleteOnExit();

        // Write some content to the file to set its size
        Files.writeString(tempFile.toPath(), "Test content");

        // Call the method
        Boolean result = (Boolean) addFileToTableMethod.invoke(batchProcessingView, tempFile);

        // Verify result and interactions
        assertTrue(result, "Should return true for successfully added file");
        assertEquals(1, items.size(), "Should add one item to the table");
        assertEquals(tempFile.getName(), items.get(0).getName(), "File name should match");
        assertEquals(tempFile.getAbsolutePath(), items.get(0).getPath(), "File path should match");
        assertEquals("Ready", items.get(0).getStatus(), "Initial status should be 'Ready'");
    }

    @Test
    public void testSaveResultToFile() throws Exception {
        // Get access to the private method using reflection
        Method saveResultToFileMethod = BatchProcessingView.class.getDeclaredMethod(
                "saveResultToFile", File.class, String.class, String.class);
        saveResultToFileMethod.setAccessible(true);

        // Setup temporary directory as output directory
        File tempOutputDir = new File(System.getProperty("java.io.tmpdir"));
        setField("outputDirectory", tempOutputDir);

        // Create a temporary input file
        File tempInputFile = File.createTempFile("input", ".txt");
        tempInputFile.deleteOnExit();

        // Define output filename and content
        String outputFilename = "test_output.txt";
        String resultText = "Test result content";

        // Call the method
        saveResultToFileMethod.invoke(batchProcessingView, tempInputFile, outputFilename, resultText);

        // Verify file was created with correct content
        File outputFile = new File(tempOutputDir, outputFilename);
        outputFile.deleteOnExit();

        assertTrue(outputFile.exists(), "Output file should be created");
        String fileContent = Files.readString(outputFile.toPath());
        assertEquals(resultText, fileContent, "File content should match the result text");
    }

    @Test
    public void testConvertToJson() throws Exception {
        // Get access to the private method using reflection
        Method convertToJsonMethod = BatchProcessingView.class.getDeclaredMethod(
                "convertToJson", java.util.Map.class);
        convertToJsonMethod.setAccessible(true);

        // Create test data
        java.util.Map<String, Object> testMap = new java.util.HashMap<>();
        testMap.put("stringValue", "test");
        testMap.put("booleanValue", true);
        testMap.put("numericValue", 123);

        // Call the method
        String jsonResult = (String) convertToJsonMethod.invoke(batchProcessingView, testMap);

        // Verify JSON structure (basic validation)
        assertTrue(jsonResult.startsWith("{"), "JSON should start with opening brace");
        assertTrue(jsonResult.endsWith("}"), "JSON should end with closing brace");
        assertTrue(jsonResult.contains("\"stringValue\": \"test\""),
                "JSON should contain string value properly formatted");
        assertTrue(jsonResult.contains("\"booleanValue\": true"),
                "JSON should contain boolean value properly formatted");
    }

    @Test
    public void testHandleProcessBatch_EmptyFileList() {
        // Setup empty file list
        ObservableList<BatchProcessingView.FileItem> emptyItems = FXCollections.observableArrayList();
        when(mockTableView.getItems()).thenReturn(emptyItems);

        // Call the method
        batchProcessingView.handleProcessBatch();

        // Verify status update for empty list
        verify(mockStatusLabel).setText("No files selected for processing");
        verify(mockProcessButton, never()).setDisable(true);
    }

    @Test
    public void testHandleProcessBatch_NoOperationSelected() {
        // Setup non-empty file list but null operation
        ObservableList<BatchProcessingView.FileItem> nonEmptyItems = FXCollections.observableArrayList(
                new BatchProcessingView.FileItem("test.txt", "1KB", "/path/to/test.txt", "Ready")
        );
        when(mockTableView.getItems()).thenReturn(nonEmptyItems);
        when(mockOperationComboBox.getSelectionModel()).thenReturn(mock(SingleSelectionModel.class));
        when(mockOperationComboBox.getSelectionModel().getSelectedItem()).thenReturn(null);

        // Call the method
        batchProcessingView.handleProcessBatch();

        // Verify status update for no operation selected
        verify(mockStatusLabel).setText("Please select an operation");
    }
}