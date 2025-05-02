package main.java.com.autotextengine.view;

import main.java.com.autotextengine.controller.FileController;
import main.java.com.autotextengine.model.ProcessedResult;
import main.java.com.autotextengine.model.TextData;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * View controller for batch file processing operations
 */
public class BatchProcessingView {
    private static final Logger logger = LogManager.getLogger(BatchProcessingView.class);

    private final FileController fileController = new FileController();

    // Add processing state tracker
    private AtomicBoolean processingInProgress = new AtomicBoolean(false);

    // UI Components matching the FXML file exactly
    @FXML private TextField inputDirectoryField;
    @FXML private TextField fileExtensionsField;
    @FXML private CheckBox includeSubdirectoriesCheckBox;
    @FXML private TextField fileNamePatternField;
    @FXML private Spinner<Integer> minFileSizeSpinner;
    @FXML private Spinner<Integer> maxFileSizeSpinner;
    @FXML private TableView<FileItem> filesTableView;
    @FXML private TableColumn<FileItem, String> fileNameColumn;
    @FXML private TableColumn<FileItem, String> fileSizeColumn;
    @FXML private TableColumn<FileItem, String> filePathColumn;
    @FXML private TableColumn<FileItem, String> fileStatusColumn;
    @FXML private Button addFilesButton;
    @FXML private Button removeFileButton;
    @FXML private Button clearFilesButton;
    @FXML private ComboBox<String> operationComboBox;
    @FXML private Button processButton;
    @FXML private TextField outputDirectoryField;
    @FXML private Button browseOutputDirButton;
    @FXML private ComboBox<String> outputFormatComboBox;
    @FXML private CheckBox createSeparateFilesCheckBox;
    @FXML private Button stopButton;
    @FXML private ProgressBar processingProgressBar;
    @FXML private Label statusLabel;
    @FXML private Label filesProcessedLabel;

    private List<File> selectedFiles = new ArrayList<>();
    private File outputDirectory;

    @FXML
    public void initialize() {
        try {
            // Initialize operations combo box
            operationComboBox.setItems(FXCollections.observableArrayList(
                    "Character Count", "Word Count", "Line Count", "Sentence Count",
                    "Convert to Uppercase", "Convert to Lowercase", "Format JSON",
                    "Format XML", "Extract Emails", "Extract URLs",
                    "Remove Duplicate Lines", "Sort Lines", "Remove Extra Whitespace"
            ));
            operationComboBox.getSelectionModel().selectFirst();

            // Initialize output format combo box
            outputFormatComboBox.setItems(FXCollections.observableArrayList(
                    "Same as Input", "Append Operation", "Add Timestamp"
            ));
            outputFormatComboBox.getSelectionModel().selectFirst();

            // Initialize table columns for filesTableView
            fileNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            fileSizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));
            filePathColumn.setCellValueFactory(new PropertyValueFactory<>("path"));
            fileStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

            // Initialize filesTableView with an empty observable list
            filesTableView.setItems(FXCollections.observableArrayList());

