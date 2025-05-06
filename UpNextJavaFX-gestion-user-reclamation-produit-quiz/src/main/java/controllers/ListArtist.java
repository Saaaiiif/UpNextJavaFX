package controllers;


import edu.up_next.entities.Produit;
import edu.up_next.services.ProduitServices;
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
import javafx.scene.layout.HBox;
import javafx.stage.Stage;


import java.io.IOException;
import java.net.URL;
import java.util.List;

public class ListArtist {
    @FXML private TableView<Produit> produitsTableView;
    @FXML private TableColumn<Produit, String> actionsColumn;
    @FXML private TextField searchField;
    @FXML private Button statisticsButton;
    @FXML private Button addButton;
    @FXML private Button commandeArtistButton;

    private final ProduitServices produitServices = new ProduitServices();
    private ObservableList<Produit> produitsList;

    @FXML
    private void initialize() {
        loadProduits();

        // Recherche dynamique sur le nom ou la catégorie
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterProduits(newValue));

        // Configure the Image column to display images
        TableColumn<Produit, String> imageColumn = (TableColumn<Produit, String>) produitsTableView.getColumns().get(1);
        imageColumn.setCellFactory(column -> new TableCell<>() {
            private final ImageView imageView = new ImageView();

            @Override
            protected void updateItem(String imagePath, boolean empty) {
                super.updateItem(imagePath, empty);
                if (empty || imagePath == null) {
                    setGraphic(null);
                } else {
                    try {
                        if (imagePath != null && !imagePath.isEmpty()) {
                            imageView.setImage(new Image("file:" + imagePath));
                        } else {
                            imageView.setImage(new Image("file:default_image.png"));
                        }
                    } catch (Exception e) {
                        imageView.setImage(new Image("file:default_image.png"));
                        System.out.println("Erreur lors du chargement de l'image : " + e.getMessage());
                    }
                    imageView.setFitHeight(50);
                    imageView.setFitWidth(50);
                    setGraphic(imageView);
                }
            }
        });

        // Configure the Prix column to format the price
        TableColumn<Produit, Double> prixColumn = (TableColumn<Produit, Double>) produitsTableView.getColumns().get(2);
        prixColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double prix, boolean empty) {
                super.updateItem(prix, empty);
                if (empty || prix == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f DT", prix));
                }
            }
        });

        // Configure the Actions column with buttons
        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button deleteButton = new Button("Suppr.");
            private final Button editButton = new Button("Modif.");
            private final Button viewButton = new Button("Voir");
            private final HBox actionsBox = new HBox(10);

            {
                deleteButton.setStyle("-fx-background-color: #FF6347; -fx-text-fill: white; -fx-font-family: 'Georgia'; -fx-font-size: 12px;");
                editButton.setStyle("-fx-background-color: #FFD700; -fx-text-fill: black; -fx-font-family: 'Georgia'; -fx-font-size: 12px;");
                viewButton.setStyle("-fx-background-color: #4682B4; -fx-text-fill: white; -fx-font-family: 'Georgia'; -fx-font-size: 12px;");

                deleteButton.setOnAction(event -> {
                    Produit produit = getTableView().getItems().get(getIndex());
                    produitServices.supprimerEntite(produit, produit.getId());
                    produitsList.remove(produit);
                    new Alert(Alert.AlertType.INFORMATION, "Produit supprimé avec succès !").showAndWait();
                });

                editButton.setOnAction(event -> {
                    Produit produit = getTableView().getItems().get(getIndex());
                    showModifierProduit(produit);
                });

                viewButton.setOnAction(event -> {
                    Produit produit = getTableView().getItems().get(getIndex());
                    showDetailsProduit(produit);
                });

                actionsBox.getChildren().addAll(deleteButton, editButton, viewButton);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(actionsBox);
                }
            }
        });

        statisticsButton.setOnAction(event -> new Alert(Alert.AlertType.INFORMATION, "Fonctionnalité Statistiques à implémenter.").showAndWait());

        commandeArtistButton.setOnAction(event -> handleVoirCommandesArtist());

        produitsTableView.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Stage stage = (Stage) produitsTableView.getScene().getWindow();
                stage.setWidth(1200);
                stage.setHeight(700);
                stage.centerOnScreen();
            }
        });
    }

    @FXML
    void handleAjoutProduct(ActionEvent event) {
        try {
            if (addButton == null || addButton.getScene() == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Le bouton de retour ou la scène associée est introuvable.");
                return;
            }

            URL resource = getClass().getResource("/AjouterProduit.fxml");
            if (resource == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "AjouterProduit.fxml introuvable ! Vérifiez que le fichier est dans src/main/resources.");
                return;
            }

            Parent root = FXMLLoader.load(resource);
            Scene scene = addButton.getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la redirection vers la liste des artistes : " + e.getMessage());
        }
    }

    private void loadProduits() {
        List<Produit> produits = produitServices.getAllData();
        if (produits.isEmpty()) {
            System.out.println("Aucun produit trouvé dans la base de données.");
        } else {
            System.out.println("Produits chargés : " + produits.size());
        }

        produitsList = FXCollections.observableArrayList(produits);
        produitsTableView.setItems(produitsList);
    }

    private void filterProduits(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            produitsTableView.setItems(produitsList);
        } else {
            ObservableList<Produit> filteredList = FXCollections.observableArrayList();
            for (Produit produit : produitsList) {
                if (produit.getNom().toLowerCase().contains(searchText.toLowerCase()) ||
                        produit.getCategorie().toLowerCase().contains(searchText.toLowerCase())) {
                    filteredList.add(produit);
                }
            }
            produitsTableView.setItems(filteredList);
        }
    }

    private void showModifierProduit(Produit produit) {
        try {
            URL resource = getClass().getResource("/ModifierProduit.fxml");
            if (resource == null) {
                new Alert(Alert.AlertType.ERROR, "ModifierProduit.fxml introuvable ! Vérifiez que le fichier est dans src/main/resources.").showAndWait();
                return;
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            ModifierProduit controller = loader.getController();
            controller.setProduit(produit);
            Stage stage = (Stage) produitsTableView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier Produit");
            stage.show();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur lors de l'ouverture de l'interface de modification: " + e.getMessage()).showAndWait();
        }
    }

    private void showDetailsProduit(Produit produit) {
        try {
            URL resource = getClass().getResource("/DetailsProduitArtist.fxml");
            if (resource == null) {
                new Alert(Alert.AlertType.ERROR, "DetailsProduit.fxml introuvable ! Vérifiez que le fichier est dans src/main/resources.").showAndWait();
                return;
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            DetailsProduitArtist controller = loader.getController();
            controller.setProduit(produit);
            Stage stage = (Stage) produitsTableView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Détails Produit");
            stage.show();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur lors de l'ouverture des détails du produit: " + e.getMessage()).showAndWait();
        }
    }

    private void handleVoirCommandesArtist() {
        try {
            URL resource = getClass().getResource("/CommandeArtist.fxml");
            System.out.println("Chemin FXML : " + resource);
            if (resource == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "CommandeArtist.fxml introuvable ! Vérifiez que le fichier est dans src/main/resources.");
                return;
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            Stage stage = (Stage) commandeArtistButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Commandes");
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la redirection vers les commandes : " + e.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Exception inattendue : " + ex.getClass().getName() + " - " + ex.getMessage());
        }
    }

    @FXML
    void handleGenerateIaImage(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GenererImageIA.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Générer une image IA");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre de génération IA : " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}