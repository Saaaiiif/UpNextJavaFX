package controllers;


import edu.up_next.entities.Produit;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;

public class DetailsProduit {

    @FXML private Label titleLabel;
    @FXML private ImageView imageView;
    @FXML private Label nomLabel;
    @FXML private Label prixLabel;
    @FXML private Label categorieLabel;
    @FXML private Label descriptionLabel;
    @FXML private Button retourButton;

    private Produit produit;
    private boolean retourToListUser = false;

    public void setProduit(Produit produit) {
        this.produit = produit;
        displayProduitDetails();
    }

    public void setRetourToListUser(boolean retourToListUser) {
        this.retourToListUser = retourToListUser;
    }

    private void displayProduitDetails() {
        if (produit != null) {
            nomLabel.setText("Nom : " + produit.getNom());
            prixLabel.setText("Prix : " + String.format("%.2f DT", produit.getPrix()));
            categorieLabel.setText("Catégorie : " + produit.getCategorie());
            descriptionLabel.setText("Description : " + produit.getDescription());

            try {
                if (produit.getImage() != null && !produit.getImage().isEmpty()) {
                    imageView.setImage(new Image("file:" + produit.getImage()));
                } else {
                    imageView.setImage(new Image("file:default_image.png"));
                }
            } catch (Exception e) {
                imageView.setImage(new Image("file:default_image.png"));
                System.out.println("Erreur lors du chargement de l'image : " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleRetour() {
        try {
            String fxml = retourToListUser ? "/ListUser.fxml" : "/ListArtist.fxml";
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) retourButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(retourToListUser ? "Liste des Produits" : "Liste des Œuvres");
            stage.show();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur lors du retour à la liste: " + e.getMessage()).showAndWait();
        }
    }
}