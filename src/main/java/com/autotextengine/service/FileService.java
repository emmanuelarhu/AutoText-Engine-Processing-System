package main.java.com.autotextengine.service;

import main.java.com.autotextengine.model.TextData;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;

public class FileService {

    public TextData loadFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
        }
        File file = new File(filePath);
        return new TextData(content.toString(), file.getName());
    }

    public void saveFile(String filePath, String content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content);
        }
    }

    public TextData readTextFromFile(String filePath) throws IOException {
        return loadFile(filePath);
    }

    public void writeTextToFile(String filePath, String content) throws IOException {
        saveFile(filePath, content);
    }

    public File showOpenFileDialog() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Open Text File");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text files", "txt", "csv", "log", "xml", "json", "html"));
        int result = fileChooser.showOpenDialog(null);
        return (result == JFileChooser.APPROVE_OPTION) ? fileChooser.getSelectedFile() : null;
    }

    public File showSaveFileDialog() {
        return showSaveFileDialog(null);
    }

    public File showSaveFileDialog(String defaultFileName) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Text File");
        if (defaultFileName != null && !defaultFileName.isEmpty()) {
            fileChooser.setSelectedFile(new File(defaultFileName));
        }
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text files", "txt"));
        int result = fileChooser.showSaveDialog(null);
        return (result == JFileChooser.APPROVE_OPTION) ? fileChooser.getSelectedFile() : null;
    }
}
