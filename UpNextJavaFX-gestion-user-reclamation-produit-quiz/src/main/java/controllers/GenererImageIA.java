package controllers;

import edu.up_next.services.HuggingFaceSpaceImageService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;

public class GenererImageIA {
    @FXML private TextField promptField;
    @FXML private Button generateImageButton;
    @FXML private ImageView generatedImageView;
    @FXML private Button useImageButton;

    private byte[] lastGeneratedImage;
    private String lastSavedPath;
    private final HuggingFaceSpaceImageService imageService = new HuggingFaceSpaceImageService();

    @FXML
    public void initialize() {
        System.out.println("[IA] Contrôleur GenererImageIAController initialisé.");
        useImageButton.setDisable(true);
        useImageButton.setOnAction(e -> handleUseImage());
    }

    @FXML
    private void handleGenerateImage(ActionEvent event) {
        System.out.println("[IA] Bouton 'Générer l'image' cliqué.");
        String prompt = promptField.getText();
        if (prompt == null || prompt.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le prompt ne peut pas être vide.");
            return;
        }
        generateImageButton.setDisable(true);
        useImageButton.setDisable(true);
        new Thread(() -> {
            try {
                System.out.println("[IA] Appel API avec prompt: " + prompt);
                byte[] imageBytes = imageService.generateImageFromPrompt(prompt);
                if (imageBytes == null || imageBytes.length == 0) {
                    System.out.println("[IA] Aucune image reçue de l'API.");
                    javafx.application.Platform.runLater(() -> {
                        showAlert(Alert.AlertType.ERROR, "Erreur", "Aucune image reçue de l'API Hugging Face. Timeout, quota, ou format inattendu. Consulte la console pour plus de détails.");
                        generateImageButton.setDisable(false);
                    });
                    return;
                }
                lastGeneratedImage = imageBytes;
                Image image = new Image(new ByteArrayInputStream(imageBytes));
                javafx.application.Platform.runLater(() -> {
                    generatedImageView.setImage(image);
                    useImageButton.setDisable(false);
                    generateImageButton.setDisable(false);
                });
            } catch (Exception e) {
                System.out.println("[IA] Exception: " + e.getMessage());
                javafx.application.Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Erreur Hugging Face", e.getMessage());
                    generateImageButton.setDisable(false);
                });
            }
        }).start();
    }

    private void handleUseImage() {
        if (lastGeneratedImage == null) return;
        try {
            // Sauvegarde l'image dans un dossier temporaire
            String tempDir = System.getProperty("user.home") + File.separator + "images_temp";
            new File(tempDir).mkdirs();
            String fileName = "ia_image_" + System.currentTimeMillis() + ".png";
            String filePath = tempDir + File.separator + fileName;
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(lastGeneratedImage);
            }
            lastSavedPath = filePath;
            // Stocke le chemin dans une variable statique accessible par AjouterProduit
            AjouterProduit.setLastIaImagePath(filePath);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Image enregistrée et prête à être utilisée dans l'ajout de produit.");
            // Ferme la fenêtre
            Stage stage = (Stage) useImageButton.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de sauvegarder l'image : " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 