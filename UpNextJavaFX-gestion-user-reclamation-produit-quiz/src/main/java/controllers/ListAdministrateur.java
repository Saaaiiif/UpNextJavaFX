package controllers;


import edu.up_next.entities.Produit;
import edu.up_next.entities.User;
import edu.up_next.services.ProduitServices;
import controllers.admin.AdminHome;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import java.util.List;
import java.util.stream.Collectors;

public class ListAdministrateur {
    @FXML private TableView<Produit> produitsTableView;
    @FXML private TableColumn<Produit, String> actionsColumn;
    @FXML private TextField searchField;

    private final ProduitServices produitServices = new ProduitServices();
    private ObservableList<Produit> produitsList;
    private User currentUser;

    public void setUser(User user) {
        this.currentUser = user;
        // You can add any UI updates based on user here if needed
    }

    @FXML
    private void initialize() {
        loadProduits();

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

        // Configure the Actions column with buttons for Accept, Reject, and Details
        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button acceptButton = new Button("Accepter");
            private final Button rejectButton = new Button("Rejeter");
            private final Button detailsButton = new Button("Détails");
            private final HBox actionsBox = new HBox(10);

            {
                acceptButton.setStyle("-fx-background-color: #32CD32; -fx-text-fill: white; -fx-font-family: 'Georgia'; -fx-font-size: 12px;");
                rejectButton.setStyle("-fx-background-color: #FF6347; -fx-text-fill: white; -fx-font-family: 'Georgia'; -fx-font-size: 12px;");
                detailsButton.setStyle("-fx-background-color: #FFA500; -fx-text-fill: white; -fx-font-family: 'Georgia'; -fx-font-size: 12px;");

                acceptButton.setOnAction(event -> {
                    Produit produit = getTableView().getItems().get(getIndex());
                    produit.setApprovalStatus("ACCEPTED");
                    produitServices.modifierEntite(produit);
                    loadProduits(); // Refresh the table
                    new Alert(Alert.AlertType.INFORMATION, "Œuvre acceptée avec succès !").showAndWait();
                });

                rejectButton.setOnAction(event -> {
                    Produit produit = getTableView().getItems().get(getIndex());
                    produit.setApprovalStatus("REJECTED");
                    produitServices.modifierEntite(produit);
                    loadProduits(); // Refresh the table
                    new Alert(Alert.AlertType.INFORMATION, "Œuvre rejetée avec succès !").showAndWait();
                });

                detailsButton.setOnAction(event -> {
                    Produit produit = getTableView().getItems().get(getIndex());
                    showDetailsProduit(produit);
                });

                actionsBox.getChildren().addAll(acceptButton, rejectButton, detailsButton);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Produit produit = getTableView().getItems().get(getIndex());
                    // Show buttons only if the approval status is PENDING
                    if ("PENDING".equals(produit.getApprovalStatus())) {
                        acceptButton.setVisible(true);
                        rejectButton.setVisible(true);
                    } else {
                        acceptButton.setVisible(false);
                        rejectButton.setVisible(false);
                    }
                    setGraphic(actionsBox);
                }
            }
        });

        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterProduits(newValue));
    }

    private void loadProduits() {
        try {
            System.out.println("Starting to load products...");
            List<Produit> produits = produitServices.getAllData();
            System.out.println("Total products loaded: " + (produits != null ? produits.size() : "null"));
            
            // Filtrer pour n'afficher que les produits en attente de validation
            List<Produit> produitsEnAttente = produits.stream()
                .filter(p -> "PENDING".equalsIgnoreCase(p.getApprovalStatus()))
                .collect(Collectors.toList());

            System.out.println("Pending products count: " + produitsEnAttente.size());

            produitsList = FXCollections.observableArrayList(produitsEnAttente);
            produitsTableView.setItems(produitsList);
            System.out.println("Products loaded into table view successfully");
            
        } catch (Exception e) {
            System.err.println("Error loading products: " + e.getMessage());
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error loading products: " + e.getMessage()).showAndWait();
        }
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

    private void showDetailsProduit(Produit produit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailsProduitAdmin.fxml"));
            Parent root = loader.load();
            DetailsProduitAdmin controller = loader.getController();
            controller.setProduit(produit);
            Stage stage = (Stage) produitsTableView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Détails Produit");
            stage.show();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur lors de l'ouverture des détails du produit: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin.fxml"));
            Parent root = loader.load();
            
            // Get the controller and pass the current user
            AdminHome adminController = loader.getController();
            if (currentUser != null) {
                adminController.setUser(currentUser);
            }
            
            Stage stage = (Stage) produitsTableView.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Admin Dashboard");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error returning to admin dashboard: " + e.getMessage()).showAndWait();
        }
    }
}