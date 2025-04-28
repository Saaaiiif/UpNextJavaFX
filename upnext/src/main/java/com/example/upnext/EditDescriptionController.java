package com.example.upnext;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.Button;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class EditDescriptionController {
    @FXML private TextArea descriptionField;
    @FXML private Button closeButton;
    @FXML private HBox titleBar;

    private BooleanProperty validDescription = new SimpleBooleanProperty(false);
    private int communityId;
    private boolean isDarkMode = true; // Default is dark mode

    // Variables for window dragging
    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    public void initialize() {
        setupDescriptionValidation();

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

    private void setupDescriptionValidation() {
        descriptionField.textProperty().addListener((obs, oldVal, newVal) -> {
            validDescription.set(!newVal.trim().isEmpty());

            // Limit to 500 characters
            if (newVal.length() > 500) {
                descriptionField.setText(newVal.substring(0, 500));
            }
        });
    }

    public void setCommunityId(int id) {
        this.communityId = id;
    }

    public void setDescription(String description) {
        descriptionField.setText(description);
        validDescription.set(!description.trim().isEmpty());
    }

    public String getDescription() {
        return descriptionField.getText().trim();
    }

    public BooleanProperty validDescriptionProperty() {
        return validDescription;
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
     * Updates the theme toggle button text based on the current theme.
     * This method is kept for compatibility but does nothing since the button has been removed.
     */
    private void updateThemeButtonText() {
        // No-op - button has been removed
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
