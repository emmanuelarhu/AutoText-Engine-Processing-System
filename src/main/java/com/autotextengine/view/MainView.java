package main.java.com.autotextengine.view;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class MainView {
    @FXML
    private Label statusLabel;

    @FXML
    private Label versionLabel;

    @FXML
    private TextArea inputTextArea; // Assuming you have this in your FXML

    @FXML
    private TextArea outputTextArea; // Assuming you have this in your FXML

    public void initialize() {
        // Set version info
        versionLabel.setText("Version 1.0.0");
        statusLabel.setText("Ready");
    }

    @FXML
    public void handleClearInput() {
        if (inputTextArea != null) {
            inputTextArea.clear();
            updateStatus("Input cleared");
        }
    }

    @FXML
    public void handleClearOutput() {
        if (outputTextArea != null) {
            outputTextArea.clear();
            updateStatus("Output cleared");
        }
    }

    @FXML
    public void handleExitAction() {
        Platform.exit();
    }

    @FXML
    public void showHelp() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("AutoText Engine Help");
        alert.setHeaderText("AutoText Engine Processing System");

        // Create a more detailed and better structured help content
        StringBuilder content = new StringBuilder();
        content.append("AutoText Engine is a comprehensive text processing application designed for text analysis, manipulation, and transformation.\n\n");

        content.append("MAIN FEATURES:\n\n");

        content.append("• Text Processing\n");
        content.append("  - Character, word, line, and sentence counting\n");
        content.append("  - Case conversion (uppercase, lowercase, title case)\n");
        content.append("  - Text summarization and format normalization\n");
        content.append("  - Word frequency analysis\n\n");

        content.append("• Regular Expression Operations\n");
        content.append("  - Pattern matching and extraction\n");
        content.append("  - Text replacement using regex patterns\n");
        content.append("  - Predefined patterns library (emails, URLs, phone numbers, etc.)\n");
        content.append("  - Pattern testing and visualization\n\n");

        content.append("• Batch Processing\n");
        content.append("  - Process multiple files in one operation\n");
        content.append("  - Filter files by extension, name pattern, or size\n");
        content.append("  - Apply operations to selected files\n");
        content.append("  - Customizable output formats\n\n");

        content.append("QUICK TIPS:\n\n");
        content.append("• Navigate between tabs to access different functionality\n");
        content.append("• Save your work frequently using the Save button\n");
        content.append("• Use the pattern library in Regex Operations to access common patterns\n");
        content.append("• For batch operations, configure filters before browsing for files\n\n");

        content.append("For more information or support, visit our documentation or contact support at emmanuelarhu706@gmail.com");

        // Set the content and show the dialog
        alert.setContentText(content.toString());

        // Make the dialog resizable for better readability of the larger content
        alert.getDialogPane().setMinHeight(400);
        alert.getDialogPane().setMinWidth(500);
        alert.setResizable(true);

        alert.showAndWait();
    }

    @FXML
    public void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About AutoText Engine");
        alert.setHeaderText("AutoText Engine");

        // Enhanced about dialog with more information
        StringBuilder content = new StringBuilder();
        content.append("AutoText Engine Text Processing System\n");
        content.append("Version 1.0.0\n\n");
        content.append("A powerful tool for text analysis, regex operations, and batch processing.\n\n");
        content.append("Developed by: Emmanuel Arhu\n");
        content.append("Copyright © 2025\n");
        content.append("All rights reserved.\n\n");
        content.append("www.autotextengine-example.com");

        alert.setContentText(content.toString());
        alert.showAndWait();
    }

    public void updateStatus(String message) {
        statusLabel.setText(message);
    }

    @FXML
    public void handleImportAction() {
        updateStatus("Importing file...");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
                new FileChooser.ExtensionFilter("JSON Files", "*.json"),
                new FileChooser.ExtensionFilter("XML Files", "*.xml"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(statusLabel.getScene().getWindow());
        if (selectedFile != null) {
            try {
                // Get the currently active tab to determine which view to send the content to
                TabPane tabPane = (TabPane) statusLabel.getScene().lookup("TabPane");
                if (tabPane != null) {
                    Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
                    String tabText = selectedTab.getText();

                    // Read the file content
                    String content = new String(Files.readAllBytes(selectedFile.toPath()));

                    // Find the appropriate TextArea based on the active tab
                    TextArea inputTextArea = null;
                    if ("Text Processing".equals(tabText)) {
                        inputTextArea = (TextArea) selectedTab.getContent().lookup("#inputTextArea");
                    } else if ("Regex Operations".equals(tabText)) {
                        inputTextArea = (TextArea) selectedTab.getContent().lookup("#inputTextArea");
                    } else if ("Batch Processing".equals(tabText)) {
                        // For batch processing, we'd add the file to the list instead
                        // This would depend on the specific implementation
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Batch Import");
                        alert.setHeaderText("Batch Processing");
                        alert.setContentText("The file has been added to the batch processing queue.");
                        alert.showAndWait();
                        updateStatus("File added to batch processing: " + selectedFile.getName());
                        return;
                    }

                    // Set the content to the found TextArea
                    if (inputTextArea != null) {
                        inputTextArea.setText(content);
                        updateStatus("File imported successfully: " + selectedFile.getName());
                    } else {
                        updateStatus("Could not find input area in the current tab");
                    }
                }
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Import Error");
                alert.setHeaderText("Error importing file");
                alert.setContentText("Could not read the selected file:\n" + e.getMessage());
                alert.showAndWait();

                updateStatus("Error importing file: " + e.getMessage());
            }
        } else {
            updateStatus("Import canceled");
        }
    }

    @FXML
    public void handleExportAction() {
        updateStatus("Exporting file...");

        // Try to get the currently active content from the active tab
        TextArea outputTextArea = findActiveOutputTextArea();

        if (outputTextArea == null || outputTextArea.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Export Warning");
            alert.setHeaderText("No Content to Export");
            alert.setContentText("There is no content to export. Please process some data first.");
            alert.showAndWait();

            updateStatus("Export canceled: No content to export");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
                new FileChooser.ExtensionFilter("JSON Files", "*.json"),
                new FileChooser.ExtensionFilter("XML Files", "*.xml")
        );

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = now.format(formatter);
        fileChooser.setInitialFileName("AutoText Engine_Export_" + timestamp + ".txt");

        File file = fileChooser.showSaveDialog(statusLabel.getScene().getWindow());
        if (file != null) {
            try {
                // Write the content to the selected file
                Files.write(file.toPath(), outputTextArea.getText().getBytes());

                updateStatus("File exported successfully: " + file.getName());
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Export Error");
                alert.setHeaderText("Error exporting file");
                alert.setContentText("Could not write to the selected file:\n" + e.getMessage());
                alert.showAndWait();

                updateStatus("Error exporting file: " + e.getMessage());
            }
        } else {
            updateStatus("Export canceled");
        }
    }

    @FXML
    public void handleProcessAction() {
        updateStatus("Processing data...");

        // Try to find the active tab and trigger its processing action
        TabPane tabPane = (TabPane) statusLabel.getScene().lookup("TabPane");
        if (tabPane != null) {
            Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
            String tabText = selectedTab.getText();

            Button processButton = null;

            // Find the appropriate process button based on the active tab
            if ("Text Processing".equals(tabText)) {
                processButton = (Button) selectedTab.getContent().lookup("Button[text~='Process']");
            } else if ("Regex Operations".equals(tabText)) {
                processButton = (Button) selectedTab.getContent().lookup("Button[text~='Find Matches']");
            } else if ("Batch Processing".equals(tabText)) {
                processButton = (Button) selectedTab.getContent().lookup("Button[text~='Start Processing']");
            }

            // Fire the process button's action
            if (processButton != null) {
                processButton.fire();
                // Status update will be handled by the specific view controller
            } else {
                // If no process button found, show a general message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Process Action");
                alert.setHeaderText("Process Data");
                alert.setContentText("Processing initiated in " + tabText + " tab.");
                alert.showAndWait();

                updateStatus("Processing complete in " + tabText);
            }
        }
    }

    @FXML
    public void handleSettingsAction() {
        updateStatus("Opening settings...");

        // Create a custom dialog for settings
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("AutoText Engine Settings");
        dialog.setHeaderText("Application Settings");

        // Set the button types (OK and Cancel)
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the settings form layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Create settings controls

        // General Settings Section
        Label generalSettingsLabel = new Label("General Settings");
        generalSettingsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        grid.add(generalSettingsLabel, 0, 0, 2, 1);

        CheckBox autosaveCheckbox = new CheckBox("Enable auto-save");
        autosaveCheckbox.setSelected(true);
        grid.add(new Label("Auto-save:"), 0, 1);
        grid.add(autosaveCheckbox, 1, 1);

        ComboBox<String> themesComboBox = new ComboBox<>();
        themesComboBox.getItems().addAll("Default Theme", "Dark Theme", "High Contrast");
        themesComboBox.setValue("Default Theme");
        grid.add(new Label("Application Theme:"), 0, 2);
        grid.add(themesComboBox, 1, 2);

        // Processing Settings Section
        Label processingSettingsLabel = new Label("Processing Settings");
        processingSettingsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        grid.add(processingSettingsLabel, 0, 3, 2, 1);
        grid.add(new Separator(), 0, 4, 2, 1);

        TextField defaultEncodingField = new TextField("UTF-8");
        grid.add(new Label("Default Encoding:"), 0, 5);
        grid.add(defaultEncodingField, 1, 5);

        Spinner<Integer> maxFileSizeSpinner = new Spinner<>(1, 100, 10);
        grid.add(new Label("Max File Size (MB):"), 0, 6);
        grid.add(maxFileSizeSpinner, 1, 6);

        CheckBox processingLoggingCheckbox = new CheckBox("Enable detailed processing logs");
        grid.add(new Label("Processing Logs:"), 0, 7);
        grid.add(processingLoggingCheckbox, 1, 7);

        // Output Settings Section
        Label outputSettingsLabel = new Label("Output Settings");
        outputSettingsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        grid.add(outputSettingsLabel, 0, 8, 2, 1);
        grid.add(new Separator(), 0, 9, 2, 1);

        ComboBox<String> defaultFormatComboBox = new ComboBox<>();
        defaultFormatComboBox.getItems().addAll("TXT", "CSV", "JSON", "XML");
        defaultFormatComboBox.setValue("TXT");
        grid.add(new Label("Default Output Format:"), 0, 10);
        grid.add(defaultFormatComboBox, 1, 10);

        TextField outputDirectoryField = new TextField(System.getProperty("user.home"));
        Button browseButton = new Button("Browse");
        HBox outputDirBox = new HBox(5, outputDirectoryField, browseButton);
        grid.add(new Label("Default Output Directory:"), 0, 11);
        grid.add(outputDirBox, 1, 11);

        // Browse button action
        browseButton.setOnAction(e -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Select Default Output Directory");
            if (!outputDirectoryField.getText().isEmpty()) {
                File currentDir = new File(outputDirectoryField.getText());
                if (currentDir.exists()) {
                    directoryChooser.setInitialDirectory(currentDir);
                }
            }

            File selectedDirectory = directoryChooser.showDialog(dialog.getOwner());
            if (selectedDirectory != null) {
                outputDirectoryField.setText(selectedDirectory.getAbsolutePath());
            }
        });

        // Set the content for the dialog
        dialog.getDialogPane().setContent(grid);

        // Make the dialog resizable
        dialog.setResizable(true);

        // Show the dialog and process the result
        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == saveButtonType) {
            // Here you would normally save the settings to a configuration file
            // For this example, we'll just update the status
            updateStatus("Settings updated");
        } else {
            updateStatus("Settings canceled");
        }
    }

    // Helper method to find the active output TextArea
    private TextArea findActiveOutputTextArea() {
        TabPane tabPane = (TabPane) statusLabel.getScene().lookup("TabPane");
        if (tabPane != null) {
            Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
            String tabText = selectedTab.getText();

            TextArea outputTextArea = null;

            if ("Text Processing".equals(tabText)) {
                outputTextArea = (TextArea) selectedTab.getContent().lookup("#outputTextArea");
            } else if ("Regex Operations".equals(tabText)) {
                // For regex operations, the output might be in a different control
                outputTextArea = (TextArea) selectedTab.getContent().lookup("#resultTextArea");
                if (outputTextArea == null) {
                    outputTextArea = (TextArea) selectedTab.getContent().lookup("#outputTextArea");
                }
            } else if ("Batch Processing".equals(tabText)) {
                // For batch processing, we might not have a direct TextArea output
                // This would depend on the specific implementation
                return null;
            }

            return outputTextArea;
        }

        return null;
    }
}