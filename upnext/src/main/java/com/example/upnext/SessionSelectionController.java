package com.example.upnext;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.Parent;
import javafx.stage.Stage;

/**
 * Controller for the session selection dialog.
 * Handles the selection between admin, artist, and user sessions.
 */
public class SessionSelectionController {

    @FXML
    private Button userButton;

    @FXML
    private Button artistButton;

    @FXML
    private Button adminButton;


    @FXML
    private HBox titleBar;

    private Stage dialogStage;
    private boolean isDarkMode = true; // Default is dark mode

    // Variables for window dragging
    private double xOffset = 0;
    private double yOffset = 0;

    /**
     * Sets the stage of this dialog.
     * 
     * @param dialogStage the stage for this dialog
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;

        // Set up title bar functionality for window dragging
        titleBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        titleBar.setOnMouseDragged(event -> {
            dialogStage.setX(event.getScreenX() - xOffset);
            dialogStage.setY(event.getScreenY() - yOffset);
        });
    }

    /**
     * Sets the initial theme mode for the dialog.
     * 
     * @param isDarkMode true for dark mode, false for light mode
     */
    public void setThemeMode(boolean isDarkMode) {
        this.isDarkMode = isDarkMode;

        // Apply the theme to the dialog if the stage and scene are available
        if (dialogStage != null && dialogStage.getScene() != null) {
            Parent root = dialogStage.getScene().getRoot();
            if (isDarkMode) {
                root.getStyleClass().remove("light-mode");
            } else {
                if (!root.getStyleClass().contains("light-mode")) {
                    root.getStyleClass().add("light-mode");
                }
            }
        }
        // If stage or scene is not available yet, the theme will be applied when the dialog is shown
    }

    /**
     * Updates the theme toggle button text based on the current theme.
     * This method is kept for compatibility but does nothing since the button has been removed.
     */
    private void updateThemeButtonText() {
        // No-op - button has been removed
    }

    /**
     * Handles when the user selects the User Session button.
     */
    @FXML
    private void handleUserSession() {
        SessionManager sessionManager = SessionManager.getInstance();
        sessionManager.setSessionType(SessionType.USER);
        sessionManager.setDarkMode(isDarkMode); // Save the current theme preference
        dialogStage.close();
    }

    /**
     * Handles when the user selects the Admin Session button.
     */
    @FXML
    private void handleAdminSession() {
        SessionManager sessionManager = SessionManager.getInstance();
        sessionManager.setSessionType(SessionType.ADMIN);
        sessionManager.setDarkMode(isDarkMode); // Save the current theme preference
        dialogStage.close();
    }

    /**
     * Handles when the user selects the Artist Session button.
     */
    @FXML
    private void handleArtistSession() {
        SessionManager sessionManager = SessionManager.getInstance();
        sessionManager.setSessionType(SessionType.ARTIST);
        sessionManager.setDarkMode(isDarkMode); // Save the current theme preference
        dialogStage.close();
    }


}
