package controllers;

import com.google.zxing.WriterException;

import edu.up_next.entities.Produit;
import edu.up_next.services.PanierService;
import edu.up_next.tools.QRCode;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;



import java.io.IOException;

public class Panier {
    @FXML private ListView<Produit> produitsList;
    @FXML private Label sousTotalLabel;
    @FXML private Label livraisonLabel;
    @FXML private Label totalLabel;
    @FXML private Button finaliserButton;
    @FXML private Button continuerButton;

    private final PanierService panierService = PanierService.getInstance();

    @FXML
    private void initialize() {
        produitsList.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Produit produit, boolean empty) {
                super.updateItem(produit, empty);
                if (empty || produit == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox hBox = new HBox(10);
                    hBox.setStyle("-fx-padding: 10px; -fx-background-color: white; -fx-border-color: #E0E0E0; -fx-border-radius: 5px;");

                    ImageView imageView = new ImageView();
                    try {
                        imageView.setImage(new Image("file:" + produit.getImage()));
                    } catch (Exception e) {
                        imageView.setImage(new Image("file:default_image.png"));
                    }
                    imageView.setFitWidth(60);
                    imageView.setFitHeight(60);

                    VBox detailsBox = new VBox(5);
                    Label nomLabel = new Label(produit.getNom());
                    nomLabel.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 14px; -fx-font-weight: bold;");

                    Label descLabel = new Label(produit.getCategorie());
                    descLabel.setWrapText(true);
                    descLabel.setMaxWidth(300);
                    descLabel.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 12px;");

                    Label prixLabel = new Label(String.format("%.2f DT", produit.getPrix()));
                    prixLabel.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #32CD32;");

                    Button supprimerButton = new Button("Supprimer");
                    supprimerButton.setStyle("-fx-background-color: #FF6347; -fx-text-fill: white; -fx-font-family: 'Georgia'; -fx-font-size: 12px; -fx-border-radius: 5px;");
                    supprimerButton.setOnAction(event -> {
                        panierService.supprimerDuPanier(produit.getId());
                        refreshPanier();
                    });

                    Label quantiteLabel = new Label("1");
                    quantiteLabel.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 14px; -fx-font-weight: bold;");

                    ImageView qrImageView = new ImageView();
                    try {
                        String qrContent = "https://tonsite.com/produit/" + produit.getId();
                        Image qrImage = QRCode.generateQRCodeImage(qrContent, 100, 100);
                        qrImageView.setImage(qrImage);
                        qrImageView.setFitWidth(100);
                        qrImageView.setFitHeight(100);
                    } catch (WriterException e) {
                        // Optionnel : image par défaut ou rien
                    }

                    detailsBox.getChildren().addAll(nomLabel, descLabel, prixLabel);
                    hBox.getChildren().addAll(imageView, detailsBox, quantiteLabel, supprimerButton, qrImageView);
                    setGraphic(hBox);
                }
            }
        });

        refreshPanier();
    }

    private void refreshPanier() {
        produitsList.getItems().setAll(panierService.getPanier());
        double sousTotal = panierService.getSousTotal();
        sousTotalLabel.setText(String.format("%.2f DT", sousTotal));
        livraisonLabel.setText(String.format("%.2f DT", panierService.getFraisLivraison()));
        totalLabel.setText(String.format("%.2f DT", panierService.getTotal()));
        finaliserButton.setDisable(panierService.getPanier().isEmpty());
    }

    @FXML
    private void handleFinaliser() {
        try {
            double remise = panierService.getRemiseActivePourcentage(java.time.LocalDate.now());
            if (remise > 0) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION,
                    "Félicitations ! Aujourd'hui, vous bénéficiez d'une remise exceptionnelle de " + remise + "% sur votre commande !");
                alert.setHeaderText("Journée promotionnelle !");
                alert.showAndWait();
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Payment.fxml"));
            Parent root = loader.load();
            PaymentController paymentController = loader.getController();
            paymentController.setTotalAmount(panierService.getTotal());

            Stage paymentStage = new Stage();
            paymentStage.setTitle("Paiement sécurisé");
            paymentStage.setScene(new Scene(root));
            paymentStage.showAndWait();

            if (paymentController.isPaymentSuccess()) {
                panierService.finaliserCommande();
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Commande finalisée avec succès !");
                alert.showAndWait();

                Parent commandeRoot = FXMLLoader.load(getClass().getClassLoader().getResource("CommandeUser.fxml"));
                if (commandeRoot == null) {
                    new Alert(Alert.AlertType.ERROR, "Erreur : CommandeUser.fxml introuvable.").showAndWait();
                    return;
                }
                Stage stage = (Stage) finaliserButton.getScene().getWindow();
                stage.setScene(new Scene(commandeRoot));
                stage.show();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Le paiement n'a pas été validé. La commande n'est pas enregistrée.");
                alert.showAndWait();
            }
        } catch (IllegalStateException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage());
            alert.showAndWait();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de la redirection vers les commandes: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleContinuer() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ListUser.fxml")); // Chemin corrigé
            if (root == null) {
                new Alert(Alert.AlertType.ERROR, "Erreur : ListUser.fxml introuvable.").showAndWait();
                return;
            }
            Stage stage = (Stage) continuerButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors du retour à la liste des œuvres: " + e.getMessage());
            alert.showAndWait();
        }
    }
}