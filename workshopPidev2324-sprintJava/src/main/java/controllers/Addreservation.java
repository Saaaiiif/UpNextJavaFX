package controllers;

import edu.up_next.entities.Event;
import edu.up_next.entities.Reservation;
import edu.up_next.services.EventServices;
import edu.up_next.services.ReservationServices;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class Addreservation implements Initializable {

    private Event selectedEvent; // L'événement sélectionné
    EventServices es = new EventServices(); // Service des événements
    private float totalPriceCalculated = 0.0f;


    @FXML
    private Label comment;

    @FXML
    private Label successLabel;

    @FXML
    private Label ticketsErrorLabel;

    @FXML
    private TextField count_ticket;

    @FXML
    private Button eventlist;

    @FXML
    private TextField event_name;

    @FXML
    private TextField host_id;

    @FXML
    private TextField ticket_price;

    ReservationServices rs = new ReservationServices(); // Service des réservations

    public void save(ActionEvent reservation) {
        if (!validateFields()) {
            return; // stop everything if validation fails
        }
        try {
            int userId = Integer.parseInt(host_id.getText());
            int eventId = selectedEvent.getId();


            // Si les vérifications passent, continuer à ajouter la réservation
            Reservation res = new Reservation(
                    0, // id fictif, la base s'en occupe
                    eventId,
                    userId,
                    totalPriceCalculated,
                    Integer.parseInt(count_ticket.getText()) // Nombre de billets
            );

            rs.addEntity(res); // Ajouter la réservation
            successLabel.setVisible(true);

            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(e -> successLabel.setVisible(false));
            pause.play();

            // Afficher un message de confirmation
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Réservation réussie");
            alert.setContentText("Réservation ajoutée avec succès.");
            alert.showAndWait();

        } catch (NumberFormatException e) {
            // Gestion des erreurs de saisie
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de saisie");
            alert.setContentText("Veuillez vérifier les valeurs saisies (notamment les nombres).");
            alert.showAndWait();
        } catch (IllegalArgumentException e) {
            // Erreur si l'événement n'est pas valide
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }



    public void setSelectedEvent(Event event) {
        this.selectedEvent = event;
        if (event != null) {
            // Utiliser l'ID pour la réservation
            event_name.setText(event.getTitle());         }
    }



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        successLabel.setVisible(false);
        ticket_price.setEditable(false);
        // Ajout du listener sur le TextField count_ticket
        count_ticket.textProperty().addListener((observable, oldValue, newValue) -> {
            // Vérifier si la valeur est vide
            if (newValue.isEmpty()) {
                ticket_price.setText("");  // Effacer le prix total si le champ est vide
                comment.setVisible(false); // Masquer le commentaire si vide
                return;  // Arrêter ici
            }

            try {
                // Vérifier que la valeur saisie est un nombre entier valide
                int quantityReserved = Integer.parseInt(newValue);

                // Vérifier que la quantité de tickets est valide
                if (quantityReserved > 0 && selectedEvent != null) {
                    double ticketUnitPrice = selectedEvent.getTicket_price();  // Récupérer le prix du ticket
                    calculateTotalPrice(quantityReserved, ticketUnitPrice);  // Mettre à jour le total
                } else {
                    ticket_price.setText("");  // Effacer le prix total si la quantité est invalide
                    comment.setVisible(false);  // Masquer le commentaire si quantité invalide
                }
            } catch (NumberFormatException e) {
                // Si l'utilisateur ne saisit pas un nombre valide
                ticket_price.setText("");  // Effacer le prix total si l'entrée n'est pas valide
                comment.setVisible(false);  // Masquer le commentaire si l'entrée est invalide
            }
        });
    }

    // Méthode de validation des champs
    private boolean validateFields() {
        boolean isValid = true;

        // Vérifier la quantité demandée
        try {
            int requestedTickets = Integer.parseInt(count_ticket.getText()); // Essayer de convertir le texte en nombre
            if (requestedTickets <= 0) {
                // Quantité invalide (inférieure ou égale à zéro)
                ticketsErrorLabel.setText("Quantité invalide.");
                ticketsErrorLabel.setVisible(true);
                isValid = false;
            } else if (selectedEvent != null && requestedTickets > selectedEvent.getTicket_count()) {
                // Quantité demandée supérieure au nombre de tickets disponibles
                ticketsErrorLabel.setText("Seulement " + selectedEvent.getTicket_count() + " tickets disponibles.");
                ticketsErrorLabel.setVisible(true);
                isValid = false;
            } else {
                // Aucune erreur, on cache le label d'erreur
                ticketsErrorLabel.setVisible(false);
            }
        } catch (NumberFormatException e) {
            // Si l'utilisateur entre une valeur non numérique
            ticketsErrorLabel.setText("Veuillez entrer un nombre valide.");
            ticketsErrorLabel.setVisible(true);
            isValid = false;
        }

        return isValid;
    }


    // Retourner à la liste des événements
    public void show_list(MouseEvent mouseEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/eventlist.fxml"));
            ticket_price.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace(); // ou affiche une alerte
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Chargement échoué");
            alert.setContentText("Impossible de charger la page Eventlist.fxml");
            alert.showAndWait();
        }
    }
    public void calculateTotalPrice(int quantityReserved, double ticketUnitPrice) {
        double totalPrice = quantityReserved * ticketUnitPrice;
        totalPriceCalculated = (float) totalPrice; // Stocker pour save()

        ticket_price.setText(String.format("%.2f", totalPrice));

        if (totalPrice > 200.0) {
            comment.setVisible(true);
        } else {
            comment.setVisible(false);
        }
    }

    public void show_reserv(MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/reservationlist.fxml"));
            ticket_price.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace(); // ou affiche une alerte
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Chargement échoué");
            alert.setContentText("Impossible de charger la page reservationlist.fxml");
            alert.showAndWait();
        }
    }
}
