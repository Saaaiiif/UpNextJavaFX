package edu.up_next.tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class MainFX extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the login FXML file
        URL fxmlLocation = getClass().getResource("/login.fxml");
        if (fxmlLocation == null) {
            System.err.println("Error: /login.fxml not found in resources");
            throw new RuntimeException("Cannot find login.fxml");
        }
        FXMLLoader loader = new FXMLLoader(fxmlLocation);
        Parent root = loader.load();
        Scene scene = new Scene(root);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Up Next - Login");
        primaryStage.show();
    }
}