package controllers;

import edu.up_next.entities.Commande;
import edu.up_next.services.CommandeServices;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;


import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.ResourceBundle;

public class CommandeArtist implements Initializable {
    @FXML private TableView<Commande> commandesTableView;
    @FXML private TableColumn<Commande, Integer> idColumn;
    @FXML private TableColumn<Commande, Double> prixTotalColumn;
    @FXML private TableColumn<Commande, String> statusColumn;
    @FXML private TableColumn<Commande, String> produitsNomsColumn;
    @FXML private TableColumn<Commande, Void> actionsColumn;
    @FXML private Button retourButton;
    @FXML private Button statistiquesButton;
    @FXML private ImageView statistiquesImageView;

    private final CommandeServices commandeServices = new CommandeServices();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        prixTotalColumn.setCellValueFactory(new PropertyValueFactory<>("prixTotal"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
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
                    setText(empty ? null : item);
                    setWrapText(true);
                    setStyle("-fx-alignment: TOP-LEFT;");
                }
            };
            return cell;
        });
        addActionsToTable();
        loadCommandes();
    }

    private void loadCommandes() {
        ObservableList<Commande> commandes = FXCollections.observableArrayList((Commande) commandeServices.getAllData());
        commandesTableView.setItems(commandes);
    }

    private void addActionsToTable() {
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Commande commande = getTableView().getItems().get(getIndex());
                    DatePicker datePicker = new DatePicker();
                    Button confirmerButton = new Button("Confirmer livraison");
                    Button supprimerButton = new Button("Supprimer");
                    Button smsButton = new Button("SMS");
                    Button detailButton = new Button("Détail");
                    HBox box = new HBox(5, datePicker, confirmerButton, supprimerButton, smsButton, detailButton);
                    // Styles
                    datePicker.setPromptText("mm/dd/yyyy");
                    datePicker.setPrefWidth(110);
                    confirmerButton.setStyle("-fx-background-color: #43a047; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-font-size: 13px;");
                    supprimerButton.setStyle("-fx-background-color: #e53935; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-font-size: 13px;");
                    smsButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-font-size: 13px;");
                    detailButton.setStyle("-fx-background-color: #FFA726; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-font-size: 13px;");
                    // DatePicker : afficher la date de livraison si déjà fixée
                    if (commande.getDeliveryDate() != null) {
                        datePicker.setValue(commande.getDeliveryDate().toLocalDate());
                    } else {
                        datePicker.setValue(null);
                    }
                    // Désactiver/activer le bouton Supprimer selon la date de livraison
                    if (commande.getStatus().equalsIgnoreCase("LIVREE") && commande.getDeliveryDate() != null) {
                        java.time.LocalDate today = java.time.LocalDate.now();
                        java.time.LocalDate livraison = commande.getDeliveryDate().toLocalDate();
                        if (today.isAfter(livraison)) {
                            supprimerButton.setDisable(false);
                        } else {
                            supprimerButton.setDisable(true);
                        }
                    } else if (commande.getStatus().equalsIgnoreCase("LIVREE")) {
                        supprimerButton.setDisable(true);
                    } else {
                        supprimerButton.setDisable(false);
                    }
                    // Actions
                    confirmerButton.setOnAction(event -> {
                        java.time.LocalDate dateCommande = commande.getDateCommande().toLocalDate();
                        java.time.LocalDate dateLivraison = datePicker.getValue();
                        if (dateLivraison == null) {
                            new Alert(Alert.AlertType.ERROR, "Veuillez choisir une date de livraison.").showAndWait();
                            return;
                        }
                        if (!dateLivraison.isAfter(dateCommande)) {
                            new Alert(Alert.AlertType.ERROR, "La date de livraison doit être après la date de commande (" + dateCommande + ").").showAndWait();
                            return;
                        }
                        if (dateLivraison.isAfter(dateCommande.plusDays(20))) {
                            new Alert(Alert.AlertType.ERROR, "La date de livraison ne doit pas dépasser 20 jours après la commande.").showAndWait();
                            return;
                        }
                        commande.setStatus("LIVREE");
                        commande.setDeliveryDate(dateLivraison.atStartOfDay());
                        commandeServices.modifierEntite(commande);
                        loadCommandes();
                        new Alert(Alert.AlertType.INFORMATION, "Commande livrée avec succès !").showAndWait();
                    });
                    supprimerButton.setOnAction(event -> {
                        commandeServices.supprimerEntite(commande, commande.getId());
                        loadCommandes();
                        new Alert(Alert.AlertType.INFORMATION, "Commande supprimée avec succès !").showAndWait();
                    });
                    smsButton.setOnAction(event -> {
                        // Action SMS non fonctionnelle pour l'instant
                    });
                    detailButton.setOnAction(event -> {
                        StringBuilder details = new StringBuilder();
                        details.append("Détails de la commande :\n\n");
                        details.append("ID : ").append(commande.getId()).append("\n");
                        details.append("Prix total : ").append(commande.getPrixTotal()).append(" DT\n");
                        details.append("Statut : ").append(commande.getStatus()).append("\n");
                        details.append("Date de commande : ").append(commande.getDateCommande().toLocalDate()).append("\n");
                        if (commande.getDeliveryDate() != null) {
                            details.append("Date de livraison : ").append(commande.getDeliveryDate().toLocalDate()).append("\n");
                        }
                        details.append("\nProduits :\n");
                        commande.getProduits().forEach(p ->
                            details.append("- ").append(p.getNom()).append(" | Catégorie : ").append(p.getCategorie()).append("\n")
                        );
                        new Alert(Alert.AlertType.INFORMATION, details.toString()).showAndWait();
                    });
                    setGraphic(box);
                }
            }
        });
    }

    @FXML
    private void handleRetour() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ListArtist.fxml"));
            Stage stage = (Stage) retourButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Mes Œuvres");
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Erreur lors du retour à la liste des œuvres: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void handleStatistiques() {
        Map<String, Double> stats = commandeServices.getChiffreAffairesParCategorie();
        StringBuilder labels = new StringBuilder();
        StringBuilder data = new StringBuilder();
        for (Map.Entry<String, Double> entry : stats.entrySet()) {
            if (labels.length() > 0) {
                labels.append(",");
                data.append(",");
            }
            labels.append('"').append(entry.getKey()).append('"');
            data.append(entry.getValue());
        }
        String chartConfig = String.format("{type:'pie',data:{labels:[%s],datasets:[{data:[%s]}]}}", labels, data);
        try {
            String url = "https://quickchart.io/chart?c=" + URLEncoder.encode(chartConfig, "UTF-8");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/StatistiquesCategorie.fxml"));
            Parent root = loader.load();
            StatistiquesCategorieController controller = loader.getController();
            controller.setStatistiquesImageUrl(url);
            Stage stage = new Stage();
            stage.setTitle("Statistiques par Catégorie");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Erreur lors de la génération du graphique: " + e.getMessage()).showAndWait();
        }
    }
}
