package main.java.com.autotextengine.model;

import java.io.Serializable;

/**
 * Represents a regular expression pattern with associated flags
 */
public class RegexPattern implements Serializable {
    private static final long serialVersionUID = 1L;

    private String pattern;
    private String name;
    private String description;
    private boolean caseSensitive = true;
    private boolean multiline = false;
    private boolean dotAll = false;

    public RegexPattern(String pattern) {
        this.pattern = pattern;
    }

    public RegexPattern(String pattern, String name, String description) {
        this.pattern = pattern;
        this.name = name;
        this.description = description;
    }

    public RegexPattern(String pattern, boolean caseSensitive, boolean multiline, boolean dotAll) {
        this.pattern = pattern;
        this.caseSensitive = caseSensitive;
        this.multiline = multiline;
        this.dotAll = dotAll;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public boolean isMultiline() {
        return multiline;
    }

    public void setMultiline(boolean multiline) {
        this.multiline = multiline;
    }

    public boolean isDotAll() {
        return dotAll;
    }

    public void setDotAll(boolean dotAll) {
        this.dotAll = dotAll;
    }

    public int getFlags() {
        int flags = 0;
        if (!caseSensitive) {
            flags |= java.util.regex.Pattern.CASE_INSENSITIVE;
        }
        if (multiline) {
            flags |= java.util.regex.Pattern.MULTILINE;
        }
        if (dotAll) {
            flags |= java.util.regex.Pattern.DOTALL;
        }
        return flags;
    }

    @Override
    public String toString() {
        return "RegexPattern{" +
                "pattern='" + pattern + '\'' +
                ", caseSensitive=" + caseSensitive +
                ", multiline=" + multiline +
                ", dotAll=" + dotAll +
                '}';
    }
}