package controllers;

import edu.up_next.dao.QuestionDAO;
import edu.up_next.dao.QuizDAO;
import edu.up_next.dao.impl.QuestionDAOImpl;
import edu.up_next.dao.impl.QuizDAOImpl;
import edu.up_next.entities.Question;
import edu.up_next.entities.Quiz;
import edu.up_next.entities.User;
import edu.up_next.tools.TwilioSMS;
import edu.up_next.tools.TTSMp3;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;
import java.io.File;
import edu.up_next.entities.Event;
import controllers.event_reser_ouma.Eventlist;
import controllers.communities.ArtistsController;
import controllers.communities.CommunitiesController;
import controllers.communities.RootLayoutController;
import controllers.communities.SceneTransitionUtil;
import controllers.communities.SessionManager;
import controllers.communities.SessionType;
import controllers.communities.UserCommunitiesController;
import java.sql.SQLException;
import javafx.application.Platform;
import javafx.scene.text.Text;
public class UserController implements Initializable {
    @FXML private TableView<Quiz> quizToAnswerTable;
    @FXML private TableColumn<Quiz, String> quizToAnswerTitreCol;
    @FXML private TableColumn<Quiz, String> quizToAnswerSujetCol;
    @FXML private TableColumn<Quiz, Void> quizToAnswerActionsCol;
    @FXML private Text authentifiedFirstname;
    @FXML private Text AuthenticatedUser;

    private final QuizDAO quizDAO = new QuizDAOImpl();
    private final QuestionDAO questionDAO = new QuestionDAOImpl();
    private final ObservableList<Quiz> quizzesToAnswer = FXCollections.observableArrayList();

    // Stocker l'id du dernier quiz répondu
    private int lastQuizIdAnswered = -1;

