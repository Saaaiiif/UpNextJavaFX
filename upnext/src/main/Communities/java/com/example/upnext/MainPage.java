package com.example.upnext;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Modality;

import java.io.IOException;
import java.util.Objects;

public class MainPage extends Application {
    /**
     * Shows the session selection dialog to choose between admin and user sessions.
     * 
     * @throws IOException if the FXML file cannot be loaded
     */
    private void showSessionSelectionDialog() throws IOException {
        // Create the dialog Stage
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Select Session Type");
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initStyle(StageStyle.UNDECORATED);

        // Load the FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("session-selection-dialog.fxml"));
        javafx.scene.Parent dialogRoot = loader.load();

        // Set the scene
        Scene dialogScene = new Scene(dialogRoot);
        dialogScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm());
        dialogStage.setScene(dialogScene);

        // Set the controller
        SessionSelectionController controller = loader.getController();
        controller.setDialogStage(dialogStage);

        // Show the dialog and wait until the user closes it
        dialogStage.showAndWait();
    }

    @Override
    public void start(Stage stage) throws IOException {
        // Show the session selection dialog first
        showSessionSelectionDialog();

        // Set the application icon
        Image appIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/logo.png")));
        stage.getIcons().add(appIcon);

        // Make the stage undecorated to remove the default title bar
        stage.initStyle(StageStyle.UNDECORATED);

        // Load the root layout
        FXMLLoader rootLoader = new FXMLLoader(MainPage.class.getResource("root-layout.fxml"));
        javafx.scene.Parent rootLayout = rootLoader.load();
        rootLayout.setStyle("-fx-background-color: #121212;");

        Scene scene = new Scene(rootLayout, 1200, 700);
        // Set the scene fill to dark color to prevent white flash
        scene.setFill(javafx.scene.paint.Color.rgb(18, 18, 18));
        scene.getStylesheets().add(Objects.requireNonNull(MainPage.class.getResource("styles.css")).toExternalForm());

        // Get the root controller and set the stage
        RootLayoutController rootController = rootLoader.getController();
        rootController.setStage(stage);

        // Set the root controller in SceneTransitionUtil
        SceneTransitionUtil.setRootController(rootController);

        stage.setTitle("UpNext!");
        stage.setScene(scene);
        stage.show();

        // Load the initial content (hello-view.fxml)
        MainController mainController = SceneTransitionUtil.changeContent(
            "/com/example/upnext/hello-view.fxml", 
            SceneTransitionUtil.TransitionType.FADE, 
            MainController.class
        );
        mainController.setStage(stage);
    }


    public static void main(String[] args) {
        launch();
    }

    @Override
    public void stop() {
        // Ensure all resources are released when the application is stopped
        SceneTransitionUtil.shutdown();

        // Call the superclass implementation
        try {
            super.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
