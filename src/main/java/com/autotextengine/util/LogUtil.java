package main.java.com.autotextengine.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class for logging operations
 */
public class LogUtil {
    private static final Logger logger = LogManager.getLogger(LogUtil.class);

    /**
     * Start timing an operation
     *
     * @param operation Name of the operation
     * @return Start time in milliseconds
     */
    public static long startTiming(String operation) {
        logger.debug("Starting operation: {}", operation);
        return System.currentTimeMillis();
    }

    /**
     * End timing an operation and log the duration
     *
     * @param operation Name of the operation
     * @param startTime Start time in milliseconds
     * @return Duration in milliseconds
     */
    public static long endTiming(String operation, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        logger.debug("Operation: {} completed in {}ms", operation, duration);
        return duration;
    }

    /**
     * Log method entry
     *
     * @param className Class name
     * @param methodName Method name
     */
    public static void enterMethod(String className, String methodName) {
        logger.trace("Entering method: {}.{}", className, methodName);
    }

    /**
     * Log method exit
     *
     * @param className Class name
     * @param methodName Method name
     */
    public static void exitMethod(String className, String methodName) {
        logger.trace("Exiting method: {}.{}", className, methodName);
    }

    /**
     * Log an exception with a custom message
     *
     * @param message Error context message
     * @param exception The exception to log
     */
    public static void logException(String message, Exception exception) {
        logger.error("{}: {}", message, exception.getMessage(), exception);
    }
}