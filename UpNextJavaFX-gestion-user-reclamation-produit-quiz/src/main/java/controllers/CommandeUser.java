package controllers;


import edu.up_next.entities.Commande;
import edu.up_next.services.CommandeServices;
import edu.up_next.services.TwilioService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;


import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class CommandeUser implements Initializable {
    @FXML private TableView<Commande> commandesTableView;
    @FXML private TableColumn<Commande, Double> prixTotalColumn;
    @FXML private TableColumn<Commande, String> statusColumn;
    @FXML private TableColumn<Commande, String> produitsNomsColumn;
    @FXML private TableColumn<Commande, Void> actionsColumn;
    @FXML private Button retourButton;

    private final CommandeServices commandeServices = new CommandeServices();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialisation des colonnes (même si elles ne sont pas toutes dans le FXML, on évite les erreurs)
        if (commandesTableView != null) {
            if (prixTotalColumn != null) prixTotalColumn.setCellValueFactory(new PropertyValueFactory<>("prixTotal"));
            if (statusColumn != null) statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
            if (produitsNomsColumn != null) {
                produitsNomsColumn.setCellValueFactory(cellData -> {
                    Commande commande = cellData.getValue();
                    StringBuilder noms = new StringBuilder();
                    commande.getProduits().forEach(p -> noms.append(p.getNom()).append("\n"));
                    return new javafx.beans.property.SimpleStringProperty(noms.toString().trim());
                });
                produitsNomsColumn.setCellFactory(tc -> {
                    TableCell<Commande, String> cell = new TableCell<>() {
                        @Override
                        protected void updateItem(String item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                                setMinHeight(24); // hauteur par défaut
                            } else {
                                setText(item);
                                setWrapText(true);
                                setStyle("-fx-alignment: TOP-LEFT;");
                                this.setPrefHeight(Control.USE_COMPUTED_SIZE);
                                TableRow<?> row = getTableRow();
                                if (row != null) {
                                    row.setPrefHeight(Control.USE_COMPUTED_SIZE);
                                }
                            }
                        }
                    };
                    return cell;
                });
            }
            if (actionsColumn != null) addActionsToTable();
            commandesTableView.setFixedCellSize(-1);
            loadCommandes();
            commandesTableView.setRowFactory(tv -> {
                TableRow<Commande> row = new TableRow<>();
                row.setPrefHeight(Control.USE_COMPUTED_SIZE);
                return row;
            });
        }
    }

    private void loadCommandes() {
        ObservableList<Commande> commandes = FXCollections.observableArrayList(commandeServices.getAllData());
        commandesTableView.setItems(commandes);
    }

    private void addActionsToTable() {
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button smsButton = new Button("SMS");
            private final Button detailsButton = new Button("Détail");
            private final HBox actionsBox = new HBox(10, smsButton, detailsButton);
            {
                smsButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
                smsButton.setOnAction(event -> {
                    Commande commande = getTableView().getItems().get(getIndex());
                    String artistPhone = TwilioService.getArtistPhoneNumber();
                    if (artistPhone != null) {
                        artistPhone = artistPhone.trim();
                    }
                    System.out.println("Numéro utilisé pour l'envoi : '" + artistPhone + "'");
                    TwilioService twilioService = new TwilioService();
                    String message = "[" + commande.getId() + "] - Bonjour, je n'ai pas encore reçu ma commande. Pourriez-vous vérifier s'il vous plaît ? Merci.";
                    twilioService.sendDeliveryNotification(artistPhone, message);
                });
                detailsButton.setStyle("-fx-background-color: #FFA726; -fx-text-fill: white; -fx-font-weight: bold;");
                detailsButton.setOnAction(event -> {
                    Commande commande = getTableView().getItems().get(getIndex());
                    StringBuilder details = new StringBuilder("Détails de la commande :\n\n");
                    commande.getProduits().forEach(p ->
                        details.append("- Produit : ")
                               .append(p.getNom())
                               .append(" | Catégorie : ")
                               .append(p.getCategorie())
                               .append("\n")
                    );
                    new Alert(Alert.AlertType.INFORMATION, details.toString()).showAndWait();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(actionsBox);
                }
            }
        });
    }

    @FXML
    private void handleRetour() {
        try {
            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("ListUser.fxml"));
            Stage stage = (Stage) retourButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Œuvres Disponibles");
            stage.show();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur lors du retour à la liste des œuvres: " + e.getMessage()).showAndWait();
        }
    }
}
