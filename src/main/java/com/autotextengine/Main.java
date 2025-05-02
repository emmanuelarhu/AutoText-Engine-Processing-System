package main.java.com.autotextengine;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main extends Application {
    private static final Logger logger = LogManager.getLogger(Main.class);

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the main view FXML with correct resource path
            // Note: Resources in the resources folder are accessed without the 'main/resources/' prefix
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main-view.fxml"));            Parent root = loader.load();

            // Set up the scene and stage
            Scene scene = new Scene(root);

            // Optional: Add CSS if you have it
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            primaryStage.setScene(scene);
            primaryStage.setTitle("AutoText Engine Processing System");
            primaryStage.show();

            logger.info("Application UI initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize application UI", e);
            e.printStackTrace();
            // Show more detailed error information in console
            System.err.println("Error loading FXML: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Cause: " + e.getCause().getMessage());
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}