package log;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.Level;

public class MyLogger {

    // Create a Logger instance
    private static final Logger logger = Logger.getLogger(MyLogger.class.getName());
    private static FileHandler fileHandler;

    public static void setupLogger() {
        if (fileHandler == null) { // Check if handler is already set
            try {
                // Specify the file where logs will be written in the source directory
                fileHandler = new FileHandler("app/src/main/java/com/log/app.log", true); // Append mode set to true
                
                // Set the format of the logs (you can customize this)
                SimpleFormatter formatter = new SimpleFormatter();
                fileHandler.setFormatter(formatter);

                // Set the logging level for the logger
                logger.setLevel(Level.ALL);

                // Add the handler to the logger
                logger.addHandler(fileHandler);
                
                logger.info("Logger initialized successfully."); // Log initialization success
            } catch (IOException e) {
                logger.severe("Failed to set up logger: " + e.getMessage());
            }
        }
    }

    // Method for logging informational messages
    public static void logInfo(String message) {
        logger.info(message);
    }

    // Method for logging error messages
    public static void logError(String message) {
        logger.severe(message);
    }
}
