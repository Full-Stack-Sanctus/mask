package log;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.Level;



public class MyLogger {

    // Create a Logger instance
    private static final Logger logger = Logger.getLogger(MyLogger.class.getName());

    public static void setupLogger() {
        try {
            // Specify the file where logs will be written
            FileHandler fileHandler = new FileHandler("app/src/main/java/com/log/app.log", true); // true to append to existing log file
            
            // Set the format of the logs (you can customize this)
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);
            
            // Add the handler to the logger
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            logger.severe("Failed to set up logger: " + e.getMessage());
        }
    }

    // Example of logging an info message
    public static void logInfo(String message) {
        logger.info(message);
    }

    // Example of logging an error message
    public static void logError(String message) {
        logger.severe(message);
    }
}
