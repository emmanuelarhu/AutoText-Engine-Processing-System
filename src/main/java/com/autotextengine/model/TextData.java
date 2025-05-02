package main.java.com.autotextengine.model;

import java.io.Serializable;

/**
 * Represents text data for processing
 */
public class TextData implements Serializable {
    private static final long serialVersionUID = 1L;

    private String content;
    private String filePath;

    public TextData(String content) {
        this.content = content;
    }

    public TextData(String content, String filePath) {
        this.content = content;
        this.filePath = filePath;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Count the number of characters in the text
     * @return The character count
     */
    public int getCharacterCount() {
        return content != null ? content.length() : 0;
    }

    /**
     * Count the number of words in the text
     * @return The word count
     */
    public int getWordCount() {
        if (content == null || content.trim().isEmpty()) {
            return 0;
        }

        String[] words = content.trim().split("\\s+");
        return words.length;
    }

    /**
     * Count the number of lines in the text
     * @return The line count
     */
    public int getLineCount() {
        if (content == null || content.isEmpty()) {
            return 0;
        }

        String[] lines = content.split("\\r\\n|\\r|\\n");
        return lines.length;
    }
}