package controllers;

import edu.up_next.entities.Event;
import edu.up_next.services.EventServices;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class Eventlist {

    @FXML
    private TableView<Event> table_view;

    @FXML
    private TableColumn<Event, String> col_title;

    @FXML
    private TableColumn<Event, String> col_status;

    @FXML
    private TableColumn<Event, String> col_desc;


    private final EventServices es = new EventServices();

    @FXML
    public void initialize() {
        try {
            List<Event> events = es.getAllData();
            ObservableList<Event> data = FXCollections.observableArrayList(events);
            table_view.setItems(data);

            table_view.getSelectionModel().setCellSelectionEnabled(true);

            col_title.setCellValueFactory(new PropertyValueFactory<>("title"));
            col_status.setCellValueFactory(new PropertyValueFactory<>("status_event"));
            col_desc.setCellValueFactory(new PropertyValueFactory<>("descrip"));


        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Error while fetching events from database.");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
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
    void add_event(MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Addevent.fxml"));
            table_view.getScene().setRoot(root);
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("File Load Error");
            alert.setHeaderText("Error loading Add Event window.");
            alert.setContentText(e.getMessage());
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


    public void back_list(MouseEvent mouseEvent) {
    }
}


