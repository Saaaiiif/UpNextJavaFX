package controllers.event_reser_ouma;

import controllers.VerifiedArtist;
import controllers.home;
import controllers.profile;
import edu.up_next.ReclamationController;
import edu.up_next.entities.Event;
import edu.up_next.entities.User;
import edu.up_next.services.EventServices;
import edu.up_next.services.UserServices;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

public class Addevent implements Initializable {
        private FileChooser fileChooser = new FileChooser();
        private File selectedImageFile;
        private String eventImageFileName;
        private User currentUser;
        private UserServices userServices = new UserServices();

        public void setUser(User user) {
                this.currentUser = user;
                System.out.println("setUser: User set with ID=" + (user != null ? user.getId() : "null") + ", Firstname=" + (user != null ? user.getFirstname() : "null"));

                if (user != null && !hasRole("Artist")) {
                        navigateTo("/eventlist.fxml", "Événements");
                        return; // Ajouté : éviter la suite si l'accès est refusé
                }

                // Déplacement ici : assure l'initialisation après que currentUser soit défini
                if (user != null) {
                        AuthenticatedUser.setText(user.getFirstname() != null ? user.getFirstname() : "Guest");
                        if (user_id != null) {
                                user_id.setText(String.valueOf(user.getId()));
                                user_id.setDisable(true);
                        }
                        if (user.getImage() != null && !user.getImage().isEmpty()) {
                                File imgFile = new File("src/main/resources/images/" + user.getImage());
                                if (imgFile.exists()) {
                                        Image profileImg = new Image(imgFile.toURI().toString());
                                        ProfileImage.setImage(profileImg);
                                        ProfileImage1.setImage(profileImg);
                                }
                        }
                }
        }

        private boolean hasRole(String roleName) {
                if (currentUser == null) {
                        System.out.println("hasRole: currentUser is null");
                        return false;
                }
                String roles = currentUser.getRoles();
                boolean hasRole = roles != null && roles.toLowerCase().contains(roleName.toLowerCase());
                System.out.println("hasRole: Checking for role=" + roleName + ", Roles=" + roles + ", Result=" + hasRole);
                return hasRole;
        }

        @FXML private Text AuthenticatedUser;
        @FXML private Hyperlink HomeLink;
        @FXML private Hyperlink EventLink;
        @FXML private Hyperlink Logout;
        @FXML private Hyperlink ProductLink;
        @FXML private Hyperlink ProfileLink;
        @FXML private Hyperlink QuizLink;
        @FXML private Hyperlink VerifiedArtistLink;
        @FXML private Hyperlink VerifiedArtistLink1;
        @FXML private ImageView ProfileImage;
        @FXML private ImageView ProfileImage1;
        @FXML private Label dateErrorLabel;
        @FXML private Label imageErrorLabel;
        @FXML private Label locationErrorLabel;
        @FXML private Label priceErrorLabel;
        @FXML private Label quantityErrorLabel;
        @FXML private Label successLabel;
        @FXML private Label titleErrorLabel;
        @FXML private TextField count_ticket;
        @FXML private TextField description;
        @FXML private DatePicker end_date;
        @FXML private TextField user_id;
        @FXML private ComboBox<User> guest_id;
        @FXML private ImageView image;
        @FXML private TextField location;
        @FXML private DatePicker start_date;
        @FXML private TextField status_event;
        @FXML private TextField ticket_price;
        @FXML private TextField title;

