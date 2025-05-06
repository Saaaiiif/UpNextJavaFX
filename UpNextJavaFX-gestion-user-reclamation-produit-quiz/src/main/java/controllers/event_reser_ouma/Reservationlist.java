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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class Reservationlist implements Initializable {

    private final ReservationServices rs = new ReservationServices();
    private final EventServices es = new EventServices();
    private User currentUser;

    @FXML private Text AuthenticatedUser;
    @FXML private TableColumn<Reservation, String> col_evename;
    @FXML private TableColumn<Reservation, Float> col_sum;
    @FXML private TableColumn<Reservation, Integer> col_tickets;
    @FXML private TableView<Reservation> table_view;

    @FXML
    void delete_reser(MouseEvent event) {
        Reservation selectedReservation = table_view.getSelectionModel().getSelectedItem();

        if (selectedReservation == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de sélection");
            alert.setHeaderText("Aucune réservation sélectionnée");
            alert.setContentText("Veuillez sélectionner une réservation à supprimer.");
            alert.showAndWait();
            return;
        }

        try {
            rs.deleteEntity(selectedReservation);
            table_view.getItems().remove(selectedReservation);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText("Réservation supprimée");
            alert.setContentText("La réservation a été supprimée avec succès.");
            alert.showAndWait();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de suppression");
            alert.setHeaderText("Échec de la suppression");
            alert.setContentText("Détails : " + e.getMessage());
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

    private boolean hasRole(String roleName) {
        String roles = currentUser.getRoles();
        return roles != null && roles.toLowerCase().contains(roleName.toLowerCase());
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setUser(null);

        col_evename.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    Reservation reservation = (Reservation) getTableRow().getItem();
                    int eventId = reservation.getEvent_id();
                    try {
                        String title = new EventServices().getEventTitleById(eventId);
                        setText(title);
                    } catch (Exception e) {
                        setText("Erreur");
                    }
                }
            }
        });

        col_tickets.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        col_sum.setCellValueFactory(new PropertyValueFactory<>("total_price"));
    }

    public void setUser(User user) {
        this.currentUser = user;
        if (user != null) {
            AuthenticatedUser.setText(user.getFirstname());
        } else {
            AuthenticatedUser.setText("Guest");
        }
        try {
            List<Reservation> reservations = (user != null) ? rs.getReservationsByUser(user.getId()) : rs.getAllData();
            ObservableList<Reservation> data = FXCollections.observableArrayList(reservations);
            table_view.setItems(data);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec du chargement des réservations", "Impossible de charger les réservations: " + e.getMessage());
        }

        // Ajout des boutons conditionnels (exemple fictif à adapter selon UI)
        if (currentUser != null) {
            boolean client = hasRole("Client");
            boolean artist = hasRole("Artist");
            HBox btns = new HBox();
            String style = "-fx-background-radius: 8px; -fx-text-fill: white; -fx-background-color: #4285F4; -fx-padding: 5px 10px;";

            Event event = new Event(); // Ce event doit être l'event concerné (à adapter selon contexte)

            if (client) {
                Button b = new Button("Book Now");
                b.setStyle(style);
                b.setOnAction(e -> booknow(event));
                btns.getChildren().add(b);
            }
            if (artist) {
                Button eBtn = new Button("Edit");
                eBtn.setStyle(style);
                eBtn.setOnAction(e -> {
                    try {
                        edit_event(event);
                    } catch (IOException ex) {
                        showAlert(Alert.AlertType.ERROR, "Error", "Edit failed", ex.getMessage());
                    }
                });

                Button dBtn = new Button("Delete");
                dBtn.setStyle(style + " -fx-background-color:#d70015;");
                dBtn.setOnAction(e -> delete_event(event));

                btns.getChildren().addAll(eBtn, dBtn);
            }

            // Ajouter btns à un container visuel comme VBox, HBox ou autre dans la vue (FXML)
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

    @FXML
    public void GoToProduct(ActionEvent actionEvent) {}

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

    @FXML
    public void GoToQuiz(ActionEvent actionEvent) {}

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
    public void GoToVerifiedArtist(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/verifiedArtist.fxml"));
            Parent root = loader.load();
            VerifiedArtist controller = loader.getController();
            if (controller != null) {
                controller.setUser(currentUser);
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de contrôleur", "Impossible de charger le contrôleur VerifiedArtist");
            }
            table_view.getScene().setRoot(root);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Chargement échoué", "Impossible de charger la page verifiedArtist.fxml: " + e.getMessage());
        }
    }

    @FXML
    void goToHome(ActionEvent event) {
        try {
            URL fxmlLocation = getClass().getResource("/home.fxml");
            if (fxmlLocation == null) {
                System.err.println("Error: /home.fxml not found in resources");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            home homeController = loader.getController();
            homeController.setUser(currentUser);

            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error loading home page: " + e.getMessage());
        }
    }

    private void booknow(Event event) {
        System.out.println("Book now clicked for event: " + event.getId());
    }

    private void edit_event(Event event) throws IOException {
        System.out.println("Edit clicked for event: " + event.getId());
    }

    private void delete_event(Event event) {
        System.out.println("Delete clicked for event: " + event.getId());
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
