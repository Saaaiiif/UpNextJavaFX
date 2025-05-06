package edu.up_next;

import edu.up_next.entities.Quiz;
import edu.up_next.entities.Question;
import edu.up_next.tools.MyConnexion;
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
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class QuizController implements Initializable {

    @FXML
    private TableView<Quiz> quizTable;
    @FXML
    private TableColumn<Quiz, String> titreCol;
    @FXML
    private TableColumn<Quiz, String> sujetCol;
    @FXML
    private TextField titreField;
    @FXML
    private TextField sujetField;
    @FXML
    private Button addButton;
    @FXML
    private Button updateButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button backButton;

    private ObservableList<Quiz> quizzes;
    private Connection connection;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        connection = MyConnexion.getInstance().getConnection();
        quizzes = FXCollections.observableArrayList();

        // Configure table columns
        titreCol.setCellValueFactory(new PropertyValueFactory<>("titre"));
        sujetCol.setCellValueFactory(new PropertyValueFactory<>("sujet"));

        // Load quizzes
        loadQuizzes();

        // Add selection listener
        quizTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                titreField.setText(newSelection.getTitre());
                sujetField.setText(newSelection.getSujet());
            }
        });
    }

    private void loadQuizzes() {
        String query = "SELECT * FROM quiz";
        try (PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            quizzes.clear();
            while (rs.next()) {
                Quiz quiz = new Quiz(
                    rs.getString("titre"),
                    rs.getString("sujet")
                );
                quiz.setId(rs.getInt("id"));
                quizzes.add(quiz);
            }

            quizTable.setItems(quizzes);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load quizzes: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddQuiz(ActionEvent event) {
        String titre = titreField.getText().trim();
        String sujet = sujetField.getText().trim();

        if (titre.isEmpty() || sujet.isEmpty()) {
            showAlert("Error", "Please fill in all fields");
            return;
        }

        String query = "INSERT INTO quiz (titre, sujet) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, titre);
            ps.setString(2, sujet);
            ps.executeUpdate();

            loadQuizzes();
            clearFields();
        } catch (SQLException e) {
            showAlert("Error", "Failed to add quiz: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateQuiz(ActionEvent event) {
        Quiz selectedQuiz = quizTable.getSelectionModel().getSelectedItem();
        if (selectedQuiz == null) {
            showAlert("Error", "Please select a quiz to update");
            return;
        }

        String titre = titreField.getText().trim();
        String sujet = sujetField.getText().trim();

        if (titre.isEmpty() || sujet.isEmpty()) {
            showAlert("Error", "Please fill in all fields");
            return;
        }

        String query = "UPDATE quiz SET titre = ?, sujet = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, titre);
            ps.setString(2, sujet);
            ps.setInt(3, selectedQuiz.getId());
            ps.executeUpdate();

            loadQuizzes();
            clearFields();
        } catch (SQLException e) {
            showAlert("Error", "Failed to update quiz: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteQuiz(ActionEvent event) {
        Quiz selectedQuiz = quizTable.getSelectionModel().getSelectedItem();
        if (selectedQuiz == null) {
            showAlert("Error", "Please select a quiz to delete");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete this quiz?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            String query = "DELETE FROM quiz WHERE id = ?";
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setInt(1, selectedQuiz.getId());
                ps.executeUpdate();

                loadQuizzes();
                clearFields();
            } catch (SQLException e) {
                showAlert("Error", "Failed to delete quiz: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/home.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to return to home page: " + e.getMessage());
        }
    }

    @FXML
    public void GoToQuizManagement(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/quiz.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to load quiz management page: " + e.getMessage());
        }
    }

    private void clearFields() {
        titreField.clear();
        sujetField.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Navigation methods for sidebar
    @FXML
    public void goToHome(ActionEvent event) {
        navigateTo("/home.fxml", event);
    }

    @FXML
    public void goToProfile(ActionEvent event) {
        navigateTo("/profile.fxml", event);
    }

    @FXML
    public void GoToProduct(ActionEvent event) {
        navigateTo("/UserView.fxml", event);
    }

    @FXML
    public void GoToEvent(ActionEvent event) {
        navigateTo("/eventlist.fxml", event);
    }

    @FXML
    public void GoToCommunities(ActionEvent event) {
        try {
            String contentFxmlPath;
            Class<?> controllerClass;

            // Get the current user from SessionManager
            edu.up_next.entities.User currentUser = controllers.communities.SessionManager.getInstance().getCurrentUser();

            // Set the session type in SessionManager based on the user's role
            String roles = currentUser != null ? currentUser.getRoles() : null;
            if (roles != null && roles.contains("ROLE_ARTIST")) {
                // For artists, load the user communities view
                contentFxmlPath = "/views/communities/user-communities-view.fxml";
                controllerClass = controllers.communities.UserCommunitiesController.class;
                controllers.communities.SessionManager.getInstance().setSessionType(controllers.communities.SessionType.ARTIST);
            } else if (roles != null && roles.contains("ROLE_ADMIN")) {
                // For admins, load the main communities view (with admin capabilities)
                contentFxmlPath = "/views/communities/communities-view.fxml";
                controllerClass = controllers.communities.CommunitiesController.class;
                controllers.communities.SessionManager.getInstance().setSessionType(controllers.communities.SessionType.ADMIN);
            } else {
                // For regular users, load the user communities view
                contentFxmlPath = "/views/communities/user-communities-view.fxml";
                controllerClass = controllers.communities.UserCommunitiesController.class;
                controllers.communities.SessionManager.getInstance().setSessionType(controllers.communities.SessionType.USER);
            }

            // First, load the root layout
            FXMLLoader rootLoader = new FXMLLoader(getClass().getResource("/views/communities/root-layout.fxml"));
            Parent rootView = rootLoader.load();

            // Get the root layout controller
            controllers.communities.RootLayoutController rootController = rootLoader.getController();

            // Set the stage on the root controller
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            rootController.setStage(stage);

            // Set the root controller in SceneTransitionUtil
            controllers.communities.SceneTransitionUtil.setRootController(rootController);

            // Load the content into the root layout's content area
            rootController.loadContent(contentFxmlPath, controllerClass);

            // Set the scene with the root layout
            Scene scene = new Scene(rootView);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load communities page: " + e.getMessage());
        }
    }

    @FXML
    public void GoToVerifiedArtist(ActionEvent event) {
        navigateTo("/verifiedArtist.fxml", event);
    }

    @FXML
    public void GoToQuiz(ActionEvent event) {
        navigateTo("/quiz.fxml", event);
    }

    @FXML
    public void GoToReclamationClient(ActionEvent event) {
        navigateTo("/GestionRec.fxml", event);
    }

    @FXML
    public void Logout(ActionEvent event) {
        navigateTo("/login.fxml", event);
    }

    private void navigateTo(String fxmlPath, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to navigate to " + fxmlPath + ": " + e.getMessage());
        }
    }
}
