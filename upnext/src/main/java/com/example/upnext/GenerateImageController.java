package com.example.upnext;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.io.ByteArrayInputStream;
import javafx.concurrent.Task;
import javafx.application.Platform;

/**
 * Controller for the generate image dialog.
 * Handles user interactions for generating images using Stability AI.
 */
public class GenerateImageController {
    @FXML private HBox titleBar;
    @FXML private Button closeButton;
    @FXML private TextArea promptField;
    @FXML private ImageView imagePreview;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Button generateButton;
    @FXML private Button useImageButton;

    private StabilityAIService stabilityService;
    private byte[] generatedImageData;
    private boolean isDarkMode = true;
    
    // Variables for window dragging
    private double xOffset = 0;
    private double yOffset = 0;

    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        stabilityService = new StabilityAIService();
        
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
    
    /**
     * Applies the theme if the scene is available.
     */
    private void applyThemeIfPossible() {
        if (titleBar != null && titleBar.getScene() != null && titleBar.getScene().getRoot() != null) {
            if (isDarkMode) {
                titleBar.getScene().getRoot().getStyleClass().remove("light-mode");
            } else {
                if (!titleBar.getScene().getRoot().getStyleClass().contains("light-mode")) {
                    titleBar.getScene().getRoot().getStyleClass().add("light-mode");
                }
            }
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

    /**
     * Handles the generate image button click.
     * Calls the Stability AI service to generate an image based on the prompt.
     */
    @FXML
    private void handleGenerateImage() {
        String prompt = promptField.getText().trim();
        if (prompt.isEmpty()) {
            // Show error or notification that prompt is required
            return;
        }
        
        // Disable generate button and show loading indicator
        generateButton.setDisable(true);
        loadingIndicator.setVisible(true);
        
        // Create a background task to generate the image
        Task<byte[]> task = new Task<>() {
            @Override
            protected byte[] call() throws Exception {
                return stabilityService.generateImage(prompt);
            }
        };
        
        task.setOnSucceeded(event -> {
            generatedImageData = task.getValue();
            if (generatedImageData != null) {
                // Display the generated image
                Image image = new Image(new ByteArrayInputStream(generatedImageData));
                imagePreview.setImage(image);
                
                // Enable the use image button
                useImageButton.setDisable(false);
            } else {
                // Handle error - image generation failed
                System.err.println("Failed to generate image");
            }
            
            // Re-enable generate button and hide loading indicator
            generateButton.setDisable(false);
            loadingIndicator.setVisible(false);
        });
        
        task.setOnFailed(event -> {
            // Handle error
            System.err.println("Error generating image: " + task.getException().getMessage());
            task.getException().printStackTrace();
            
            // Re-enable generate button and hide loading indicator
            generateButton.setDisable(false);
            loadingIndicator.setVisible(false);
        });
        
        // Start the task in a background thread
        new Thread(task).start();
    }

    /**
     * Handles the use image button click.
     * Closes the dialog and returns the generated image data.
     */
    @FXML
    private void handleUseImage() {
        // Close the dialog
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Handles the cancel button click.
     * Closes the dialog without using the generated image.
     */
    @FXML
    private void handleCancel() {
        // Clear the generated image data
        generatedImageData = null;
        
        // Close the dialog
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Gets the generated image data.
     * 
     * @return The generated image data as a byte array, or null if no image was generated
     */
    public byte[] getGeneratedImageData() {
        return generatedImageData;
    }
}