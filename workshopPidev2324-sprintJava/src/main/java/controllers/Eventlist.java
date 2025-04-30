package controllers;

import com.clarifai.channel.ClarifaiChannel;
import com.clarifai.credentials.ClarifaiCallCredentials;
import com.clarifai.grpc.api.*;
import com.clarifai.grpc.api.status.StatusCode;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.URL;


public class Eventlist {

    @FXML
    private TextField search;
    private ObservableList<Event> allEvents;

    @FXML
    private TableView<Event> table_view;
    @FXML
    private TableColumn<Event, String> col_title;
    @FXML
    private TableColumn<Event, String> col_status;
    @FXML
    private TableColumn<Event, String> col_desc;
    @FXML
    private ProgressIndicator progressIndicator;

    private final EventServices es = new EventServices();
    private final FileChooser fileChooser = new FileChooser();
    private V2Grpc.V2BlockingStub clarifaiClient;
    private final Map<Integer, List<String>> imageLabelsCache = new HashMap<>();

    @FXML
    public void initialize() throws SQLException {

        if (search != null) {
            search.textProperty().addListener((observable, oldValue, newValue) -> {
                filterEvents(newValue);
            });
        }
        allEvents = FXCollections.observableArrayList(es.getAllData());
        table_view.setItems(allEvents);
        // Initialize Clarifai client
        String clarifaiApiKey = "af090ca908284708bb29e6aba6517da5"; // Remplacez par votre clé API Clarifai
        clarifaiClient = V2Grpc.newBlockingStub(ClarifaiChannel.INSTANCE.getGrpcChannel())
                .withCallCredentials(new ClarifaiCallCredentials(clarifaiApiKey));

        // Configure FileChooser
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        // Set up table columns
        col_title.setCellValueFactory(new PropertyValueFactory<>("title"));
        col_status.setCellValueFactory(new PropertyValueFactory<>("status"));
        col_desc.setCellValueFactory(new PropertyValueFactory<>("description"));


        try {
            // Load events from database
            ObservableList<Event> events = FXCollections.observableArrayList(es.getAllData());
            table_view.setItems(events);
            // Cache image labels
            cacheImageLabels(events);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Load Failed",
                    "Failed to load events: " + e.getMessage());
        }
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
                        .setModelId("aaa03c23b3724a16a56b629203edc62c") // Modèle général de Clarifai
                        .addInputs(Input.newBuilder()
                                .setData(Data.newBuilder()
                                        .setImage(Image.newBuilder()
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
                        ObservableList<Event> data = FXCollections.observableArrayList(matchingEvents);
                        table_view.setItems(data);
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
        for (Event event : table_view.getItems()) {
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

    @FXML
    void delete_event(MouseEvent event) {
        Event selectedEvent = table_view.getSelectionModel().getSelectedItem();

        if (selectedEvent != null) {
            try {
                es.deleteEntity(selectedEvent);
                table_view.getItems().remove(selectedEvent);

            } catch (SQLException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Database Error");
                alert.setHeaderText("Error while deleting the event.");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Selection Error");
            alert.setHeaderText("No event selected for deletion.");
            alert.setContentText("Please select an event to delete.");
            alert.showAndWait();
        }
    }

    @FXML
    void edit_event(MouseEvent event) throws IOException {
        // Récupérer l'événement sélectionné dans la TableView
        Event selectedEvent = table_view.getSelectionModel().getSelectedItem();

        if (selectedEvent != null) {
            // Assigner l'événement sélectionné à la variable statique 'eventToEdit' dans Editevent
            Editevent.eventToEdit = selectedEvent;

            // Charger la fenêtre d'édition
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/editevent.fxml"));
                Parent root = loader.load();

                // Créer une nouvelle scène pour la fenêtre d'édition
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("File Load Error");
                alert.setHeaderText("Error loading Edit Event window.");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Selection Error");
            alert.setHeaderText("No event selected for editing.");
            alert.setContentText("Please select an event to edit.");
            alert.showAndWait();
        }
    }

    @FXML
    public void booknow(MouseEvent mouseEvent) {
        Event selectedEvent = table_view.getSelectionModel().getSelectedItem();

        if (selectedEvent != null) {
            try {
                // Charger le fichier FXML
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/addreservation.fxml"));
                Parent root = loader.load();

                // Récupérer le contrôleur du fichier FXML
                Addreservation controller = loader.getController();

                // Passer l'événement sélectionné au contrôleur
                controller.setSelectedEvent(selectedEvent);

                // Afficher la nouvelle scène
                table_view.getScene().setRoot(root);

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucun événement sélectionné");
            alert.setContentText("Veuillez sélectionner un événement à réserver.");
            alert.showAndWait();
        }
    }
    public void show_list(MouseEvent event) {
    }
    @FXML
    void Logout(ActionEvent event) {
        try {
            URL fxmlLocation = getClass().getResource("/profile.fxml");
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
    void back(MouseEvent event) {
        Scene previousScene = NavigationManager.pop();
        if (previousScene != null) {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(previousScene);
            stage.show();
        } else {
            System.out.println("🚫 Pas de page précédente.");
        }
    }

    private void filterEvents(String searchText) {
        ObservableList<Event> filteredEvents = FXCollections.observableArrayList();
        if (searchText == null || searchText.trim().isEmpty()) {
            filteredEvents.addAll(allEvents);
        } else {
            String lowerCaseFilter = searchText.toLowerCase();
            for (Event event : allEvents) {
                boolean matches = false;
                if (event.getTitle() != null && event.getTitle().toLowerCase().contains(lowerCaseFilter)) {
                    matches = true;
                }
                if (matches) {
                    filteredEvents.add(event);
                }
            }
        }
        table_view.setItems(filteredEvents);
    }
}