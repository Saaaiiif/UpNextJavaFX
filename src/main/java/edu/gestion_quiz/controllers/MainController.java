package edu.gestion_quiz.controllers;
import edu.gestion_quiz.dao.QuestionDAO;
import edu.gestion_quiz.dao.QuizDAO;
import edu.gestion_quiz.dao.impl.QuestionDAOImpl;
import edu.gestion_quiz.dao.impl.QuizDAOImpl;
import edu.gestion_quiz.entities.Question;
import edu.gestion_quiz.entities.Quiz;
import edu.gestion_quiz.tools.PDFGenerator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public class MainController implements Initializable {

    @FXML private TextField quizTitre;
    @FXML private TextField quizSujet;
    @FXML private TextField searchQuizField;
    @FXML private TableView<Quiz> quizTable;
    @FXML private TableColumn<Quiz, Integer> quizIdCol;
    @FXML private TableColumn<Quiz, String> quizTitreCol;
    @FXML private TableColumn<Quiz, String> quizSujetCol;
    @FXML private TableColumn<Quiz, Void> quizActionsCol;

    @FXML private ComboBox<Quiz> quizComboBox;
    @FXML private TextField questionTexte;
    @FXML private TextField option1;
    @FXML private TextField option2;
    @FXML private TextField option3;
    @FXML private TextField option4;
    @FXML private TextField reponseCorrecte;
    @FXML private TextField searchQuestionField;
    @FXML private TableView<Question> questionTable;
    @FXML private TableColumn<Question, Integer> questionIdCol;
    @FXML private TableColumn<Question, String> questionTexteCol;
    @FXML private TableColumn<Question, String> questionOptionsCol;
    @FXML private TableColumn<Question, String> questionReponseCol;
    @FXML private TableColumn<Question, Void> questionActionsCol;

    @FXML private Button printButton;

    @FXML private TableView<Quiz> quizToAnswerTable;
    @FXML private TableColumn<Quiz, String> quizToAnswerTitreCol;
    @FXML private TableColumn<Quiz, String> quizToAnswerSujetCol;
    @FXML private TableColumn<Quiz, Void> quizToAnswerActionsCol;

    private final QuizDAO quizDAO = new QuizDAOImpl();
    private final QuestionDAO questionDAO = new QuestionDAOImpl();
    private final PDFGenerator pdfGenerator = new PDFGenerator(questionDAO);
    private final ObservableList<Quiz> quizzes = FXCollections.observableArrayList();
    private final ObservableList<Question> questions = FXCollections.observableArrayList();
    private FilteredList<Quiz> filteredQuizzes;
    private FilteredList<Question> filteredQuestions;
    private final ObservableList<Quiz> quizzesToAnswer = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupQuizTable();
        setupQuestionTable();
        setupQuizComboBox();
        setupSearchFields();
        refreshQuizzes();
        setupQuizToAnswerTable();
    }

    private void setupQuizTable() {
        quizIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        quizTitreCol.setCellValueFactory(new PropertyValueFactory<>("titre"));
        quizSujetCol.setCellValueFactory(new PropertyValueFactory<>("sujet"));

        quizActionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");
            private final Button printButton = new Button("Imprimer");
            private final HBox buttons = new HBox(5, editButton, deleteButton, printButton);

            {
                editButton.setOnAction(event -> {
                    Quiz quiz = getTableView().getItems().get(getIndex());
                    showEditQuizDialog(quiz);
                });

                deleteButton.setOnAction(event -> {
                    Quiz quiz = getTableView().getItems().get(getIndex());
                    if (confirmDelete("quiz")) {
                        quizDAO.supprimer(quiz.getId());
                        refreshQuizzes();
                    }
                });

                printButton.setOnAction(event -> {
                    Quiz quiz = getTableView().getItems().get(getIndex());
                    generateQuizPDF(quiz);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });

        quizTable.setItems(quizzes);
    }

    private void setupQuestionTable() {
        questionIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        questionTexteCol.setCellValueFactory(new PropertyValueFactory<>("texteQuestion"));
        questionOptionsCol.setCellValueFactory(cellData -> {
            Question q = cellData.getValue();
            String options = String.format("1.%s 2.%s 3.%s 4.%s",
                    q.getOption1(), q.getOption2(), q.getOption3(), q.getOption4());
            return javafx.beans.binding.Bindings.createStringBinding(() -> options);
        });
        questionReponseCol.setCellValueFactory(new PropertyValueFactory<>("reponseCorrecte"));

        questionActionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");
            private final HBox buttons = new HBox(5, editButton, deleteButton);

            {
                editButton.setOnAction(event -> {
                    Question question = getTableView().getItems().get(getIndex());
                    showEditQuestionDialog(question);
                });

                deleteButton.setOnAction(event -> {
                    Question question = getTableView().getItems().get(getIndex());
                    if (confirmDelete("question")) {
                        questionDAO.supprimer(question.getId());
                        refreshQuestions();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });

        questionTable.setItems(questions);
    }

    private void setupQuizComboBox() {
        quizComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Quiz quiz) {
                return quiz != null ? quiz.getTitre() : "";
            }

            @Override
            public Quiz fromString(String string) {
                return null;
            }
        });

        quizComboBox.setItems(quizzes);
        quizComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                refreshQuestions();
            }
        });
    }

    private void setupSearchFields() {
        // Configuration de la recherche pour les quiz
        filteredQuizzes = new FilteredList<>(quizzes, p -> true);
        searchQuizField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredQuizzes.setPredicate(quiz -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return quiz.getTitre().toLowerCase().contains(lowerCaseFilter) ||
                       quiz.getSujet().toLowerCase().contains(lowerCaseFilter);
            });
        });

        SortedList<Quiz> sortedQuizzes = new SortedList<>(filteredQuizzes);
        sortedQuizzes.comparatorProperty().bind(quizTable.comparatorProperty());
        quizTable.setItems(sortedQuizzes);

        // Configuration de la recherche pour les questions
        filteredQuestions = new FilteredList<>(questions, p -> true);
        searchQuestionField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredQuestions.setPredicate(question -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return question.getTexteQuestion().toLowerCase().contains(lowerCaseFilter) ||
                       question.getOption1().toLowerCase().contains(lowerCaseFilter) ||
                       question.getOption2().toLowerCase().contains(lowerCaseFilter) ||
                       question.getOption3().toLowerCase().contains(lowerCaseFilter) ||
                       question.getOption4().toLowerCase().contains(lowerCaseFilter) ||
                       question.getReponseCorrecte().toLowerCase().contains(lowerCaseFilter);
            });
        });

        SortedList<Question> sortedQuestions = new SortedList<>(filteredQuestions);
        sortedQuestions.comparatorProperty().bind(questionTable.comparatorProperty());
        questionTable.setItems(sortedQuestions);
    }

    @FXML
    private void ajouterQuiz() {
        String titre = quizTitre.getText().trim();
        String sujet = quizSujet.getText().trim();

        if (titre.isEmpty() || sujet.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Tous les champs sont obligatoires");
            return;
        }

        Quiz quiz = new Quiz(titre, sujet);
        quizDAO.ajouter(quiz);
        refreshQuizzes();
        clearQuizFields();
    }

    @FXML
    private void ajouterQuestion() {
        Quiz selectedQuiz = quizComboBox.getValue();
        if (selectedQuiz == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner un quiz");
            return;
        }

        if (questionTexte.getText().trim().isEmpty() || option1.getText().trim().isEmpty() ||
                option2.getText().trim().isEmpty() || option3.getText().trim().isEmpty() ||
                option4.getText().trim().isEmpty() || reponseCorrecte.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Tous les champs sont obligatoires");
            return;
        }

        Question question = new Question(
                questionTexte.getText().trim(),
                option1.getText().trim(),
                option2.getText().trim(),
                option3.getText().trim(),
                option4.getText().trim(),
                reponseCorrecte.getText().trim(),
                selectedQuiz
        );

        questionDAO.ajouter(question);
        refreshQuestions();
        clearQuestionFields();
    }

    private void showEditQuizDialog(Quiz quiz) {
        Dialog<Quiz> dialog = new Dialog<>();
        dialog.setTitle("Modifier Quiz");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField titreField = new TextField(quiz.getTitre());
        TextField sujetField = new TextField(quiz.getSujet());

        dialogPane.setContent(new VBox(10,
                new Label("Titre:"), titreField,
                new Label("Sujet:"), sujetField
        ));

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                quiz.setTitre(titreField.getText().trim());
                quiz.setSujet(sujetField.getText().trim());
                return quiz;
            }
            return null;
        });

        Optional<Quiz> result = dialog.showAndWait();
        result.ifPresent(updatedQuiz -> {
            quizDAO.modifier(updatedQuiz);
            refreshQuizzes();
        });
    }

    private void showEditQuestionDialog(Question question) {
        Dialog<Question> dialog = new Dialog<>();
        dialog.setTitle("Modifier Question");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField texteField = new TextField(question.getTexteQuestion());
        TextField option1Field = new TextField(question.getOption1());
        TextField option2Field = new TextField(question.getOption2());
        TextField option3Field = new TextField(question.getOption3());
        TextField option4Field = new TextField(question.getOption4());
        TextField reponseField = new TextField(question.getReponseCorrecte());

        dialogPane.setContent(new VBox(10,
                new Label("Question:"), texteField,
                new Label("Option 1:"), option1Field,
                new Label("Option 2:"), option2Field,
                new Label("Option 3:"), option3Field,
                new Label("Option 4:"), option4Field,
                new Label("Réponse correcte:"), reponseField
        ));

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                question.setTexteQuestion(texteField.getText().trim());
                question.setOption1(option1Field.getText().trim());
                question.setOption2(option2Field.getText().trim());
                question.setOption3(option3Field.getText().trim());
                question.setOption4(option4Field.getText().trim());
                question.setReponseCorrecte(reponseField.getText().trim());
                return question;
            }
            return null;
        });

        Optional<Question> result = dialog.showAndWait();
        result.ifPresent(updatedQuestion -> {
            questionDAO.modifier(updatedQuestion);
            refreshQuestions();
        });
    }

    private void refreshQuizzes() {
        quizzes.setAll(quizDAO.getAll());
    }

    private void refreshQuestions() {
        Quiz selectedQuiz = quizComboBox.getValue();
        if (selectedQuiz != null) {
            questions.setAll(questionDAO.getByQuizId(selectedQuiz.getId()));
        } else {
            questions.clear();
        }
    }

    private void clearQuizFields() {
        quizTitre.clear();
        quizSujet.clear();
    }

    private void clearQuestionFields() {
        questionTexte.clear();
        option1.clear();
        option2.clear();
        option3.clear();
        option4.clear();
        reponseCorrecte.clear();
    }

    private boolean confirmDelete(String type) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer " + type);
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce " + type + " ?");

        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    protected void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void generateQuizPDF(Quiz quiz) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le PDF");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );
        fileChooser.setInitialFileName(quiz.getTitre() + ".pdf");

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                List<Quiz> quizList = Collections.singletonList(quiz);
                pdfGenerator.generateAllQuizzesPDF(quizList, file.getAbsolutePath());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Le PDF a été généré avec succès");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la génération du PDF: " + e.getMessage());
            }
        }
    }

    private void setupQuizToAnswerTable() {
        if (quizToAnswerTable == null) return;
        quizToAnswerTitreCol.setCellValueFactory(new PropertyValueFactory<>("titre"));
        quizToAnswerSujetCol.setCellValueFactory(new PropertyValueFactory<>("sujet"));
        quizToAnswerActionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button answerButton = new Button("Répondre");
            {
                answerButton.setOnAction(event -> {
                    Quiz quiz = getTableView().getItems().get(getIndex());
                    openQuizAnswerDialog(quiz);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : answerButton);
            }
        });
        quizToAnswerTable.setItems(quizzesToAnswer);
        refreshQuizzesToAnswer();
    }

    private void refreshQuizzesToAnswer() {
        quizzesToAnswer.setAll(quizDAO.getAll());
    }

    private void openQuizAnswerDialog(Quiz quiz) {
        List<Question> questions = questionDAO.getByQuizId(quiz.getId());
        if (questions.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Aucune question", "Ce quiz ne contient aucune question.");
            return;
        }
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Répondre au quiz : " + quiz.getTitre());
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        VBox vbox = new VBox(10);
        TextField[] reponses = new TextField[questions.size()];
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            Label label = new Label((i+1) + ". " + q.getTexteQuestion());
            TextField tf = new TextField();
            tf.setPromptText("Votre réponse");
            reponses[i] = tf;
            vbox.getChildren().addAll(label, tf);
        }
        dialogPane.setContent(vbox);
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            int correct = 0;
            for (int i = 0; i < questions.size(); i++) {
                Question q = questions.get(i);
                String userRep = reponses[i].getText().trim();
                boolean isCorrect = userRep.equalsIgnoreCase(q.getReponseCorrecte());
                if (isCorrect) correct++;
                enregistrerReponse(q.getId(), userRep, isCorrect);
            }
            showQuizResult(correct, questions.size());
        }
    }

    private void enregistrerReponse(int questionId, String reponseUtilisateur, boolean estCorrecte) {
        // À adapter selon ton DAO ou JDBC
        // Ici, exemple simple avec JDBC
        try (java.sql.Connection conn = java.sql.DriverManager.getConnection("jdbc:mysql://localhost:3306/quiz", "root", "")) {
            String sql = "INSERT INTO reponse (question_id, reponse_utilisateur, correcte) VALUES (?, ?, ?)";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, questionId);
                ps.setString(2, reponseUtilisateur);
                ps.setString(3, estCorrecte ? "oui" : "non");
                ps.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showQuizResult(int correct, int total) {
        double percent = total == 0 ? 0 : (correct * 100.0 / total);
        String message = String.format("Pourcentage de bonnes réponses : %.0f%%\nScore : %d/%d", percent, correct, total);
        showAlert(Alert.AlertType.INFORMATION, "Résultat du Quiz", message);
    }
}