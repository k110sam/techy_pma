package org.sam.projectmanager.techy_pma;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.sam.projectmanager.techy_pma.database.UserDAO;
import org.sam.projectmanager.techy_pma.models.User;
import org.sam.projectmanager.techy_pma.utils.PasswordUtil;


import java.io.IOException;

import static org.sam.projectmanager.techy_pma.database.DatabaseManager.initializeDatabase;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        initializeDatabase();
        System.out.println(Main.class.getResource("fxml/login.fxml"));
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/org/sam/projectmanager/techy_pma/fxml/login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 500);

        stage.setTitle("TECHY | Project Manager - Login");
        stage.setScene(scene);
        stage.show();
    }

}
