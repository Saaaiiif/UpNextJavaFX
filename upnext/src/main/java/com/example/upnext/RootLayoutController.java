package com.example.upnext;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * Controller for the root layout that contains the title bar and content area.
 */
public class RootLayoutController {

    @FXML
    private ImageView logoImage;

    @FXML
    private HBox titleBar;

    @FXML
    private Button minimizeButton;

    @FXML
    private Button closeButton;

    @FXML
    private StackPane contentArea;

    private Stage stage;
    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    public void initialize() {
        // Load logo
        Image logo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/logo.png")));
        logoImage.setImage(logo);

        // Set up title bar functionality
        titleBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        titleBar.setOnMouseDragged(event -> {
            if (stage != null) {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            }
        });

        // Set up minimize button functionality
        minimizeButton.setOnAction(event -> {
            if (stage != null) {
                stage.setIconified(true);
            }
        });

        // Set up close button functionality
        closeButton.setOnAction(event -> {
            if (stage != null) {
                stage.close();
            }
        });
    }

    /**
     * Sets the stage for window operations.
     * @param stage The primary stage of the application
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Loads content into the content area.
     * @param fxmlPath The path to the FXML file to load
     * @return The controller of the loaded FXML
     * @throws IOException If the FXML file cannot be loaded
     */
    public <T> T loadContent(String fxmlPath, Class<T> controllerType) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent content = loader.load();
        
        // Clear existing content and add new content
        contentArea.getChildren().clear();
        contentArea.getChildren().add(content);
        
        return loader.getController();
    }

    /**
     * Gets the content area for direct manipulation.
     * @return The content area StackPane
     */
    public StackPane getContentArea() {
        return contentArea;
    }
}