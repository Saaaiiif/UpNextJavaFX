package controllers.event_reser_ouma;

import controllers.VerifiedArtist;
import controllers.home;
import controllers.profile;
import edu.up_next.ReclamationController;
import edu.up_next.entities.Event;
import edu.up_next.entities.Reservation;
import edu.up_next.entities.User;
import edu.up_next.services.EventServices;
import edu.up_next.services.ReservationServices;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Addreservation implements Initializable {

    private Event selectedEvent; // L'événement sélectionné
    private User currentUser; // L'utilisateur connecté
    EventServices es = new EventServices(); // Service des événements
    private float totalPriceCalculated = 0.0f;

    @FXML private Label comment;
    @FXML private Label successLabel;
    @FXML private Label ticketsErrorLabel;
    @FXML private TextField count_ticket;
    @FXML private TextField event_name;
    @FXML private TextField host_id;
    @FXML private TextField ticket_price;

    ReservationServices rs = new ReservationServices(); // Service des réservations

    public void setUser(User user) {
        this.currentUser = user;
        if (user != null && user.getId() >= 0) {
            host_id.setText(String.valueOf(user.getId()));
            host_id.setDisable(true); // Prevent editing
            System.out.println("setUser: host_id retrieved and set to " + user.getId());
        } else {
            host_id.setText("");
            host_id.setDisable(false);
            System.out.println("setUser: Invalid or null user, redirecting to login");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Utilisateur non connecté ou ID invalide. Redirection vers la connexion.");
            alert.showAndWait();
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
                ticket_price.getScene().setRoot(root);
            } catch (IOException e) {
                e.printStackTrace();
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Erreur");
                errorAlert.setContentText("Impossible de charger la page de connexion.");
                errorAlert.showAndWait();
            }
        }
    }

    public void setSelectedEvent(Event event) {
        this.selectedEvent = event;
        if (event != null && event.getTitle() != null && !event.getTitle().trim().isEmpty()) {
            event_name.setText(event.getTitle());
            event_name.setDisable(true); // Prevent editing
            System.out.println("setSelectedEvent: event_name retrieved and set to " + event.getTitle());
        } else {
            event_name.setText("");
            event_name.setDisable(false);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Événement non sélectionné ou titre invalide.");
            alert.showAndWait();
        }
    }

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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        successLabel.setVisible(false);
        ticket_price.setEditable(false);
        event_name.setEditable(false); // Event name set programmatically
        host_id.setEditable(false); // Host ID set programmatically
        host_id.setText(""); // Initialize as empty until setUser is called
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
                    comment.setVisible(false); // Masquer le commentaire si quantité invalide
                }
            } catch (NumberFormatException e) {
                // Si l'utilisateur ne saisit pas un nombre valide
                ticket_price.setText("");  // Effacer le prix total si l'entrée n'est pas valide
                comment.setVisible(false); // Masquer le commentaire si l'entrée est invalide
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

        // Vérifier l'utilisateur et host_id
        if (currentUser == null || host_id.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Utilisateur non connecté ou ID non défini.");
            alert.showAndWait();
            isValid = false;
        } else {
            try {
                int hostId = Integer.parseInt(host_id.getText());
                if (hostId != currentUser.getId()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setContentText("L'ID utilisateur ne correspond pas à l'utilisateur connecté.");
                    alert.showAndWait();
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setContentText("L'ID utilisateur doit être un nombre valide.");
                alert.showAndWait();
                isValid = false;
            }
        }

        // Vérifier l'événement et event_name
        if (selectedEvent == null || event_name.getText().isEmpty() || !event_name.getText().equals(selectedEvent.getTitle())) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Événement non sélectionné ou titre invalide.");
            alert.showAndWait();
            isValid = false;
        }

        return isValid;
    }
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
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

    @FXML
    void goToProfile(ActionEvent event) {
        try {
            URL fxmlLocation = getClass().getResource("/profile.fxml");
            if (fxmlLocation == null) {
                System.err.println("Error: /profile.fxml not found in resources");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();
            profile controller = loader.getController();
            controller.setUser(currentUser);
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error loading profile page: " + e.getMessage());
        }
    }

    public void GoToProduct(ActionEvent actionEvent) {
    }

    @FXML
    void GoToEvent(ActionEvent event) {
        try{
            URL fxmlLocation = getClass().getResource("/eventlist.fxml");
            if (fxmlLocation == null) {
                System.err.println("Error: /eventlist.fxml not found in resources");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();
            Eventlist controller = loader.getController();
            controller.setUser(currentUser);
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error loading profile page: " + e.getMessage());
        }
    }

    public void GoToQuiz(ActionEvent actionEvent) {
    }

    @FXML
    void Logout(ActionEvent event) {
        try {
            URL fxmlLocation = getClass().getResource("/login.fxml");
            if (fxmlLocation == null) {
                System.err.println("Error: /login.fxml not found in resources");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error loading login page: " + e.getMessage());
        }
    }

    @FXML
    void GoToVerifiedArtist(ActionEvent event) {
        try {
            URL fxmlLocation = getClass().getResource("/verifiedArtist.fxml");
            if (fxmlLocation == null) {
                System.err.println("Error: /verifiedArtist.fxml not found in resources");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();
            VerifiedArtist controller = loader.getController(); // Changed from 'profile' to 'VerifiedArtist'
            controller.setUser(currentUser);
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error loading VerifiedArtist page: " + e.getMessage());
        }
    }

    @FXML
    void goToHome(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/home.fxml"));
            Parent root = loader.load();
            home controller = loader.getController(); // Adjust to your Home controller class name
            controller.setUser(currentUser);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Failed to load Home: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Load Failed", "Failed to load home page: " + e.getMessage());
        }
    }

    public void GoToReclamationClient(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionRec.fxml"));
            Parent root = loader.load();

            // Si tu veux passer un utilisateur à ce contrôleur
            ReclamationController controller = loader.getController();
            controller.setUser(currentUser); // assure-toi que la méthode setUser existe

            System.out.println("currentUser home vers rec"+currentUser);

            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            //  showAlert("Erreur", "Impossible de charger la page gestionRec.fxml : " + e.getMessage());
        }
    }
}