    private User currentUser;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupQuizToAnswerTable();
    }

    private void setupQuizToAnswerTable() {
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

    private boolean hasAlreadyAnsweredQuiz(int userId, int quizId) {
        String sql = "SELECT COUNT(*) FROM reponse WHERE user_id = ? AND question_id IN (SELECT id FROM question WHERE quiz_id = ?)";
        try (java.sql.Connection conn = java.sql.DriverManager.getConnection("jdbc:mysql://localhost:3306/quiz", "root", "");
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, quizId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void openQuizAnswerDialog(Quiz quiz) {
        if (currentUser != null && hasAlreadyAnsweredQuiz(currentUser.getId(), quiz.getId())) {
            showAlert(Alert.AlertType.WARNING, "Déjà répondu", "Vous avez déjà répondu à ce quiz.");
            return;
        }
        lastQuizIdAnswered = quiz.getId();
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

        // TIMER VISIBLE
        Label timerLabel = new Label("Temps écoulé : 0s");
        vbox.getChildren().add(timerLabel);
        int[] elapsedSeconds = {0};
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            elapsedSeconds[0]++;
            timerLabel.setText("Temps écoulé : " + elapsedSeconds[0] + "s");
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        TextField[] reponses = new TextField[questions.size()];
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            final int questionIndex = i;
            Label label = new Label((questionIndex + 1) + ". " + q.getTexteQuestion());
            TextField tf = new TextField();
            tf.setPromptText("Votre réponse");
            reponses[i] = tf;

            Button ttsButton = new Button("Écouter");
            ttsButton.setOnAction(ev -> {
                ttsButton.setDisable(true);
                new Thread(() -> {
                    try {
                        File mp3 = TTSMp3.getSpeechMp3(q.getTexteQuestion(), "Lea");
                        if (mp3 == null || !mp3.exists()) throw new Exception("MP3 non généré");
                        Media media = new Media(mp3.toURI().toString());
                        MediaPlayer player = new MediaPlayer(media);
                        player.setOnEndOfMedia(() -> {
                            ttsButton.setDisable(false);
                            mp3.delete();
                        });
                        player.setOnError(() -> ttsButton.setDisable(false));
                        player.play();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        javafx.application.Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Erreur TTS", "Impossible de lire la question (quota dépassé ou problème réseau/voix)."));
                        ttsButton.setDisable(false);
                    }
                }).start();
            });

            HBox hbox = new HBox(10, label, ttsButton);
            vbox.getChildren().addAll(hbox, tf);
        }
        dialogPane.setContent(vbox);

        // Générer un passage_id unique pour cette session (UUID)
        String passageId = UUID.randomUUID().toString();

        // TIMER : démarrer le chrono
        long startTime = System.currentTimeMillis();

        Optional<ButtonType> result = dialog.showAndWait();

        // TIMER : arrêter le chrono
        timeline.stop();
        long endTime = System.currentTimeMillis();
        int tempsEnSecondes = (int) ((endTime - startTime) / 1000);

        if (result.isPresent() && result.get() == ButtonType.OK) {
            int correct = 0;
            for (int i = 0; i < questions.size(); i++) {
                Question q = questions.get(i);
                String userRep = reponses[i].getText().trim();
                boolean isCorrect = userRep.equalsIgnoreCase(q.getReponseCorrecte());
                if (isCorrect) correct++;
                enregistrerReponse(q.getId(), userRep, isCorrect, tempsEnSecondes, passageId);
            }
            showQuizResult(correct, questions.size());
        }
    }

    private void enregistrerReponse(int questionId, String reponseUtilisateur, boolean estCorrecte, int temps, String passageId) {
        try (java.sql.Connection conn = java.sql.DriverManager.getConnection("jdbc:mysql://localhost:3306/quiz", "root", "")) {
            String sql = "INSERT INTO reponse (question_id, reponse_utilisateur, correcte, temps, passage_id) VALUES (?, ?, ?, ?, ?)";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, questionId);
                ps.setString(2, reponseUtilisateur);
                ps.setString(3, estCorrecte ? "oui" : "non");
                ps.setInt(4, temps);
                ps.setString(5, passageId);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showQuizResult(int correct, int total) {
        double percent = total == 0 ? 0 : (correct * 100.0 / total);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Résultat du Quiz");
        alert.setHeaderText(null);
        alert.setContentText(String.format("Pourcentage de bonnes réponses : %.0f%%\nScore : %d/%d", percent, correct, total));
        ButtonType top10Button = new ButtonType("Voir le top 10");
        alert.getButtonTypes().add(top10Button);
        alert.showAndWait().ifPresent(type -> {
            if (type == top10Button) {
                showTop10Dialog(lastQuizIdAnswered);
            }
        });
        // Envoi du SMS Twilio
        try {
            String quizTitre = "";
            if (lastQuizIdAnswered != -1) {
                Quiz quiz = quizDAO.getById(lastQuizIdAnswered);
                if (quiz != null) quizTitre = quiz.getTitre();
            }
            String smsBody = String.format("Vous avez participé au quiz: %s. Pourcentage: %.0f%%, Score: %d/%d", quizTitre, percent, correct, total);
            //TwilioSMS.sendSMS("+21699375488", smsBody);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showTop10Dialog(int quizId) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Top 10 des scores pour ce quiz");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.CLOSE);

        TableView<TopScore> table = new TableView<>();
        TableColumn<TopScore, String> passageIdCol = new TableColumn<>("Passage ID");
        passageIdCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().passageId));
        TableColumn<TopScore, Integer> scoreCol = new TableColumn<>("Score");
        scoreCol.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().score).asObject());
        TableColumn<TopScore, Integer> tempsCol = new TableColumn<>("Temps total (s)");
        tempsCol.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().temps).asObject());
        table.getColumns().addAll(passageIdCol, scoreCol, tempsCol);
        table.getItems().addAll(getTop10Scores(quizId));
        dialogPane.setContent(table);
        dialog.showAndWait();
    }

    private static class TopScore {
        String passageId;
        int score;
        int temps;
        TopScore(String passageId, int score, int temps) {
            this.passageId = passageId;
            this.score = score;
            this.temps = temps;
        }
    }

    private java.util.List<TopScore> getTop10Scores(int quizId) {
        java.util.List<TopScore> list = new java.util.ArrayList<>();
        String sql = "SELECT passage_id, SUM(correcte = 'oui') AS score, SUM(temps) AS total_temps " +
                "FROM reponse WHERE question_id IN (SELECT id FROM question WHERE quiz_id = ?) " +
                "GROUP BY passage_id ORDER BY score DESC, total_temps ASC LIMIT 10";
        try (java.sql.Connection conn = java.sql.DriverManager.getConnection("jdbc:mysql://localhost:3306/quiz", "root", "");
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quizId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new TopScore(
                        rs.getString("passage_id"),
                        rs.getInt("score"),
                        rs.getInt("total_temps")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public void setUser(User user) {
        this.currentUser = user;
        // Met à jour l'affichage du prénom si le label existe
        if (authentifiedFirstname != null && user != null) {
            authentifiedFirstname.setText("Hello! " + user.getFirstname());
        }
        if (AuthenticatedUser != null && user != null) {
            AuthenticatedUser.setText(user.getFirstname() + " " + user.getLastname());
        }
    }

    @FXML
    private void handleRetourHome(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/home.fxml"));
            Parent root = loader.load();
            // Passe l'utilisateur courant au contrôleur home
            controllers.home homeController = loader.getController();
            homeController.setUser(currentUser);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    void goToHome(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/home.fxml"));
            Parent root = loader.load();
            controllers.home homeController = loader.getController();
            homeController.setUser(currentUser);
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goToProfile(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile.fxml"));
            Parent root = loader.load();
            controllers.profile profileController = loader.getController();
            profileController.setUser(currentUser);
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void GoToProduct(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/product.fxml"));
            Parent root = loader.load();
            // Ajoute setUser si besoin sur le contrôleur cible
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void GoToEvent(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/eventlist.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void GoToVerifiedArtist(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/verifiedArtist.fxml"));
            Parent root = loader.load();
            // Ajoute setUser si besoin sur le contrôleur cible
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void GoToQuiz(ActionEvent event) {
        // Tu es déjà sur la page Quiz, donc tu peux laisser vide ou afficher un message
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

    @FXML
    void GoToReclamationClient(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/reclamation.fxml"));
            Parent root = loader.load();
            // Ajoute setUser si besoin sur le contrôleur cible
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void Logout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 
