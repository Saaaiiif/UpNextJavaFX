package controllers;

import edu.up_next.entities.Produit;
import edu.up_next.services.ProduitServices;
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

public class DetailsProduitAdmin {

    @FXML private Label titleLabel;
    @FXML private ImageView imageView;
    @FXML private Label nomLabel;
    @FXML private Label prixLabel;
    @FXML private Label categorieLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label approvalStatusLabel;
    @FXML private Button accepterButton;
    @FXML private Button rejeterButton;
    @FXML private Button retourButton;

    private Produit produit;
    private final ProduitServices produitServices = new ProduitServices();

    public void setProduit(Produit produit) {
        this.produit = produit;
        displayProduitDetails();
    }

    private void displayProduitDetails() {
        if (produit != null) {
            nomLabel.setText("Nom : " + produit.getNom());
            prixLabel.setText("Prix : " + String.format("%.2f DT", produit.getPrix()));
            categorieLabel.setText("Catégorie : " + produit.getCategorie());
            descriptionLabel.setText("Description : " + produit.getDescription());
            approvalStatusLabel.setText("Approval Status : " + produit.getApprovalStatus());

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

            // Désactiver les boutons si le statut n'est pas PENDING
            if (!"PENDING".equals(produit.getApprovalStatus())) {
                accepterButton.setDisable(true);
                rejeterButton.setDisable(true);
            }
        }
    }

    @FXML
    private void handleAccepter() {
        produit.setApprovalStatus("ACCEPTED");
        produitServices.modifierEntite(produit);
        new Alert(Alert.AlertType.INFORMATION, "Œuvre acceptée avec succès !").showAndWait();
        handleRetour();
    }

    @FXML
    private void handleRejeter() {
        produit.setApprovalStatus("REJECTED");
        produitServices.modifierEntite(produit);
        new Alert(Alert.AlertType.INFORMATION, "Œuvre rejetée avec succès !").showAndWait();
        handleRetour();
    }

    @FXML
    private void handleRetour() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ListAdministrateur.fxml"));
            Stage stage = (Stage) retourButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des Œuvres");
            stage.show();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur lors du retour à la liste: " + e.getMessage()).showAndWait();
        }
    }
}