package controllers;

import edu.up_next.entities.Event;
import edu.up_next.services.EventServices;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;

public class Editevent {

    private File selectedImageFile;

    @FXML
    private Label imageErrorLabel;
    @FXML
    private Label dateErrorLabel;
    @FXML
    private Label usuccessLabel;
    @FXML
    private Label titleErrorLabel;
    @FXML
    private Label locationErrorLabel;

    @FXML
    private Label priceErrorLabel;

    @FXML
    private Label quantityErrorLabel;

    @FXML
    private TextField count_ticket;
    @FXML
    private TextField description;
    @FXML
    private DatePicker end_date;
    @FXML
    private TextField guest_id;
    @FXML
    private TextField user_id;
    @FXML
    private ImageView image;
    @FXML
    private TextField localisation;
    @FXML
    private DatePicker start_date;
    @FXML
    private TextField ticket_price;
    @FXML
    private TextField title;

    private final EventServices es = new EventServices();
    private final FileChooser fileChooser = new FileChooser();

    public static Event eventToEdit;

    @FXML
    public void initialize() {
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
            user_id.setText(String.valueOf(eventToEdit.getHost_id()));

            if (eventToEdit.getImage() != null && !eventToEdit.getImage().isEmpty()) {
                File imgFile = new File(eventToEdit.getImage());
                if (imgFile.exists()) {
                    image.setImage(new Image(imgFile.toURI().toString()));
                }
            }
        }
    }

    @FXML
    void getImage(MouseEvent event) {
        selectedImageFile = fileChooser.showOpenDialog(new Stage());
        if (selectedImageFile != null) {
            Image img = new Image(selectedImageFile.toURI().toString());
            image.setImage(img);
        }
    }

    @FXML
    void return_list(MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/eventlist.fxml"));
            title.getScene().setRoot(root);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Chargement échoué", "Impossible de charger la page eventlist.fxml");
        }
    }

    @FXML
    void update_event(MouseEvent event) {
        if (!validateFields()) {
            return; // stop everything if validation fails
        }

        try {
            eventToEdit.setTitle(title.getText());
            eventToEdit.setDescrip(description.getText());
            eventToEdit.setLocation(localisation.getText());
            eventToEdit.setStartdate(start_date.getValue().atStartOfDay());
            eventToEdit.setEnddate(end_date.getValue().atStartOfDay());
            eventToEdit.setTicket_count(Integer.parseInt(count_ticket.getText()));
            eventToEdit.setTicket_price(Double.parseDouble(ticket_price.getText()));
            eventToEdit.setGuest_id(Integer.parseInt(guest_id.getText()));
            eventToEdit.setHost_id(Integer.parseInt(user_id.getText()));

            if (selectedImageFile != null) {
                eventToEdit.setImage(selectedImageFile.getAbsolutePath());
            }

            es.updateEntity(eventToEdit, eventToEdit.getId());

            usuccessLabel.setText("Événement mis à jour avec succès !");
            usuccessLabel.setVisible(true);

            // Attendre 3 secondes avant de retourner à la liste
            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(e -> return_list(null));
            pause.play();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Mise à jour échouée", "Une erreur s'est produite lors de la mise à jour.");
        }
    }

    private boolean validateFields() {
        boolean isValid = true;

        // Title
        if (title.getText().trim().isEmpty()) {
            titleErrorLabel.setVisible(true);
            isValid = false;
        } else {
            titleErrorLabel.setVisible(false);
        }

        // Location
        if (localisation.getText().trim().isEmpty()) {
            locationErrorLabel.setVisible(true);
            isValid = false;
        } else {
            locationErrorLabel.setVisible(false);
        }
        // Ticket Count
        try {
            int tickets = Integer.parseInt(count_ticket.getText());
            if (tickets < 0) throw new NumberFormatException();
            quantityErrorLabel.setVisible(false);
        } catch (NumberFormatException e) {
            quantityErrorLabel.setVisible(true);
            isValid = false;
        }
        // Ticket Price
        try {
            double price = Double.parseDouble(ticket_price.getText());
            if (price < 0) throw new NumberFormatException();
            priceErrorLabel.setVisible(false);
        } catch (NumberFormatException e) {
            priceErrorLabel.setVisible(true);
            isValid = false;
        }

        // Dates
        if (start_date.getValue() == null || end_date.getValue() == null) {
            dateErrorLabel.setVisible(true);
            isValid = false;
        } else if (start_date.getValue().isAfter(end_date.getValue())) {
            dateErrorLabel.setVisible(true);
            isValid = false;
        } else {
            dateErrorLabel.setVisible(false);
        }

        // Image (optional but recommended)
        if (selectedImageFile == null) {
            imageErrorLabel.setVisible(true);
        } else {
            imageErrorLabel.setVisible(false);
        }

        return isValid;
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
            // 1) Charger ton FXML de la map
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LocationPicker.fxml"));
            Parent root = loader.load();

            // 2) Ouvrir la map dans une popup modale
            Stage popup = new Stage();
            popup.setTitle("Choisir une localisation");
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.setScene(new Scene(root));

            // 3) Récupérer le contrôleur pour accéder aux données après la fermeture
            LocationPicker controller = loader.getController();

            popup.showAndWait();  // ⏳ Attend que l'utilisateur ferme la popup

            // 4) Une fois fermée, récupérer le nom du lieu sélectionné
            String lieu = controller.getSelectedLocation(); // 🔥 ici on prend le nom du lieu
            if (lieu != null) {
                this.localisation.setText(lieu); // 🖋️ Remplir ton TextField avec le nom du lieu
                System.out.println("📍 Lieu sélectionné : " + lieu);
            } else {
                System.out.println("❌ Aucun lieu sélectionné.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Impossible de charger la fenêtre de localisation").showAndWait();
        }
    }
}
