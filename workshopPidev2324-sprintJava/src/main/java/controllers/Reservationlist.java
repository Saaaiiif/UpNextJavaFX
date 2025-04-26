package controllers;

import edu.up_next.entities.Event;
import edu.up_next.entities.Reservation;
import edu.up_next.services.EventServices;
import edu.up_next.services.ReservationServices;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class Reservationlist implements Initializable {

    private final ReservationServices rs = new ReservationServices();
    private final EventServices es = new EventServices(); // Service pour accéder aux événements

    @FXML
    private TableColumn<Reservation, String> col_evename;

    @FXML
    private TableColumn<Reservation, Float> col_sum;

    @FXML
    private TableColumn<Reservation, Integer> col_tickets;

    @FXML
    private TableView<Reservation> table_view;

    @FXML
    void back_list(MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/eventlist.fxml"));
            table_view.getScene().setRoot(root); // Utilise le TableView pour accéder à la scène
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Chargement échoué");
            alert.setContentText("Impossible de charger la page Eventlist.fxml");
            alert.showAndWait();
        }
    }

    @FXML
    void delete_reser(MouseEvent event) {
        Reservation selectedReservation = table_view.getSelectionModel().getSelectedItem();

        if (selectedReservation != null) {
            // Supprimer la réservation via le service
            rs.deleteEntity(selectedReservation);

            // Retirer la réservation de la TableView
            table_view.getItems().remove(selectedReservation);

            // Afficher un message de succès
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Réservation supprimée");
            alert.setContentText("La réservation a été supprimée avec succès.");
            alert.showAndWait();

        } else {
            // Gérer le cas où aucune réservation n'est sélectionnée
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de sélection");
            alert.setHeaderText("Aucune réservation sélectionnée.");
            alert.setContentText("Veuillez sélectionner une réservation à supprimer.");
            alert.showAndWait();
        }
    }


    @FXML
    void edit_reser(MouseEvent event) throws IOException {
        // Récupérer la réservation sélectionnée dans la TableView
        Reservation selectedReservation = table_view.getSelectionModel().getSelectedItem();

        if (selectedReservation != null) {
            // Assigner la réservation sélectionnée à une variable statique (si nécessaire) dans le contrôleur d'édition
            Editreservation.reservToEdit = selectedReservation;

            // Charger la fenêtre d'édition de la réservation
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/editreservation.fxml"));
                Parent root = loader.load();

                // Créer une nouvelle scène pour la fenêtre d'édition
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur de chargement");
                alert.setHeaderText("Erreur lors du chargement de la fenêtre d'édition");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de sélection");
            alert.setHeaderText("Aucune réservation sélectionnée");
            alert.setContentText("Veuillez sélectionner une réservation à modifier.");
            alert.showAndWait();
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        List<Reservation> resers = rs.getAllData(); // Récupération des réservations
        ObservableList<Reservation> data = FXCollections.observableArrayList(resers);
        table_view.setItems(data);

        // Désactivation de la sélection de cellule seule (optionnel)
        table_view.getSelectionModel().setCellSelectionEnabled(false);

        // Configuration des colonnes — vérifie bien les noms de propriétés dans ta classe Reservation
        col_evename.setCellValueFactory(new PropertyValueFactory<>("event_id"));

        col_tickets.setCellValueFactory(new PropertyValueFactory<>("quantity")); // Nombre de tickets
        col_sum.setCellValueFactory(new PropertyValueFactory<>("total_price")); // Prix total
    }
}
