package controllers;

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
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Editreservation {

    public static Reservation reservToEdit;
    private final ReservationServices rs = new ReservationServices();
    private float totalPriceCalculated = 0.0f;



    @FXML
    private Label comment;

    @FXML
    private TextField count_ticket;

    @FXML
    private TextField event_name;

    @FXML
    private TextField host_id;

    @FXML
    private TextField ticket_price;

    @FXML
    private Label ticketsErrorLabel;

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
                // Si aucune réservation n'est sélectionnée, on affiche une alerte
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur de sélection");
                alert.setHeaderText("Aucune réservation sélectionnée");
                alert.setContentText("Veuillez sélectionner une réservation à mettre à jour.");
                alert.showAndWait();
                return;  // Arrêter l'exécution si aucune réservation n'est sélectionnée
            }

            // Récupérer les nouvelles valeurs des champs
            int newUserId = Integer.parseInt(host_id.getText());  // Récupère l'ID de l'utilisateur
            int newEventId = Integer.parseInt(event_name.getText());  // Récupère l'ID de l'événement
            int newQuantity = Integer.parseInt(count_ticket.getText());  // Récupère la quantité de billets
            float newTotal = Float.parseFloat(ticket_price.getText());  // Récupère le prix total

            // Met à jour la réservation avec les nouvelles valeurs
            reservToEdit.setUser_id(newUserId);
            reservToEdit.setEvent_id(newEventId);
            reservToEdit.setQuantity(newQuantity);
            reservToEdit.setTotal_price(newTotal);

            // Récupère l'ID de la réservation
            int reservationId = reservToEdit.getId();

            // Mise à jour de la réservation dans la base de données
            rs.updateEntity(reservToEdit, reservationId);

            // Attendre 3 secondes avant de revenir à la liste des événements
            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(e -> return_list(null));  // Appel de la méthode pour retourner à la liste
            pause.play();

            // Affichage d'une alerte de succès
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Réservation mise à jour");
            alert.setContentText("La réservation a été mise à jour avec succès.");
            alert.showAndWait();

        } catch (NumberFormatException e) {
            // Si un champ a un format invalide (par exemple, texte au lieu d'un nombre)
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de saisie");
            alert.setHeaderText("Format invalide");
            alert.setContentText("Veuillez entrer des valeurs valides pour les champs numériques.");
            alert.showAndWait();
        } catch (Exception e) {
            // Pour toute autre erreur, afficher une alerte générale
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Une erreur est survenue");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }



    // Méthode de retour à la liste des événements
    @FXML
    void return_list(MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/eventlist.fxml"));
            count_ticket.getScene().setRoot(root);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Chargement échoué", "Impossible de charger la page eventlist.fxml");
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
        totalPriceCalculated = (float) totalPrice; // Stocker pour save()

        ticket_price.setText(String.format("%.2f", totalPrice));

        // Afficher un message si le prix total dépasse 200
        if (totalPrice > 200.0) {
            comment.setVisible(true);
        } else {
            comment.setVisible(false);
        }
    }

}
