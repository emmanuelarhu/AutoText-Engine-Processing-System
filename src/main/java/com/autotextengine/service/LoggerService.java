package main.java.com.autotextengine.service;

import java.io.IOException;

public class LoggerService {

    public static void logException(String message, Exception e) {
        System.err.println(message + ": " + e.getClass().getSimpleName() + " - " + e.getMessage());
        e.printStackTrace();
    }

    public static void logException(String message, IOException e) {
        System.err.println(message + ": " + e.getClass().getSimpleName() + " - " + e.getMessage());
        e.printStackTrace();
    }
}