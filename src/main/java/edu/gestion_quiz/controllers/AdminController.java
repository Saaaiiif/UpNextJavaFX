package edu.gestion_quiz.controllers;

import edu.gestion_quiz.dao.QuestionDAO;
import edu.gestion_quiz.dao.QuizDAO;
import edu.gestion_quiz.dao.impl.QuestionDAOImpl;
import edu.gestion_quiz.dao.impl.QuizDAOImpl;
import edu.gestion_quiz.entities.Question;
import edu.gestion_quiz.entities.Quiz;
import edu.gestion_quiz.tools.PDFGenerator;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.List;

public class AdminController extends MainController {
    private final QuizDAO quizDAO = new QuizDAOImpl();
    private final QuestionDAO questionDAO = new QuestionDAOImpl();
    private final PDFGenerator pdfGenerator = new PDFGenerator(questionDAO);

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
} 