        private String uploadImage(File imageFile) {
                if (imageFile == null) return null;

                String originalName = imageFile.getName();
                String baseName = originalName.substring(0, originalName.lastIndexOf('.'));
                String extension = originalName.substring(originalName.lastIndexOf('.') + 1);
                String safeName = baseName.replaceAll("[^a-zA-Z0-9\\-_]", "_");
                String newFileName = safeName + "-" + UUID.randomUUID() + "." + extension;

                Path destinationPath = Paths.get("src/main/resources/images/" + newFileName);
                try {
                        Files.createDirectories(destinationPath.getParent());
                        Files.copy(imageFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
                        return newFileName;
                } catch (IOException e) {
                        System.err.println("Failed to upload image: " + e.getMessage());
                        return null;
                }
        }

        @FXML
        void getImage(MouseEvent event) {
                selectedImageFile = fileChooser.showOpenDialog(new Stage());
                if (selectedImageFile != null) {
                        String uploadedFileName = uploadImage(selectedImageFile);
                        if (uploadedFileName != null) {
                                eventImageFileName = uploadedFileName;
                                File imgFile = new File("src/main/resources/images/" + uploadedFileName);
                                if (imgFile.exists()) {
                                        Image img = new Image(imgFile.toURI().toString());
                                        image.setImage(img);
                                        imageErrorLabel.setVisible(false);
                                } else {
                                        showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur Image", "L'image n'existe pas après l'upload !");
                                }
                        } else {
                                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'Upload", "L'upload de l'image a échoué.");
                        }
                }
        }

        private EventServices es = new EventServices();

        @FXML
        public void save(MouseEvent event) {
                if (!validateFields()) {
                        return;
                }
            try {
                try {
                    String status = (status_event != null && !status_event.getText().trim().isEmpty()) ? status_event.getText() : "Non défini";
                    String imageNameToStore = (eventImageFileName != null) ? eventImageFileName : "";

                    User selectedArtist = guest_id.getValue();
                    if (selectedArtist == null) {
                        return;
                    }

                    Event ev = new Event(
                            0,
                            Integer.parseInt(user_id.getText()), // Use user_id for ID
                            title.getText(),
                            description.getText(),
                            start_date.getValue().atStartOfDay(),
                            end_date.getValue().atStartOfDay(),
                            status,
                            Integer.parseInt(count_ticket.getText()),
                            selectedArtist.getId(),
                            location.getText(),
                            imageNameToStore,
                            Double.parseDouble(ticket_price.getText())
                    );
                    es.addEntity(ev);
                    successLabel.setText("Événement ajouté avec succès !");
                    successLabel.setVisible(true);

                    PauseTransition pause = new PauseTransition(Duration.seconds(3));
                    pause.setOnFinished(e -> navigateTo("/eventlist.fxml", "Événements"));
                    pause.play();
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Entrée Invalide", "Veuillez vérifier les valeurs numériques (ID utilisateur, ID invité, nombre de billets, prix).");
                } catch (Exception e) {
                    System.err.println("Failed to save event: " + e.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'Enregistrement", "Échec de l'enregistrement de l'événement : " + e.getMessage());
                }
            } finally {

            }
        }

        @Override
        public void initialize(URL url, ResourceBundle resourceBundle) {
                if (currentUser != null) {
                        String firstname = currentUser.getFirstname() != null ? currentUser.getFirstname() : "Guest";
                        String userId = currentUser.getId() >= 0 ? String.valueOf(currentUser.getId()) : "";

                        AuthenticatedUser.setText(firstname);
                        if (user_id != null) {
                                user_id.setText(userId);
                                user_id.setDisable(true);
                        }

                        if (currentUser.getImage() != null && !currentUser.getImage().isEmpty()) {
                                File imgFile = new File("src/main/resources/images/" + currentUser.getImage());
                                if (imgFile.exists()) {
                                        Image profileImg = new Image(imgFile.toURI().toString());
                                        ProfileImage.setImage(profileImg);
                                        ProfileImage1.setImage(profileImg);
                                }
                        }
                } else {
                        AuthenticatedUser.setText("Guest");
                        if (user_id != null) user_id.setText("");
                }

                // Hide error and success labels
                successLabel.setVisible(false);
                imageErrorLabel.setVisible(false);
                dateErrorLabel.setVisible(false);
                titleErrorLabel.setVisible(false);
                locationErrorLabel.setVisible(false);
                priceErrorLabel.setVisible(false);
                quantityErrorLabel.setVisible(false);
// Populate ComboBox with artists
                        try {
                                List<User> artists = userServices.getArtists();
                                guest_id.setItems(FXCollections.observableArrayList(artists));
                                guest_id.setConverter(new StringConverter<User>() {
                                        @Override
                                        public String toString(User user) {
                                                return user != null ? user.getFirstname() + " (" + user.getEmail() + ")" : "";
                                        }

                                        @Override
                                        public User fromString(String string) {
                                                return null; // Not needed for selection
                                        }
                                });
                        } catch (SQLException e) {
                                System.err.println("Failed to load artists: " + e.getMessage());
                        }
                // Initialize FileChooser
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
                File defaultDir = new File(System.getProperty("user.home"));
                if (defaultDir.exists() && defaultDir.isDirectory()) {
                        fileChooser.setInitialDirectory(defaultDir);
                }

                // Set user details
                if (currentUser != null) {
                        String firstname = currentUser.getFirstname() != null ? currentUser.getFirstname() : "Guest";
                        String userId = currentUser.getId() >= 0 ? String.valueOf(currentUser.getId()) : "";
                        System.out.println("initialize: Setting AuthenticatedUser to " + firstname + ", user_id to " + userId);
                        AuthenticatedUser.setText(firstname);
                        if (user_id != null) {
                                user_id.setText(userId);
                                user_id.setDisable(true);
                                System.out.println("initialize: user_id set to " + userId);
                        } else {
                                System.err.println("initialize: Warning: user_id TextField is null");
                        }
                        if (currentUser.getImage() != null && !currentUser.getImage().isEmpty()) {
                                File imgFile = new File("src/main/resources/images/" + currentUser.getImage());
                                if (imgFile.exists()) {
                                        Image profileImg = new Image(imgFile.toURI().toString());
                                        ProfileImage.setImage(profileImg);
                                        ProfileImage1.setImage(profileImg);
                                } else {
                                        System.err.println("initialize: Profile image not found: " + currentUser.getImage());
                                }
                        }
                } else {
                        System.out.println("initialize: currentUser is null, setting defaults");
                }
        }

        private boolean validateFields() {
                boolean isValid = true;
                titleErrorLabel.setVisible(title.getText().trim().isEmpty());
                locationErrorLabel.setVisible(location.getText().trim().isEmpty());
                try {
                        int tickets = Integer.parseInt(count_ticket.getText());
                        if (tickets < 0) throw new NumberFormatException();
                        quantityErrorLabel.setVisible(false);
                } catch (Exception e) {
                        quantityErrorLabel.setVisible(true);
                        isValid = false;
                }
                try {
                        double price = Double.parseDouble(ticket_price.getText());
                        if (price < 0) throw new NumberFormatException();
                        priceErrorLabel.setVisible(false);
                } catch (Exception e) {
                        priceErrorLabel.setVisible(true);
                        isValid = false;
                }
                try {
                        if (user_id != null) {
                                Integer.parseInt(user_id.getText());
                        } else {
                                showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "ID Utilisateur Invalide", "Le champ ID utilisateur n'est pas initialisé.");
                                isValid = false;
                        }
                } catch (NumberFormatException e) {
                        showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "ID Utilisateur Invalide", "L'ID utilisateur doit être un nombre valide.");
                        isValid = false;
                }
                boolean dateErr = start_date.getValue() == null || end_date.getValue() == null || start_date.getValue().isAfter(end_date.getValue());
                dateErrorLabel.setVisible(dateErr);
                imageErrorLabel.setVisible(selectedImageFile == null && eventImageFileName == null);
                return !titleErrorLabel.isVisible() && !locationErrorLabel.isVisible() && !quantityErrorLabel.isVisible() &&
                        !priceErrorLabel.isVisible() && !dateErr && !imageErrorLabel.isVisible();
        }

