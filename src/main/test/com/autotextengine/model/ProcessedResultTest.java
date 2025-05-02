package com.autotextengine.model;

import main.java.com.autotextengine.model.ProcessedResult;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ProcessedResult class
 */
public class ProcessedResultTest {

    @Test
    public void testDefaultConstructor() {
        ProcessedResult result = new ProcessedResult();
        assertEquals(ProcessedResult.ResultType.TEXT, result.getResultType(),
                "Default constructor should set TEXT result type");
        assertNull(result.getTextResult(), "Default constructor should initialize with null text result");
    }

    @Test
    public void testConstructorWithText() {
        String textResult = "Sample result";
        ProcessedResult result = new ProcessedResult(textResult);

        assertEquals(ProcessedResult.ResultType.TEXT, result.getResultType(),
                "Constructor with text should set TEXT result type");
        assertEquals(textResult, result.getTextResult(),
                "Constructor with text should set the text result");
    }

    @Test
    public void testConstructorWithNumericResult() {
        long numericResult = 42L;
        ProcessedResult result = new ProcessedResult(numericResult);

        assertEquals(ProcessedResult.ResultType.NUMERIC, result.getResultType(),
                "Constructor with numeric value should set NUMERIC result type");
        assertEquals(numericResult, result.getNumericResult(),
                "Constructor with numeric value should set the numeric result");
    }

    @Test
    public void testConstructorWithListResult() {
        List<String> listResult = Arrays.asList("item1", "item2", "item3");
        ProcessedResult result = new ProcessedResult(listResult);

        assertEquals(ProcessedResult.ResultType.LIST, result.getResultType(),
                "Constructor with list should set LIST result type");
        assertEquals(listResult, result.getListResult(),
                "Constructor with list should set the list result");
    }

    @Test
    public void testConstructorWithMapResult() {
        Map<String, Long> mapResult = new HashMap<>();
        mapResult.put("key1", 1L);
        mapResult.put("key2", 2L);

        ProcessedResult result = new ProcessedResult(mapResult);

        assertEquals(ProcessedResult.ResultType.MAP, result.getResultType(),
                "Constructor with map should set MAP result type");
        assertEquals(mapResult, result.getMapResult(),
                "Constructor with map should set the map result");
    }

    @Test
    public void testConstructorWithAllParameters() {
        String originalText = "Original text";
        String operationType = "Test Operation";
        List<String> matches = Arrays.asList("match1", "match2");
        String processedText = "Processed text";
        long executionTime = 123L;

        ProcessedResult result = new ProcessedResult(
                originalText, operationType, matches, processedText, executionTime);

        assertEquals(ProcessedResult.ResultType.TEXT, result.getResultType(),
                "Full constructor should set TEXT result type");
        assertEquals(originalText, result.getOriginalText(),
                "Full constructor should set the original text");
        assertEquals(operationType, result.getOperationType(),
                "Full constructor should set the operation type");
        assertEquals(matches, result.getMatches(),
                "Full constructor should set the matches list");
        assertEquals(processedText, result.getProcessedText(),
                "Full constructor should set the processed text");
        assertEquals(executionTime, result.getExecutionTimeMs(),
                "Full constructor should set the execution time");
    }

    @Test
    public void testSetAndGetOriginalText() {
        ProcessedResult result = new ProcessedResult();
        String originalText = "Original text";

        result.setOriginalText(originalText);
        assertEquals(originalText, result.getOriginalText(),
                "getOriginalText should return what was set");
    }

    @Test
    public void testSetAndGetOperationType() {
        ProcessedResult result = new ProcessedResult();
        String operationType = "Test Operation";

        result.setOperationType(operationType);
        assertEquals(operationType, result.getOperationType(),
                "getOperationType should return what was set");
    }

    @Test
    public void testSetAndGetMatches() {
        ProcessedResult result = new ProcessedResult();
        List<String> matches = Arrays.asList("match1", "match2");

        result.setMatches(matches);
        assertEquals(matches, result.getMatches(),
                "getMatches should return what was set");
    }

    @Test
    public void testSetAndGetProcessedText() {
        ProcessedResult result = new ProcessedResult();
        String processedText = "Processed text";

        result.setProcessedText(processedText);
        assertEquals(processedText, result.getProcessedText(),
                "getProcessedText should return what was set");
        assertEquals(processedText, result.getTextResult(),
                "getTextResult should return the same as getProcessedText");
    }

    @Test
    public void testSetAndGetExecutionTimeMs() {
        ProcessedResult result = new ProcessedResult();
        long executionTime = 123L;

        result.setExecutionTimeMs(executionTime);
        assertEquals(executionTime, result.getExecutionTimeMs(),
                "getExecutionTimeMs should return what was set");
    }

    @Test
    public void testGetMatchCount() {
        ProcessedResult result = new ProcessedResult();

        // Test with null matches
        assertEquals(0, result.getMatchCount(),
                "getMatchCount should return 0 for null matches");

        // Test with empty matches
        result.setMatches(Collections.emptyList());
        assertEquals(0, result.getMatchCount(),
                "getMatchCount should return 0 for empty matches list");

        // Test with matches
        List<String> matches = Arrays.asList("match1", "match2", "match3");
        result.setMatches(matches);
        assertEquals(3, result.getMatchCount(),
                "getMatchCount should return the size of the matches list");
    }

    @Test
    public void testSetAndGetResultType() {
        ProcessedResult result = new ProcessedResult();

        result.setResultType(ProcessedResult.ResultType.NUMERIC);
        assertEquals(ProcessedResult.ResultType.NUMERIC, result.getResultType(),
                "getResultType should return what was set");
    }

    @Test
    public void testSetAndGetNumericResult() {
        ProcessedResult result = new ProcessedResult();
        long numericResult = 42L;

        result.setNumericResult(numericResult);
        assertEquals(numericResult, result.getNumericResult(),
                "getNumericResult should return what was set");
        assertEquals(ProcessedResult.ResultType.NUMERIC, result.getResultType(),
                "setNumericResult should update result type to NUMERIC");
    }

    @Test
    public void testSetAndGetListResult() {
        ProcessedResult result = new ProcessedResult();
        List<String> listResult = Arrays.asList("item1", "item2", "item3");

        result.setListResult(listResult);
        assertEquals(listResult, result.getListResult(),
                "getListResult should return what was set");
        assertEquals(ProcessedResult.ResultType.LIST, result.getResultType(),
                "setListResult should update result type to LIST");
    }

    @Test
    public void testSetAndGetMapResult() {
        ProcessedResult result = new ProcessedResult();
        Map<String, Long> mapResult = new HashMap<>();
        mapResult.put("key1", 1L);
        mapResult.put("key2", 2L);

        result.setMapResult(mapResult);
        assertEquals(mapResult, result.getMapResult(),
                "getMapResult should return what was set");
        assertEquals(ProcessedResult.ResultType.MAP, result.getResultType(),
                "setMapResult should update result type to MAP");
    }
}