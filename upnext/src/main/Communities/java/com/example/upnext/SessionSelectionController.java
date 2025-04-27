package com.example.upnext;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
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

    private Stage dialogStage;

    /**
     * Sets the stage of this dialog.
     * 
     * @param dialogStage the stage for this dialog
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Handles when the user selects the User Session button.
     */
    @FXML
    private void handleUserSession() {
        SessionManager.getInstance().setSessionType(SessionType.USER);
        dialogStage.close();
    }

    /**
     * Handles when the user selects the Admin Session button.
     */
    @FXML
    private void handleAdminSession() {
        SessionManager.getInstance().setSessionType(SessionType.ADMIN);
        dialogStage.close();
    }

    /**
     * Handles when the user selects the Artist Session button.
     */
    @FXML
    private void handleArtistSession() {
        SessionManager.getInstance().setSessionType(SessionType.ARTIST);
        dialogStage.close();
    }
}