            // Initialize spinner values
            SpinnerValueFactory<Integer> minSizeValueFactory =
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1000000, 0);
            minFileSizeSpinner.setValueFactory(minSizeValueFactory);

            SpinnerValueFactory<Integer> maxSizeValueFactory =
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1000000, 1000000);
            maxFileSizeSpinner.setValueFactory(maxSizeValueFactory);

            // Initialize progress bar
            processingProgressBar.setProgress(0);

            // Set default output directory to user home
            String userHome = System.getProperty("user.home");
            outputDirectoryField.setText(userHome);
            outputDirectory = new File(userHome);

            // Initialize stop button to be disabled
            stopButton.setDisable(true);

            // Initialize buttons state
            updateControlsState();

            // Set status label
            statusLabel.setText("Ready");

            // Initialize filesProcessedLabel
            filesProcessedLabel.setText("0/0");

            logger.info("BatchProcessingView initialized");
        } catch (Exception e) {
            logger.error("Error initializing BatchProcessingView", e);
            if (statusLabel != null) {
                statusLabel.setText("Error initializing: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleAddFiles() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Files to Process");

        // Set up file filters based on extensions in the text field
        String extensions = fileExtensionsField.getText();
        if (extensions != null && !extensions.isEmpty()) {
            List<FileChooser.ExtensionFilter> filters = new ArrayList<>();
            for (String ext : extensions.split(",")) {
                ext = ext.trim();
                if (!ext.isEmpty()) {
                    if (!ext.startsWith(".")) {
                        ext = "." + ext;
                    }
                    filters.add(new FileChooser.ExtensionFilter(ext.toUpperCase() + " Files", "*" + ext));
                }
            }

            if (!filters.isEmpty()) {
                fileChooser.getExtensionFilters().addAll(filters);
            }
        }

        // Always add All Files filter
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("All Files", "*.*"));

        List<File> files = fileChooser.showOpenMultipleDialog(addFilesButton.getScene().getWindow());
        if (files != null && !files.isEmpty()) {
            int addedCount = 0;
            for (File file : files) {
                if (addFileToTable(file)) {
                    addedCount++;
                }
            }
            updateControlsState();
            statusLabel.setText("Added " + addedCount + " files");
        }
    }

    private boolean addFileToTable(File file) {
        // Check if the file is already in the table
        for (FileItem item : filesTableView.getItems()) {
            if (item.getPath().equals(file.getAbsolutePath())) {
                return false; // Skip duplicate files
            }
        }

        // Check file size constraints
        long minSize = minFileSizeSpinner.getValue() * 1024L; // Convert KB to bytes
        long maxSize = maxFileSizeSpinner.getValue() * 1024L; // Convert KB to bytes
        long fileSize = file.length();

        if ((minSize > 0 && fileSize < minSize) || (maxSize > 0 && fileSize > maxSize)) {
            return false; // Skip files that don't meet size criteria
        }

        // Check file name pattern if specified
        String pattern = fileNamePatternField.getText();
        if (pattern != null && !pattern.isEmpty()) {
            // Convert glob pattern to regex
            String regex = pattern.replace(".", "\\.").replace("*", ".*").replace("?", ".");
            if (!file.getName().matches(regex)) {
                return false; // Skip files that don't match pattern
            }
        }

        // Add file to the table
        FileItem item = new FileItem(
                file.getName(),
                formatFileSize(file.length()),
                file.getAbsolutePath(),
                "Ready"
        );
        filesTableView.getItems().add(item);

        // Also add to selectedFiles list for backward compatibility
        selectedFiles.add(file);

        return true;
    }

    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.1f GB", size / (1024.0 * 1024 * 1024));
        }
    }

    @FXML
    public void handleAddDirectory() {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Select Directory with Files to Process");

        File selectedDir = dirChooser.showDialog(addFilesButton.getScene().getWindow());
        if (selectedDir != null) {
            inputDirectoryField.setText(selectedDir.getAbsolutePath());
            loadFilesFromDirectory(selectedDir);
        }
    }

    private void loadFilesFromDirectory(File directory) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            updateStatus("Invalid directory");
            return;
        }

        updateStatus("Scanning directory: " + directory.getAbsolutePath());

        try {
            // Get file filters
            String extensions = fileExtensionsField.getText();
            String namePattern = fileNamePatternField.getText();
            boolean includeSubdirs = includeSubdirectoriesCheckBox.isSelected();
            long minSize = minFileSizeSpinner.getValue() * 1024L; // Convert KB to bytes
            long maxSize = maxFileSizeSpinner.getValue() * 1024L; // Convert KB to bytes

            // Create regex for name pattern if specified
            Pattern nameRegex = null;
            if (namePattern != null && !namePattern.isEmpty()) {
                // Convert glob pattern to regex
                String regex = namePattern.replace(".", "\\.").replace("*", ".*").replace("?", ".");
                nameRegex = Pattern.compile(regex);
            }

            // Create extension list
            List<String> extensionList = new ArrayList<>();
            if (extensions != null && !extensions.isEmpty()) {
                for (String ext : extensions.split(",")) {
                    ext = ext.trim().toLowerCase();
                    if (!ext.isEmpty()) {
                        if (ext.startsWith(".")) {
                            ext = ext.substring(1);
                        }
                        extensionList.add(ext);
                    }
                }
            }

            // Define a recursive file finder
            List<File> matchingFiles = new ArrayList<>();
            findMatchingFiles(directory, extensionList, nameRegex, minSize, maxSize, includeSubdirs, matchingFiles);

            // Add files to table
            int addedCount = 0;
            if (!matchingFiles.isEmpty()) {
                for (File file : matchingFiles) {
                    if (addFileToTable(file)) {
                        addedCount++;
                    }
                }
                updateStatus("Found and added " + addedCount + " files");
            } else {
                updateStatus("No matching files found");
            }

            // Update controls
            updateControlsState();

        } catch (Exception e) {
            logger.error("Error loading files", e);
            updateStatus("Error loading files: " + e.getMessage());
            showErrorAlert("Directory Error", "Could not load files from directory", e.getMessage());
        }
    }

    private void findMatchingFiles(File directory, List<String> extensions, Pattern nameRegex,
                                   long minSize, long maxSize, boolean includeSubdirs,
                                   List<File> matchingFiles) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                if (includeSubdirs) {
                    findMatchingFiles(file, extensions, nameRegex, minSize, maxSize, true, matchingFiles);
                }
            } else if (file.isFile()) {
                // Check file size
                long fileSize = file.length();
                if (minSize > 0 && fileSize < minSize) {
                    continue;
                }
                if (maxSize > 0 && fileSize > maxSize) {
                    continue;
                }

                // Check file extension
                String fileName = file.getName().toLowerCase();
                boolean extensionMatched = extensions.isEmpty();

                for (String ext : extensions) {
                    if (fileName.endsWith("." + ext)) {
                        extensionMatched = true;
                        break;
                    }
                }

                if (!extensionMatched) {
                    continue;
                }

                // Check name pattern
                if (nameRegex != null && !nameRegex.matcher(fileName).matches()) {
                    continue;
                }

                // All checks passed, add to results
                matchingFiles.add(file);
            }
        }
    }

    private String showExtensionFilterDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Filter Files");
        dialog.setHeaderText("Select file extension to filter (leave empty for all files)");

        // Set the button types
        ButtonType applyButtonType = new ButtonType("Apply", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(applyButtonType, ButtonType.CANCEL);

        // Create a ComboBox with common extensions
        ComboBox<String> extensionCombo = new ComboBox<>();
        extensionCombo.setEditable(true);
        extensionCombo.setItems(FXCollections.observableArrayList(
                "", ".txt", ".json", ".xml", ".csv", ".log", ".md", ".html"
        ));
        extensionCombo.getSelectionModel().selectFirst();

        dialog.getDialogPane().setContent(extensionCombo);

        // Convert the result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == applyButtonType) {
                return extensionCombo.getValue();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    @FXML
    public void handleRemoveFile() {
        FileItem selectedItem = filesTableView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            // Remove from table
            filesTableView.getItems().remove(selectedItem);

            // Also remove from selectedFiles list
            selectedFiles.removeIf(file -> file.getAbsolutePath().equals(selectedItem.getPath()));

            updateControlsState();
            statusLabel.setText("Removed file from list");
        } else {
            statusLabel.setText("No file selected for removal");
        }
    }

    @FXML
    public void handleClearFiles() {
        filesTableView.getItems().clear();
        selectedFiles.clear();
        updateControlsState();
        statusLabel.setText("Cleared file list");
    }

    @FXML
    public void handleBrowseOutputDir() {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Select Output Directory");

        if (outputDirectory != null && outputDirectory.exists()) {
            dirChooser.setInitialDirectory(outputDirectory);
        }

        File selectedDir = dirChooser.showDialog(browseOutputDirButton.getScene().getWindow());
        if (selectedDir != null) {
            outputDirectory = selectedDir;
            outputDirectoryField.setText(selectedDir.getAbsolutePath());
        }
    }

    @FXML
    public void handleStartProcessing() {
        // Call the process batch method
        handleProcessBatch();
    }

    @FXML
    public void handleProcessBatch() {
        ObservableList<FileItem> fileItems = filesTableView.getItems();
        if (fileItems.isEmpty()) {
            statusLabel.setText("No files selected for processing");
            return;
        }

        String operation = operationComboBox.getSelectionModel().getSelectedItem();
        if (operation == null) {
            statusLabel.setText("Please select an operation");
            return;
        }

        // Validate output directory
        if (outputDirectory == null || !outputDirectory.exists()) {
            String outputDirPath = outputDirectoryField.getText();
            if (outputDirPath != null && !outputDirPath.isEmpty()) {
                outputDirectory = new File(outputDirPath);
                if (!outputDirectory.exists()) {
                    boolean created = outputDirectory.mkdirs();
                    if (!created) {
                        statusLabel.setText("Failed to create output directory");
                        return;
                    }
                }
            } else {
                statusLabel.setText("Please select an output directory");
                return;
            }
        }

        // Set processing state
        processingInProgress.set(true);

        // Set up UI for processing
        processingProgressBar.setProgress(0);
        for (FileItem item : fileItems) {
            item.setStatus("Pending");
        }
        filesTableView.refresh();

        // Update UI controls state
        updateControlsState();

        // Setup progress tracking
        AtomicInteger processedCount = new AtomicInteger(0);
        int totalFiles = fileItems.size();
        filesProcessedLabel.setText("0/" + totalFiles);
        statusLabel.setText("Processing batch...");

        // Create AtomicBoolean to track cancellation
        final AtomicBoolean stopRequested = new AtomicBoolean(false);

        // Setup stop button to set flag
        stopButton.setOnAction(e -> {
            stopRequested.set(true);
            statusLabel.setText("Stopping processing...");
        });

        // Process each file in a background thread
        new Thread(() -> {
            int successCount = 0;
            int errorCount = 0;

            for (FileItem item : fileItems) {
                // Check if processing was stopped
                if (stopRequested.get()) {
                    break;
                }

                try {
                    // Update UI to show current file status
                    Platform.runLater(() -> {
                        item.setStatus("Processing...");
                        filesTableView.refresh();
                    });

                    // Process the file
                    File file = new File(item.getPath());
                    String content = Files.readString(file.toPath());

                    // Process content based on operation
                    String resultText;
                    switch (operation) {
                        case "Character Count": {
                            long count = content.length();
                            resultText = "Character Count: " + count;
                            break;
                        }
                        case "Word Count": {
                            String[] words = content.split("\\s+");
                            resultText = "Word Count: " + words.length;
                            break;
                        }
                        case "Line Count": {
                            String[] lines = content.split("\r\n|\r|\n");
                            resultText = "Line Count: " + lines.length;
                            break;
                        }
                        case "Sentence Count": {
                            String[] sentences = content.split("[.!?]+");
                            resultText = "Sentence Count: " + sentences.length;
                            break;
                        }
                        case "Convert to Uppercase":
                            resultText = content.toUpperCase();
                            break;
                        case "Convert to Lowercase":
                            resultText = content.toLowerCase();
                            break;
                        case "Remove Duplicate Lines": {
                            String[] lines = content.split("\r\n|\r|\n");
                            Set<String> uniqueLines = new LinkedHashSet<>(Arrays.asList(lines));
                            resultText = String.join(System.lineSeparator(), uniqueLines);
                            break;
                        }
                        case "Sort Lines": {
                            String[] lines = content.split("\r\n|\r|\n");
                            Arrays.sort(lines);
                            resultText = String.join(System.lineSeparator(), lines);
                            break;
                        }
                        case "Remove Extra Whitespace":
                            resultText = content.replaceAll("\\s+", " ").trim();
                            break;
                        case "Format JSON":
                            // Simple JSON formatting (would normally use a library)
                            resultText = formatJson(content);
                            break;
                        case "Format XML":
                            // Simple XML formatting (would normally use a library)
                            resultText = formatXml(content);
                            break;
                        case "Extract Emails": {
                            // Simple regex for email extraction
                            Pattern pattern = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
                            java.util.regex.Matcher matcher = pattern.matcher(content);
                            Set<String> emails = new LinkedHashSet<>();
                            while (matcher.find()) {
                                emails.add(matcher.group());
                            }
                            resultText = String.join(System.lineSeparator(), emails);
                            break;
                        }
                        case "Extract URLs": {
                            // Simple regex for URL extraction
                            Pattern pattern = Pattern.compile("(https?|ftp)://[^\\s/$.?#].[^\\s]*");
                            java.util.regex.Matcher matcher = pattern.matcher(content);
                            Set<String> urls = new LinkedHashSet<>();
                            while (matcher.find()) {
                                urls.add(matcher.group());
                            }
                            resultText = String.join(System.lineSeparator(), urls);
                            break;
                        }
                        default:
                            TextData textData = new TextData(content);
                            ProcessedResult result = fileController.processFile(textData, operation);
                            resultText = formatResult(result);
                            break;
                    }

                    // Save result to file if option is selected
                    if (createSeparateFilesCheckBox.isSelected()) {
                        String outputFileName = generateOutputFilename(file.getName(), operation);
                        saveResultToFile(file, outputFileName, resultText);
                    }

                    // Update item status
                    successCount++;
                    Platform.runLater(() -> {
                        item.setStatus("Success");
                        filesTableView.refresh();
                    });

                } catch (Exception e) {
                    logger.error("Error processing file: " + item.getPath(), e);
                    final String errorMsg = e.getMessage();
                    Platform.runLater(() -> {
                        item.setStatus("Error: " + (errorMsg != null ? errorMsg : "Unknown error"));
                        filesTableView.refresh();
                    });
                    errorCount++;
                }

                // Update progress
                int processed = processedCount.incrementAndGet();
                final int currentProcessed = processed;
                final int currentSuccess = successCount;
                final int currentError = errorCount;

                Platform.runLater(() -> {
                    processingProgressBar.setProgress((double) currentProcessed / totalFiles);
                    filesProcessedLabel.setText(currentProcessed + "/" + totalFiles +
                            " (Success: " + currentSuccess + ", Failed: " + currentError + ")");
                    statusLabel.setText("Processed " + currentProcessed + " of " + totalFiles + " files");
                });
            }

            // Update UI when all files are processed
            final int finalSuccess = successCount;
            final int finalError = errorCount;
            final boolean wasStopped = stopRequested.get();

            Platform.runLater(() -> {
                processingInProgress.set(false);
                processingProgressBar.setProgress(1.0);

                if (wasStopped) {
                    statusLabel.setText("Processing stopped. Processed " + processedCount.get() +
                            " of " + totalFiles + " files.");
                } else {
                    statusLabel.setText("Batch processing completed");
                }

                filesProcessedLabel.setText(String.format("%d/%d (Success: %d, Failed: %d)",
                        processedCount.get(), totalFiles, finalSuccess, finalError));

                updateControlsState();
            });

        }).start();
    }

    // Simple JSON formatter - in production use a proper library
    private String formatJson(String json) {
        StringBuilder formatted = new StringBuilder();
        int indentLevel = 0;
        boolean inQuotes = false;

        for (char c : json.toCharArray()) {
            if (c == '\"' && formatted.length() > 0 && formatted.charAt(formatted.length() - 1) != '\\') {
                inQuotes = !inQuotes;
                formatted.append(c);
            } else if (!inQuotes) {
                if (c == '{' || c == '[') {
                    formatted.append(c).append("\n").append("  ".repeat(++indentLevel));
                } else if (c == '}' || c == ']') {
                    formatted.append("\n").append("  ".repeat(--indentLevel)).append(c);
                } else if (c == ',') {
                    formatted.append(c).append("\n").append("  ".repeat(indentLevel));
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
    }

    // Simple XML formatter - in production use a proper library
    private String formatXml(String xml) {
        StringBuilder formatted = new StringBuilder();
        int indentLevel = 0;
        boolean inTag = false;
        boolean inContent = false;

        for (int i = 0; i < xml.length(); i++) {
            char c = xml.charAt(i);

            if (c == '<' && xml.charAt(i + 1) != '/') {
                if (inContent) {
                    formatted.append("\n");
                    inContent = false;
                }
                formatted.append("\n").append("  ".repeat(indentLevel)).append(c);
                inTag = true;
                indentLevel++;
            } else if (c == '<' && xml.charAt(i + 1) == '/') {
                indentLevel--;
                if (inContent) {
                    formatted.append("\n").append("  ".repeat(indentLevel));
                    inContent = false;
                } else {
                    formatted.append("\n").append("  ".repeat(indentLevel));
                }
                formatted.append(c);
                inTag = true;
            } else if (c == '>') {
                formatted.append(c);
                inTag = false;
                inContent = true;
            } else {
                formatted.append(c);
            }
        }

        return formatted.toString();
    }

    private String formatResult(ProcessedResult result) {
        if (result == null) {
            return "No result";
        }

        switch (result.getResultType()) {
            case TEXT:
                return result.getTextResult();
            case NUMERIC:
                return String.valueOf(result.getNumericResult());
            case LIST:
                return String.join(System.lineSeparator(), result.getListResult());
            case MAP:
                return result.getMapResult().entrySet().stream()
                        .map(e -> e.getKey() + ": " + e.getValue())
                        .collect(Collectors.joining(System.lineSeparator()));
            default:
                return "Unknown result type";
        }
    }

    private String generateOutputFilename(String inputFilename, String operation) {
        if (outputFormatComboBox.getValue() == null) {
            return inputFilename;
        }

        String baseName = inputFilename;
        String extension = "";

        // Extract base name and extension
        int dotIndex = inputFilename.lastIndexOf(".");
        if (dotIndex > 0) {
            baseName = inputFilename.substring(0, dotIndex);
            extension = inputFilename.substring(dotIndex);
        }

        // Generate filename based on format
        switch (outputFormatComboBox.getValue()) {
            case "Same as Input":
                return inputFilename;

            case "Append Operation":
                String opSuffix = operation.replaceAll("\\s+", "_").toLowerCase();
                return baseName + "_" + opSuffix + extension;

            case "Add Timestamp":
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
                return baseName + "_" + dateFormat.format(new Date()) + extension;

            default:
                return inputFilename;
        }
    }

    private void saveResultToFile(File originalFile, String outputFileName, String resultText) {
        try {
            if (!outputFileName.contains(".")) {
                // Add .txt extension if no extension exists
                outputFileName += ".txt";
            }

            Path outputPath = Paths.get(outputDirectory.getAbsolutePath(), outputFileName);
            Files.writeString(outputPath, resultText);
        } catch (IOException e) {
            logger.error("Error saving result to file", e);
        }
    }

    @FXML
    public void handleStopProcessing() {
        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Stop Processing");
        alert.setHeaderText("Are you sure you want to stop processing?");
        alert.setContentText("Current progress will be lost.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Update UI
            statusLabel.setText("Processing stopped by user");
            stopButton.setDisable(true);
            processButton.setDisable(false);
            processingInProgress.set(false);

            // Update status for incomplete entries
            for (FileItem item : filesTableView.getItems()) {
                if ("Processing...".equals(item.getStatus()) || "Pending".equals(item.getStatus())) {
                    item.setStatus("Cancelled");
                }
            }
            filesTableView.refresh();

            updateControlsState();
        }
    }

    @FXML
    public void handleSaveConfiguration() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Configuration");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Configuration Files", "*.json"));

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        fileChooser.setInitialFileName("batch_config_" + timestamp + ".json");

        File saveFile = fileChooser.showSaveDialog(addFilesButton.getScene().getWindow());
        if (saveFile != null) {
            try {
                // Create configuration map
                Map<String, Object> config = new HashMap<>();

                // Save files
                List<Map<String, String>> filesList = new ArrayList<>();
                for (FileItem item : filesTableView.getItems()) {
                    Map<String, String> fileInfo = new HashMap<>();
                    fileInfo.put("name", item.getName());
                    fileInfo.put("path", item.getPath());
                    fileInfo.put("size", item.getSize());
                    fileInfo.put("status", item.getStatus());
                    filesList.add(fileInfo);
                }
                config.put("files", filesList);

                // Save selected operation
                config.put("operation", operationComboBox.getValue());

                // Save output directory
                config.put("outputDirectory", outputDirectory.getAbsolutePath());

                // Save output options
                config.put("outputFormat", outputFormatComboBox.getValue());
                config.put("createSeparateFiles", createSeparateFilesCheckBox.isSelected());

                // Convert to JSON
                String jsonConfig = convertToJson(config);
                Files.writeString(saveFile.toPath(), jsonConfig);

                statusLabel.setText("Configuration saved to: " + saveFile.getName());
            } catch (IOException e) {
                logger.error("Error saving configuration", e);
                showErrorAlert("Save Error", "Could not save configuration", e.getMessage());
            }
        }
    }

    // Helper method to convert Map to JSON (simplified implementation)
    private String convertToJson(Map<String, Object> map) {
        StringBuilder json = new StringBuilder("{\n");

        Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> entry = it.next();
            json.append("  \"").append(entry.getKey()).append("\": ");

            if (entry.getValue() instanceof String) {
                json.append("\"").append(entry.getValue()).append("\"");
            } else if (entry.getValue() instanceof Boolean) {
                json.append(entry.getValue());
            } else if (entry.getValue() instanceof List) {
                json.append(convertListToJson((List<?>) entry.getValue()));
            } else {
                json.append("\"").append(entry.getValue()).append("\"");
            }

            if (it.hasNext()) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("}");
        return json.toString();
    }

    // Helper method to convert List to JSON array
    private String convertListToJson(List<?> list) {
        StringBuilder json = new StringBuilder("[\n");

        Iterator<?> it = list.iterator();
        while (it.hasNext()) {
            Object item = it.next();
            if (item instanceof String) {
                json.append("    \"").append(item).append("\"");
            } else if (item instanceof Map) {
                json.append("    ").append(convertToJson((Map<String, Object>) item));
            } else {
                json.append("    ").append(item);
            }

            if (it.hasNext()) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("  ]");
        return json.toString();
    }

    @FXML
    public void handleLoadConfiguration() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Configuration");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Configuration Files", "*.json"));

        File loadFile = fileChooser.showOpenDialog(addFilesButton.getScene().getWindow());
        if (loadFile != null) {
            try {
                String jsonContent = Files.readString(loadFile.toPath());

                // Parse JSON (This is a simplified implementation)
                Map<String, Object> config = parseJson(jsonContent);

                // Load files
                if (config.containsKey("files")) {
                    List<Map<String, String>> filesList = (List<Map<String, String>>) config.get("files");
                    filesTableView.getItems().clear();
                    selectedFiles.clear();

                    for (Map<String, String> fileInfo : filesList) {
                        String path = fileInfo.get("path");
                        if (path != null) {
                            File file = new File(path);
                            if (file.exists()) {
                                FileItem item = new FileItem(
                                        fileInfo.get("name"),
                                        fileInfo.get("size"),
                                        path,
                                        "Ready"
                                );
                                filesTableView.getItems().add(item);
                                selectedFiles.add(file);
                            }
                        }
                    }
                }

                // Load selected operation
                String operation = (String) config.get("operation");
                if (operation != null) {
                    operationComboBox.setValue(operation);
                }

                // Load output directory
                String outputDir = (String) config.get("outputDirectory");
                if (outputDir != null) {
                    outputDirectory = new File(outputDir);
                    if (outputDirectory.exists()) {
                        outputDirectoryField.setText(outputDir);
                    }
                }

                // Load output options
                String outputFormat = (String) config.get("outputFormat");
                if (outputFormat != null) {
                    outputFormatComboBox.setValue(outputFormat);
                }

                Boolean createSeparateFiles = (Boolean) config.get("createSeparateFiles");
                if (createSeparateFiles != null) {
                    createSeparateFilesCheckBox.setSelected(createSeparateFiles);
                }

                updateControlsState();
                statusLabel.setText("Configuration loaded from: " + loadFile.getName());
            } catch (Exception e) {
                logger.error("Error loading configuration", e);
                showErrorAlert("Load Error", "Could not load configuration", e.getMessage());
            }
        }
    }

    // Helper method for parsing JSON
    // This is a very simplified implementation
    private Map<String, Object> parseJson(String json) {
        Map<String, Object> result = new HashMap<>();

        try {
            // This is just a placeholder - in a real app, use a proper JSON library
            // This placeholder implementation provides minimal functionality to make the example work

            // Extract files
            List<Map<String, String>> files = new ArrayList<>();
            result.put("files", files);

            // Extract operation
            int operationStart = json.indexOf("\"operation\"");
            if (operationStart >= 0) {
                int valueStart = json.indexOf("\"", operationStart + 12);
                int valueEnd = json.indexOf("\"", valueStart + 1);
                if (valueStart >= 0 && valueEnd >= 0) {
                    String operation = json.substring(valueStart + 1, valueEnd);
                    result.put("operation", operation);
                }
            }

            // Extract output directory
            int dirStart = json.indexOf("\"outputDirectory\"");
            if (dirStart >= 0) {
                int valueStart = json.indexOf("\"", dirStart + 18);
                int valueEnd = json.indexOf("\"", valueStart + 1);
                if (valueStart >= 0 && valueEnd >= 0) {
                    String dir = json.substring(valueStart + 1, valueEnd);
                    result.put("outputDirectory", dir);
                }
            }

            // Extract output format
            int formatStart = json.indexOf("\"outputFormat\"");
            if (formatStart >= 0) {
                int valueStart = json.indexOf("\"", formatStart + 15);
                int valueEnd = json.indexOf("\"", valueStart + 1);
                if (valueStart >= 0 && valueEnd >= 0) {
                    String format = json.substring(valueStart + 1, valueEnd);
                    result.put("outputFormat", format);
                }
            }

            // Extract createSeparateFiles
            int separateFilesStart = json.indexOf("\"createSeparateFiles\"");
            if (separateFilesStart >= 0) {
                int valueStart = separateFilesStart + 21;
                int commaOrBracePos = json.indexOf(",", valueStart);
                if (commaOrBracePos < 0) {
                    commaOrBracePos = json.indexOf("}", valueStart);
                }
                if (commaOrBracePos >= 0) {
                    String value = json.substring(valueStart, commaOrBracePos).trim();
                    result.put("createSeparateFiles", Boolean.parseBoolean(value));
                }
            }

        } catch (Exception e) {
            logger.error("Error parsing JSON", e);
        }

        return result;
    }

    @FXML
    public void handleBrowseInputDirectory() {
        handleAddDirectory();
    }

    @FXML
    public void handleRefreshFiles() {
        // Verify that all files still exist and update their information
        List<FileItem> itemsToRemove = new ArrayList<>();

        for (FileItem item : filesTableView.getItems()) {
            File file = new File(item.getPath());
            if (!file.exists()) {
                itemsToRemove.add(item);
            }
        }

        // Remove files that no longer exist
        filesTableView.getItems().removeAll(itemsToRemove);

        // Update selectedFiles list
        selectedFiles.clear();
        for (FileItem item : filesTableView.getItems()) {
            selectedFiles.add(new File(item.getPath()));
        }

        updateControlsState();
        statusLabel.setText("File list refreshed - " + filesTableView.getItems().size() + " files");
    }

    @FXML
    public void handleSelectAllFiles() {
        filesTableView.getSelectionModel().selectAll();
    }

    // This is just the fixed updateControlsState method - to avoid NullPointerExceptions
    private void updateControlsState() {
        boolean hasFiles = !filesTableView.getItems().isEmpty();

        // Check each control for null before using it
        if (removeFileButton != null) {
            removeFileButton.setDisable(!hasFiles);
        }

        if (clearFilesButton != null) {
            clearFilesButton.setDisable(!hasFiles);
        }

        if (processButton != null) {
            processButton.setDisable(!hasFiles);
        }
    }

    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // FileItem class for TableView
    public static class FileItem {
        private final String name;
        private final String size;
        private final String path;
        private String status;

        public FileItem(String name, String size, String path, String status) {
            this.name = name;
            this.size = size;
            this.path = path;
            this.status = status;
        }

        public String getName() {
            return name;
        }

        public String getSize() {
            return size;
        }

        public String getPath() {
            return path;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    private void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
        logger.info(message);
    }
}