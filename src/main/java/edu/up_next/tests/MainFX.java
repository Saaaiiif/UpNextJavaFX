package edu.up_next.tests;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class MainFX extends Application {

    public static void main(String[] args) {
        launch(args); //demarrage de l'application from start methode


    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/UsersManagement.fxml")); // adjust path if needed
        Scene scene = new Scene(root);

        // Attach CSS
        scene.getStylesheets().add(getClass().getResource("/table-style.css").toExternalForm());

        primaryStage.setTitle("User Management");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
