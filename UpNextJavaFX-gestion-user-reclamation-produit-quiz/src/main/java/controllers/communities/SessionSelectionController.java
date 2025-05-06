package controllers.communities;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Controller for the session selection dialog.
 * This is a simplified version since session type is handled by the main project.
 */
public class SessionSelectionController {

    @FXML
    private HBox titleBar;

    @FXML
    private Button userButton;

    @FXML
    private Button artistButton;

    @FXML
    private Button adminButton;

    private Stage dialogStage;
    private boolean isDarkMode = true; // Default is dark mode

    /**
     * Sets the dialog stage.
     * @param dialogStage The stage of the dialog
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Sets the theme mode.
     * @param isDarkMode true for dark mode, false for light mode
     */
    public void setThemeMode(boolean isDarkMode) {
        this.isDarkMode = isDarkMode;
    }

    /**
     * Handles the user session button click.
     * This method is called when the user clicks the User button.
     */
    @FXML
    private void handleUserSession() {
        // Close the dialog
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    /**
     * Handles the artist session button click.
     * This method is called when the user clicks the Artist button.
     */
    @FXML
    private void handleArtistSession() {
        // Close the dialog
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    /**
     * Handles the admin session button click.
     * This method is called when the user clicks the Admin button.
     */
    @FXML
    private void handleAdminSession() {
        // Close the dialog
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}