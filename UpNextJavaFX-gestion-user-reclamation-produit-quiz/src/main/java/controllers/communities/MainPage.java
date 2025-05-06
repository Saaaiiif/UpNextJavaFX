package controllers.communities;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Modality;

import java.io.IOException;
import java.util.Objects;

public class MainPage extends Application {
    /**
     * Loads the Feather font for use in the application.
     * 
     * @throws IOException if the font file cannot be loaded
     */
    private void loadFonts() throws IOException {
        Font.loadFont(getClass().getResourceAsStream("/Feather.ttf"), 18);
                // Register the font as "Feather Bold" for use in CSS
                Font boldFont = Font.font("Feather Bold", 18);
    }

    @Override
    public void start(Stage stage) throws IOException {
        // Load fonts
        loadFonts();

        // Set the application icon
        Image appIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/logo.png")));
        stage.getIcons().add(appIcon);

        // Make the stage undecorated to remove the default title bar
        stage.initStyle(StageStyle.UNDECORATED);

        // Load the root layout
        FXMLLoader rootLoader = new FXMLLoader(MainPage.class.getResource("/views/communities/root-layout.fxml"));
        javafx.scene.Parent rootLayout = rootLoader.load();
        rootLayout.setStyle("-fx-background-color: #121212;");

        Scene scene = new Scene(rootLayout, 1200, 700);
        // Set the scene fill to dark color to prevent white flash
        scene.setFill(javafx.scene.paint.Color.rgb(18, 18, 18));
        scene.getStylesheets().add(Objects.requireNonNull(MainPage.class.getResource("/styles.css")).toExternalForm());

        // Get the root controller and set the stage
        RootLayoutController rootController = rootLoader.getController();
        rootController.setStage(stage);

        // Apply the theme preference from SessionManager
        boolean isDarkMode = SessionManager.getInstance().isDarkMode();
        if (!isDarkMode) {
            // If light mode is selected, apply it to the root layout
            rootLayout.getStyleClass().add("light-mode");
            rootController.setThemeMode(false);
        }

        // Set the root controller in SceneTransitionUtil
        SceneTransitionUtil.setRootController(rootController);

        stage.setTitle("UpNext!");
        stage.setScene(scene);
        stage.show();

        // Load the initial content (hello-view.fxml)
        MainController mainController = SceneTransitionUtil.changeContent(
                "/views/communities/hello-view.fxml",
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
