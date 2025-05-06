package controllers;

import edu.up_next.services.ReplicateImageService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class GenererImageIAController {
    @FXML
    private TextArea promptTextArea;
    
    @FXML
    private Button generateImageButton;
    
    @FXML
    private ImageView generatedImageView;
    
    @FXML
    private ProgressIndicator loadingIndicator;
    
    private final ReplicateImageService imageService;
    
    public GenererImageIAController() {
        this.imageService = new ReplicateImageService();
    }
    
    @FXML
    private void initialize() {
        loadingIndicator.setVisible(false);
        generateImageButton.setOnAction(e -> handleGenerateImage());
    }
    @FXML
    private void handleGenerateImage() {
        String prompt = promptTextArea.getText();
        if (prompt == null || prompt.trim().isEmpty()) {
            showAlert("Erreur", "Veuillez entrer un prompt pour générer l'image.");
            return;
        }

        // Disable the button and show loading
        generateImageButton.setDisable(true);
        loadingIndicator.setVisible(true);

        // Call the API in a background thread
        new Thread(() -> {
            try {
                // Call the API to generate the image
                String imageUrl = imageService.generateImageFromPrompt(prompt);
                
                // Update the UI on the JavaFX thread
                Platform.runLater(() -> {
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        // Load and display the image
                        Image image = new Image(imageUrl);
                        generatedImageView.setImage(image);
                    } else {
                        showAlert("Erreur", "Impossible de générer l'image. Veuillez réessayer.");
                    }
                    // Re-enable the button and hide loading
                    generateImageButton.setDisable(false);
                    loadingIndicator.setVisible(false);
                });
            } catch (Exception e) {
                e.printStackTrace();
                // Handle any errors
                Platform.runLater(() -> {
                    showAlert("Erreur", "Une erreur est survenue: " + e.getMessage());
                    generateImageButton.setDisable(false);
                    loadingIndicator.setVisible(false);
                });
            }
        }).start();
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 