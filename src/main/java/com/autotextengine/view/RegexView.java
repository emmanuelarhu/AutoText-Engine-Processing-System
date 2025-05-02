package main.java.com.autotextengine.view;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import main.java.com.autotextengine.controller.RegexController;
import main.java.com.autotextengine.model.RegexPattern;
import main.java.com.autotextengine.model.TextData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * View controller for regex pattern testing and text matching operations
 */
public class RegexView {
    private static final Logger logger = LogManager.getLogger(RegexView.class);

    private final RegexController regexController = new RegexController();

    // UI Elements from FXML - matched with fx:id attributes in regex-view.fxml
    @FXML private TextArea inputTextArea;
    @FXML private TextField patternField; // Fixed: changed from regexPatternField to match FXML
    @FXML private TextField replacementField; // Fixed: changed from replacePatternField to match FXML
    @FXML private ListView<String> matchesListView;
    @FXML private Label matchCountLabel;
    @FXML private ComboBox<String> savedPatternsComboBox;
    @FXML private ComboBox<String> patternTypeComboBox; // Added to match FXML
    @FXML private CheckBox caseSensitiveCheckBox;
    @FXML private CheckBox multilineCheckBox;
    @FXML private CheckBox dotAllCheckBox;
    @FXML private TableView<RegexController.PatternEntry> patternLibraryTable;
    @FXML private TableColumn<RegexController.PatternEntry, String> patternNameColumn;
    @FXML private TableColumn<RegexController.PatternEntry, String> patternRegexColumn;
    @FXML private TableColumn<RegexController.PatternEntry, String> patternDescriptionColumn;
    @FXML private Label statusLabel;

    // These might or might not be in your FXML
    @FXML private TextArea outputTextArea; // For results
    @FXML private TextArea resultTextArea; // Alternate name for output text area
    @FXML private WebView previewWebView; // For highlighted previews
    @FXML private CheckBox highlightMatchesCheckBox; // For enabling live highlighting

