package controllers;


import edu.up_next.entities.Produit;
import edu.up_next.services.ProduitServices;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


import java.io.File;
import java.io.IOException;
import java.net.URL;

public class ModifierProduit {

    @FXML private TextField nomField;
    @FXML private TextField prixField;
    @FXML private ComboBox<String> categorieComboBox;
    @FXML private TextArea descriptionArea;
    @FXML private Label imagePathLabel;
    @FXML private Button chooseImageButton;
    @FXML private Button modifierButton;
    @FXML private Button retourButton;

    private File selectedImageFile;
    private Produit produit;
    private final ProduitServices produitServices = new ProduitServices();

    @FXML
    private void initialize() {
        // Configurer le bouton pour choisir une image
        chooseImageButton.setOnAction(event -> handleChooseImage());

        // Configurer le bouton Modifier
        modifierButton.setOnAction(event -> handleModifier());

        // Configurer le bouton Retour
        retourButton.setOnAction(event -> handleRetour());
    }

    // Méthode pour définir le produit à modifier et pré-remplir les champs
    public void setProduit(Produit produit) {
        this.produit = produit;
        if (produit != null) {
            nomField.setText(produit.getNom());
            prixField.setText(String.format("%.2f", produit.getPrix()));
            categorieComboBox.setValue(produit.getCategorie());
            descriptionArea.setText(produit.getDescription());
            imagePathLabel.setText(produit.getImage() != null ? new File(produit.getImage()).getName() : "Aucun fichier choisi");
            selectedImageFile = produit.getImage() != null ? new File(produit.getImage()) : null;
        }
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        selectedImageFile = fileChooser.showOpenDialog(chooseImageButton.getScene().getWindow());
        if (selectedImageFile != null) {
            imagePathLabel.setText(selectedImageFile.getName());
        }
    }

    @FXML
    private void handleModifier() {
        // Validation des champs
        String nom = nomField.getText();
        String prixText = prixField.getText();
        String categorie = categorieComboBox.getValue();
        String description = descriptionArea.getText();

        if (nom == null || nom.trim().isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Le nom de l'œuvre ne peut pas être vide.").showAndWait();
            return;
        }
        if (prixText == null || prixText.trim().isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Le prix de l'œuvre ne peut pas être vide.").showAndWait();
            return;
        }
        if (categorie == null) {
            new Alert(Alert.AlertType.ERROR, "Veuillez sélectionner une catégorie.").showAndWait();
            return;
        }
        if (description == null || description.trim().isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "La description de l'œuvre ne peut pas être vide.").showAndWait();
            return;
        }
        if (selectedImageFile == null) {
            new Alert(Alert.AlertType.ERROR, "Veuillez sélectionner une image.").showAndWait();
            return;
        }

        double prix;
        try {
            prix = Double.parseDouble(prixText);
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Le prix doit être un nombre valide.").showAndWait();
            return;
        }

        // Mettre à jour l'objet Produit avec les nouvelles valeurs
        produit.setNom(nom);
        produit.setPrix(prix);
        produit.setCategorie(categorie);
        produit.setDescription(description);
        produit.setImage(selectedImageFile.getAbsolutePath());
        // Les statuts approval_status et statut sont conservés (déjà définis dans l'objet produit)

        // Appeler le service pour modifier le produit
        produitServices.modifierEntite(produit);

        // Afficher une confirmation
        new Alert(Alert.AlertType.INFORMATION, "Produit modifié avec succès !").showAndWait();

        // Retourner à la liste
        handleRetour();
    }

    @FXML
    private void handleRetour() {
        try {
            URL resource = getClass().getResource("/ListArtist.fxml");
            if (resource == null) {
                new Alert(Alert.AlertType.ERROR, "ListArtist.fxml introuvable ! Vérifiez que le fichier est dans src/main/resources.").showAndWait();
                return;
            }
            Parent root = FXMLLoader.load(resource);
            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur lors du retour à la liste des œuvres : " + e.getMessage()).showAndWait();
        }
    }
}