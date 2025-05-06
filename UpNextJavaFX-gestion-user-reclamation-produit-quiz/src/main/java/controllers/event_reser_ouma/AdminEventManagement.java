package controllers.event_reser_ouma;

import edu.up_next.entities.Event;
import edu.up_next.entities.User;
import edu.up_next.services.EventServices;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AdminEventManagement {

    @FXML
    private VBox eventList;
    @FXML
    private Text AuthenticatedUser;

    private ObservableList<Event> allEvents;
    private final EventServices es = new EventServices();
    private User currentUser;

    @FXML
    public void initialize() {
        if (eventList == null) {
            System.err.println("eventList is null - check fx:id in AdminEventManagement.fxml");
        }
        if (AuthenticatedUser == null) {
            System.err.println("AuthenticatedUser is null - check fx:id in AdminEventManagement.fxml");
        }
        AuthenticatedUser.setText("Admin");
        // Hardcode user for testing
        currentUser = new User();
        currentUser.setEmail("admin@gmail.com");
        currentUser.setFirstname("Admin");
        currentUser.setLastname("User");
        currentUser.setRoles("ROLE_ADMIN");
        loadAllEvents();
    }

    private void loadAllEvents() {
        try {
            List<Event> events = es.getAllData();
            allEvents = FXCollections.observableArrayList(events != null ? events : new ArrayList<>());
            System.out.println("Loaded " + allEvents.size() + " events");
            displayEvents(allEvents);
        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur Base de Données", "Échec du chargement des événements", e.getMessage());
        }
    }
    private void displayEvents(ObservableList<Event> events) {
        if (eventList == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur d'Initialisation", "Conteneur d'événements non initialisé",
                    "L'élément VBox 'eventList' n'est pas défini dans le fichier FXML.");
            return;
        }
        eventList.getChildren().clear();
        System.out.println("Displaying events. Total events: " + events.size());

        ObservableList<Event> filteredEvents = filterEventsByStatusAndRole(events);
        System.out.println("Filtered events: " + filteredEvents.size());

        if (filteredEvents.isEmpty()) {
            Text noEvents = new Text("Aucun événement disponible.");
            noEvents.setStyle("-fx-font-size: 14; -fx-fill: #666666;");
            eventList.getChildren().add(noEvents);
        } else {
            for (Event event : filteredEvents) {
                HBox card = createEventCard(event);
                eventList.getChildren().add(card);
            }
        }
    }

    private ObservableList<Event> filterEventsByStatusAndRole(ObservableList<Event> events) {
        ObservableList<Event> filteredEvents = FXCollections.observableArrayList();
        System.out.println("Filtering events. Current user: " + (currentUser != null ? currentUser.getEmail() : "null"));
        System.out.println("Total events before filtering: " + events.size());

        if (currentUser != null && hasRole("ROLE_ADMIN")) {
            filteredEvents.addAll(events);
            System.out.println("User has ROLE_ADMIN. Filtered events: " + filteredEvents.size());
        } else {
            System.out.println("No events filtered. User is null or lacks ROLE_ADMIN.");
        }

        return filteredEvents;
    }

    private boolean hasRole(String roleName) {
        if (currentUser == null || currentUser.getRoles() == null) {
            System.out.println("hasRole: currentUser is null or roles are null");
            return false;
        }
        String[] roles = currentUser.getRoles().toLowerCase().split(",");
        System.out.println("User roles: " + String.join(", ", roles));
        for (String role : roles) {
            if (role.trim().equals(roleName.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private HBox createEventCard(Event event) {
        HBox card = new HBox(10);
        card.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #cccccc; -fx-border-radius: 10; " +
                "-fx-background-radius: 10; -fx-padding: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        Text eventInfo = new Text(String.format("Événement: %s | Statut: %s", event.getTitle(), event.getStatus_event()));
        card.getChildren().add(eventInfo);

        if ("pending".equalsIgnoreCase(event.getStatus_event())) {
            Button approveBtn = new Button("Approuver");
            approveBtn.setOnAction(e -> approveEvent(event));

            Button rejectBtn = new Button("Rejeter");
            rejectBtn.setOnAction(e -> rejectEvent(event));

            card.getChildren().addAll(approveBtn, rejectBtn);
        }

        return card;
    }

    private void approveEvent(Event event) {
        if (!"pending".equalsIgnoreCase(event.getStatus_event())) {
            showAlert(Alert.AlertType.WARNING, "Action Invalide", "Impossible d'approuver", "Cet événement n'est pas en attente.");
            return;
        }
        try {
            event.setStatus_event("approved");
            es.updateStatus(event);
            loadAllEvents();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Événement Approuvé", "L'événement a été approuvé.");
        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur Base de Données", "Échec de l'approbation", e.getMessage());
        }
    }

    private void rejectEvent(Event event) {
        if (!"pending".equalsIgnoreCase(event.getStatus_event())) {
            showAlert(Alert.AlertType.WARNING, "Action Invalide", "Impossible de rejeter", "Cet événement n'est pas en attente.");
            return;
        }
        try {
            event.setStatus_event("rejected");
            es.updateStatus(event);
            loadAllEvents();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Événement Rejeté", "L'événement a été rejeté.");
        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur Base de Données", "Échec du rejet", e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setUser(User user) {
        this.currentUser = user;
        AuthenticatedUser.setText(user != null ? user.getFirstname() + " " + user.getLastname() : "Admin");
        System.out.println("setUser called. User: " + (user != null ? user.getEmail() : "null"));
        loadAllEvents();
    }

    public void GoToQuizManagement(ActionEvent actionEvent) {
        navigateTo("/path/to/QuizManagement.fxml", "Gestion des Quiz");
    }

    public void GoToCarpooling(ActionEvent actionEvent) {
        navigateTo("/path/to/Carpooling.fxml", "Gestion du Covoiturage");
    }

    public void GoToProduct(ActionEvent actionEvent) {
        navigateTo("/path/to/ProductManagement.fxml", "Gestion des Produits");
    }

    public void Logout(ActionEvent actionEvent) {
        navigateTo("/path/to/Login.fxml", "Connexion");
    }

    public void GoToReclamationAdmin(ActionEvent actionEvent) {
        navigateTo("/path/to/ReclamationAdmin.fxml", "Gestion des Réclamations");
    }

    public void GoToEvent(ActionEvent actionEvent) {
        // Already on the event management page
    }

    public void goToProfile(ActionEvent actionEvent) {
        navigateTo("/path/to/Profile.fxml", "Profil Utilisateur");
    }

    public void GoToUsersManagement(ActionEvent actionEvent) {
        navigateTo("/path/to/UsersManagement.fxml", "Gestion des Utilisateurs");
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            if (loader.getLocation() == null) {
                throw new IOException("FXML file not found at: " + fxmlPath);
            }
            Parent root = loader.load();
            Stage stage = (Stage) eventList.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de Navigation", "Échec du chargement de la page", e.getMessage());
        }
    }
}