        @FXML
        private void choose_location(MouseEvent event) {
                try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/LocationPicker.fxml"));
                        Parent root = loader.load();
                        Stage popup = new Stage();
                        popup.setTitle("Choisir une localisation");
                        popup.initModality(Modality.APPLICATION_MODAL);
                        popup.setScene(new Scene(root));
                        LocationPicker controller = loader.getController();
                        popup.showAndWait();

                        String lieu = controller.getSelectedLocation();
                        if (lieu != null) {
                                this.location.setText(lieu);
                                System.out.println("📍 Lieu sélectionné : " + lieu);
                        } else {
                                System.out.println("❌ Aucun lieu sélectionné.");
                        }
                } catch (IOException e) {
                        System.err.println("Failed to load LocationPicker: " + e.getMessage());
                        showAlert(Alert.AlertType.ERROR, "Erreur", "Échec du chargement", "Impossible de charger la fenêtre de localisation.");
                }
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
        public void GoToProduct(ActionEvent event) {
                navigateTo("/product.fxml", "Produits");
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
        public void GoToQuiz(ActionEvent event) {
                navigateTo("/quiz.fxml", "Quiz");
        }

        @FXML
        public void goToHome(ActionEvent event) {
                navigateTo("/home.fxml", "Accueil");
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

        private void navigateTo(String fxmlPath, String pageName) {
                try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                        if (loader.getLocation() == null) {
                                throw new IOException("Fichier FXML " + fxmlPath + " introuvable dans les ressources");
                        }
                        Parent root = loader.load();
                        // Pass currentUser to specific controllers
                        if (fxmlPath.equals("/eventlist.fxml")) {
                                Eventlist controller = loader.getController();
                                if (controller != null) {
                                        controller.setUser(currentUser);
                                }
                        } else if (fxmlPath.equals("/home.fxml")) {
                                home controller = loader.getController();
                                if (controller != null) {
                                        controller.setUser(currentUser);
                                }
                        } else if (fxmlPath.equals("/addevent.fxml")) {
                                Addevent controller = loader.getController();
                                if (controller != null) {
                                        controller.setUser(currentUser);
                                }
                        }
                        Stage stage = (Stage) (AuthenticatedUser != null ? AuthenticatedUser.getScene().getWindow() : new Stage());
                        stage.setScene(new Scene(root));
                        stage.show();
                } catch (IOException e) {
                        System.err.println("Échec du chargement de " + pageName + " : " + e.getMessage());
                        showAlert(Alert.AlertType.ERROR, "Erreur", "Échec du chargement", "Impossible de charger " + pageName + " : " + e.getMessage());
                }
        }

        private void showAlert(Alert.AlertType type, String title, String header, String message) {
                Alert alert = new Alert(type);
                alert.setTitle(title);
                alert.setHeaderText(header);
                alert.setContentText(message);
                alert.showAndWait();
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




