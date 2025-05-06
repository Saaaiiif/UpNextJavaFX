package controllers.communities;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class EditSocialController {
    @FXML private TextField socialField;
    @FXML private Button closeButton;
    @FXML private HBox titleBar;

    private BooleanProperty validSocial = new SimpleBooleanProperty(false);
    private int communityId;
    private boolean isDarkMode = true; // Default is dark mode

    // Variables for window dragging
    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    public void initialize() {
        // Initialize isDarkMode from SessionManager
        isDarkMode = SessionManager.getInstance().isDarkMode();

        // Apply the theme if it was set before the scene was available
        applyThemeIfPossible();

        // Register as a theme change listener
        SessionManager.getInstance().addThemeChangeListener(this::handleThemeChange);

        // Set up title bar functionality for window dragging
        titleBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        titleBar.setOnMouseDragged(event -> {
            Stage stage = (Stage) titleBar.getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        // Set up validation for social field
        socialField.textProperty().addListener((obs, oldVal, newVal) -> {
            // Social field can be empty, but if it's not, it should be a valid URL
            if (newVal == null || newVal.isEmpty()) {
                validSocial.set(true);
            } else {
                // Simple validation - just check if it contains a domain
                validSocial.set(newVal.contains("."));
            }
        });
    }

    /**
     * Handles theme changes from SessionManager.
     * 
     * @param isDarkMode true for dark mode, false for light mode
     */
    private void handleThemeChange(boolean isDarkMode) {
        this.isDarkMode = isDarkMode;
        applyThemeIfPossible();
    }

    public void setCommunityId(int id) {
        this.communityId = id;
    }

    public void setSocial(String social) {
        if (social != null) {
            socialField.setText(social);
            // Social field can be empty, so always set validSocial to true initially
            validSocial.set(true);
        }
    }

    public String getSocial() {
        return socialField.getText().trim();
    }

    public BooleanProperty validSocialProperty() {
        return validSocial;
    }

    /**
     * Sets the initial theme mode for the dialog.
     * 
     * @param isDarkMode true for dark mode, false for light mode
     */
    public void setThemeMode(boolean isDarkMode) {
        this.isDarkMode = isDarkMode;

        // Store the theme mode for later application when the dialog is shown
        // The actual application will happen in initialize or when the scene becomes available
        applyThemeIfPossible();
    }

    /**
     * Applies the theme if the scene is available, otherwise does nothing.
     * This method is safe to call at any time.
     */
    private void applyThemeIfPossible() {
        // Apply the theme to the dialog if the scene is available
        if (closeButton != null && closeButton.getScene() != null && closeButton.getScene().getRoot() != null) {
            Parent root = closeButton.getScene().getRoot();
            if (isDarkMode) {
                root.getStyleClass().remove("light-mode");
            } else {
                if (!root.getStyleClass().contains("light-mode")) {
                    root.getStyleClass().add("light-mode");
                }
            }
        } else if (closeButton != null) {
            // If the scene is not available yet, add a listener to apply the theme when it becomes available
            closeButton.sceneProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && newValue.getRoot() != null) {
                    Parent root = newValue.getRoot();
                    if (isDarkMode) {
                        root.getStyleClass().remove("light-mode");
                    } else {
                        if (!root.getStyleClass().contains("light-mode")) {
                            root.getStyleClass().add("light-mode");
                        }
                    }
                }
            });
        }
    }

    /**
     * Handles the close button click.
     */
    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}