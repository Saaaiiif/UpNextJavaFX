package controllers;

import edu.up_next.dao.QuestionDAO;
import edu.up_next.dao.QuizDAO;
import edu.up_next.dao.impl.QuestionDAOImpl;
import edu.up_next.dao.impl.QuizDAOImpl;
import edu.up_next.entities.Question;
import edu.up_next.entities.Quiz;
import edu.up_next.entities.User;
import edu.up_next.tools.PDFGenerator;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.List;
import javafx.scene.text.Text;

public class AdminController extends MainController {
    private final QuizDAO quizDAO = new QuizDAOImpl();
    private final QuestionDAO questionDAO = new QuestionDAOImpl();
    private final PDFGenerator pdfGenerator = new PDFGenerator(questionDAO);
    private User currentUser;

    @FXML
    private TextField quizTitre;
    @FXML
    private TextField quizSujet;
    @FXML
    private TextField searchQuizField;
    @FXML
    private TableView<Quiz> quizTable;
    @FXML
    private TableColumn<Quiz, String> quizIdCol;
    @FXML
    private TableColumn<Quiz, String> quizTitreCol;
    @FXML
    private TableColumn<Quiz, String> quizSujetCol;
    @FXML
    private TableColumn<Quiz, Void> quizActionsCol;

    @FXML
    private ComboBox<Quiz> quizComboBox;
    @FXML
    private TextField questionTexte;
    @FXML
    private TextField option1;
    @FXML
    private TextField option2;
    @FXML
    private TextField option3;
    @FXML
    private TextField option4;
    @FXML
    private TextField reponseCorrecte;
    @FXML
    private TextField searchQuestionField;
    @FXML
    private TableView<Question> questionTable;
    @FXML
    private TableColumn<Question, String> questionIdCol;
    @FXML
    private TableColumn<Question, String> questionTexteCol;
    @FXML
    private TableColumn<Question, String> questionOptionsCol;
    @FXML
    private TableColumn<Question, String> questionReponseCol;
    @FXML
    private TableColumn<Question, Void> questionActionsCol;

    @FXML
    private Text authentifiedFirstname;
    @FXML
    private Text AuthenticatedUser;

    public void setUser(User user) {
        this.currentUser = user;
        if (authentifiedFirstname != null && user != null) {
            authentifiedFirstname.setText("Hello! " + user.getFirstname());
        }
        if (AuthenticatedUser != null && user != null) {
            AuthenticatedUser.setText(user.getFirstname() + " " + user.getLastname());
        }
    }

    @FXML
    private void imprimerTousLesQuiz() {
        List<Quiz> tousLesQuiz = quizDAO.getAll();
        if (tousLesQuiz.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Information", "Aucun quiz à imprimer");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le PDF");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );
        fileChooser.setInitialFileName("tous_les_quiz.pdf");

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                pdfGenerator.generateAllQuizzesPDF(tousLesQuiz, file.getAbsolutePath());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Le PDF a été généré avec succès");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la génération du PDF: " + e.getMessage());
            }
        }
    }

    @FXML
    private void goToHome(javafx.event.ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/home.fxml"));
            javafx.scene.Parent root = loader.load();
            controllers.home homeController = loader.getController();
            homeController.setUser(currentUser);
            javafx.stage.Stage stage = (javafx.stage.Stage)((javafx.scene.Node)event.getSource()).getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }
    @FXML
    private void goToProfile(javafx.event.ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/profile.fxml"));
            javafx.scene.Parent root = loader.load();
            controllers.profile profileController = loader.getController();
            profileController.setUser(currentUser);
            javafx.stage.Stage stage = (javafx.stage.Stage)((javafx.scene.Node)event.getSource()).getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }
    @FXML
    private void GoToProduct(javafx.event.ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/product.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage)((javafx.scene.Node)event.getSource()).getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }
    @FXML
    private void GoToEvent(javafx.event.ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/eventlist.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage)((javafx.scene.Node)event.getSource()).getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }
    @FXML
    private void GoToVerifiedArtist(javafx.event.ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/verifiedArtist.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage)((javafx.scene.Node)event.getSource()).getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }
    @FXML
    private void GoToQuiz(javafx.event.ActionEvent event) {
        // Déjà sur la page Quiz
    }
    @FXML
    private void Logout(javafx.event.ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/login.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage)((javafx.scene.Node)event.getSource()).getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }
} 