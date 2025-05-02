package main.java.com.autotextengine.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Service for performing regex operations on text
 */
public class RegexService {
    private static final Logger logger = LogManager.getLogger(RegexService.class);

    /**
     * Find all matches of a regex pattern in a text
     *
     * @param text The text to search in
     * @param regex The regular expression pattern
     * @return A list of matched strings
     * @throws PatternSyntaxException If the regex pattern is invalid
     */
    public List<String> findMatches(String text, String regex) throws PatternSyntaxException {
        if (text == null || regex == null || text.isEmpty() || regex.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> matches = new ArrayList<>();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            matches.add(matcher.group());
        }

        logger.debug("Found {} matches for pattern: {}", matches.size(), regex);
        return matches;
    }

    /**
     * Find matches with capture groups
     *
     * @param text The text to search in
     * @param regex The regular expression pattern
     * @return A map of group names to lists of matched strings
     * @throws PatternSyntaxException If the regex pattern is invalid
     */
    public Map<String, List<String>> findMatchesWithGroups(String text, String regex) throws PatternSyntaxException {
        if (text == null || regex == null || text.isEmpty() || regex.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, List<String>> groupedMatches = new HashMap<>();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            for (int i = 0; i <= matcher.groupCount(); i++) {
                String groupName = i == 0 ? "Full match" : "Group " + i;
                String match = matcher.group(i);

                groupedMatches.computeIfAbsent(groupName, k -> new ArrayList<>()).add(match);
            }
        }

        logger.debug("Found matches in {} groups for pattern: {}", groupedMatches.size(), regex);
        return groupedMatches;
    }

    /**
     * Replace all occurrences of a pattern with replacement text
     *
     * @param text The text to perform replacement on
     * @param regex The regular expression pattern
     * @param replacement The replacement string
     * @return The text with all matches replaced
     * @throws PatternSyntaxException If the regex pattern is invalid
     */
    public String replacePattern(String text, String regex, String replacement) throws PatternSyntaxException {
        if (text == null || regex == null || text.isEmpty() || regex.isEmpty()) {
            return text;
        }

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        String result = matcher.replaceAll(replacement);

        logger.debug("Replaced pattern: {} with: {}", regex, replacement);
        return result;
    }

    /**
     * Create HTML with highlights for regex matches
     *
     * @param text The text to highlight
     * @param regex The regular expression pattern
     * @return HTML string with highlighted matches
     * @throws PatternSyntaxException If the regex pattern is invalid
     */
    public String createHighlightedHtml(String text, String regex) throws PatternSyntaxException {
        if (text == null || regex == null || text.isEmpty() || regex.isEmpty()) {
            return escapeHtml(text);
        }

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><style>");
        html.append(".match { background-color: yellow; color: black; }");
        html.append("body { font-family: Arial, sans-serif; white-space: pre-wrap; }");
        html.append("</style></head><body>");

        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(text);

            int lastEnd = 0;
            while (matcher.find()) {
                // Append text before the match
                html.append(escapeHtml(text.substring(lastEnd, matcher.start())));

                // Append the matched text with highlighting
                html.append("<span class=\"match\">")
                        .append(escapeHtml(matcher.group()))
                        .append("</span>");

                lastEnd = matcher.end();
            }

            // Append the remaining text
            if (lastEnd < text.length()) {
                html.append(escapeHtml(text.substring(lastEnd)));
            }

        } catch (Exception e) {
            logger.error("Error creating highlighted HTML", e);
            html.append(escapeHtml(text));
        }

        html.append("</body></html>");
        return html.toString();
    }

    /**
     * Validate that a regex pattern is syntactically correct
     *
     * @param regex The regular expression pattern to validate
     * @return True if the pattern is valid, false otherwise
     */
    public boolean validatePattern(String regex) {
        if (regex == null || regex.isEmpty()) {
            return false;
        }

        try {
            Pattern.compile(regex);
            return true;
        } catch (PatternSyntaxException e) {
            logger.debug("Invalid regex pattern: {}", regex);
            return false;
        }
    }

    /**
     * Escape special HTML characters
     */
    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}