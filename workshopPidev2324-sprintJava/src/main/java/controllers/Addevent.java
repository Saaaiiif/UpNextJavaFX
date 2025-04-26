package controllers;

import edu.up_next.entities.Event;
import edu.up_next.services.EventServices;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import java.util.UUID;

public class Addevent implements Initializable {

        FileChooser fileChooser = new FileChooser();//ouutil JavaFX pour ouvrir une fenêtre de sélection de fichiers.
        private File selectedImageFile;//stocke le fichier image choisi par l'utilisateur.
        private String eventImageFileName;


        @FXML
        private Label dateErrorLabel;
        @FXML
        private Label imageErrorLabel;
        @FXML
        private Label locationErrorLabel;
        @FXML
        private Label priceErrorLabel;
        @FXML
        private Label quantityErrorLabel;
        @FXML
        private Label successLabel;
        @FXML
        private Label titleErrorLabel;
        @FXML
        private TextField count_ticket;

        @FXML
        private TextField description;

        @FXML
        private DatePicker end_date;

        @FXML
        private TextField guest_id;

        @FXML
        private TextField host_id;

        @FXML
        private ImageView image;

        @FXML
        private TextField location;

        @FXML
        private DatePicker start_date;

        @FXML
        private TextField status_event;

        @FXML
        private TextField ticket_price;

        @FXML
        private TextField title;

        private String uploadImage(File imageFile) {
                if (imageFile == null) return null;

                // Récupérer le nom sans extension
                String originalName = imageFile.getName();
                String baseName = originalName.substring(0, originalName.lastIndexOf('.'));
                String extension = originalName.substring(originalName.lastIndexOf('.') + 1);

                // Nettoyer le nom (slug) + ajout ID unique
                String safeName = baseName.replaceAll("[^a-zA-Z0-9\\-_]", "_");
                String newFileName = safeName + "-" + UUID.randomUUID() + "." + extension;

                // Chemin vers ton dossier images
                Path destinationPath = Paths.get("src/main/resources/images/" + newFileName);

                try {
                        Files.copy(imageFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
                        return newFileName; // Tu peux l'enregistrer en BDD ou autre
                } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                }
        }


        @FXML
        void getImage(MouseEvent event) {
                selectedImageFile = fileChooser.showOpenDialog(new Stage());
                if (selectedImageFile != null) {
                        String uploadedFileName = uploadImage(selectedImageFile);

                        if (uploadedFileName != null) {
                                eventImageFileName = uploadedFileName; // <--- ici

                                File imgFile = new File("src/main/resources/images/" + uploadedFileName);
                                if (imgFile.exists()) {
                                        Image img = new Image(imgFile.toURI().toString());
                                        image.setImage(img);
                                } else {
                                        Alert alert = new Alert(Alert.AlertType.ERROR);
                                        alert.setTitle("Erreur");
                                        alert.setContentText("L'image n'existe pas après l'upload !");
                                        alert.showAndWait();
                                }

                        } else {
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Erreur");
                                alert.setContentText("L'upload de l'image a échoué.");
                                alert.showAndWait();
                        }
        }}
        EventServices es= new EventServices();
        @FXML
        public void save(MouseEvent event) {
                if (!validateFields()) {
                        return; // stop everything if validation fails
                }
            try {
                // Conversion des dates en LocalDateTime
                LocalDateTime startDateTime = start_date.getValue().atStartOfDay();
                LocalDateTime endDateTime = end_date.getValue().atStartOfDay();

                    // Gestion de status_event : si le champ est vide ou null, attribuer une valeur par défaut
                    String status = (status_event != null && !status_event.getText().trim().isEmpty()) ? status_event.getText() : "Non défini";

                    // 🖼️ Gestion de l’image (copier dans resources/images et générer un nom unique)
                    String imageNameToStore = (eventImageFileName != null) ? eventImageFileName : "";


                    Event ev = new Event(
                            0,  // id de l'événement
                            Integer.parseInt(host_id.getText()), // host_id
                            title.getText(),  // titre de l'événement
                            description.getText(), // descrip// description
                            start_date.getValue().atStartOfDay(), // startdate (converti en LocalDateTime)
                            end_date.getValue().atStartOfDay(), // enddate (converti en LocalDateTime)
                            status,  // statut de l'événement
                            Integer.parseInt(count_ticket.getText()),  // nombre de billets disponibles
                            Integer.parseInt(guest_id.getText()), // guest_id  // guest_id
                            location.getText(),  // lieu
                            eventImageFileName, // image (chemin du fichier)
                            Double.parseDouble(ticket_price.getText()) // ticket_price   // prix du billet
                    );
                    es.addEntity(ev);
                    successLabel.setVisible(true);

                    PauseTransition pause = new PauseTransition(Duration.seconds(3));
                    pause.setOnFinished(e -> successLabel.setVisible(false));
                    pause.play();

            } catch (NumberFormatException e) {
                    // Gestion des erreurs de conversion (si un champ n'est pas un nombre valide)
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur de saisie");
                    alert.setContentText("Veuillez vérifier les valeurs saisies (notamment les nombres).");
                    alert.showAndWait();
            }
        }

        @Override
        public void initialize(URL url, ResourceBundle resourceBundle) {
                successLabel.setVisible(false);
                File defaultDir = new File(System.getProperty("user.home")); // ou un dossier comme "images"
                if (defaultDir.exists() && defaultDir.isDirectory()) {
                        fileChooser.setInitialDirectory(defaultDir);
                }
        }

        @FXML
        void show_list(MouseEvent event) {
                try {
                        Parent root = FXMLLoader.load(getClass().getResource("/eventlist.fxml"));
                        title.getScene().setRoot(root);
                }catch (IOException e){
                        e.printStackTrace(); // ou affiche une alerte
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Erreur");
                        alert.setHeaderText("Chargement échoué");
                        alert.setContentText("Impossible de charger la page Eventlist.fxml");
                        alert.showAndWait();
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
                if (location.getText().trim().isEmpty()) {
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
                                this.location.setText(lieu); // 🖋️ Remplir ton TextField avec le nom du lieu
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

