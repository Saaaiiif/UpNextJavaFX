package controllers;


import edu.up_next.entities.Produit;
import edu.up_next.services.PanierService;
import edu.up_next.services.ProduitServices;
import edu.up_next.tools.QRCodeScanner;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;



import java.io.IOException;
import java.net.URL;
import java.util.List;

public class ListUser {
    @FXML private FlowPane productsFlowPane;
    @FXML private TextField searchField;
    @FXML private Button cartButton;
    @FXML private Button voirCommandesButton;
    @FXML private Button scannerQRButton;
    @FXML private TableView<Produit> produitsTableView;

    private final ProduitServices produitServices = new ProduitServices();
    private ObservableList<Produit> produitsList;

    @FXML
    private void initialize() {
        loadProduits();

        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterProduits(newValue));

        updateCartButton();
        voirCommandesButton.setOnAction(event -> handleVoirCommandes());
        scannerQRButton.setOnAction(event -> handleScannerQR());
    }

    private void loadProduits() {
        List<Produit> produits = produitServices.getProduitsAcceptes();
        if (produits.isEmpty()) {
            System.out.println("Aucun produit accepté et disponible trouvé.");
        } else {
            System.out.println("Produits chargés pour l'utilisateur : " + produits.size());
        }

        produitsList = FXCollections.observableArrayList(produits);
        displayProducts(produitsList);
    }

    private void filterProduits(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            displayProducts(produitsList);
        } else {
            ObservableList<Produit> filteredList = FXCollections.observableArrayList();
            for (Produit produit : produitsList) {
                if (produit.getNom().toLowerCase().contains(searchText.toLowerCase()) ||
                    produit.getCategorie().toLowerCase().contains(searchText.toLowerCase())) {
                    filteredList.add(produit);
                }
            }
            displayProducts(filteredList);
        }
    }

    private void displayProducts(ObservableList<Produit> products) {
        productsFlowPane.getChildren().clear();
        for (Produit produit : products) {
            VBox card = new VBox(10);
            card.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #D3C8A9; -fx-border-radius: 5px; -fx-padding: 10px;");

            ImageView imageView = new ImageView();
            try {
                String imagePath = produit.getImage();
                if (imagePath != null && !imagePath.isEmpty()) {
                    imageView.setImage(new Image("file:" + imagePath));
                } else {
                    imageView.setImage(new Image("file:default_image.png"));
                }
            } catch (Exception e) {
                imageView.setImage(new Image("file:default_image.png"));
                System.out.println("Erreur lors du chargement de l'image pour le produit " + produit.getNom() + " : " + e.getMessage());
            }
            imageView.setFitHeight(100);
            imageView.setFitWidth(100);
            imageView.setPreserveRatio(true);

            Label nameLabel = new Label(produit.getNom());
            nameLabel.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #3A2F2F;");

            Label categorieLabel = new Label(produit.getCategorie());
            categorieLabel.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 14px; -fx-text-fill: #3A2F2F;");
            categorieLabel.setWrapText(true);

            Label priceLabel = new Label(String.format("%.2f DT", produit.getPrix()));
            priceLabel.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #32CD32;");

            Button buyButton = new Button("Acheter");
            buyButton.setStyle("-fx-background-color: #32CD32; -fx-text-fill: white; -fx-font-family: 'Georgia'; -fx-font-size: 14px;");
            buyButton.setOnAction(event -> {
                try {
                    PanierService panierService = PanierService.getInstance();
                    if (panierService.getPanier().stream().anyMatch(p -> p.getId() == produit.getId())) {
                        new Alert(Alert.AlertType.WARNING, "Ce produit existe déjà dans le panier !").showAndWait();
                    } else {
                        panierService.ajouterAuPanier(produit.getId());
                        updateCartButton();
                        new Alert(Alert.AlertType.INFORMATION, "Produit " + produit.getNom() + " ajouté au panier !").showAndWait();
                    }
                } catch (IllegalArgumentException e) {
                    new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
                }
            });

            card.getChildren().addAll(imageView, nameLabel, categorieLabel, priceLabel, buyButton);
            card.setPrefWidth(200);
            productsFlowPane.getChildren().add(card);
        }
    }
    @FXML
    void handlePanier(ActionEvent event) {
        try {
            if (cartButton == null || cartButton.getScene() == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Le bouton de retour ou la scène associée est introuvable.");
                return;
            }

            URL resource = getClass().getResource("/Panier.fxml");
            if (resource == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "ListArtist.fxml introuvable ! Vérifiez que le fichier est dans src/main/resources.");
                return;
            }

            Parent root = FXMLLoader.load(resource);
            Scene scene = cartButton.getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la redirection vers la liste des artistes : " + e.getMessage());
        }
    }


    private void updateCartButton() {
        PanierService panierService = PanierService.getInstance();
        cartButton.setText("Panier (" + panierService.getNombreArticles() + ")");
    }

    private void showCart() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Cart.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) cartButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Mon Panier");
            stage.show();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur lors de l'ouverture du panier : " + e.getMessage()).showAndWait();
        }
    }
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleVoirCommandes() {
        try {
            URL fxmlUrl = getClass().getClassLoader().getResource("CommandeUser.fxml");
            if (fxmlUrl == null) {
                throw new IOException("CommandeUser.fxml introuvable !");
            }
            Parent root = FXMLLoader.load(fxmlUrl);
            Stage stage = (Stage) voirCommandesButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Mes Commandes");
            stage.show();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur lors de la redirection vers les commandes: " + e.getMessage()).showAndWait();
        }
    }

    private void handleScannerQR() {
        String qrContent = QRCodeScanner.scanQRCode();
        if (qrContent != null && qrContent.startsWith("https://tonsite.com/produit/")) {
            String idStr = qrContent.substring(qrContent.lastIndexOf("/") + 1);
            try {
                int produitId = Integer.parseInt(idStr);
                showDetailsProduitById(produitId);
            } catch (NumberFormatException e) {
                new Alert(Alert.AlertType.ERROR, "QR code invalide : ID produit non reconnu").showAndWait();
            }
        } else {
            new Alert(Alert.AlertType.ERROR, "QR code non reconnu ou format inattendu").showAndWait();
        }
    }

    private void showDetailsProduitById(int produitId) {
        try {
            // Recherche du produit par ID
            Produit produit = null;
            for (Produit p : produitsList) {
                if (p.getId() == produitId) {
                    produit = p;
                    break;
                }
            }
            if (produit == null) {
                new Alert(Alert.AlertType.ERROR, "Produit non trouvé pour l'ID : " + produitId).showAndWait();
                return;
            }
            URL resource = getClass().getResource("/DetailsProduit.fxml");
            if (resource == null) {
                new Alert(Alert.AlertType.ERROR, "DetailsProduit.fxml introuvable !").showAndWait();
                return;
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            DetailsProduit controller = loader.getController();
            controller.setProduit(produit);
            controller.setRetourToListUser(true);
            Stage stage = (Stage) scannerQRButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Détails Produit");
            stage.show();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Erreur lors de l'ouverture des détails du produit: " + e.getMessage()).showAndWait();
        }
    }
}