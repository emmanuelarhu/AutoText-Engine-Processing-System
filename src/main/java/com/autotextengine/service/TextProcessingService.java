
package main.java.com.autotextengine.service;

import main.java.com.autotextengine.model.ProcessedResult;
import main.java.com.autotextengine.model.TextData;

import java.util.List;
import java.util.stream.Collectors;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextProcessingService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\d{3}-\\d{3}-\\d{4}");
    private static final Pattern URL_PATTERN = Pattern.compile("https?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
    private static final Pattern DATE_PATTERN = Pattern.compile("\\d{1,2}[/.-]\\d{1,2}[/.-]\\d{2,4}");
    private static final Pattern IP_PATTERN = Pattern.compile("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b");

    public Map<String, Long> analyzeWordFrequency(TextData textData) {
        String text = textData.getContent().toLowerCase();
        String[] words = text.replaceAll("[^a-zA-Z0-9\\s]", "").split("\\s+");
        Map<String, Long> frequency = new HashMap<>();
        for (String word : words) {
            if (!word.trim().isEmpty()) {
                frequency.put(word, frequency.getOrDefault(word, 0L) + 1);
            }
        }
        return frequency;
    }

    public List<Map.Entry<String, Long>> getTopWords(TextData textData, int limit) {
        return analyzeWordFrequency(textData)
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public int countWords(TextData textData) {
        return textData.getContent().trim().split("\\s+").length;
    }

    public int countCharacters(TextData textData) {
        return textData.getContent().length();
    }

    public int countCharacters(TextData textData, boolean includeSpaces) {
        if (includeSpaces) {
            return textData.getContent().length();
        } else {
            return textData.getContent().replace(" ", "").length();
        }
    }

    public int countSentences(TextData textData) {
        String[] sentences = textData.getContent().split("[.!?]+");
        int count = 0;
        for (String sentence : sentences) {
            if (!sentence.trim().isEmpty()) {
                count++;
            }
        }
        return count;
    }

    public String transformCase(TextData textData, String caseType) {
        String content = textData.getContent();
        switch (caseType.toUpperCase()) {
            case "UPPER":
                return content.toUpperCase();
            case "LOWER":
                return content.toLowerCase();
            case "TITLE":
                return Arrays.stream(content.split("\\s+"))
                        .map(word -> word.isEmpty() ? word : Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase())
                        .collect(Collectors.joining(" "));
            default:
                return content;
        }
    }

    public List<String> extractSentences(TextData textData) {
        return Arrays.asList(textData.getContent().split("(?<=[.!?])\\s+"));
    }

    public List<ProcessedResult> processBatch(List<TextData> batch, String operationType) {
        List<ProcessedResult> results = new ArrayList<>();
        for (TextData textData : batch) {
            ProcessedResult result = new ProcessedResult();
            if (operationType.equalsIgnoreCase("WORD_COUNT")) {
                result.setNumericResult((long) countWords(textData));
            } else if (operationType.equalsIgnoreCase("CHARACTER_COUNT")) {
                result.setNumericResult((long) countCharacters(textData));
            }
            results.add(result);
        }
        return results;
    }

    public String summarizeText(TextData textData, int sentenceCount) {
        String[] sentences = textData.getContent().split("(?<=[.!?])\\s+");
        StringBuilder summary = new StringBuilder();
        for (int i = 0; i < Math.min(sentenceCount, sentences.length); i++) {
            summary.append(sentences[i]).append(" ");
        }
        return summary.toString().trim();
    }

    // Filter lines based on a keyword - Fix return type to match what the controller expects
    public ProcessedResult filterLines(TextData data, String keyword, boolean includeMatching) {
        String[] lines = data.getContent().split("\n");
        List<String> matchingLines = new ArrayList<>();

        for (String line : lines) {
            boolean matches = includeMatching ?
                    line.contains(keyword) :
                    !line.contains(keyword);

            if (matches) {
                matchingLines.add(line);
            }
        }

        String resultText = String.join("\n", matchingLines);

        ProcessedResult result = new ProcessedResult();
        result.setOriginalText(data.getContent());
        result.setOperationType("Filter Lines");
        result.setMatches(matchingLines);
        result.setProcessedText(resultText);
        result.setExecutionTimeMs(0); // You might want to track actual execution time

        return result;
    }

    // Extract structured data - Fix return type to match what the controller expects
    public ProcessedResult extractStructuredData(TextData data, String dataType) {
        String content = data.getContent();
        Pattern pattern;

        switch (dataType.toLowerCase()) {
            case "email":
                pattern = EMAIL_PATTERN;
                break;
            case "phone":
                pattern = PHONE_PATTERN;
                break;
            case "url":
                pattern = URL_PATTERN;
                break;
            case "date":
                pattern = DATE_PATTERN;
                break;
            case "ip":
                pattern = IP_PATTERN;
                break;
            default:
                throw new IllegalArgumentException("Unsupported data type: " + dataType);
        }

        List<String> matches = new ArrayList<>();
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            matches.add(matcher.group());
        }

        StringBuilder processedText = new StringBuilder();
        processedText.append("Extracted ").append(matches.size())
                .append(" ").append(dataType).append(" items:\n\n");

        for (int i = 0; i < matches.size(); i++) {
            processedText.append(i + 1).append(". ").append(matches.get(i)).append("\n");
        }

        ProcessedResult result = new ProcessedResult();
        result.setOriginalText(content);
        result.setOperationType("Extract " + dataType);
        result.setMatches(matches);
        result.setProcessedText(processedText.toString());
        result.setExecutionTimeMs(0); // You might want to track actual execution time

        return result;
    }

    // Generate a text summary - Fix to return String as expected by the controller
    public String generateTextSummary(TextData data, int summaryLength) {
        String[] sentences = data.getContent().split("(?<=[.!?])\\s+");
        StringBuilder summary = new StringBuilder();
        for (int i = 0; i < Math.min(summaryLength, sentences.length); i++) {
            summary.append(sentences[i]).append(" ");
        }
        return summary.toString().trim();
    }
}
