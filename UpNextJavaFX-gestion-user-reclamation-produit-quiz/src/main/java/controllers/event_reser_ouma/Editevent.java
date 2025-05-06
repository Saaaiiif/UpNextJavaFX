package controllers.event_reser_ouma;

import controllers.VerifiedArtist;
import controllers.home;
import controllers.profile;
import edu.up_next.ReclamationController;
import edu.up_next.entities.Event;
import edu.up_next.entities.User;
import edu.up_next.services.EventServices;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class Editevent {

    private User currentUser;

    @FXML private Text AuthenticatedUser;
    @FXML private Label imageErrorLabel, dateErrorLabel, usuccessLabel,
            titleErrorLabel, locationErrorLabel,
            priceErrorLabel, quantityErrorLabel;
    @FXML private TextField count_ticket, description, guest_id, user_name,
            localisation, ticket_price, title;
    @FXML private DatePicker end_date, start_date;
    @FXML private ImageView image;

    private File selectedImageFile;
    private final EventServices es = new EventServices();
    private final FileChooser fileChooser = new FileChooser();
    public static Event eventToEdit;

    /**
     * Injection de l'utilisateur connecté
     */
    public void setUser(User user) {
        this.currentUser = user;
        // on profite de l’injection pour remplir immédiatement le champ
        if (currentUser != null) {
            user_name.setText(currentUser.getFirstname());
            user_name.setDisable(true);
            AuthenticatedUser.setText(currentUser.getFirstname());
        }
    }

    @FXML
    public void initialize() {
        // Afficher le nom de l'utilisateur connecté
        if (currentUser != null) {
            AuthenticatedUser.setText(currentUser.getFirstname());
            user_name.setText(currentUser.getFirstname());
            user_name.setDisable(true);
        } else {
            AuthenticatedUser.setText("Guest");
        }

        usuccessLabel.setVisible(false);
        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg");
        fileChooser.getExtensionFilters().add(imageFilter);

        if (eventToEdit != null) {
            title.setText(eventToEdit.getTitle());
            description.setText(eventToEdit.getDescrip());
            localisation.setText(eventToEdit.getLocation());
            start_date.setValue(eventToEdit.getStartdate().toLocalDate());
            end_date.setValue(eventToEdit.getEnddate().toLocalDate());
            count_ticket.setText(String.valueOf(eventToEdit.getTicket_count()));
            ticket_price.setText(String.valueOf(eventToEdit.getTicket_price()));
            guest_id.setText(String.valueOf(eventToEdit.getGuest_id()));

            if (eventToEdit.getImage() != null && !eventToEdit.getImage().isEmpty()) {
                File imgFile = new File(eventToEdit.getImage());
                if (imgFile.exists()) image.setImage(new Image(imgFile.toURI().toString()));
            }
        }
    }

    @FXML
    void getImage(MouseEvent event) {
        selectedImageFile = fileChooser.showOpenDialog(image.getScene().getWindow());
        if (selectedImageFile != null) {
            image.setImage(new Image(selectedImageFile.toURI().toString()));
        }
    }

    // Méthode utilitaire pour afficher la scène EventList dans la même fenêtre
    private void showEventList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/eventlist.fxml"));
            Parent root = loader.load();
            Eventlist controller = loader.getController();
            controller.setUser(currentUser);

            Stage stage = (Stage) title.getScene().getWindow(); // ou n’importe quel Node déjà en scène
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR,
                    "Erreur",
                    "Chargement échoué",
                    "Impossible de charger eventlist.fxml : " + e.getMessage());
        }
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

    @FXML
    void update_event(MouseEvent event) {
        if (!validateFields()) return;

        try {
            // Mise à jour de l'entité
            eventToEdit.setTitle(title.getText());
            eventToEdit.setDescrip(description.getText());
            eventToEdit.setLocation(localisation.getText());
            eventToEdit.setStartdate(start_date.getValue().atStartOfDay());
            eventToEdit.setEnddate(end_date.getValue().atStartOfDay());
            eventToEdit.setTicket_count(Integer.parseInt(count_ticket.getText()));
            eventToEdit.setTicket_price(Double.parseDouble(ticket_price.getText()));
            eventToEdit.setGuest_id(Integer.parseInt(guest_id.getText()));
            if (selectedImageFile != null) {
                eventToEdit.setImage(selectedImageFile.getAbsolutePath());
            }
            es.updateEntity(eventToEdit, eventToEdit.getId());

            // Message de succès
            usuccessLabel.setText("Événement mis à jour avec succès !");
            usuccessLabel.setVisible(true);

            // Après 1 seconde, fermer la fenêtre d’édition en cours
            PauseTransition pause = new PauseTransition(Duration.seconds(1));
            pause.setOnFinished(e -> {
                Stage stage = (Stage) title.getScene().getWindow();
                stage.close();
            });
            pause.play();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR,
                    "Erreur",
                    "Mise à jour échouée",
                    "Une erreur est survenue.");
        }
    }



    private boolean validateFields() {
        boolean isValid = true;
        titleErrorLabel.setVisible(title.getText().trim().isEmpty());
        locationErrorLabel.setVisible(localisation.getText().trim().isEmpty());
        try { if (Integer.parseInt(count_ticket.getText()) < 0) throw new NumberFormatException(); quantityErrorLabel.setVisible(false);}
        catch (Exception e) { quantityErrorLabel.setVisible(true); isValid=false; }
        try { if (Double.parseDouble(ticket_price.getText()) < 0) throw new NumberFormatException(); priceErrorLabel.setVisible(false);}
        catch (Exception e) { priceErrorLabel.setVisible(true); isValid=false; }
        boolean dateErr = start_date.getValue()==null||end_date.getValue()==null||start_date.getValue().isAfter(end_date.getValue());
        dateErrorLabel.setVisible(dateErr);
        imageErrorLabel.setVisible(selectedImageFile==null);
        return !titleErrorLabel.isVisible() && !locationErrorLabel.isVisible() && !quantityErrorLabel.isVisible() && !priceErrorLabel.isVisible() && !dateErr;
    }

    private void showAlert(Alert.AlertType type, String title, String header, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void choose_location(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LocationPicker.fxml"));
            Parent root = loader.load();
            Stage popup = new Stage(); popup.setTitle("Choisir une localisation"); popup.initModality(Modality.APPLICATION_MODAL);
            popup.setScene(new Scene(root));
            LocationPicker controller = loader.getController();
            popup.showAndWait();
            String lieu = controller.getSelectedLocation();
            if (lieu != null) localisation.setText(lieu);
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Impossible de charger la fenêtre de localisation").showAndWait();
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
    public void GoToProduct(ActionEvent actionEvent) {}
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
    public void GoToQuiz(ActionEvent actionEvent) {}
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
