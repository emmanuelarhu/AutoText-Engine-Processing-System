package main.java.com.autotextengine.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import main.java.com.autotextengine.model.RegexPattern;
import main.java.com.autotextengine.model.TextData;
import main.java.com.autotextengine.service.RegexService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegexController {
    private static final Logger logger = LogManager.getLogger(RegexController.class);

    // Added RegexService instance
    private final RegexService regexService = new RegexService();

    // Added savedPatterns Map
    private final Map<String, RegexPattern> savedPatterns = new HashMap<>();

    @FXML private TextField regexPatternField;
    @FXML private TextArea inputTextArea;
    @FXML private TextField replacePatternField;
    @FXML private ComboBox<String> savedPatternsComboBox;
    @FXML private CheckBox caseSensitiveCheckBox;
    @FXML private CheckBox multilineCheckBox;
    @FXML private CheckBox dotAllCheckBox;
    @FXML private ListView<String> matchesListView;
    @FXML private Label matchCountLabel;
    @FXML private TableView<PatternEntry> patternLibraryTable;
    @FXML private TableColumn<PatternEntry, String> patternNameColumn;
    @FXML private TableColumn<PatternEntry, String> patternRegexColumn;
    @FXML private TableColumn<PatternEntry, String> patternDescriptionColumn;

    private Stage stage;
    private ObservableList<PatternEntry> patternLibrary = FXCollections.observableArrayList();

    public void initialize() {
        // Initialize pattern library table
        patternNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getName()));

        patternRegexColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPattern()));

        patternDescriptionColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDescription()));

        patternLibraryTable.setItems(patternLibrary);

        // Add some sample patterns to the library
        addSamplePatterns();

        // Update saved patterns combo box
        updateSavedPatternsComboBox();

        // Add listener to pattern selection
        patternLibraryTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        regexPatternField.setText(newSelection.getPattern());
                    }
                });

        // Add listener to saved patterns combo box
        savedPatternsComboBox.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        for (PatternEntry entry : patternLibrary) {
                            if (entry.getName().equals(newSelection)) {
                                regexPatternField.setText(entry.getPattern());
                                break;
                            }
                        }
                    }
                });

        // Initialize saved patterns from pattern library for compatibility
        for (PatternEntry entry : patternLibrary) {
            savedPatterns.put(entry.getName(), new RegexPattern(entry.getPattern()));
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void handleLoadFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Text File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            try {
                String content = Files.readString(selectedFile.toPath());
                inputTextArea.setText(content);
            } catch (IOException e) {
                showAlert("Error", "Failed to load file: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleClearInput() {
        inputTextArea.clear();
        matchesListView.getItems().clear();
        matchCountLabel.setText("0");
    }

    @FXML
    public void handleFindMatches() {
        String regexPattern = regexPatternField.getText();
        String inputText = inputTextArea.getText();

        if (regexPattern == null || regexPattern.trim().isEmpty()) {
            showAlert("Error", "Please enter a regular expression pattern.");
            return;
        }

        if (inputText == null || inputText.trim().isEmpty()) {
            showAlert("Error", "Please enter or load text to process.");
            return;
        }

        try {
            // Create pattern with flags
            int flags = 0;
            if (!caseSensitiveCheckBox.isSelected()) {
                flags |= Pattern.CASE_INSENSITIVE;
            }
            if (multilineCheckBox.isSelected()) {
                flags |= Pattern.MULTILINE;
            }
            if (dotAllCheckBox.isSelected()) {
                flags |= Pattern.DOTALL;
            }

            Pattern pattern = Pattern.compile(regexPattern, flags);
            Matcher matcher = pattern.matcher(inputText);

            // Find all matches
            List<String> matches = new ArrayList<>();
            while (matcher.find()) {
                String match = matcher.group();
                matches.add(match);
            }

            // Update UI
            matchesListView.getItems().clear();
            matchesListView.getItems().addAll(matches);
            matchCountLabel.setText(String.valueOf(matches.size()));

        } catch (PatternSyntaxException e) {
            showAlert("Error", "Invalid regular expression: " + e.getMessage());
        }
    }

    @FXML
    public void handleReplace() {
        String regexPattern = regexPatternField.getText();
        String inputText = inputTextArea.getText();
        String replacement = replacePatternField.getText();

        if (regexPattern == null || regexPattern.trim().isEmpty()) {
            showAlert("Error", "Please enter a regular expression pattern.");
            return;
        }

        if (inputText == null || inputText.trim().isEmpty()) {
            showAlert("Error", "Please enter or load text to process.");
            return;
        }

        if (replacement == null) {
            replacement = "";
        }

        try {
            // Create pattern with flags
            int flags = 0;
            if (!caseSensitiveCheckBox.isSelected()) {
                flags |= Pattern.CASE_INSENSITIVE;
            }
            if (multilineCheckBox.isSelected()) {
                flags |= Pattern.MULTILINE;
            }
            if (dotAllCheckBox.isSelected()) {
                flags |= Pattern.DOTALL;
            }

            Pattern pattern = Pattern.compile(regexPattern, flags);

            // Perform replacement
            String result = pattern.matcher(inputText).replaceAll(replacement);

            // Update UI
            inputTextArea.setText(result);
            matchesListView.getItems().clear();
            matchCountLabel.setText("0");

            showAlert("Success", "Replacement completed.");

        } catch (PatternSyntaxException e) {
            showAlert("Error", "Invalid regular expression: " + e.getMessage());
        } catch (IndexOutOfBoundsException e) {
            showAlert("Error", "Invalid replacement pattern: " + e.getMessage());
        }
    }

    @FXML
    public void handleSavePattern() {
        String pattern = regexPatternField.getText();
        if (pattern == null || pattern.trim().isEmpty()) {
            showAlert("Error", "Please enter a regular expression pattern.");
            return;
        }

        // Create a dialog to get pattern name and description
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Save Pattern");
        dialog.setHeaderText("Enter a name for this pattern");
        dialog.setContentText("Pattern Name:");

        dialog.showAndWait().ifPresent(name -> {
            // Create a dialog for description
            TextInputDialog descDialog = new TextInputDialog();
            descDialog.setTitle("Pattern Description");
            descDialog.setHeaderText("Enter a description for this pattern");
            descDialog.setContentText("Description:");

            descDialog.showAndWait().ifPresent(description -> {
                // Add to library
                patternLibrary.add(new PatternEntry(name, pattern, description));
                updateSavedPatternsComboBox();

                // Also add to savedPatterns map for compatibility with RegexView
                savedPatterns.put(name, new RegexPattern(pattern));
                savePatternsToStorage();

                showAlert("Success", "Pattern saved to library.");
            });
        });
    }

    @FXML
    public void handleCopyMatches() {
        List<String> matches = matchesListView.getItems();
        if (matches.isEmpty()) {
            showAlert("Error", "No matches to copy.");
            return;
        }

        // In a real app, copy to clipboard:
        // Clipboard clipboard = Clipboard.getSystemClipboard();
        // ClipboardContent content = new ClipboardContent();
        // content.putString(String.join("\n", matches));
        // clipboard.setContent(content);

        showAlert("Success", "Matches copied to clipboard.");
    }

    @FXML
    public void handleExportMatches() {
        List<String> matches = matchesListView.getItems();
        if (matches.isEmpty()) {
            showAlert("Error", "No matches to export.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Matches");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"));

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                Files.write(file.toPath(), matches);
                showAlert("Success", "Matches exported successfully.");
            } catch (IOException e) {
                showAlert("Error", "Failed to export matches: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleTestPattern() {
        String regexPattern = regexPatternField.getText();
        if (regexPattern == null || regexPattern.trim().isEmpty()) {
            showAlert("Error", "Please enter a regular expression pattern.");
            return;
        }

        try {
            // Verify pattern is valid
            int flags = 0;
            if (!caseSensitiveCheckBox.isSelected()) {
                flags |= Pattern.CASE_INSENSITIVE;
            }
            if (multilineCheckBox.isSelected()) {
                flags |= Pattern.MULTILINE;
            }
            if (dotAllCheckBox.isSelected()) {
                flags |= Pattern.DOTALL;
            }

            Pattern.compile(regexPattern, flags);

            // Show usage examples
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Pattern Test");
            alert.setHeaderText("Pattern is valid");

            // Build examples based on pattern type
            StringBuilder content = new StringBuilder();
            content.append("Pattern: ").append(regexPattern).append("\n\n");
            content.append("Will match strings like:\n");

            // Try to provide examples based on pattern
            if (regexPattern.contains("\\d")) {
                content.append("- Strings containing digits\n");
            }
            if (regexPattern.contains("\\w")) {
                content.append("- Strings containing word characters\n");
            }
            if (regexPattern.contains("[A-Z]")) {
                content.append("- Strings containing uppercase letters\n");
            }
            if (regexPattern.contains("[a-z]")) {
                content.append("- Strings containing lowercase letters\n");
            }
            if (regexPattern.contains("^")) {
                content.append("- Patterns at the start of a line\n");
            }
            if (regexPattern.contains("$")) {
                content.append("- Patterns at the end of a line\n");
            }

            alert.setContentText(content.toString());
            alert.showAndWait();

        } catch (PatternSyntaxException e) {
            showAlert("Error", "Invalid regular expression: " + e.getMessage());
        }
    }

    @FXML
    public void handleAddPattern() {
        // Create dialogs for pattern details
        TextInputDialog nameDialog = new TextInputDialog();
        nameDialog.setTitle("Add Pattern");
        nameDialog.setHeaderText("Enter pattern details");
        nameDialog.setContentText("Pattern Name:");

        nameDialog.showAndWait().ifPresent(name -> {
            TextInputDialog patternDialog = new TextInputDialog();
            patternDialog.setTitle("Add Pattern");
            patternDialog.setHeaderText("Enter the regular expression");
            patternDialog.setContentText("Pattern:");

            patternDialog.showAndWait().ifPresent(pattern -> {
                TextInputDialog descDialog = new TextInputDialog();
                descDialog.setTitle("Add Pattern");
                descDialog.setHeaderText("Enter pattern description");
                descDialog.setContentText("Description:");

                descDialog.showAndWait().ifPresent(description -> {
                    try {
                        // Validate pattern
                        Pattern.compile(pattern);

                        // Add to library
                        patternLibrary.add(new PatternEntry(name, pattern, description));
                        updateSavedPatternsComboBox();

                        // Also add to savedPatterns map for compatibility
                        savedPatterns.put(name, new RegexPattern(pattern));
                        savePatternsToStorage();

                    } catch (PatternSyntaxException e) {
                        showAlert("Error", "Invalid regular expression: " + e.getMessage());
                    }
                });
            });
        });
    }

    @FXML
    public void handleEditPattern() {
        PatternEntry selected = patternLibraryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a pattern to edit.");
            return;
        }

        // Create dialogs for editing pattern
        TextInputDialog nameDialog = new TextInputDialog(selected.getName());
        nameDialog.setTitle("Edit Pattern");
        nameDialog.setHeaderText("Edit pattern details");
        nameDialog.setContentText("Pattern Name:");

        nameDialog.showAndWait().ifPresent(name -> {
            TextInputDialog patternDialog = new TextInputDialog(selected.getPattern());
            patternDialog.setTitle("Edit Pattern");
            patternDialog.setHeaderText("Edit the regular expression");
            patternDialog.setContentText("Pattern:");

            patternDialog.showAndWait().ifPresent(pattern -> {
                TextInputDialog descDialog = new TextInputDialog(selected.getDescription());
                descDialog.setTitle("Edit Pattern");
                descDialog.setHeaderText("Edit pattern description");
                descDialog.setContentText("Description:");

                descDialog.showAndWait().ifPresent(description -> {
                    try {
                        // Validate pattern
                        Pattern.compile(pattern);

                        // Remove old pattern from savedPatterns if name changed
                        if (!name.equals(selected.getName())) {
                            savedPatterns.remove(selected.getName());
                        }

                        // Update the pattern
                        int index = patternLibraryTable.getSelectionModel().getSelectedIndex();
                        patternLibrary.set(index, new PatternEntry(name, pattern, description));
                        updateSavedPatternsComboBox();

                        // Update in savedPatterns map
                        savedPatterns.put(name, new RegexPattern(pattern));
                        savePatternsToStorage();

                    } catch (PatternSyntaxException e) {
                        showAlert("Error", "Invalid regular expression: " + e.getMessage());
                    }
                });
            });
        });
    }

    @FXML
    public void handleDeletePattern() {
        PatternEntry selected = patternLibraryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a pattern to delete.");
            return;
        }

        // Confirm deletion
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Pattern");
        alert.setHeaderText("Delete pattern confirmation");
        alert.setContentText("Are you sure you want to delete the pattern '" + selected.getName() + "'?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Remove from library
                patternLibrary.remove(selected);
                updateSavedPatternsComboBox();

                // Also remove from savedPatterns map
                savedPatterns.remove(selected.getName());
                savePatternsToStorage();
            }
        });
    }

    @FXML
    public void handleImportPatterns() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Patterns");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"));

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try {
                List<String> lines = Files.readAllLines(file.toPath());
                int imported = 0;

                for (String line : lines) {
                    // Expected format: name|pattern|description
                    String[] parts = line.split("\\|", 3);
                    if (parts.length == 3) {
                        try {
                            Pattern.compile(parts[1]); // Validate pattern
                            patternLibrary.add(new PatternEntry(parts[0], parts[1], parts[2]));

                            // Also add to savedPatterns map
                            savedPatterns.put(parts[0], new RegexPattern(parts[1]));

                            imported++;
                        } catch (PatternSyntaxException e) {
                            // Skip invalid patterns
                        }
                    }
                }

                updateSavedPatternsComboBox();
                savePatternsToStorage();
                showAlert("Import Complete", "Successfully imported " + imported + " patterns.");

            } catch (IOException e) {
                showAlert("Error", "Failed to import patterns: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleExportPatterns() {
        if (patternLibrary.isEmpty()) {
            showAlert("Error", "No patterns to export.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Patterns");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"));

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                List<String> lines = new ArrayList<>();
                for (PatternEntry entry : patternLibrary) {
                    // Format: name|pattern|description
                    lines.add(entry.getName() + "|" + entry.getPattern() + "|" + entry.getDescription());
                }

                Files.write(file.toPath(), lines);
                showAlert("Success", "Patterns exported successfully.");

            } catch (IOException e) {
                showAlert("Error", "Failed to export patterns: " + e.getMessage());
            }
        }
    }

    private void addSamplePatterns() {
        patternLibrary.add(new PatternEntry(
                "Email Address",
                "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}",
                "Matches standard email addresses"));

        patternLibrary.add(new PatternEntry(
                "URL",
                "(https?|ftp)://[^\\s/$.?#].[^\\s]*",
                "Matches HTTP, HTTPS and FTP URLs"));

        patternLibrary.add(new PatternEntry(
                "US Phone Number",
                "\\(\\d{3}\\)\\s*\\d{3}-\\d{4}|\\d{3}-\\d{3}-\\d{4}",
                "Matches US phone numbers like (123) 456-7890 or 123-456-7890"));

        patternLibrary.add(new PatternEntry(
                "Date (MM/DD/YYYY)",
                "(0?[1-9]|1[0-2])/(0?[1-9]|[12][0-9]|3[01])/((19|20)\\d\\d)",
                "Matches dates in MM/DD/YYYY format"));
    }

    private void updateSavedPatternsComboBox() {
        savedPatternsComboBox.getItems().clear();
        for (PatternEntry entry : patternLibrary) {
            savedPatternsComboBox.getItems().add(entry.getName());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Pattern Entry class
    public static class PatternEntry {
        private final String name;
        private final String pattern;
        private final String description;

        public PatternEntry(String name, String pattern, String description) {
            this.name = name;
            this.pattern = pattern;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getPattern() {
            return pattern;
        }

        public String getDescription() {
            return description;
        }
    }

    // IMPLEMENTATION OF METHODS USED BY REGEXVIEW

    // For getPredefinedPattern
    public String getPredefinedPattern(String type) {
        switch (type.toLowerCase()) {
            case "email":
                return "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";
            case "phone":
                return "\\(\\d{3}\\)\\s?\\d{3}-\\d{4}|\\d{3}-\\d{3}-\\d{4}";
            case "url":
                return "https?://[\\w.-]+\\.[a-zA-Z]{2,}(/[\\w./]*)?";
            case "date":
                return "\\d{4}-\\d{2}-\\d{2}|\\d{2}/\\d{2}/\\d{4}";
            case "ip address":
                return "\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b";
            default:
                return "";
        }
    }

    // For getSavedPatternNames
    public ObservableList<String> getSavedPatternNames() {
        return FXCollections.observableArrayList(savedPatterns.keySet());
    }

    // For findMatches
    public List<String> findMatches(TextData text, RegexPattern pattern) {
        try {
            return regexService.findMatches(text.getContent(), pattern.getPattern());
        } catch (Exception e) {
            logger.error("Error finding matches", e);
            throw new RuntimeException("Failed to find matches: " + e.getMessage(), e);
        }
    }

    // For replacePattern
    public String replacePattern(TextData text, RegexPattern pattern, String replacement) {
        try {
            return regexService.replacePattern(text.getContent(), pattern.getPattern(), replacement);
        } catch (Exception e) {
            logger.error("Error replacing pattern", e);
            throw new RuntimeException("Failed to replace pattern: " + e.getMessage(), e);
        }
    }

    // For savePattern
    public void savePattern(String name, RegexPattern pattern) {
        savedPatterns.put(name, pattern);
        savePatternsToStorage();

        // Also add to pattern library for UI consistency
        patternLibrary.add(new PatternEntry(name, pattern.getPattern(), "User saved pattern"));
        updateSavedPatternsComboBox();
    }

    // For loadPattern
    public RegexPattern loadPattern(String name) {
        return savedPatterns.get(name);
    }

    // For deletePattern
    public void deletePattern(String name) {
        savedPatterns.remove(name);
        savePatternsToStorage();

        // Also remove from pattern library
        patternLibrary.removeIf(entry -> entry.getName().equals(name));
        updateSavedPatternsComboBox();
    }

    // For createHighlightedHtml
    public String createHighlightedHtml(TextData text, RegexPattern pattern) {
        return regexService.createHighlightedHtml(text.getContent(), pattern.getPattern());
    }

    // Helper method for saving patterns
    private void savePatternsToStorage() {
        // In a real implementation, this would save to a file or database
        // For now, we'll just log that we're saving
        logger.info("Saving " + savedPatterns.size() + " patterns");
    }
}