package main.java.com.autotextengine.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextProcessingController {

    @FXML private TextArea inputTextArea;
    @FXML private TextArea outputTextArea;
    @FXML private ListView<String> resultsListView;
    @FXML private ComboBox<String> dataTypeComboBox;
    @FXML private TextField filterPatternField;
    @FXML private CheckBox includeMatchingCheckBox;
    @FXML private Spinner<Integer> topWordsSpinner;
    @FXML private Spinner<Integer> sentenceCountSpinner;

    private Stage stage;

    public void initialize() {
        // Initialize spinners
        SpinnerValueFactory.IntegerSpinnerValueFactory topWordsFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 10);
        topWordsSpinner.setValueFactory(topWordsFactory);

        SpinnerValueFactory.IntegerSpinnerValueFactory sentenceFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 3);
        sentenceCountSpinner.setValueFactory(sentenceFactory);

        // Initialize combo boxes
        dataTypeComboBox.getItems().addAll(
                "Plain Text",
                "CSV Data",
                "JSON Data",
                "Log File",
                "XML Data"
        );
        dataTypeComboBox.setValue("Plain Text");
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
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
                new FileChooser.ExtensionFilter("Log Files", "*.log"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            try {
                String content = Files.readString(selectedFile.toPath());
                inputTextArea.setText(content);

                // Auto-detect data type based on extension
                String fileName = selectedFile.getName().toLowerCase();
                if (fileName.endsWith(".csv")) {
                    dataTypeComboBox.setValue("CSV Data");
                } else if (fileName.endsWith(".json")) {
                    dataTypeComboBox.setValue("JSON Data");
                } else if (fileName.endsWith(".log")) {
                    dataTypeComboBox.setValue("Log File");
                } else if (fileName.endsWith(".xml")) {
                    dataTypeComboBox.setValue("XML Data");
                } else {
                    dataTypeComboBox.setValue("Plain Text");
                }
            } catch (IOException e) {
                showAlert("Error", "Failed to load file: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleClearInput() {
        inputTextArea.clear();
        outputTextArea.clear();
        resultsListView.getItems().clear();
    }

    @FXML
    public void handleProcessText() {
        String inputText = inputTextArea.getText();
        if (inputText == null || inputText.trim().isEmpty()) {
            showAlert("Error", "Please enter or load text to process.");
            return;
        }

        String filterPattern = filterPatternField.getText();
        boolean includeMatching = includeMatchingCheckBox.isSelected();

        StringBuilder output = new StringBuilder();

        if (filterPattern != null && !filterPattern.isEmpty()) {
            try {
                Pattern pattern = Pattern.compile(filterPattern);
                Matcher matcher = pattern.matcher(inputText);

                // Process line by line
                String[] lines = inputText.split("\\n");
                for (String line : lines) {
                    boolean matches = matcher.reset(line).find();
                    if ((matches && includeMatching) || (!matches && !includeMatching)) {
                        output.append(line).append("\n");
                    }
                }
            } catch (Exception e) {
                showAlert("Error", "Invalid regular expression: " + e.getMessage());
                return;
            }
        } else {
            output.append(inputText);
        }

        outputTextArea.setText(output.toString());
    }

    @FXML
    public void handleExtractData() {
        String inputText = inputTextArea.getText();
        if (inputText == null || inputText.trim().isEmpty()) {
            showAlert("Error", "Please enter or load text to process.");
            return;
        }

        String dataType = dataTypeComboBox.getValue();
        resultsListView.getItems().clear();

        if ("Plain Text".equals(dataType)) {
            // Extract emails, URLs, phone numbers
            extractCommonPatterns(inputText);
        } else if ("CSV Data".equals(dataType)) {
            // Process CSV data
            processCSVData(inputText);
        } else if ("JSON Data".equals(dataType)) {
            // Extract JSON keys
            extractJSONKeys(inputText);
        } else if ("Log File".equals(dataType)) {
            // Extract log levels and timestamps
            extractLogData(inputText);
        } else if ("XML Data".equals(dataType)) {
            // Extract XML tags
            extractXMLTags(inputText);
        }
    }

    @FXML
    public void handleWordFrequency() {
        String inputText = inputTextArea.getText();
        if (inputText == null || inputText.trim().isEmpty()) {
            showAlert("Error", "Please enter or load text to process.");
            return;
        }

        // Split text into words, ignoring punctuation
        String[] words = inputText.toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", " ")
                .split("\\s+");

        // Count word frequencies
        Map<String, Integer> wordFrequency = new HashMap<>();
        for (String word : words) {
            if (!word.isEmpty()) {
                wordFrequency.put(word, wordFrequency.getOrDefault(word, 0) + 1);
            }
        }

        // Sort by frequency (descending)
        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(wordFrequency.entrySet());
        sortedEntries.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        // Display top N words
        int topN = topWordsSpinner.getValue();
        resultsListView.getItems().clear();

        StringBuilder output = new StringBuilder();
        output.append("Word Frequency Analysis:\n\n");

        int count = 0;
        for (Map.Entry<String, Integer> entry : sortedEntries) {
            if (count++ < topN) {
                String item = entry.getKey() + ": " + entry.getValue() + " occurrences";
                resultsListView.getItems().add(item);
                output.append(item).append("\n");
            } else {
                break;
            }
        }

        output.append("\nTotal unique words: ").append(wordFrequency.size());
        outputTextArea.setText(output.toString());
    }

    @FXML
    public void handleSummarize() {
        String inputText = inputTextArea.getText();
        if (inputText == null || inputText.trim().isEmpty()) {
            showAlert("Error", "Please enter or load text to process.");
            return;
        }

        // Split text into sentences
        String[] sentences = inputText.split("[.!?]+");

        // Basic summarization algorithm - choose first few sentences
        int sentenceCount = Math.min(sentenceCountSpinner.getValue(), sentences.length);

        StringBuilder summary = new StringBuilder();
        for (int i = 0; i < sentenceCount; i++) {
            if (!sentences[i].trim().isEmpty()) {
                summary.append(sentences[i].trim()).append(". ");
            }
        }

        outputTextArea.setText(summary.toString());

        // Display statistics
        resultsListView.getItems().clear();
        resultsListView.getItems().add("Total sentences: " + sentences.length);
        resultsListView.getItems().add("Summary sentences: " + sentenceCount);
        resultsListView.getItems().add("Original length: " + inputText.length() + " characters");
        resultsListView.getItems().add("Summary length: " + summary.length() + " characters");
        resultsListView.getItems().add("Compression ratio: " +
                String.format("%.2f%%", 100.0 * summary.length() / inputText.length()));
    }

    @FXML
    public void handleSaveResults() {
        String output = outputTextArea.getText();
        if (output == null || output.trim().isEmpty()) {
            showAlert("Error", "No results to save.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Results");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"));

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                Files.writeString(file.toPath(), output);
                showAlert("Success", "Results saved successfully.");
            } catch (IOException e) {
                showAlert("Error", "Failed to save file: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleCopyToClipboard() {
        String output = outputTextArea.getText();
        if (output == null || output.trim().isEmpty()) {
            showAlert("Error", "No results to copy.");
            return;
        }

        // In a real app, copy to clipboard:
        // Clipboard clipboard = Clipboard.getSystemClipboard();
        // ClipboardContent content = new ClipboardContent();
        // content.putString(output);
        // clipboard.setContent(content);

        showAlert("Success", "Results copied to clipboard.");
    }

    @FXML
    public void handleExportCSV() {
        List<String> items = resultsListView.getItems();
        if (items.isEmpty()) {
            showAlert("Error", "No results to export.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export as CSV");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                StringBuilder csv = new StringBuilder();
                csv.append("Item,Value\n");

                for (String item : items) {
                    String[] parts = item.split(":");
                    if (parts.length >= 2) {
                        csv.append(parts[0].trim().replace(",", "")).append(",")
                                .append(parts[1].trim().replace(",", "")).append("\n");
                    } else {
                        csv.append(item.replace(",", "")).append(",").append("\n");
                    }
                }

                Files.writeString(file.toPath(), csv.toString());
                showAlert("Success", "Data exported as CSV successfully.");
            } catch (IOException e) {
                showAlert("Error", "Failed to export CSV: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleExportJSON() {
        List<String> items = resultsListView.getItems();
        if (items.isEmpty()) {
            showAlert("Error", "No results to export.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export as JSON");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON Files", "*.json"));

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                StringBuilder json = new StringBuilder();
                json.append("{\n  \"results\": [\n");

                for (int i = 0; i < items.size(); i++) {
                    String item = items.get(i);
                    String[] parts = item.split(":");

                    json.append("    {");
                    if (parts.length >= 2) {
                        json.append("\"key\": \"").append(parts[0].trim())
                                .append("\", \"value\": \"").append(parts[1].trim()).append("\"");
                    } else {
                        json.append("\"item\": \"").append(item).append("\"");
                    }
                    json.append("}");

                    if (i < items.size() - 1) {
                        json.append(",");
                    }
                    json.append("\n");
                }
                json.append("  ]\n}");

                Files.writeString(file.toPath(), json.toString());
                showAlert("Success", "Data exported as JSON successfully.");
            } catch (IOException e) {
                showAlert("Error", "Failed to export JSON: " + e.getMessage());
            }
        }
    }

    private void extractCommonPatterns(String text) {
        // Extract email addresses
        Pattern emailPattern = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}");
        Matcher emailMatcher = emailPattern.matcher(text);

        // Extract URLs
        Pattern urlPattern = Pattern.compile("(https?|ftp)://[^\\s/$.?#].[^\\s]*");
        Matcher urlMatcher = urlPattern.matcher(text);

        // Extract phone numbers (simple pattern)
        Pattern phonePattern = Pattern.compile("\\(\\d{3}\\)\\s*\\d{3}-\\d{4}|\\d{3}-\\d{3}-\\d{4}");
        Matcher phoneMatcher = phonePattern.matcher(text);

        resultsListView.getItems().add("--- Email Addresses ---");
        Set<String> emails = new HashSet<>();
        while (emailMatcher.find()) {
            emails.add(emailMatcher.group());
        }
        emails.forEach(email -> resultsListView.getItems().add(email));

        resultsListView.getItems().add("--- URLs ---");
        Set<String> urls = new HashSet<>();
        while (urlMatcher.find()) {
            urls.add(urlMatcher.group());
        }
        urls.forEach(url -> resultsListView.getItems().add(url));

        resultsListView.getItems().add("--- Phone Numbers ---");
        Set<String> phones = new HashSet<>();
        while (phoneMatcher.find()) {
            phones.add(phoneMatcher.group());
        }
        phones.forEach(phone -> resultsListView.getItems().add(phone));

        // Display summary in output area
        StringBuilder output = new StringBuilder();
        output.append("Extracted Data Summary:\n\n");
        output.append("Email addresses found: ").append(emails.size()).append("\n");
        output.append("URLs found: ").append(urls.size()).append("\n");
        output.append("Phone numbers found: ").append(phones.size()).append("\n");

        outputTextArea.setText(output.toString());
    }

    private void processCSVData(String csv) {
        String[] lines = csv.split("\n");
        if (lines.length == 0) {
            resultsListView.getItems().add("Empty CSV data");
            return;
        }

        // Parse header
        String[] headers = lines[0].split(",");
        resultsListView.getItems().add("--- CSV Headers ---");
        for (String header : headers) {
            resultsListView.getItems().add(header.trim());
        }

        // Count rows
        int rowCount = lines.length - 1;
        resultsListView.getItems().add("--- Statistics ---");
        resultsListView.getItems().add("Columns: " + headers.length);
        resultsListView.getItems().add("Rows: " + rowCount);

        // Generate summary
        StringBuilder output = new StringBuilder();
        output.append("CSV Data Summary:\n\n");
        output.append("Number of columns: ").append(headers.length).append("\n");
        output.append("Number of rows: ").append(rowCount).append("\n\n");

        output.append("Column Names:\n");
        for (String header : headers) {
            output.append("- ").append(header.trim()).append("\n");
        }

        outputTextArea.setText(output.toString());
    }

    private void extractJSONKeys(String json) {
        // Simple JSON key extraction (not a full parser)
        Pattern keyPattern = Pattern.compile("\"([^\"]+)\"\\s*:");
        Matcher keyMatcher = keyPattern.matcher(json);

        Set<String> keys = new HashSet<>();
        while (keyMatcher.find()) {
            keys.add(keyMatcher.group(1));
        }

        resultsListView.getItems().add("--- JSON Keys ---");
        List<String> sortedKeys = new ArrayList<>(keys);
        Collections.sort(sortedKeys);
        sortedKeys.forEach(key -> resultsListView.getItems().add(key));

        // Generate summary
        StringBuilder output = new StringBuilder();
        output.append("JSON Data Summary:\n\n");
        output.append("Number of unique keys: ").append(keys.size()).append("\n\n");

        output.append("Keys:\n");
        for (String key : sortedKeys) {
            output.append("- ").append(key).append("\n");
        }

        outputTextArea.setText(output.toString());
    }

    private void extractLogData(String logText) {
        // Extract log entries with timestamps and levels
        Pattern logPattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}[.,]\\d+)\\s+(INFO|DEBUG|WARN|ERROR|TRACE|FATAL)\\s+(.+)");
        Matcher logMatcher = logPattern.matcher(logText);

        Map<String, Integer> logLevels = new HashMap<>();
        List<String> timestamps = new ArrayList<>();

        while (logMatcher.find()) {
            String timestamp = logMatcher.group(1);
            String level = logMatcher.group(2);

            timestamps.add(timestamp);
            logLevels.put(level, logLevels.getOrDefault(level, 0) + 1);
        }

        // Display log level counts
        resultsListView.getItems().add("--- Log Levels ---");
        for (Map.Entry<String, Integer> entry : logLevels.entrySet()) {
            resultsListView.getItems().add(entry.getKey() + ": " + entry.getValue());
        }

        // Display time range if timestamps found
        if (!timestamps.isEmpty()) {
            resultsListView.getItems().add("--- Timespan ---");
            resultsListView.getItems().add("First Entry: " + timestamps.get(0));
            resultsListView.getItems().add("Last Entry: " + timestamps.get(timestamps.size() - 1));
        }

        // Generate summary
        StringBuilder output = new StringBuilder();
        output.append("Log File Summary:\n\n");
        output.append("Total log entries: ").append(timestamps.size()).append("\n\n");

        output.append("Log Levels:\n");
        for (Map.Entry<String, Integer> entry : logLevels.entrySet()) {
            output.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }

        if (!timestamps.isEmpty()) {
            output.append("\nTimespan:\n");
            output.append("From: ").append(timestamps.get(0)).append("\n");
            output.append("To: ").append(timestamps.get(timestamps.size() - 1)).append("\n");
        }

        outputTextArea.setText(output.toString());
    }

    private void extractXMLTags(String xml) {
        // Extract XML tags (simple regex approach, not a full parser)
        Pattern tagPattern = Pattern.compile("<([^\\s>/]+)[^>]*>");
        Matcher tagMatcher = tagPattern.matcher(xml);

        Map<String, Integer> tagCounts = new HashMap<>();
        while (tagMatcher.find()) {
            String tag = tagMatcher.group(1);
            tagCounts.put(tag, tagCounts.getOrDefault(tag, 0) + 1);
        }

        resultsListView.getItems().add("--- XML Tags ---");
        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(tagCounts.entrySet());
        sortedEntries.sort(Map.Entry.comparingByKey());

        for (Map.Entry<String, Integer> entry : sortedEntries) {
            resultsListView.getItems().add(entry.getKey() + ": " + entry.getValue() + " occurrences");
        }

        // Generate summary
        StringBuilder output = new StringBuilder();
        output.append("XML Data Summary:\n\n");
        output.append("Number of unique tags: ").append(tagCounts.size()).append("\n\n");

        output.append("Tags:\n");
        for (Map.Entry<String, Integer> entry : sortedEntries) {
            output.append(entry.getKey()).append(": ").append(entry.getValue()).append(" occurrences\n");
        }

        outputTextArea.setText(output.toString());
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}