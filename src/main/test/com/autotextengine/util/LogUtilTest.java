package com.autotextengine.util;

import main.java.com.autotextengine.util.LogUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LogUtil class
 */
@ExtendWith(MockitoExtension.class)
public class LogUtilTest {

    @Mock
    private Logger mockLogger;

    @BeforeEach
    public void setUp() {
        // No additional setup needed
    }

    @Test
    public void testStartTiming() {
        try (MockedStatic<LogManager> mockedLogManager = Mockito.mockStatic(LogManager.class);
             MockedStatic<System> mockedSystem = Mockito.mockStatic(System.class)) {

            // Mock LogManager to return our mock logger
            mockedLogManager.when(() -> LogManager.getLogger(LogUtil.class)).thenReturn(mockLogger);

            // Mock System.currentTimeMillis() to return a fixed value
            long mockTime = 12345L;
            mockedSystem.when(System::currentTimeMillis).thenReturn(mockTime);

            // Call the method
            long result = LogUtil.startTiming("Test Operation");

            // Verify logger was called with correct message
            verify(mockLogger).debug("Starting operation: {}", "Test Operation");

            // Verify correct time was returned
            assertEquals(mockTime, result, "startTiming should return the current system time");
        }
    }

    @Test
    public void testEndTiming() {
        try (MockedStatic<LogManager> mockedLogManager = Mockito.mockStatic(LogManager.class)) {
            // Mock LogManager to return our mock logger
            mockedLogManager.when(() -> LogManager.getLogger(LogUtil.class)).thenReturn(mockLogger);

            // Instead of mocking System.currentTimeMillis(), use actual values
            long startTime = 12345L;

            // Call the method and capture the duration
            long duration = LogUtil.endTiming("Test Operation", startTime);

            // Verify logger was called
            verify(mockLogger).debug(contains("Operation: Test Operation completed in"), any(Long.class));

            // Verify the duration calculation logic
            assertTrue(duration >= 0, "Duration should be non-negative");
        }
    }

    @Test
    public void testEnterMethod() {
        try (MockedStatic<LogManager> mockedLogManager = Mockito.mockStatic(LogManager.class)) {
            // Mock LogManager to return our mock logger
            mockedLogManager.when(() -> LogManager.getLogger(LogUtil.class)).thenReturn(mockLogger);

            // Call the method
            LogUtil.enterMethod("TestClass", "testMethod");

            // Verify logger was called with correct message
            verify(mockLogger).trace("Entering method: {}.{}", "TestClass", "testMethod");
        }
    }

    @Test
    public void testExitMethod() {
        try (MockedStatic<LogManager> mockedLogManager = Mockito.mockStatic(LogManager.class)) {
            // Mock LogManager to return our mock logger
            mockedLogManager.when(() -> LogManager.getLogger(LogUtil.class)).thenReturn(mockLogger);

            // Call the method
            LogUtil.exitMethod("TestClass", "testMethod");

            // Verify logger was called with correct message
            verify(mockLogger).trace("Exiting method: {}.{}", "TestClass", "testMethod");
        }
    }

    @Test
    public void testLogException() {
        try (MockedStatic<LogManager> mockedLogManager = Mockito.mockStatic(LogManager.class)) {
            // Mock LogManager to return our mock logger
            mockedLogManager.when(() -> LogManager.getLogger(LogUtil.class)).thenReturn(mockLogger);

            // Create an exception
            Exception testException = new RuntimeException("Test exception message");

            // Call the method
            LogUtil.logException("Test error context", testException);

            // Verify logger was called with correct message and exception
            verify(mockLogger).error("{}: {}", "Test error context", "Test exception message", testException);
        }
    }

    @Test
    public void testRealTimingFunctionality() {
        // This test verifies actual timing functionality without mocks

        // Start timing
        long startTime = LogUtil.startTiming("Real timing test");

        // Ensure startTime is reasonable (close to current time)
        long currentTime = System.currentTimeMillis();
        assertTrue(Math.abs(currentTime - startTime) < 100,
                "startTiming should return a time close to current time");

        // Introduce a small delay
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            fail("Test was interrupted: " + e.getMessage());
        }

        // End timing
        long duration = LogUtil.endTiming("Real timing test", startTime);

        // Verify duration is reasonable (at least the sleep time)
        assertTrue(duration >= 50, "Duration should be at least the sleep time");
        assertTrue(duration < 1000, "Duration should not be unreasonably large");
    }

    @Test
    public void testEndTimingWithoutMockingSystem() {
        try (MockedStatic<LogManager> mockedLogManager = Mockito.mockStatic(LogManager.class)) {
            // Mock LogManager to return our mock logger
            mockedLogManager.when(() -> LogManager.getLogger(LogUtil.class)).thenReturn(mockLogger);

            // Use real time values
            long startTime = System.currentTimeMillis() - 100; // 100ms ago

            // Call the method
            long duration = LogUtil.endTiming("Test Operation", startTime);

            // Verify logger was called with correct message (use argument captor if needed)
            verify(mockLogger).debug(eq("Operation: {} completed in {}ms"),
                    eq("Test Operation"),
                    argThat(arg -> (Long)arg >= 100));

            // Verify correct duration was returned
            assertTrue(duration >= 100, "Duration should be at least 100ms");
            assertTrue(duration < 1000, "Duration should not be unreasonably large");
        }
    }
}