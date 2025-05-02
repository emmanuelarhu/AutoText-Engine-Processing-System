package main.java.com.autotextengine.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import java.util.*;

import main.java.com.autotextengine.model.TextData;
import main.java.com.autotextengine.model.ProcessedResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileController {

    @FXML private TextField inputDirectoryField;
    @FXML private TextField outputDirectoryField;
    @FXML private TextField fileExtensionsField;
    @FXML private TextField fileNamePatternField;
    @FXML private CheckBox includeSubdirectoriesCheckBox;
    @FXML private Spinner<Integer> minFileSizeSpinner;
    @FXML private Spinner<Integer> maxFileSizeSpinner;
    @FXML private TableView<File> filesTableView;
    @FXML private TableColumn<File, String> fileNameColumn;
    @FXML private TableColumn<File, String> fileSizeColumn;
    @FXML private TableColumn<File, String> filePathColumn;
    @FXML private TableColumn<File, String> fileStatusColumn;
    @FXML private ComboBox<String> operationComboBox;
    @FXML private ComboBox<String> outputFormatComboBox;
    @FXML private CheckBox overwriteExistingCheckBox;
    @FXML private Button stopButton;
    @FXML private ProgressBar processingProgressBar;
    @FXML private Label processingStatusLabel;
    @FXML private Label filesProcessedLabel;

    private Stage stage;

    public void initialize() {
        // Initialize spinners with default values
        SpinnerValueFactory.IntegerSpinnerValueFactory minSizeValueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, 0);
        minFileSizeSpinner.setValueFactory(minSizeValueFactory);

        SpinnerValueFactory.IntegerSpinnerValueFactory maxSizeValueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, 1024);
        maxFileSizeSpinner.setValueFactory(maxSizeValueFactory);

        // Initialize combo boxes
        operationComboBox.getItems().addAll(
                "Text Extraction",
                "Format Conversion",
                "Data Analysis",
                "Content Filtering"
        );

        outputFormatComboBox.getItems().addAll(
                "Text (TXT)",
                "Comma Separated (CSV)",
                "JSON",
                "XML"
        );

        // Configure table columns
        fileNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getName()));

        fileSizeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatFileSize(cellData.getValue().length())));

        filePathColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getAbsolutePath()));

        fileStatusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty("Ready"));
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void handleBrowseInputDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Input Directory");

        File selectedDirectory = directoryChooser.showDialog(stage);
        if (selectedDirectory != null) {
            inputDirectoryField.setText(selectedDirectory.getAbsolutePath());
            loadFilesFromDirectory(selectedDirectory);
        }
    }

    @FXML
    public void handleBrowseOutputDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Output Directory");

        File selectedDirectory = directoryChooser.showDialog(stage);
        if (selectedDirectory != null) {
            outputDirectoryField.setText(selectedDirectory.getAbsolutePath());
        }
    }

    @FXML
    public void handleRefreshFiles() {
        String inputDir = inputDirectoryField.getText();
        if (inputDir != null && !inputDir.isEmpty()) {
            loadFilesFromDirectory(new File(inputDir));
        }
    }

    @FXML
    public void handleSelectAllFiles() {
        filesTableView.getSelectionModel().selectAll();
    }

    @FXML
    public void handleClearSelection() {
        filesTableView.getSelectionModel().clearSelection();
    }

    @FXML
    public void handleRemoveSelectedFiles() {
        List<File> selectedFiles = filesTableView.getSelectionModel().getSelectedItems();
        filesTableView.getItems().removeAll(selectedFiles);
    }

    @FXML
    public void handleAddFiles() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Files");

        // Set file extensions filter based on input
        String extensions = fileExtensionsField.getText();
        if (extensions != null && !extensions.isEmpty()) {
            String[] exts = extensions.split(",");
            for (String ext : exts) {
                ext = ext.trim();
                if (!ext.isEmpty()) {
                    fileChooser.getExtensionFilters().add(
                            new FileChooser.ExtensionFilter(ext.toUpperCase() + " Files", "*." + ext));
                }
            }
        }
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("All Files", "*.*"));

        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(stage);
        if (selectedFiles != null) {
            filesTableView.getItems().addAll(selectedFiles);
        }
    }

    @FXML
    public void handleStartProcessing() {
        if (filesTableView.getItems().isEmpty()) {
            showAlert("No files to process", "Please add files to the list first.");
            return;
        }

        String operation = operationComboBox.getValue();
        if (operation == null || operation.isEmpty()) {
            showAlert("No operation selected", "Please select a processing operation.");
            return;
        }

        // In a real application, this would start a background task
        processingStatusLabel.setText("Processing...");
        processingProgressBar.setProgress(0.0);
        stopButton.setDisable(false);

        // Simulate processing
        // In a real app, you would use Task or Service for background processing
        new Thread(() -> {
            int totalFiles = filesTableView.getItems().size();
            for (int i = 0; i < totalFiles; i++) {
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }

                final int fileIndex = i;
                final double progress = (double) (i + 1) / totalFiles;

                // Update UI on JavaFX application thread
                Platform.runLater(() -> {
                    processingProgressBar.setProgress(progress);
                    filesProcessedLabel.setText((fileIndex + 1) + "/" + totalFiles);

                    // Update status in table
                    File file = filesTableView.getItems().get(fileIndex);
                    fileStatusColumn.setCellValueFactory(cellData -> {
                        if (cellData.getValue().equals(file)) {
                            return new SimpleStringProperty("Processed");
                        }
                        return new SimpleStringProperty("Ready");
                    });
                });

                try {
                    Thread.sleep(500); // Simulate processing time
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            Platform.runLater(() -> {
                processingStatusLabel.setText("Complete");
                stopButton.setDisable(true);
            });
        }).start();
    }

    @FXML
    public void handleStopProcessing() {
        // In a real app, this would stop the background task
        processingStatusLabel.setText("Stopped");
        processingProgressBar.setProgress(0.0);
        stopButton.setDisable(true);
    }

    @FXML
    public void handleSaveConfiguration() {
        // In a real app, this would save the current configuration to a file
        showAlert("Configuration Saved", "Your processing configuration has been saved.");
    }

    @FXML
    public void handleLoadConfiguration() {
        // In a real app, this would load a configuration from a file
        showAlert("Configuration Loaded", "Processing configuration has been loaded.");
    }

    private void loadFilesFromDirectory(File directory) {
        filesTableView.getItems().clear();

        if (directory.isDirectory()) {
            File[] files = directory.listFiles(file -> {
                // Apply filters
                if (file.isDirectory() && includeSubdirectoriesCheckBox.isSelected()) {
                    return true;
                }

                // Check extensions
                String extensions = fileExtensionsField.getText();
                if (extensions != null && !extensions.isEmpty()) {
                    String fileName = file.getName().toLowerCase();
                    String[] exts = extensions.split(",");
                    boolean matched = false;
                    for (String ext : exts) {
                        ext = ext.trim().toLowerCase();
                        if (!ext.isEmpty() && fileName.endsWith("." + ext)) {
                            matched = true;
                            break;
                        }
                    }
                    if (!matched) {
                        return false;
                    }
                }

                // Check name pattern
                String pattern = fileNamePatternField.getText();
                if (pattern != null && !pattern.isEmpty()) {
                    // Convert glob pattern to regex
                    pattern = pattern.replace(".", "\\.").replace("*", ".*").replace("?", ".");
                    if (!file.getName().matches(pattern)) {
                        return false;
                    }
                }

                // Check file size
                long minSize = minFileSizeSpinner.getValue() * 1024L;
                long maxSize = maxFileSizeSpinner.getValue() * 1024L;
                long fileSize = file.length();

                if (minSize > 0 && fileSize < minSize) {
                    return false;
                }

                if (maxSize > 0 && fileSize > maxSize) {
                    return false;
                }

                return true;
            });

            if (files != null) {
                filesTableView.getItems().addAll(files);
            }
        }
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static final Logger logger = LogManager.getLogger(FileController.class);
    /**
     * Process a file with the specified operation
     *
     * @param textData The text data to process
     * @param operation The operation to perform
     * @return The processed result
     */
    public ProcessedResult processFile(TextData textData, String operation) {
        logger.info("Processing file with operation: {}", operation);

        switch (operation) {
            case "Character Count":
                return new ProcessedResult(countCharacters(textData));
            case "Word Count":
                return new ProcessedResult(countWords(textData));
            case "Line Count":
                return new ProcessedResult(countLines(textData));
            case "Sentence Count":
                return new ProcessedResult(countSentences(textData));
            case "Convert to Uppercase":
                return new ProcessedResult(textData.getContent().toUpperCase());
            case "Convert to Lowercase":
                return new ProcessedResult(textData.getContent().toLowerCase());
            case "Format JSON":
                return new ProcessedResult(formatJson(textData.getContent()));
            case "Format XML":
                return new ProcessedResult(formatXml(textData.getContent()));
            case "Extract Emails":
                return new ProcessedResult(extractEmails(textData.getContent()));
            case "Extract URLs":
                return new ProcessedResult(extractUrls(textData.getContent()));
            case "Remove Duplicate Lines":
                return new ProcessedResult(removeDuplicateLines(textData.getContent()));
            case "Sort Lines":
                return new ProcessedResult(sortLines(textData.getContent()));
            case "Remove Extra Whitespace":
                return new ProcessedResult(removeExtraWhitespace(textData.getContent()));
            default:
                logger.warn("Unknown operation: {}", operation);
                return new ProcessedResult("Unknown operation: " + operation);
        }
    }

    /**
     * Count the number of characters in the text
     */
    private long countCharacters(TextData textData) {
        return textData.getContent().length();
    }

    /**
     * Count the number of words in the text
     */
    private long countWords(TextData textData) {
        String content = textData.getContent().trim();
        if (content.isEmpty()) {
            return 0;
        }
        return content.split("\\s+").length;
    }

    /**
     * Count the number of lines in the text
     */
    private long countLines(TextData textData) {
        String content = textData.getContent();
        if (content.isEmpty()) {
            return 0;
        }
        return content.split("\\r?\\n").length;
    }

    /**
     * Count the number of sentences in the text
     */
    private long countSentences(TextData textData) {
        String content = textData.getContent().trim();
        if (content.isEmpty()) {
            return 0;
        }
        // Simple sentence counting - split by period, exclamation, question mark
        return content.split("[.!?]+").length;
    }

    /**
     * Format JSON string
     */
    private String formatJson(String json) {
        try {
            // This is a simplified implementation
            // In a real app, you'd use a JSON library
            StringBuilder formatted = new StringBuilder();
            int indentLevel = 0;
            boolean inQuotes = false;

            for (char c : json.toCharArray()) {
                if (c == '\"' && (formatted.length() == 0 || formatted.charAt(formatted.length() - 1) != '\\')) {
                    inQuotes = !inQuotes;
                    formatted.append(c);
                } else if (!inQuotes) {
                    if (c == '{' || c == '[') {
                        formatted.append(c).append("\n");
                        indentLevel++;
                        for (int i = 0; i < indentLevel; i++) {
                            formatted.append("  ");
                        }
                    } else if (c == '}' || c == ']') {
                        formatted.append("\n");
                        indentLevel--;
                        for (int i = 0; i < indentLevel; i++) {
                            formatted.append("  ");
                        }
                        formatted.append(c);
                    } else if (c == ',') {
                        formatted.append(c).append("\n");
                        for (int i = 0; i < indentLevel; i++) {
                            formatted.append("  ");
                        }
                    } else if (c == ':') {
                        formatted.append(c).append(" ");
                    } else if (!Character.isWhitespace(c)) {
                        formatted.append(c);
                    }
                } else {
                    formatted.append(c);
                }
            }

            return formatted.toString();
        } catch (Exception e) {
            logger.error("Error formatting JSON", e);
            return json;
        }
    }

    /**
     * Format XML string
     */
    private String formatXml(String xml) {
        try {
            // This is a simplified implementation
            // In a real app, you'd use an XML library
            StringBuilder formatted = new StringBuilder();
            int indentLevel = 0;
            boolean inTag = false;
            boolean inContent = false;

            for (int i = 0; i < xml.length(); i++) {
                char c = xml.charAt(i);

                if (c == '<') {
                    // Check if it's a closing tag
                    if (i + 1 < xml.length() && xml.charAt(i + 1) == '/') {
                        indentLevel--;
                    }

                    if (inContent) {
                        inContent = false;
                    } else {
                        formatted.append('\n');
                        for (int j = 0; j < indentLevel; j++) {
                            formatted.append("  ");
                        }
                    }

                    inTag = true;
                    formatted.append(c);
                } else if (c == '>') {
                    formatted.append(c);
                    inTag = false;

                    // Check if it's a self-closing tag
                    if (i > 0 && xml.charAt(i - 1) != '/') {
                        inContent = true;
                    }

                    // Check if it's an opening tag (not self-closing and not a closing tag)
                    if (i > 0 && xml.charAt(i - 1) != '/' && (i < 2 || xml.charAt(i - 2) != '/')) {
                        indentLevel++;
                    }
                } else {
                    formatted.append(c);
                }
            }

            return formatted.toString();
        } catch (Exception e) {
            logger.error("Error formatting XML", e);
            return xml;
        }
    }

    /**
     * Extract email addresses from text
     */
    private List<String> extractEmails(String text) {
        List<String> emails = new ArrayList<>();
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
        java.util.regex.Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            emails.add(matcher.group());
        }

        return emails;
    }

    /**
     * Extract URLs from text
     */
    private List<String> extractUrls(String text) {
        List<String> urls = new ArrayList<>();
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "(https?|ftp)://[^\\s/$.?#].[^\\s]*");
        java.util.regex.Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            urls.add(matcher.group());
        }

        return urls;
    }

    /**
     * Remove duplicate lines from text
     */
    private String removeDuplicateLines(String text) {
        Set<String> uniqueLines = new LinkedHashSet<>();
        String[] lines = text.split("\\r?\\n");

        for (String line : lines) {
            uniqueLines.add(line);
        }

        return String.join("\n", uniqueLines);
    }

    /**
     * Sort lines alphabetically
     */
    private String sortLines(String text) {
        String[] lines = text.split("\\r?\\n");
        Arrays.sort(lines);
        return String.join("\n", lines);
    }

    /**
     * Remove extra whitespace from text
     */
    private String removeExtraWhitespace(String text) {
        // Replace multiple spaces with single space
        String result = text.replaceAll("[ \\t]+", " ");
        // Replace multiple newlines with single newline
        result = result.replaceAll("\\n\\s*\\n+", "\n\n");
        return result;
    }
}