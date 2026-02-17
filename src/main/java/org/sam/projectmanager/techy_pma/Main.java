package org.sam.projectmanager.techy_pma;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

import static org.sam.projectmanager.techy_pma.database.DatabaseManager.initializeDatabase;

/**
 * Application entry point for Techy PMA — Project Management System.
 *
 * <p>Extends {@link Application} as required by JavaFX. The JVM launches
 * the app by calling {@link #start(Stage)}, which initializes the database
 * and loads the Login screen as the first scene.</p>
 *
 * <p>The {@code main()} method is not required for JavaFX 11+ when using
 * the Maven JavaFX plugin, but can be added if launching outside the plugin.</p>
 */
public class Main extends Application {

    /**
     * JavaFX entry point — called automatically after the JavaFX runtime initializes.
     *
     * <p>Performs three startup tasks in order:</p>
     * <ol>
     *   <li>Initializes the SQLite database and creates tables if they don't exist</li>
     *   <li>Loads the Login screen FXML as the first scene</li>
     *   <li>Configures and displays the primary application window</li>
     * </ol>
     *
     * @param stage The primary window provided by the JavaFX runtime
     * @throws IOException if the login.fxml file cannot be found or loaded
     */
    @Override
    public void start(Stage stage) throws IOException {

        // DatabaseManager — creates tables on first run, safe to call every startup
        initializeDatabase();

        // DEBUG: prints the resolved FXML resource path to verify correct location at runtime
        System.out.println(Main.class.getResource("fxml/login.fxml"));

        // Full classpath used because resources mirror the package structure under src/main/resources
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/org/sam/projectmanager/techy_pma/fxml/login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 500);

        stage.setTitle("TECHY | Project Manager - Login");
        stage.setScene(scene);
        stage.show();
    }

}