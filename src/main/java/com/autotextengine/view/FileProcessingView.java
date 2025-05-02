package main.java.com.autotextengine.view;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import main.java.com.autotextengine.model.TextData;
import main.java.com.autotextengine.service.TextProcessingService;

public class FileProcessingView {
    private static final Logger logger = LogManager.getLogger(FileProcessingView.class);

    private final TextProcessingService textProcessingService = new TextProcessingService();

    @FXML private TextArea inputTextArea;
    @FXML private TextArea outputTextArea;
    @FXML private TextField filePathField;
    @FXML private Label statusLabel;
    @FXML private ComboBox<String> operationComboBox; // Added for operation selection

    public void initialize() {
        statusLabel.setText("Ready");

        // Initialize the operation dropdown if it exists
        if (operationComboBox != null) {
            operationComboBox.setItems(FXCollections.observableArrayList(
                    "Character Count",
                    "Word Count",
                    "Line Count",
                    "Sentence Count",
                    "Convert to Uppercase",
                    "Convert to Lowercase",
                    "Sort Lines",
                    "Remove Duplicate Lines",
                    "Remove Extra Whitespace",
                    "Extract Emails",
                    "Extract URLs"
            ));
            operationComboBox.getSelectionModel().selectFirst();
        } else {
            logger.warn("Operation ComboBox not found in FXML - basic echo functionality will be used");
        }
    }

    @FXML
    public void handleClearInput() {
        inputTextArea.clear();
        updateStatus("Input cleared");
    }

    @FXML
    public void handleClearOutput() {
        outputTextArea.clear();
        updateStatus("Output cleared");
    }

    @FXML
    public void handleBrowse() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Text File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            filePathField.setText(selectedFile.getAbsolutePath());
            updateStatus("File selected: " + selectedFile.getName());
        }
    }

    @FXML
    public void handleLoadFile() {
        String filePath = filePathField.getText();
        if (filePath != null && !filePath.isEmpty()) {
            try {
                String content = Files.readString(new File(filePath).toPath());
                inputTextArea.setText(content);
                updateStatus("File loaded successfully");
            } catch (IOException e) {
                logger.error("Error loading file", e);
                updateStatus("Error loading file: " + e.getMessage());
                showErrorAlert("File Load Error", "Could not load the selected file", e.getMessage());
            }
        } else {
            updateStatus("No file selected");
        }
    }

    @FXML
    public void handleSaveFile() {
        if (outputTextArea.getText().isEmpty()) {
            updateStatus("No content to save");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Text File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                Files.writeString(file.toPath(), outputTextArea.getText());
                updateStatus("File saved successfully");
            } catch (IOException e) {
                logger.error("Error saving file", e);
                updateStatus("Error saving file: " + e.getMessage());
                showErrorAlert("File Save Error", "Could not save to file", e.getMessage());
            }
        }
    }

    @FXML
    public void handleProcessFile() {
        String inputText = inputTextArea.getText();
        if (inputText == null || inputText.isEmpty()) {
            updateStatus("No input to process");
            return;
        }

        // Check if we have the operation ComboBox
        String operation = operationComboBox != null ?
                operationComboBox.getValue() : "Echo";

        try {
            String result;
            TextData textData = new TextData(inputText);

            // Process based on selected operation
            switch (operation) {
                case "Character Count":
                    result = "Total characters: " + textProcessingService.countCharacters(textData);
                    break;
                case "Word Count":
                    result = "Total words: " + textProcessingService.countWords(textData);
                    break;
                case "Line Count":
                    result = "Total lines: " + textData.getLineCount();
                    break;
                case "Sentence Count":
                    result = "Total sentences: " + textProcessingService.countSentences(textData);
                    break;
                case "Convert to Uppercase":
                    result = textProcessingService.transformCase(textData, "UPPER");
                    break;
                case "Convert to Lowercase":
                    result = textProcessingService.transformCase(textData, "LOWER");
                    break;
                case "Sort Lines":
                    // This requires additional implementation in TextProcessingService
                    result = sortLines(inputText);
                    break;
                case "Remove Duplicate Lines":
                    // This requires additional implementation in TextProcessingService
                    result = removeDuplicateLines(inputText);
                    break;
                case "Remove Extra Whitespace":
                    // This requires additional implementation in TextProcessingService
                    result = removeExtraWhitespace(inputText);
                    break;
                case "Extract Emails":
                    // Use regex service or implement extraction
                    result = extractPatterns(inputText, "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
                    break;
                case "Extract URLs":
                    // Use regex service or implement extraction
                    result = extractPatterns(inputText, "https?://[^\\s/$.?#].[^\\s]*");
                    break;
                case "Echo":
                default:
                    // Simple echo when no operation ComboBox is available
                    result = inputText;
                    break;
            }

            outputTextArea.setText(result);
            updateStatus("Processing complete: " + operation);

        } catch (Exception e) {
            logger.error("Error processing text", e);
            updateStatus("Error processing: " + e.getMessage());
            showErrorAlert("Processing Error", "Failed to process text", e.getMessage());
        }
    }

    // Helper methods for text processing
    private String sortLines(String text) {
        String[] lines = text.split("\\r?\\n");
        java.util.Arrays.sort(lines);
        return String.join("\n", lines);
    }

// Enhanced text processing operations for FileProcessingView

    /**
     * Remove duplicate lines from text while preserving order
     *
     * @param text The input text
     * @return Text with duplicate lines removed
     */
    private String removeDuplicateLines(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        String[] lines = text.split("\\r?\\n");
        java.util.List<String> uniqueLines = new java.util.ArrayList<>();
        java.util.Set<String> seenLines = new java.util.HashSet<>();

        for (String line : lines) {
            if (seenLines.add(line)) {  // Set.add() returns true if the element was added (wasn't already in the set)
                uniqueLines.add(line);
            }
        }

        return String.join("\n", uniqueLines);
    }

    /**
     * Remove extra whitespace from text
     * - Replaces multiple spaces/tabs with a single space
     * - Normalizes multiple consecutive blank lines to a single blank line
     *
     * @param text The input text
     * @return Text with extra whitespace removed
     */
    private String removeExtraWhitespace(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        // Step 1: Replace sequences of spaces and tabs with a single space
        String result = text.replaceAll("[ \\t]+", " ");

        // Step 2: Replace runs of multiple blank lines with a single blank line
        // This regex matches a newline followed by optional whitespace and one or more newlines
        result = result.replaceAll("\\n\\s*\\n+", "\n\n");

        // Step 3: Trim leading/trailing whitespace
        return result.trim();
    }

    private String extractPatterns(String text, String regexPattern) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regexPattern);
        java.util.regex.Matcher matcher = pattern.matcher(text);

        java.util.Set<String> matches = new java.util.HashSet<>();
        while (matcher.find()) {
            matches.add(matcher.group());
        }

        return "Found " + matches.size() + " matches:\n\n" +
                String.join("\n", matches);
    }

    private void updateStatus(String message) {
        statusLabel.setText(message);
        logger.info(message);
    }

    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}