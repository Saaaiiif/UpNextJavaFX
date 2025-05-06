package controllers;


import edu.up_next.entities.Produit;
import edu.up_next.services.ProduitServices;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;


import java.io.File;
import java.io.IOException;
import java.net.URL;

public class AjouterProduit {

    @FXML private Button ajouterButton;
    @FXML private ComboBox<String> categorieComboBox;
    @FXML private Button chooseImageButton;
    @FXML private Button importIaImageButton;
    @FXML private TextArea descriptionArea;
    @FXML private Label imagePathLabel;
    @FXML private TextField nomField;
    @FXML private TextField prixField;
    @FXML private Button retourButton;

    private ProduitServices ps = new ProduitServices();
    private String selectedImagePath;
    private static String lastIaImagePath;

    public static void setLastIaImagePath(String path) {
        lastIaImagePath = path;
    }

    @FXML
    public void initialize() {
        categorieComboBox.setPromptText("Choisir une catégorie");
    }

    @FXML
    void handleAjouter(ActionEvent event) {
        try {
            String nom = nomField.getText();
            String description = descriptionArea.getText();
            String categorie = categorieComboBox.getSelectionModel().getSelectedItem();
            String prixText = prixField.getText();

            if (nom == null || nom.trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Le nom de l'œuvre ne peut pas être vide.");
                return;
            }
            if (description == null || description.trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "La description de l'œuvre ne peut pas être vide.");
                return;
            }
            if (categorie == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner une catégorie.");
                return;
            }
            if (prixText == null || prixText.trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Le prix de l'œuvre ne peut pas être vide.");
                return;
            }
            if (selectedImagePath == null || selectedImagePath.trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez choisir une image pour l'œuvre.");
                return;
            }

            double prix;
            try {
                prix = Double.parseDouble(prixText);
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Le prix doit être un nombre valide.");
                return;
            }

            Produit produit = new Produit(nom, selectedImagePath, prix, categorie, description);
            produit.setApprovalStatus("PENDING");
            produit.setStatut("DISPONIBLE");

            int generatedId = ps.ajouterEntite(produit);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Œuvre ajoutée avec succès ! ID: " + generatedId + ". Elle est en attente de validation par l'administrateur.");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListArtist.fxml"));
                        Parent root = loader.load();
                        Scene scene = ajouterButton.getScene();
                        scene.setRoot(root);
                    } catch (IOException e) {
                        showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la redirection vers la liste des œuvres : " + e.getMessage());
                    }
                }
            });

            clearForm();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout de l'œuvre : " + e.getMessage());
        }
    }

    @FXML
    void handleChooseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.PNG", "*.JPG")
        );
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            String fileName = selectedFile.getName();
            if (fileName.toLowerCase().endsWith(".png") || fileName.toLowerCase().endsWith(".jpg")) {
                selectedImagePath = selectedFile.getAbsolutePath();
                imagePathLabel.setText(selectedImagePath);
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "L'image doit être au format .png, .jpg, .PNG ou .JPG.");
                selectedImagePath = null;
                imagePathLabel.setText("Aucun fichier choisi");
            }
        }
    }

    @FXML
    void handleImportIaImage(ActionEvent event) {
        if (lastIaImagePath == null || lastIaImagePath.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucune image IA n'a été générée récemment. Veuillez d'abord générer une image IA.");
            return;
        }
        File iaFile = new File(lastIaImagePath);
        if (!iaFile.exists()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le fichier image IA n'existe plus. Veuillez régénérer une image IA.");
            return;
        }
        selectedImagePath = lastIaImagePath;
        imagePathLabel.setText(selectedImagePath);
        showAlert(Alert.AlertType.INFORMATION, "Succès", "Image IA importée avec succès !");
    }

    @FXML
    void handleRetour(ActionEvent event) {
        try {
            if (retourButton == null || retourButton.getScene() == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Le bouton de retour ou la scène associée est introuvable.");
                return;
            }

            URL resource = getClass().getResource("/ListArtist.fxml");
            if (resource == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "ListArtist.fxml introuvable ! Vérifiez que le fichier est dans src/main/resources.");
                return;
            }

            Parent root = FXMLLoader.load(resource);
            Scene scene = retourButton.getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la redirection vers la liste des artistes : " + e.getMessage());
        }
    }

    private void clearForm() {
        nomField.clear();
        descriptionArea.clear();
        categorieComboBox.getSelectionModel().clearSelection();
        prixField.clear();
        imagePathLabel.setText("Aucun fichier choisi");
        selectedImagePath = null;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}