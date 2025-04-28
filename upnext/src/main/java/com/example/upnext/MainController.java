package com.example.upnext;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import javafx.scene.shape.Circle;
import javafx.scene.effect.DropShadow;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.nio.file.Files;
import java.io.ByteArrayInputStream;
import javafx.scene.control.Dialog;
import javafx.scene.Cursor;
import javafx.scene.layout.HBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

public class MainController {

    @FXML
    private ImageView logoImage;

    @FXML
    private Button communitiesButton;

    @FXML
    private Button createCommunityButton;

    @FXML
    private Button upNextButton;

    @FXML
    private Button artistsButton;

    // Communities-related fields have been moved to CommunitiesController

    // Field for window operations
    private Stage stage;

    @FXML
    public void initialize() {

        Image logo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/logo.png")));
        logoImage.setImage(logo);

        // Display session type information
        SessionType currentSession = SessionManager.getInstance().getSessionType();
        String sessionTypeText;
        if (currentSession == SessionType.ADMIN) {
            sessionTypeText = "Administrator";
            createCommunityButton.setVisible(false);
        } else if (currentSession == SessionType.ARTIST) {
            sessionTypeText = "Artist";
            createCommunityButton.setVisible(true);
        } else {
            sessionTypeText = "User";
            createCommunityButton.setVisible(false);
        }
        // Session info display has been moved to specific controllers

        // Highlight the current page's navigation label
        if (upNextButton != null) {
            upNextButton.getStyleClass().add("active");
        }
    }

    @FXML
    private void handleCommunitiesButtonClick() {
        try {
            // Check the session type to determine which view to show
            SessionType currentSession = SessionManager.getInstance().getSessionType();

            if (currentSession == SessionType.ADMIN) {
                // Admin users see the communities-view
                SceneTransitionUtil.changeContentWithPreload(
                        "/com/example/upnext/communities-view.fxml",
                    SceneTransitionUtil.TransitionType.FADE, 
                    CommunitiesController.class,
                    controller -> controller.loadCommunities()
                );
            } else {
                // User and Artist users see the user-communities-view
                SceneTransitionUtil.changeContentWithPreload(
                        "/com/example/upnext/user-communities-view.fxml",
                    SceneTransitionUtil.TransitionType.FADE, 
                    UserCommunitiesController.class,
                    controller -> controller.loadCommunities()
                );
            }
        } catch (IOException e) {
            System.err.println("Failed to load communities view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpNextButtonClick() {
        try {
            SceneTransitionUtil.changeContentWithPreload(
                    "/com/example/upnext/hello-view.fxml",
                SceneTransitionUtil.TransitionType.SLIDE_RIGHT, 
                MainController.class,
                controller -> {} // No data loading needed for this view
            );
        } catch (IOException e) {
            System.err.println("Failed to load hello-view.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleArtistsButtonClick() {
        try {
            SceneTransitionUtil.changeContentWithPreload(
                    "/com/example/upnext/artists-view.fxml",
                SceneTransitionUtil.TransitionType.FADE, 
                ArtistsController.class,
                controller -> controller.loadArtists()
            );
        } catch (IOException e) {
            System.err.println("Failed to load artists-view.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCreateCommunityButtonClick() {
        try {
            // Navigate to the create community page
            SceneTransitionUtil.changeContentWithPreload(
                    "/com/example/upnext/create-community-view.fxml",
                SceneTransitionUtil.TransitionType.FADE, 
                AddCommunityController.class,
                controller -> {
                    // Set the theme mode
                    controller.setThemeMode(SessionManager.getInstance().isDarkMode());
                    // Generate a unique ID automatically
                    controller.handleGenerateId();
                }
            );
        } catch (IOException e) {
            System.err.println("Failed to load create-community-view.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Communities-related methods have been moved to CommunitiesController

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    /**
     * Sets the stage for window operations.
     * @param stage The primary stage of the application
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
