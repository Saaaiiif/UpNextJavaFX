package controllers.event_reser_ouma;

import edu.up_next.entities.Reservation;
import edu.up_next.services.ReservationServices;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class Editreservation {

    public static Reservation reservToEdit;
    private final ReservationServices rs = new ReservationServices();

    @FXML private Label comment;
    @FXML private TextField count_ticket;
    @FXML private TextField event_name;
    @FXML private TextField host_id;
    @FXML private TextField ticket_price;
    @FXML private Label ticketsErrorLabel;

    // Méthode pour valider la saisie de la quantité de tickets
    private boolean validateFields() {
        boolean isValid = true;

        try {
            int requestedTickets = Integer.parseInt(count_ticket.getText());
            if (requestedTickets <= 0) {
                ticketsErrorLabel.setText("Quantité invalide.");
                ticketsErrorLabel.setVisible(true);
                isValid = false;
            } else if (reservToEdit != null && requestedTickets > reservToEdit.getQuantity()) {
                ticketsErrorLabel.setText("Quantité invalide.");
                ticketsErrorLabel.setVisible(true);
                isValid = false;
            } else {
                ticketsErrorLabel.setVisible(false);
            }
        } catch (NumberFormatException e) {
            ticketsErrorLabel.setText("Veuillez entrer un nombre valide.");
            ticketsErrorLabel.setVisible(true);
            isValid = false;
        }

        return isValid;
    }

    @FXML
    void update_event(MouseEvent event) {
        try {
            // Vérifie si les champs sont valides avant de procéder
            if (!validateFields()) {
                return;  // Arrêter l'exécution si la validation échoue
            }

            // Vérifie que la réservation à modifier est bien sélectionnée
            if (reservToEdit == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Aucune réservation sélectionnée",
                        "Veuillez sélectionner une réservation à mettre à jour.");
                return;  // Arrêter l'exécution
            }

            // Récupérer les nouvelles valeurs des champs
            int newUserId = Integer.parseInt(host_id.getText());
            int newEventId = Integer.parseInt(event_name.getText());
            int newQuantity = Integer.parseInt(count_ticket.getText());
            float newTotal = Float.parseFloat(ticket_price.getText());

            // Met à jour la réservation avec les nouvelles valeurs
            reservToEdit.setUser_id(newUserId);
            reservToEdit.setEvent_id(newEventId);
            reservToEdit.setQuantity(newQuantity);
            reservToEdit.setTotal_price(newTotal);

            // Mise à jour de la réservation dans la base de données
            rs.updateEntity(reservToEdit, reservToEdit.getId()); // Supposons que ça réussit toujours

            // Affichage d'une alerte de succès
            showAlert(Alert.AlertType.INFORMATION, "Réservation mise à jour", "Succès",
                    "La réservation a été mise à jour avec succès.");

            PauseTransition pause = new PauseTransition(Duration.seconds(1));
            pause.setOnFinished(e -> {
                Stage stage = (Stage) count_ticket.getScene().getWindow();
                stage.close();
            });
            pause.play();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Format invalide",
                    "Veuillez entrer des valeurs valides pour les champs numériques.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue", e.getMessage());
        }
    }



    // Méthode de retour à la liste des événements
    @FXML
    void return_list(MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/eventlist.fxml"));
            count_ticket.getScene().setRoot(root);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Chargement échoué",
                    "Impossible de charger la page eventlist.fxml");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String header, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void initialize() {
        if (reservToEdit != null) {
            count_ticket.setText(String.valueOf(reservToEdit.getQuantity()));
            ticket_price.setText(String.valueOf(reservToEdit.getTotal_price()));
            event_name.setText(String.valueOf(reservToEdit.getEvent_id()));
            host_id.setText(String.valueOf(reservToEdit.getUser_id()));
        }
    }

    public void calculateTotalPrice(int quantityReserved, double ticketUnitPrice) {
        double totalPrice = quantityReserved * ticketUnitPrice;
        float totalPriceCalculated = (float) totalPrice;
        ticket_price.setText(String.format("%.2f", totalPrice));
        comment.setVisible(totalPrice > 200.0);
    }
}