package main.java.com.autotextengine.model;

import java.util.List;
import java.util.Map;

public class ProcessedResult {
    public enum ResultType {
        TEXT,
        NUMERIC,
        LIST,
        MAP
    }

    private String originalText;
    private String operationType;
    private List<String> matches;
    private String processedText;
    private long executionTimeMs;
    private long numericResult;
    private List<String> listResult;
    private Map<String, Long> mapResult;
    private ResultType resultType;

    // No-args constructor
    public ProcessedResult() {
        this.resultType = ResultType.TEXT;
    }

    // Constructor with all fields
    public ProcessedResult(String originalText, String operationType, List<String> matches,
                           String processedText, long executionTimeMs) {
        this.originalText = originalText;
        this.operationType = operationType;
        this.matches = matches;
        this.processedText = processedText;
        this.executionTimeMs = executionTimeMs;
        this.resultType = ResultType.TEXT;
    }

    // Constructor for numeric results
    public ProcessedResult(long numericResult) {
        this.numericResult = numericResult;
        this.resultType = ResultType.NUMERIC;
    }

    // Constructor for text results
    public ProcessedResult(String textResult) {
        this.processedText = textResult;
        this.resultType = ResultType.TEXT;
    }

    // Constructor for list results
    public ProcessedResult(List<String> listResult) {
        this.listResult = listResult;
        this.resultType = ResultType.LIST;
    }

    // Constructor for map results
    public ProcessedResult(Map<String, Long> mapResult) {
        this.mapResult = mapResult;
        this.resultType = ResultType.MAP;
    }

    // Getters and setters
    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public List<String> getMatches() {
        return matches;
    }

    public void setMatches(List<String> matches) {
        this.matches = matches;
    }

    public String getProcessedText() {
        return processedText;
    }

    public void setProcessedText(String processedText) {
        this.processedText = processedText;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public int getMatchCount() {
        return matches != null ? matches.size() : 0;
    }

    public ResultType getResultType() {
        return resultType;
    }

    public void setResultType(ResultType resultType) {
        this.resultType = resultType;
    }

    public String getTextResult() {
        return processedText;
    }

    public long getNumericResult() {
        return numericResult;
    }

    public void setNumericResult(long numericResult) {
        this.numericResult = numericResult;
        this.resultType = ResultType.NUMERIC;
    }

    public List<String> getListResult() {
        return listResult;
    }

    public void setListResult(List<String> listResult) {
        this.listResult = listResult;
        this.resultType = ResultType.LIST;
    }

    public Map<String, Long> getMapResult() {
        return mapResult;
    }

    public void setMapResult(Map<String, Long> mapResult) {
        this.mapResult = mapResult;
        this.resultType = ResultType.MAP;
    }
}