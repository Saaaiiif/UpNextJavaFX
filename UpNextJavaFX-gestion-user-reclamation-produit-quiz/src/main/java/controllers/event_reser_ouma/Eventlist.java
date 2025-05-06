package controllers.event_reser_ouma;

import com.clarifai.channel.ClarifaiChannel;
import com.clarifai.credentials.ClarifaiCallCredentials;
import com.clarifai.grpc.api.*;
import com.clarifai.grpc.api.status.StatusCode;
import controllers.event_reser_ouma.Addreservation;
import controllers.VerifiedArtist;
import controllers.communities.ArtistsController;
import controllers.communities.CommunitiesController;
import controllers.communities.RootLayoutController;
import controllers.communities.SceneTransitionUtil;
import controllers.communities.SessionManager;
import controllers.communities.SessionType;
import controllers.communities.UserCommunitiesController;
import controllers.home;
import controllers.profile;
import edu.up_next.ReclamationController;
import java.time.format.DateTimeFormatter;
import edu.up_next.entities.Event;
import edu.up_next.entities.User;
import edu.up_next.services.EventServices;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Eventlist {

    @FXML
    private TextField search;
    @FXML
    private VBox eventList;
    @FXML
    private Button searchByImageButton;
    @FXML
    private Text AuthenticatedUser;
    @FXML
    private ImageView ProfileImage;
    @FXML
    private Hyperlink ProfileLink;
    @FXML
    private Hyperlink ProductLink;
    @FXML
    private Hyperlink EventLink;
    @FXML
    private Hyperlink VerifiedArtistLink;
    @FXML
    private Hyperlink HomeLink;
    @FXML
    private Hyperlink Logout;
    @FXML
    private Hyperlink QuizLink;
    @FXML
    private ProgressIndicator progressIndicator;

    private ObservableList<Event> allEvents;
    private final EventServices es = new EventServices();
    private final FileChooser fileChooser = new FileChooser();
    private V2Grpc.V2BlockingStub clarifaiClient;
    private final Map<Integer, List<String>> imageLabelsCache = new HashMap<>();
    private User currentUser; // Added to store current user
    // Formatter pour les dates
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        // Configure Clarifai client
        String clarifaiApiKey = System.getenv("af090ca908284708bb29e6aba6517da5");
        clarifaiClient = V2Grpc.newBlockingStub(ClarifaiChannel.INSTANCE.getGrpcChannel())
                .withCallCredentials(new ClarifaiCallCredentials(clarifaiApiKey));

        // Configure FileChooser
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        // Setup search listener
        if (search != null) {
            search.textProperty().addListener((obs, oldV, newV) -> filterEvents(newV));
        }

        // Default UI placeholders
        AuthenticatedUser.setText("Guest");
    }

    private void displayEvents(ObservableList<Event> events) {
        eventList.getChildren().clear();  // Clear les anciens événements
        for (Event event : events) {
            HBox card = createEventCard(event); // Crée une carte pour chaque événement
            eventList.getChildren().add(card);  // Ajoute la carte au conteneur
        }
    }

    private HBox createEventCard(Event event) {
        HBox card = new HBox(10);
        card.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #cccccc; -fx-border-radius: 10; " +
                "-fx-background-radius: 10; -fx-padding: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        card.setPrefWidth(600);

        // ImageView pour afficher l'image de l'événement
        ImageView iv = new ImageView();
        iv.setFitWidth(150);
        iv.setFitHeight(100);
        iv.setPreserveRatio(true);

        // Récupérer l'image de l'événement
        String path = event.getImage();
        if (path != null && !path.isEmpty()) {
            try {
                File imgFile = new File("src/main/resources/images/" + path);
                if (imgFile.exists()) {
                    iv.setImage(new Image(imgFile.toURI().toString()));
                } else {
                    System.out.println("❌ Fichier image introuvable : " + imgFile.getAbsolutePath());
                    Label noImageLabel = new Label("Image non disponible");
                    card.getChildren().add(noImageLabel);
                }
            } catch (Exception e) {
                System.out.println("❌ Erreur de chargement de l'image : " + path + ", Erreur : " + e.getMessage());
            }
        } else {
            System.out.println("⚠️ Aucune image définie pour l'événement : " + event.getTitle());
            Label noImageLabel = new Label("Image non disponible");
            card.getChildren().add(noImageLabel);
        }

        // Détails
        VBox details = new VBox(5);
        details.setPrefWidth(300);

        Label title = new Label("Titre : " + (event.getTitle() != null ? event.getTitle() : "Untitled Event"));
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label desc = new Label("Description : " + (event.getDescrip() != null ? event.getDescrip() : "No description."));
        desc.setWrapText(true);

        String ticketPriceText = (event.getTicket_price() != 0.0) ? String.valueOf(event.getTicket_price()) : "No Price.";
        Label prix = new Label("Prix du ticket : " + ticketPriceText);
        prix.setWrapText(true);

        String ticketCountText = (event.getTicket_count() != 0) ? String.valueOf(event.getTicket_count()) : "No ticket count.";
        Label ticket_dispo = new Label("Tickets disponibles : " + ticketCountText);
        ticket_dispo.setWrapText(true);

        Label location = new Label("Lieu : " + (event.getLocation() != null ? event.getLocation() : "No location."));
        location.setWrapText(true);

        Label start_date = new Label("Date de début : " + (event.getStartdate() != null ? event.getStartdate().format(formatter) : "No start date."));
        start_date.setWrapText(true);

        Label end_date = new Label("Date de fin : " + (event.getEnddate() != null ? event.getEnddate().format(formatter) : "No end date."));
        end_date.setWrapText(true);

        details.getChildren().addAll(title, desc, prix, ticket_dispo, location, start_date, end_date);


        // Boutons d'action selon rôle
        VBox btns = new VBox(5);
        String style = "-fx-background-color: #0078d7; -fx-text-fill: white; -fx-padding: 5 10;";
        if (currentUser != null) {
            boolean client = hasRole("ROLE_CLIENT");
            boolean artist = hasRole("ROLE_ARTIST");
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
                //btns.getChildren().addAll(eBtn, dBtn);
            }
        }

        card.getChildren().addAll(iv, details, btns);
        return card;
    }


    private boolean hasRole(String roleName) {
        String roles = currentUser.getRoles();       // c’est un String
        return roles != null &&
                roles.toLowerCase().contains(roleName.toLowerCase());
    }
    private void cacheImageLabels(ObservableList<Event> events) {
        for (Event event : events) {
            String imagePath = event.getImage();
            if (imagePath != null && !imagePath.isEmpty()) {
                File imageFile = new File("src/main/resources/images/" + imagePath);
                if (imageFile.exists()) {
                    try {
                        List<String> labels = getImageLabels(imageFile);
                        imageLabelsCache.put(event.getId(), labels);
                    } catch (IOException e) {
                        System.out.println("Failed to cache labels for image " + imagePath + ": " + e.getMessage());
                    }
                } else {
                    System.out.println("Image file not found: " + imageFile.getPath());
                }
            }
        }
    }

    private List<String> getImageLabels(File imageFile) throws IOException {
        byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
        MultiOutputResponse response = clarifaiClient.postModelOutputs(
                PostModelOutputsRequest.newBuilder()
                        .setModelId("aa7f35c01e0642fda5cf400f543e7c40") // Modèle général de Clarifai
                        .addInputs(Input.newBuilder()
                                .setData(Data.newBuilder()
                                        .setImage(com.clarifai.grpc.api.Image.newBuilder()
                                                .setBase64(com.google.protobuf.ByteString.copyFrom(imageBytes)))))
                        .build());

        List<String> labels = new ArrayList<>();
        if (response.getStatus().getCode() == StatusCode.SUCCESS) {
            for (Concept concept : response.getOutputs(0).getData().getConceptsList()) {
                if (concept.getValue() > 0.9) { // Seuil de confiance
                    labels.add(concept.getName().toLowerCase());
                }
            }
        } else {
            throw new IOException("Clarifai API error: " + response.getStatus().getDescription());
        }
        return labels;
    }

    @FXML
    void searchByImage(MouseEvent event) {
        File selectedImage = fileChooser.showOpenDialog(new Stage());
        if (selectedImage != null) {
            if (!selectedImage.exists()) {
                showAlert(Alert.AlertType.ERROR, "Error", "File Not Found",
                        "The selected image does not exist: " + selectedImage.getPath());
                return;
            }
            progressIndicator.setVisible(true);
            new Thread(() -> {
                try {
                    List<Event> matchingEvents = searchEventsByImage(selectedImage);
                    Platform.runLater(() -> {
                        // Mise à jour de l'affichage dans le VBox
                        ObservableList<Event> data = FXCollections.observableArrayList(matchingEvents);
                        displayEvents(data);  // Utilisation de la méthode displayEvents pour ajouter les événements dans le VBox
                        progressIndicator.setVisible(false);
                        if (matchingEvents.isEmpty()) {
                            showAlert(Alert.AlertType.INFORMATION, "No Match", "No events found",
                                    "No events match the provided image.");
                        }
                    });
                } catch (IOException e) {
                    Platform.runLater(() -> {
                        progressIndicator.setVisible(false);
                        String message = e.getMessage();
                        if (message.contains("Clarifai API error")) {
                            showAlert(Alert.AlertType.ERROR, "API Error", "Search Failed",
                                    "Failed to process image with Clarifai API: " + message);
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Error", "Search Failed",
                                    "An error occurred during image search: " + message);
                        }
                    });
                }
            }).start();
        }
    }

    private List<Event> searchEventsByImage(File imageFile) throws IOException {
        List<String> imageLabels = getImageLabels(imageFile);
        List<Event> matchingEvents = new ArrayList<>();
        for (Event event : allEvents) {  // Utilisez allEvents qui est une liste observable de tous les événements
            List<String> eventLabels = imageLabelsCache.getOrDefault(event.getId(), new ArrayList<>());
            for (String label : imageLabels) {
                if (eventLabels.contains(label) ||
                        event.getTitle().toLowerCase().contains(label) ||
                        event.getDescrip().toLowerCase().contains(label)) {
                    matchingEvents.add(event);
                    break;
                }
            }
        }
        return matchingEvents;
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

    void delete_event(Event event) {
        try {
            es.deleteEntity(event);
            allEvents = FXCollections.observableArrayList(es.getAllData());
            displayEvents(allEvents);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Delete Failed",
                    "Error while deleting the event: " + e.getMessage());
        }
    }

    void edit_event(Event event) throws IOException {
        Editevent.eventToEdit = event;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/editevent.fxml"));
        Parent root = loader.load();

        // là où on passe bien le même currentUser :
        Editevent controller = loader.getController();
        controller.setUser(this.currentUser);

        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.show();
    }


    void booknow(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/addreservation.fxml"));
            Parent root = loader.load();
            Addreservation controller = loader.getController();
            if (controller != null) {
                controller.setSelectedEvent(event);
                controller.setUser(currentUser); // Pass the logged-in user
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Controller Error",
                        "Failed to get Addreservation controller");
            }
            eventList.getScene().setRoot(root);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Load Failed",
                    "Failed to load reservation page: " + e.getMessage());
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
    void GoToProduct(ActionEvent event) {
    }

    @FXML
    void GoToEvent(ActionEvent event) {
        // Already on events page, no action needed
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
    void GoToQuiz(ActionEvent event) {
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

    private void filterEvents(String searchText) {
        ObservableList<Event> filteredEvents = FXCollections.observableArrayList();
        if (searchText == null || searchText.trim().isEmpty()) {
            filteredEvents.addAll(allEvents);
        } else {
            String lowerCaseFilter = searchText.toLowerCase();
            for (Event event : allEvents) {
                if (event.getTitle() != null && event.getTitle().toLowerCase().contains(lowerCaseFilter)) {
                    filteredEvents.add(event);
                }
            }
        }
        displayEvents(filteredEvents);
    }

    public void setUser(User user) {
        this.currentUser = user;
        // Affiche prénom ou Guest
        if (user != null) {
            AuthenticatedUser.setText(user.getFirstname() + " " + currentUser.getLastname());
            if (user.getImage() != null && !user.getImage().isEmpty()) {
                File imgFile = new File("src/main/resources/images/" + user.getImage());
                if (imgFile.exists()) {
                    ProfileImage.setImage(new Image(imgFile.toURI().toString()));
                }
            }
        } else {
            AuthenticatedUser.setText("Guest");
        }

        // Charge les événements
        allEvents = FXCollections.observableArrayList(es.getAllData());
        displayEvents(allEvents);
        cacheImageLabels(allEvents);
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

    @FXML
    void GoToCommunities(ActionEvent event) {
        try {
            String contentFxmlPath;
            Class<?> controllerClass;

            // Set the session type in SessionManager based on the user's role
            String roles = currentUser.getRoles();
            if (roles != null && roles.contains("ROLE_ARTIST")) {
                // For artists, load the user communities view
                contentFxmlPath = "/views/communities/user-communities-view.fxml";
                controllerClass = UserCommunitiesController.class;
                SessionManager.getInstance().setSessionType(SessionType.ARTIST);
            } else if (roles != null && roles.contains("ROLE_ADMIN")) {
                // For admins, load the main communities view (with admin capabilities)
                contentFxmlPath = "/views/communities/communities-view.fxml";
                controllerClass = CommunitiesController.class;
                SessionManager.getInstance().setSessionType(SessionType.ADMIN);
            } else {
                // For regular users, load the user communities view
                contentFxmlPath = "/views/communities/user-communities-view.fxml";
                controllerClass = UserCommunitiesController.class;
                SessionManager.getInstance().setSessionType(SessionType.USER);
            }

            // Set the current user in SessionManager
            SessionManager.getInstance().setCurrentUser(currentUser);

            // First, load the root layout
            FXMLLoader rootLoader = new FXMLLoader(getClass().getResource("/views/communities/root-layout.fxml"));
            Parent rootView = rootLoader.load();

            // Get the root layout controller
            RootLayoutController rootController = rootLoader.getController();

            // Set the stage on the root controller
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            rootController.setStage(stage);

            // Set the root controller in SceneTransitionUtil
            SceneTransitionUtil.setRootController(rootController);

            // Load the content into the root layout's content area
            rootController.loadContent(contentFxmlPath, controllerClass);

            // Set the scene with the root layout
            Scene scene = new Scene(rootView);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error loading communities page: " + e.getMessage());
        }
    }
}