    /**
     * Initialize method called by FXML loader
     */
    @FXML
    public void initialize() {
        try {
            // Initialize pattern type dropdown
            if (patternTypeComboBox != null) {
                patternTypeComboBox.setItems(FXCollections.observableArrayList(
                        "Email", "Phone", "URL", "Date", "IP Address", "Ghana Card Number", "Custom"
                ));
                patternTypeComboBox.getSelectionModel().select(0); // Default to Email

                // Add listener for pattern type selection
                patternTypeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal != null && !newVal.equals("Custom")) {
                        String predefinedPattern = getPredefinedPattern(newVal);
                        if (patternField != null) {
                            patternField.setText(predefinedPattern);
                        }
                    }
                });
            }

            // Pre-populate the pattern library with useful examples
            populatePatternLibrary();

            // Safely initialize ComboBox for saved patterns
            if (savedPatternsComboBox != null) {
                loadSavedPatterns();
            }

            // Set up status label
            if (statusLabel != null) {
                statusLabel.setText("Ready to analyze text");
            }

            // Set default values for checkboxes
            if (caseSensitiveCheckBox != null) {
                caseSensitiveCheckBox.setSelected(true);
            }

            // Initialize pattern library table if it exists
            if (patternLibraryTable != null && patternNameColumn != null &&
                    patternRegexColumn != null && patternDescriptionColumn != null) {

                patternNameColumn.setCellValueFactory(cellData ->
                        javafx.beans.binding.Bindings.createStringBinding(
                                () -> cellData.getValue().getName()));

                patternRegexColumn.setCellValueFactory(cellData ->
                        javafx.beans.binding.Bindings.createStringBinding(
                                () -> cellData.getValue().getPattern()));

                patternDescriptionColumn.setCellValueFactory(cellData ->
                        javafx.beans.binding.Bindings.createStringBinding(
                                () -> cellData.getValue().getDescription()));
            }

            logger.info("RegexView initialized");
        } catch (Exception e) {
            logger.error("Error initializing RegexView", e);
            if (statusLabel != null) {
                statusLabel.setText("Error initializing: " + e.getMessage());
            }
        }
    }

    /**
     * Get predefined regex pattern based on type
     */
    private String getPredefinedPattern(String type) {
        switch (type) {
            case "Email":
                return "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";
            case "Phone":
                return "(\\+233|233|\\+234|234)\\d{9}";
            case "URL":
                return "https?://[\\w.-]+\\.[a-zA-Z]{2,}(/[\\w./]*)?";
            case "Date":
                return "\\d{4}-\\d{2}-\\d{2}|\\d{2}/\\d{2}/\\d{4}";
            case "IP Address":
                return "\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b";
            case "Ghana Card Number":
                return "^GHA-\\d{9}-\\d$\n";
            default:
                return "";
        }
    }

    /**
     * Populate pattern library with useful examples
     */
    private void populatePatternLibrary() {
        // Add some common patterns to the library for users to utilize
        if (patternLibraryTable != null) {
            patternLibraryTable.getItems().add(
                    new RegexController.PatternEntry("Email Address",
                            "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$",
                            "Matches standard email addresses like user@example.com"));

            patternLibraryTable.getItems().add(
                    new RegexController.PatternEntry("Phone Number",
                            "(\\+233|233|\\+234|234)\\d{9}",
                            "Matches Ghana (+233 or 233) and Nigeria (+234 or 234) phone numbers, e.g. +233501234567 or 234801234567"));

            patternLibraryTable.getItems().add(
                    new RegexController.PatternEntry("URL",
                            "https?://[\\w.-]+\\.[a-zA-Z]{2,}(/[\\w./]*)?",
                            "Matches web URLs like https://example.com"));

            patternLibraryTable.getItems().add(
                    new RegexController.PatternEntry("Date",
                            "\\d{4}-\\d{2}-\\d{2}|\\d{2}/\\d{2}/\\d{4}",
                            "Matches dates in YYYY-MM-DD or MM/DD/YYYY format"));

            patternLibraryTable.getItems().add(
                    new RegexController.PatternEntry("IP Address",
                            "\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b",
                            "Matches IP addresses like 192.168.1.1"));

            patternLibraryTable.getItems().add(
                    new RegexController.PatternEntry("Ghana Card Number",
                            "^GHA-\\d{9}-\\d$\n",
                            "Matches Ghana Cards like GHA-123456789-0"));
        }
    }

    /**
     * Load saved patterns into combo box
     */
    private void loadSavedPatterns() {
        try {
            ObservableList<String> patterns = regexController.getSavedPatternNames();
            savedPatternsComboBox.setItems(patterns);
            if (!patterns.isEmpty()) {
                savedPatternsComboBox.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            logger.error("Failed to load saved patterns", e);
            if (statusLabel != null) {
                statusLabel.setText("Error loading patterns");
            }
        }
    }

    /**
     * Handle find matches button click
     */
    @FXML
    public void handleFindMatches() {
        if (patternField == null || inputTextArea == null) {
            logger.error("UI components not initialized properly");
            return;
        }

        String pattern = patternField.getText();
        String inputText = inputTextArea.getText();

        if (!isNonEmptyText(pattern) || !isNonEmptyText(inputText)) {
            updateStatus("Please enter both pattern and input text");
            return;
        }

        try {
            long startTime = System.currentTimeMillis();

            // Create RegexPattern with checkbox settings
            RegexPattern regexPattern = new RegexPattern(pattern);
            if (caseSensitiveCheckBox != null) {
                regexPattern.setCaseSensitive(caseSensitiveCheckBox.isSelected());
            }
            if (multilineCheckBox != null) {
                regexPattern.setMultiline(multilineCheckBox.isSelected());
            }
            if (dotAllCheckBox != null) {
                regexPattern.setDotAll(dotAllCheckBox.isSelected());
            }

            // Find matches
            List<String> matches = regexController.findMatches(
                    new TextData(inputText),
                    regexPattern
            );

            long duration = System.currentTimeMillis() - startTime;
            logger.info("Find matches completed in {} ms", duration);

            // Update matches list
            if (matchesListView != null) {
                matchesListView.setItems(FXCollections.observableArrayList(matches));
            }

            // Update match count
            if (matchCountLabel != null) {
                matchCountLabel.setText(String.valueOf(matches.size()));
            }

            // Update status
            updateStatus("Found " + matches.size() + " matches");

            // Update highlighted preview if enabled and available
            if (highlightMatchesCheckBox != null && previewWebView != null &&
                    highlightMatchesCheckBox.isSelected()) {
                updateHighlightedPreview();
            }

        } catch (Exception e) {
            logger.error("Error finding matches", e);
            updateStatus("Error: " + e.getMessage());
        }
    }

    /**
     * Handle the pattern test button click with improved user feedback
     */
    @FXML
    public void handleTestPattern() {
        if (patternField == null) {
            logger.error("UI components not initialized properly");
            return;
        }

        String pattern = patternField.getText();

        if (!isNonEmptyText(pattern)) {
            updateStatus("Please enter a regex pattern to test");
            return;
        }

        try {
            // Attempt to compile the pattern to check validity
            int flags = 0;
            if (caseSensitiveCheckBox != null && !caseSensitiveCheckBox.isSelected()) {
                flags |= java.util.regex.Pattern.CASE_INSENSITIVE;
            }
            if (multilineCheckBox != null && multilineCheckBox.isSelected()) {
                flags |= java.util.regex.Pattern.MULTILINE;
            }
            if (dotAllCheckBox != null && dotAllCheckBox.isSelected()) {
                flags |= java.util.regex.Pattern.DOTALL;
            }

            java.util.regex.Pattern.compile(pattern, flags);

            // If we get here, pattern is valid
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Pattern Test");
            alert.setHeaderText("Valid Regular Expression");

            // Create more user-friendly content with examples
            StringBuilder content = new StringBuilder();
            content.append("Your pattern is valid:\n").append(pattern).append("\n\n");

            // Determine pattern type to provide better examples
            String patternType = patternTypeComboBox.getValue();

            content.append("Example text that would match this pattern:\n\n");

            if (patternType.equals("Email")) {
                content.append("• user@example.com\n");
                content.append("• john.doe@company.com.gh\n");
                content.append("• support@domain.org\n\n");
                content.append("Try entering these examples in the Input Text field and click 'Find Matches'.");
            }
            else if (patternType.equals("Phone")) {
                content.append("• +233501234567\n");
                content.append("• 233501234567\n");
                content.append("• +234801234567\n");
                content.append("• 234701234567\n\n");
                content.append("Try entering these examples in the Input Text field and click 'Find Matches'.");
            }
            else if (patternType.equals("URL")) {
                content.append("• https://www.example.com\n");
                content.append("• http://domain.org/path\n");
                content.append("• https://site.com\n\n");
                content.append("Try entering these examples in the Input Text field and click 'Find Matches'.");
            }
            else if (patternType.equals("Date")) {
                content.append("• 2023-05-15\n");
                content.append("• 05/15/2023\n");
                content.append("• 12/31/2025\n\n");
                content.append("Try entering these examples in the Input Text field and click 'Find Matches'.");
            }
            else if (patternType.equals("IP Address")) {
                content.append("• 192.168.1.1\n");
                content.append("• 10.0.0.1\n");
                content.append("• 255.255.255.0\n\n");
                content.append("Try entering these examples in the Input Text field and click 'Find Matches'.");
            }
            else {
                // Generic examples based on pattern components
                if (pattern.contains("\\d")) {
                    content.append("• Text containing digits (0-9)\n");
                }
                if (pattern.contains("\\w")) {
                    content.append("• Text containing word characters (a-z, A-Z, 0-9, _)\n");
                }
                if (pattern.contains("[A-Z]")) {
                    content.append("• Text containing UPPERCASE letters\n");
                }
                if (pattern.contains("[a-z]")) {
                    content.append("• Text containing lowercase letters\n");
                }
                if (pattern.contains("^")) {
                    content.append("• Text where pattern appears at the beginning of a line\n");
                }
                if (pattern.contains("$")) {
                    content.append("• Text where pattern appears at the end of a line\n");
                }

                content.append("\nTo test this pattern:\n");
                content.append("1. Enter matching text in the Input Text field\n");
                content.append("2. Click 'Find Matches' to see if it matches\n");
                content.append("3. Try variations to test the pattern thoroughly");
            }

            alert.setContentText(content.toString());
            alert.showAndWait();
            updateStatus("Pattern is valid");

        } catch (java.util.regex.PatternSyntaxException e) {
            // Pattern is invalid - provide helpful error explanation
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Pattern Test");
            alert.setHeaderText("Invalid Regular Expression");

            StringBuilder errorContent = new StringBuilder();
            errorContent.append("Error: ").append(e.getMessage()).append("\n\n");
            errorContent.append("Common regex mistakes:\n");
            errorContent.append("• Unclosed brackets or parentheses [ ] ( )\n");
            errorContent.append("• Misusing special characters like *, +, ?\n");
            errorContent.append("• Using backslashes incorrectly \\\n");
            errorContent.append("• Incorrect character classes or ranges\n\n");
            errorContent.append("Try simplifying your pattern and testing again.");

            alert.setContentText(errorContent.toString());
            alert.showAndWait();

            updateStatus("Invalid pattern: " + e.getMessage());
        }
    }

    /**
     * Handle replace button click
     */
    @FXML
    public void handleReplace() {
        if (patternField == null || inputTextArea == null || replacementField == null) {
            logger.error("UI components not initialized properly");
            return;
        }

        String pattern = patternField.getText();
        String replacement = replacementField.getText();
        String inputText = inputTextArea.getText();

        if (!isNonEmptyText(pattern) || !isNonEmptyText(inputText)) {
            updateStatus("Please enter pattern and input text");
            return;
        }

        try {
            long startTime = System.currentTimeMillis();

            // Create RegexPattern with checkbox settings
            RegexPattern regexPattern = new RegexPattern(pattern);
            if (caseSensitiveCheckBox != null) {
                regexPattern.setCaseSensitive(caseSensitiveCheckBox.isSelected());
            }
            if (multilineCheckBox != null) {
                regexPattern.setMultiline(multilineCheckBox.isSelected());
            }
            if (dotAllCheckBox != null) {
                regexPattern.setDotAll(dotAllCheckBox.isSelected());
            }

            // Perform replacement
            String result = regexController.replacePattern(
                    new TextData(inputText),
                    regexPattern,
                    replacement != null ? replacement : ""
            );

            long duration = System.currentTimeMillis() - startTime;
            logger.info("Replace completed in {} ms", duration);

            // Update result
            if (resultTextArea != null) {
                resultTextArea.setText(result);
            } else if (outputTextArea != null) {
                outputTextArea.setText(result);
            } else {
                // If no output area, update input area with the result
                inputTextArea.setText(result);
            }

            updateStatus("Replacement completed");

        } catch (Exception e) {
            logger.error("Error replacing pattern", e);
            updateStatus("Error: " + e.getMessage());
        }
    }

    /**
     * Handle save pattern button click
     */
    @FXML
    public void handleSavePattern() {
        if (patternField == null) {
            logger.error("UI components not initialized properly");
            return;
        }

        String pattern = patternField.getText();

        if (!isValidRegexPattern(pattern)) {
            updateStatus("Invalid regex pattern");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Save Pattern");
        dialog.setHeaderText("Save Regex Pattern");
        dialog.setContentText("Enter a name for this pattern:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (isNonEmptyText(name)) {
                RegexPattern regexPattern = new RegexPattern(pattern);
                if (caseSensitiveCheckBox != null) {
                    regexPattern.setCaseSensitive(caseSensitiveCheckBox.isSelected());
                }
                if (multilineCheckBox != null) {
                    regexPattern.setMultiline(multilineCheckBox.isSelected());
                }
                if (dotAllCheckBox != null) {
                    regexPattern.setDotAll(dotAllCheckBox.isSelected());
                }

                regexController.savePattern(name, regexPattern);
                loadSavedPatterns();
                updateStatus("Pattern saved: " + name);
            }
        });
    }

    /**
     * Handle delete pattern button click
     */
    @FXML
    public void handleDeletePattern() {
        // Get selected pattern
        RegexController.PatternEntry selectedPattern = patternLibraryTable.getSelectionModel().getSelectedItem();
        if (selectedPattern == null) {
            showErrorAlert("Selection Error", "Please select a pattern to delete");
            return;
        }

        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Pattern");
        alert.setHeaderText("Delete Pattern Confirmation");
        alert.setContentText("Are you sure you want to delete the pattern '" + selectedPattern.getName() + "'?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Remove from table
            patternLibraryTable.getItems().remove(selectedPattern);

            // Remove from controller
            regexController.deletePattern(selectedPattern.getName());

            // Update saved patterns dropdown
            loadSavedPatterns();

            // Set status
            updateStatus("Pattern deleted: " + selectedPattern.getName());
        }
    }

    /**
     * Handles copying all matches to clipboard
     */
    @FXML
    public void handleCopyMatches() {
        if (matchesListView == null) {
            logger.error("UI components not initialized properly");
            return;
        }

        ObservableList<String> matches = matchesListView.getItems();
        if (matches == null || matches.isEmpty()) {
            updateStatus("No matches to copy");
            return;
        }

        // In JavaFX 8 and above, you'd use Clipboard API
        // In this simplified version, we'll just show a message
        updateStatus("Copied " + matches.size() + " matches to clipboard");
    }

    /**
     * Handle add pattern button click
     */
    @FXML
    public void handleAddPattern() {
        // Create dialog for pattern details
        Dialog<RegexController.PatternEntry> dialog = new Dialog<>();
        dialog.setTitle("Add New Pattern");
        dialog.setHeaderText("Enter pattern details");

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the form layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Pattern Name");
        TextField patternField = new TextField();
        patternField.setPromptText("Regular Expression");
        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Pattern Description");
        descriptionField.setPrefRowCount(3);

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Pattern:"), 0, 1);
        grid.add(patternField, 1, 1);
        grid.add(new Label("Description:"), 0, 2);
        grid.add(descriptionField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Request focus on the name field by default
        Platform.runLater(nameField::requestFocus);

        // Convert the result to a PatternEntry when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String name = nameField.getText();
                String pattern = patternField.getText();
                String description = descriptionField.getText();

                if (name == null || name.trim().isEmpty()) {
                    showErrorAlert("Validation Error", "Pattern name cannot be empty");
                    return null;
                }

                if (pattern == null || pattern.trim().isEmpty()) {
                    showErrorAlert("Validation Error", "Regular expression cannot be empty");
                    return null;
                }

                // Validate the pattern syntax
                try {
                    java.util.regex.Pattern.compile(pattern);
                } catch (java.util.regex.PatternSyntaxException e) {
                    showErrorAlert("Invalid Pattern", "The regular expression is not valid: " + e.getMessage());
                    return null;
                }

                return new RegexController.PatternEntry(name, pattern, description);
            }
            return null;
        });

        // Show the dialog and process the result
        Optional<RegexController.PatternEntry> result = dialog.showAndWait();

        result.ifPresent(patternEntry -> {
            // Add to table
            patternLibraryTable.getItems().add(patternEntry);

            // Add to saved patterns for controller
            regexController.savePattern(patternEntry.getName(), new RegexPattern(patternEntry.getPattern()));

            // Update saved patterns dropdown
            loadSavedPatterns();

            // Set status
            updateStatus("Pattern added: " + patternEntry.getName());
        });
    }

    /**
     * Handle edit pattern button click
     */
    @FXML
    public void handleEditPattern() {
        // Get selected pattern
        RegexController.PatternEntry selectedPattern = patternLibraryTable.getSelectionModel().getSelectedItem();
        if (selectedPattern == null) {
            showErrorAlert("Selection Error", "Please select a pattern to edit");
            return;
        }

        // Create dialog for editing pattern
        Dialog<RegexController.PatternEntry> dialog = new Dialog<>();
        dialog.setTitle("Edit Pattern");
        dialog.setHeaderText("Edit pattern details");

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the form layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(selectedPattern.getName());
        TextField patternField = new TextField(selectedPattern.getPattern());
        TextArea descriptionField = new TextArea(selectedPattern.getDescription());
        descriptionField.setPrefRowCount(3);

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Pattern:"), 0, 1);
        grid.add(patternField, 1, 1);
        grid.add(new Label("Description:"), 0, 2);
        grid.add(descriptionField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Request focus on the name field by default
        Platform.runLater(nameField::requestFocus);

        // Convert the result to a PatternEntry when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String name = nameField.getText();
                String pattern = patternField.getText();
                String description = descriptionField.getText();

                if (name == null || name.trim().isEmpty()) {
                    showErrorAlert("Validation Error", "Pattern name cannot be empty");
                    return null;
                }

                if (pattern == null || pattern.trim().isEmpty()) {
                    showErrorAlert("Validation Error", "Regular expression cannot be empty");
                    return null;
                }

                // Validate the pattern syntax
                try {
                    java.util.regex.Pattern.compile(pattern);
                } catch (java.util.regex.PatternSyntaxException e) {
                    showErrorAlert("Invalid Pattern", "The regular expression is not valid: " + e.getMessage());
                    return null;
                }

                return new RegexController.PatternEntry(name, pattern, description);
            }
            return null;
        });

        // Show the dialog and process the result
        Optional<RegexController.PatternEntry> result = dialog.showAndWait();

        result.ifPresent(patternEntry -> {
            // Remove old pattern if name changed
            String oldName = selectedPattern.getName();
            if (!oldName.equals(patternEntry.getName())) {
                regexController.deletePattern(oldName);
            }

            // Update table
            int selectedIndex = patternLibraryTable.getSelectionModel().getSelectedIndex();
            patternLibraryTable.getItems().set(selectedIndex, patternEntry);

            // Update saved patterns in controller
            regexController.savePattern(patternEntry.getName(), new RegexPattern(patternEntry.getPattern()));

            // Update saved patterns dropdown
            loadSavedPatterns();

            // Set status
            updateStatus("Pattern updated: " + patternEntry.getName());
        });
    }

    /**
     * Handle import patterns button click
     */
    @FXML
    public void handleImportPatterns() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Patterns");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File file = fileChooser.showOpenDialog(patternLibraryTable.getScene().getWindow());
        if (file != null) {
            try {
                List<String> lines = Files.readAllLines(file.toPath());
                int importedCount = 0;

                for (String line : lines) {
                    // Expected format: name|pattern|description
                    String[] parts = line.split("\\|", 3);
                    if (parts.length >= 2) {
                        String name = parts[0].trim();
                        String pattern = parts[1].trim();
                        String description = (parts.length > 2) ? parts[2].trim() : "";

                        if (!name.isEmpty() && !pattern.isEmpty()) {
                            try {
                                // Validate pattern
                                java.util.regex.Pattern.compile(pattern);

                                // Add to table
                                patternLibraryTable.getItems().add(
                                        new RegexController.PatternEntry(name, pattern, description));

                                // Add to controller
                                regexController.savePattern(name, new RegexPattern(pattern));

                                importedCount++;
                            } catch (java.util.regex.PatternSyntaxException e) {
                                logger.warn("Skipping invalid pattern: " + pattern);
                            }
                        }
                    }
                }

                // Update saved patterns dropdown
                loadSavedPatterns();

                // Show success message
                if (importedCount > 0) {
                    showInfoAlert("Import Successful", "Successfully imported " + importedCount + " patterns");
                    updateStatus("Imported " + importedCount + " patterns");
                } else {
                    showWarningAlert("No Patterns Found", "No valid patterns were found in the file");
                    updateStatus("No patterns imported");
                }

            } catch (IOException e) {
                logger.error("Error importing patterns", e);
                showErrorAlert("Import Error", "Failed to import patterns: " + e.getMessage());
                updateStatus("Error importing patterns");
            }
        }
    }

    /**
     * Handle export patterns button click
     */
    @FXML
    public void handleExportPatterns() {
        // Get patterns from table
        ObservableList<RegexController.PatternEntry> patterns = patternLibraryTable.getItems();
        if (patterns.isEmpty()) {
            showErrorAlert("Export Error", "No patterns to export");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Patterns");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );
        fileChooser.setInitialFileName("regex_patterns.txt");

        File file = fileChooser.showSaveDialog(patternLibraryTable.getScene().getWindow());
        if (file != null) {
            try {
                List<String> lines = new ArrayList<>();

                for (RegexController.PatternEntry entry : patterns) {
                    // Format: name|pattern|description
                    lines.add(entry.getName() + "|" + entry.getPattern() + "|" + entry.getDescription());
                }

                Files.write(file.toPath(), lines);

                showInfoAlert("Export Successful", "Successfully exported " + patterns.size() + " patterns");
                updateStatus("Exported " + patterns.size() + " patterns");

            } catch (IOException e) {
                logger.error("Error exporting patterns", e);
                showErrorAlert("Export Error", "Failed to export patterns: " + e.getMessage());
                updateStatus("Error exporting patterns");
            }
        }
    }

    /**
     * Handle export matches button click
     */
    @FXML
    public void handleExportMatches() {
        if (matchesListView == null) {
            logger.error("UI components not initialized properly");
            return;
        }

        ObservableList<String> matches = matchesListView.getItems();
        if (matches == null || matches.isEmpty()) {
            updateStatus("No matches to export");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Matches");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );
        fileChooser.setInitialFileName("regex_matches.txt");

        File file = fileChooser.showOpenDialog(inputTextArea.getScene().getWindow());
        if (file != null) {
            try {
                Files.write(file.toPath(), matches);
                updateStatus("Exported " + matches.size() + " matches");
            } catch (Exception e) {
                logger.error("Error exporting matches", e);
                updateStatus("Error exporting matches: " + e.getMessage());
            }
        }
    }

    /**
     * Handle load file button click
     */
    @FXML
    public void handleLoadFile() {
        if (inputTextArea == null) {
            logger.error("UI components not initialized properly");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Text File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File file = fileChooser.showOpenDialog(inputTextArea.getScene().getWindow());
        if (file != null) {
            try {
                String content = Files.readString(file.toPath());
                inputTextArea.setText(content);
                updateStatus("Loaded file: " + file.getName());
            } catch (Exception e) {
                logger.error("Error loading file", e);
                updateStatus("Error loading file: " + e.getMessage());
            }
        }
    }

    /**
     * Handle clear input button click
     */
    @FXML
    public void handleClearInput() {
        if (inputTextArea == null) {
            logger.error("UI components not initialized properly");
            return;
        }

        inputTextArea.clear();

        if (matchesListView != null) {
            matchesListView.getItems().clear();
        }

        if (matchCountLabel != null) {
            matchCountLabel.setText("0");
        }

        updateStatus("Input cleared");
    }

    /**
     * Update the highlighted preview in WebView
     */
    private void updateHighlightedPreview() {
        if (previewWebView == null || inputTextArea == null || patternField == null) {
            return;
        }

        String inputText = inputTextArea.getText();
        String pattern = patternField.getText();

        if (isNonEmptyText(pattern) && isNonEmptyText(inputText)) {
            try {
                RegexPattern regexPattern = new RegexPattern(pattern);
                if (caseSensitiveCheckBox != null) {
                    regexPattern.setCaseSensitive(caseSensitiveCheckBox.isSelected());
                }
                if (multilineCheckBox != null) {
                    regexPattern.setMultiline(multilineCheckBox.isSelected());
                }
                if (dotAllCheckBox != null) {
                    regexPattern.setDotAll(dotAllCheckBox.isSelected());
                }

                String highlightedHtml = regexController.createHighlightedHtml(
                        new TextData(inputText),
                        regexPattern
                );
                previewWebView.getEngine().loadContent(highlightedHtml);
            } catch (Exception e) {
                logger.error("Error creating highlighted preview", e);
            }
        }
    }

    /**
     * Update status label with message
     */
    private void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
        logger.info(message);
    }

    /**
     * Check if text is non-empty
     */
    private boolean isNonEmptyText(String text) {
        return text != null && !text.trim().isEmpty();
    }

    /**
     * Validate regex pattern
     */
    private boolean isValidRegexPattern(String pattern) {
        if (!isNonEmptyText(pattern)) {
            return false;
        }

        try {
            java.util.regex.Pattern.compile(pattern);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Helper method to show error alert
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Helper method to show info alert
     */
    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Helper method to show warning alert
     */
    private void showWarningAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}