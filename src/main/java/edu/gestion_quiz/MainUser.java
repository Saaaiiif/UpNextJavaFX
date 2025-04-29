package edu.gestion_quiz;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainUser extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/gestion_quiz/views/main_user.fxml"));
        primaryStage.setScene(new Scene(loader.load()));
        primaryStage.setTitle("Répondre à un Quiz");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
} 