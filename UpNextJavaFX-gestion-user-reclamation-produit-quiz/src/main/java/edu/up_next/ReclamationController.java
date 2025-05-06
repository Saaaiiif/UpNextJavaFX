package edu.up_next;

import controllers.ChatbotController;
import edu.up_next.entities.Reclamation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.up_next.entities.User;
import edu.up_next.tools.MyConnexion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import edu.up_next.tools.EmailSender;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static controllers.ChatbotController.API_KEY;
import static controllers.ChatbotController.API_URL;

public class ReclamationController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnModifier;

    @FXML
    private Button btnSupp;

    @FXML
    private Button btn_ajouter;

    @FXML
    private Button btnTrie;

    @FXML
    private Label errorTitre;
    @FXML
    private Label errorEmail;
    @FXML
    private Label errorType;
    @FXML
    private Label errorDescription;




    @FXML
    private TextArea txtDescription;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtTitre;

    @FXML
    private ComboBox<String> txtType; // Remplacer le TextField par un ComboBox

    @FXML
    private TextField txtRechercherRec;

    @FXML
    private TableColumn<?, ?> MailCol;

    @FXML
    private TableColumn<?, ?> StatusRec;

    @FXML
    private TableView<Reclamation> TableReclamation;

    @FXML
    private TableColumn<Reclamation, String> TitreCol;

    @FXML
    private TableColumn<Reclamation, String> TypeCol;

    @FXML
    private Button btnImport;

    @FXML
    private Button testEmail;


    private String cheminFichier; // Stocker le chemin du fichier sélectionné


    private ObservableList<Reclamation> reclamations;
    private Connection connection;
    private User  currentUser;


    @FXML
    void EnvoyerEmail(ActionEvent event) {


        String description = txtDescription.getText();
        String msg_trd = traduireFrancaisAnglais(description);

        showAlert("Message Traduit !!",msg_trd);

        txtDescription.setText(msg_trd);


    }

/*
    public void traduireEnAnglais() {
        String texteOriginal = txtDescription.getText();
        try {
            String texteTraduit = traduireTexte(texteOriginal, "fr", "en");
            txtDescription.setText(texteTraduit); // tu peux aussi afficher dans un autre champ
        } catch (IOException e) {
            e.printStackTrace();
            txtDescription.setText("❌ Erreur de traduction.");
        }
    }
*/


    @FXML
    void initialize() {
        assert btnImport != null : "fx:id=\"btnImport\" was not injected: check your FXML file 'GestionRec.fxml'.";

        // Ajouter un gestionnaire d'événements pour le bouton d'importation
        btnImport.setOnAction(this::importerFichier);


        assert MailCol != null : "fx:id=\"MailCol\" was not injected: check your FXML file 'GestionRec.fxml'.";
        assert StatusRec != null : "fx:id=\"StatusRec\" was not injected: check your FXML file 'GestionRec.fxml'.";
        assert TableReclamation != null : "fx:id=\"TableReclamation\" was not injected: check your FXML file 'GestionRec.fxml'.";
        assert TitreCol != null : "fx:id=\"TitreCol\" was not injected: check your FXML file 'GestionRec.fxml'.";
        assert TypeCol != null : "fx:id=\"TypeCol\" was not injected: check your FXML file 'GestionRec.fxml'.";

        assert btnModifier != null : "fx:id=\"btnModifier\" was not injected: check your FXML file 'GestionRec.fxml'.";
        assert btnSupp != null : "fx:id=\"btnSupp\" was not injected: check your FXML file 'GestionRec.fxml'.";
        assert btn_ajouter != null : "fx:id=\"btn_ajouter\" was not injected: check your FXML file 'GestionRec.fxml'.";
        assert txtDescription != null : "fx:id=\"txtDescription\" was not injected: check your FXML file 'GestionRec.fxml'.";
        assert txtEmail != null : "fx:id=\"txtEmail\" was not injected: check your FXML file 'GestionRec.fxml'.";
        assert txtTitre != null : "fx:id=\"txtTitre\" was not injected: check your FXML file 'GestionRec.fxml'.";
        assert txtType != null : "fx:id=\"txtType\" was not injected: check your FXML file 'GestionRec.fxml'.";

        // Connecter à la base de données
        connection = MyConnexion.getInstance().getConnection();

        // Initialiser la liste observable
        reclamations = FXCollections.observableArrayList();
        TableReclamation.setItems(reclamations);

        // Configurer les colonnes
        TitreCol.setCellValueFactory(new PropertyValueFactory<>("titre"));
        MailCol.setCellValueFactory(new PropertyValueFactory<>("mail_client_rec"));
        TypeCol.setCellValueFactory(new PropertyValueFactory<>("type_rec"));
        StatusRec.setCellValueFactory(new PropertyValueFactory<>("status_rec"));

        // Charger les réclamations existantes
       
        //loadReclamationsForUser(currentUser, false);

        System.out.print("currentUser fil reclamation"+currentUser);
        // Remplir le ComboBox avec les options
        txtType.setItems(FXCollections.observableArrayList("Finance", "Autres", "Service"));


        txtRechercherRec.textProperty().addListener((observable, oldValue, newValue) -> {
            searchReclamations(newValue); // Appelle ta méthode à chaque frappe
        });

        // 🔹 Ajouter un gestionnaire d'événement pour remplir le formulaire lors de la sélection d'une ligne
        TableReclamation.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // Remplir le formulaire avec les informations de la ligne sélectionnée
                txtTitre.setText(newValue.getTitre());
                txtEmail.setText(newValue.getMail_client_rec());
                txtType.setValue(newValue.getType_rec()); // Sélectionner le type dans le ComboBox
                txtDescription.setText(newValue.getDescription_rec());
            }
        });
    }

    // Méthode pour importer un fichier
    private void importerFichier(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers Images et PDF", "*.jpg", "*.jpeg", "*.png", "*.pdf"));

        // Ouvrir la fenêtre de sélection de fichier
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            // Si un fichier est sélectionné, obtenir le chemin du fichier
            cheminFichier = file.getAbsolutePath();
            // Afficher le chemin du fichier (pour tests)
            showAlert("Fichier sélectionné", "Fichier importé : " + cheminFichier);
        }
    }

    // 🔹 Charger les réclamations de la base de données
    // 🔹 Charger les réclamations de la base de données
/*
    private void loadReclamations(boolean sortAscending) {
        String query = "SELECT * FROM reclamation";
        if (sortAscending) {
            query += " ORDER BY date_creation_rec ASC";  // Si on veut trier les réclamations par date de création croissante
        }

        try (PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            reclamations.clear(); // Effacer les réclamations actuelles dans la liste

            while (rs.next()) {
                Long id = rs.getLong("id");
                LocalDate dateCreation = rs.getDate("date_creation_rec") != null ? rs.getDate("date_creation_rec").toLocalDate() : null;
                LocalDate dateResolution = rs.getDate("date_resolution_rec") != null ? rs.getDate("date_resolution_rec").toLocalDate() : null;

                Reclamation rec = new Reclamation(
                        id,
                        rs.getString("titre"),
                        rs.getString("mail_client_rec"),
                        rs.getString("type_rec"),
                        rs.getString("description_rec"),
                        rs.getString("status_rec"),
                        dateCreation,
                        dateResolution,
                        rs.getString("fichier_pj_rec")
                );
                reclamations.add(rec); // Ajouter la réclamation à la liste
            }

            TableReclamation.setItems(FXCollections.observableArrayList(reclamations)); // Mettre à jour le TableView

        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des réclamations : " + e.getMessage());
        }
    }*/

    private void loadReclamationsForUser(User user, boolean sortAscending) {
        String query = "SELECT * FROM reclamation WHERE user_id = ?";
        if (sortAscending) {
            query += " ORDER BY date_creation_rec ASC";
        }

        try (PreparedStatement ps = connection.prepareStatement(query)) {
          //  ps.setInt(1, currentUser.getId());
            ps.setInt(1, currentUser.getId());  // ✅ Ici on passe bien l'ID de l'utilisateur

            try (ResultSet rs = ps.executeQuery()) {
                reclamations.clear();

                while (rs.next()) {
                    Long id = rs.getLong("id");
                    LocalDate dateCreation = rs.getDate("date_creation_rec") != null ? rs.getDate("date_creation_rec").toLocalDate() : null;
                    LocalDate dateResolution = rs.getDate("date_resolution_rec") != null ? rs.getDate("date_resolution_rec").toLocalDate() : null;

                    Reclamation rec = new Reclamation(
                            id,
                            rs.getString("titre"),
                            rs.getString("mail_client_rec"),
                            rs.getString("type_rec"),
                            rs.getString("description_rec"),
                            rs.getString("status_rec"),
                            dateCreation,
                            dateResolution,
                            rs.getString("fichier_pj_rec")
                    );

                    // 🔄 Lier l'utilisateur à la réclamation
                    rec.setUser(user);

                    reclamations.add(rec);
                }

                TableReclamation.setItems(FXCollections.observableArrayList(reclamations));
            }

        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des réclamations : " + e.getMessage());
        }
    }



    // 🔹 Ajouter une réclamation
    @FXML
    void AjouterRec(ActionEvent event) {
        String titre = txtTitre.getText();
        String email = txtEmail.getText();
        String type = txtType.getValue();
        String description = txtDescription.getText();

        // Réinitialisation des messages d'erreur
        errorTitre.setVisible(false);
        errorEmail.setVisible(false);
        errorType.setVisible(false);
        errorDescription.setVisible(false);
        //errorFichier.setVisible(false);

        boolean isValid = true;

        if (titre.isEmpty()) {
            errorTitre.setText("Le titre est obligatoire !");
            errorTitre.setVisible(true);
            isValid = false;
        }

        if (email.isEmpty() || !email.contains("@")) {
            errorEmail.setText("Email invalide !");
            errorEmail.setVisible(true);
            isValid = false;
        }

        if (type == null || type.isEmpty()) {
            errorType.setText("Veuillez sélectionner un type !");
            errorType.setVisible(true);
            isValid = false;
        }

        if (description.length() <= 10) {
            errorDescription.setText("Description trop courte !");
            errorDescription.setVisible(true);
            isValid = false;
        }
        if (description.isEmpty() ) {
            errorDescription.setText("Description Obligatoire !");
            errorDescription.setVisible(true);
            isValid = false;
        }

        System.out.println("isValid"+ isValid);

        // Vérification si un fichier a été importé
        /*
        if (cheminFichier == null || cheminFichier.isEmpty()) {
            showAlert("Erreur", "Veuillez importer un fichier !");
            return;
        }*/

        // Vérification si un fichier a été sélectionné
        String cheminFichierFinal = null;
        if (cheminFichier != null && !cheminFichier.isEmpty()) {
            File file = new File(cheminFichier);
            String fileExtension = getFileExtension(file);

            // Vérifier si le fichier a une extension valide
            if (!fileExtension.equals("pdf") && !fileExtension.equals("jpg") && !fileExtension.equals("png")) {
                showAlert("Erreur", "Le fichier importé doit être un PDF ou une image !");
                return;
            }

            // Copier le fichier dans le dossier cible
            Path destinationPath = Path.of("C://uploadsJavaFx", file.getName());
            try {
                Files.copy(file.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
                cheminFichierFinal = destinationPath.toString();  // Mettre à jour le chemin du fichier copié
            } catch (IOException e) {
                showAlert("Erreur", "Impossible de copier le fichier : " + e.getMessage());
                return;
            }
        }

         if (isValid==true){
             // Sauvegarde dans la base de données
          //   String query = "INSERT INTO reclamation (titre, mail_client_rec, type_rec, description_rec, date_creation_rec, status_rec, fichier_pj_rec) VALUES (?, ?, ?, ?, NOW(), 'Ouvert', ?)";
             String query = "INSERT INTO reclamation (titre, mail_client_rec, type_rec, description_rec, date_creation_rec, status_rec, fichier_pj_rec, user_id) VALUES (?, ?, ?, ?, NOW(), 'Ouvert', ?, ?)";

             try (PreparedStatement ps = connection.prepareStatement(query)) {
                 ps.setString(1, titre);
                 ps.setString(2, email);
                 ps.setString(3, type);
                 ps.setString(4, description);
                 ps.setString(5, cheminFichier);
                 ps.setInt(6, currentUser.getId()); // 🔗 Associe la réclamation à l'utilisateur connecté
                 ps.executeUpdate();

                 // Recharger les réclamations après ajout
                // loadReclamations(false);
                 loadReclamationsForUser(currentUser, false);

                 // Envoi de l'e-mail après ajout de la réclamation
                 String subject = "Nouvelle réclamation ajoutée";
                 String body = "Bonjour,\n\nVotre réclamation a été ajoutée.\n\n" +
                         "Titre : " + titre + "\n" +
                         "Type : " + type + "\n" +
                         "Description : " + description + "\n\n" +
                         "Nous la traiterons avec attention. " + "\n"  +
                         "Veuillez suivre nos propositions et nous donner votre avis.";

                 // Envoi de l'e-mail à l'utilisateur qui a créé la réclamation
                 //EmailSender.sendEmail("mariem.marzouk272003@gmail.com", subject, body);  // L'e-mail sera envoyé à l'adresse fournie par l'utilisateur
                 EmailSender.sendEmail(email, subject, body);  // L'e-mail sera envoyé à l'adresse fournie par l'utilisateur
                 System.out.println("E-mail envoyé à votre adresse " + email);
                 txtTitre.clear();
                 txtEmail.clear();
                 txtType.setValue(null);
                 txtDescription.clear();


             } catch (SQLException e) {
                 System.out.println("Erreur SQL : " + e.getMessage());
             }


         }else{
             Alert alert = new Alert(Alert.AlertType.WARNING); // Type Warning (Jaune)
             alert.setTitle("Alerte");
             alert.setHeaderText(null);
             alert.setContentText("veuillez verifier vos champs !");

             // Ajouter une icône d'alerte (Facultatif)
             Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
             alert.showAndWait();
         }



    }


    // Fonction pour obtenir l'extension d'un fichier
    private String getFileExtension(File file) {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) {
            return ""; // Pas d'extension
        }
        return fileName.substring(dotIndex + 1).toLowerCase();
    }


    // 🔹 Modifier une réclamation
    @FXML
    void Modifier_Rec(ActionEvent event) {
        Reclamation selectedReclamation = TableReclamation.getSelectionModel().getSelectedItem();
        if (selectedReclamation == null) {
            showAlert("Erreur", "Veuillez sélectionner une réclamation à modifier !");
            return;
        }

        Long id = selectedReclamation.getId(); // Récupération de l'ID de la réclamation sélectionnée
        if (id == null) {
            showAlert("Erreur", "L'ID de la réclamation est introuvable !");
            return;
        }

        String titre = txtTitre.getText();
        String email = txtEmail.getText();
        String type = txtType.getValue();
        String description = txtDescription.getText();

        // Réinitialisation des labels d'erreur
        errorTitre.setVisible(false);
        errorEmail.setVisible(false);
        errorType.setVisible(false);
        errorDescription.setVisible(false);

        boolean hasError = false;

        if (titre.isEmpty()) {
            errorTitre.setText("Le titre est obligatoire !");
            errorTitre.setVisible(true);  // 🔹 Rendre le label visible
            hasError = true;
        }

        if (email.isEmpty()) {
            errorEmail.setText("L'email est obligatoire !");
            errorEmail.setVisible(true);  // 🔹 Rendre le label visible
            hasError = true;
        } else if (!email.contains("@")) {
            errorEmail.setText("Veuillez entrer un email valide !");
            errorEmail.setVisible(true);
            hasError = true;
        }

        if (type == null || type.isEmpty()) {
            errorType.setText("Le type de réclamation est obligatoire !");
            errorType.setVisible(true);  // 🔹 Rendre le label visible
            hasError = true;
        }

        if (description.isEmpty()) {
            errorDescription.setText("La description est obligatoire !");
            errorDescription.setVisible(true);  // 🔹 Rendre le label visible
            hasError = true;
        } else if (description.length() <= 10) {
            errorDescription.setText("La description doit comporter plus de 10 caractères !");
            errorDescription.setVisible(true);
            hasError = true;
        }


        /////////////// muRaçbaaaa
        String rr=getChatbotResponseModifier(description);
        System.out.print(rr);


        // Bloquer si des propos déplacés sont détectés
        if (rr.equalsIgnoreCase("bad words")) {
            showAlert("Message Inacceptable", "Votre description contient des propos déplacés ou inappropriés. Veuillez corriger votre message.");
            return; // On arrête ici
        }


        ////////////////

        // Si une erreur est détectée, on arrête l'exécution
        if (hasError) return;



        // Mise à jour dans la base de données
        String query = "UPDATE reclamation SET titre=?, mail_client_rec=?, type_rec=?, description_rec=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, titre);
            ps.setString(2, email);
            ps.setString(3, type);
            ps.setString(4, description);
            ps.setLong(5, id);

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                showAlert("Succès", "Réclamation mise à jour avec succès !");
                //  loadReclamations(false); // Recharger les réclamations
                loadReclamationsForUser(currentUser, false);
                // Réinitialisation des champs après mise à jour
                txtTitre.clear();
                txtEmail.clear();
                txtType.setValue(null);
                txtDescription.clear();
            } else {
                showAlert("Erreur", "Réclamation non trouvée !");
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Problème lors de la mise à jour : " + e.getMessage());
        }
    }


    @FXML
    void SupprimerReclamation(ActionEvent event) {
        Reclamation selectedReclamation = TableReclamation.getSelectionModel().getSelectedItem();
        if (selectedReclamation == null) {
            showAlert("Erreur", "Veuillez sélectionner une réclamation à supprimer !");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText(null);
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette réclamation ainsi que ses suivis ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Long reclamationId = selectedReclamation.getId();

            try {
                connection.setAutoCommit(false); // 🔄 Démarrer une transaction

                // 🔸 Supprimer les suivis liés à la réclamation
                String deleteSuivisQuery = "DELETE FROM suivi WHERE reclamation_id = ?";
                try (PreparedStatement psSuivis = connection.prepareStatement(deleteSuivisQuery)) {
                    psSuivis.setLong(1, reclamationId);
                    psSuivis.executeUpdate();
                }

                // 🔸 Supprimer la réclamation
                String deleteReclamationQuery = "DELETE FROM reclamation WHERE id = ?";
                try (PreparedStatement psReclamation = connection.prepareStatement(deleteReclamationQuery)) {
                    psReclamation.setLong(1, reclamationId);
                    int rowsDeleted = psReclamation.executeUpdate();

                    if (rowsDeleted > 0) {
                        connection.commit(); // ✅ Confirmer la transaction
                        showAlert("Succès", "Réclamation et ses suivis supprimés avec succès !");
                        loadReclamationsForUser(currentUser, false); // 🔁 Recharger les données liées à l'utilisateur
                        // Optionnel : Vider les champs du formulaire si nécessaire
                        txtTitre.clear();
                        txtEmail.clear();
                        txtType.setValue(null);
                        txtDescription.clear();
                    } else {
                        showAlert("Erreur", "Réclamation non trouvée !");
                        connection.rollback();
                    }
                }
            } catch (SQLException e) {
                showAlert("Erreur", "Erreur lors de la suppression : " + e.getMessage());
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    showAlert("Erreur", "Échec du rollback : " + ex.getMessage());
                }
            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException ex) {
                    showAlert("Erreur", "Impossible de rétablir l'auto-commit : " + ex.getMessage());
                }
            }
        }
    }


    // 🔹 Affichage des messages d'alerte
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String getChatbotResponseModifier(String message) {
        String response = "";

        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost request = new HttpPost(API_URL);

            request.setHeader("Authorization", "Bearer " + API_KEY);
            request.setHeader("Content-Type", "application/json");

            String jsonBody = "{"
                    + "\"model\": \"deepseek/deepseek-r1-distill-llama-70b:free\","
                    + "\"messages\": ["
                    + " {\"role\": \"system\", \"content\": \"Tu es un agent d'assistance chargé de détecter les propos déplacés ou injurieux dans les messages clients.\\n"
                    + "Analyse le message fourni.\\n"
                    + "- Si le message contient des propos inappropriés ou illégaux, écris 'bad words'.\\n"
                    + "- Sinon, écris simplement 'nothing'.\"},"
                    + " {\"role\": \"user\", \"content\": \"" + message.replace("\"", "\\\"") + "\"}"
                    + "]"
                    + "}";

            StringEntity entity = new StringEntity(jsonBody, "UTF-8");
            request.setEntity(entity);

            HttpResponse httpResponse = httpClient.execute(request);

            BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }

            System.out.println("Réponse brute de l'API: " + responseBuilder.toString());

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonResponse = objectMapper.readTree(responseBuilder.toString());

            response = jsonResponse.get("choices").get(0).get("message").get("content").asText();

            httpClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }


    private String traduireFrancaisAnglais(String message) {
        String response = "";

        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost request = new HttpPost(API_URL);

            request.setHeader("Authorization", "Bearer " + API_KEY);
            request.setHeader("Content-Type", "application/json");

            String jsonBody = "{"
                    + "\"model\": \"deepseek/deepseek-r1-distill-llama-70b:free\","
                    + "\"messages\": ["
                    + " {\"role\": \"system\", \"content\": \"Tu es un agent d'assistance chargé de traduire  les textes francais en anglais dans les messages clients.\\n"
                    + "Voici le message client.\\n"
                    + " {\"role\": \"user\", \"content\": \"" + message.replace("\"", "\\\"") + "\"}"
                    + "]"
                    + "}";

            StringEntity entity = new StringEntity(jsonBody, "UTF-8");
            request.setEntity(entity);

            HttpResponse httpResponse = httpClient.execute(request);

            BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }

            System.out.println("Réponse brute de l'API: " + responseBuilder.toString());

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonResponse = objectMapper.readTree(responseBuilder.toString());

            response = jsonResponse.get("choices").get(0).get("message").get("content").asText();

            httpClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }



    @FXML
    void VoirSuiviRec(ActionEvent actionEvent) {
        // Détecter la sélection d'une ligne dans la TableView
        TableReclamation.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // Récupérer l'ID de la réclamation sélectionnée
                Long selectedReclamationId = newValue.getId();
                // Appeler directement la méthode de redirection après avoir récupéré l'ID
                showGestionSuivisClientScene(selectedReclamationId);
            }
        });
    }

    private void showGestionSuivisClientScene(Long reclamationId) {
        try {
            // Charger le FXML de GestionSuivisClientController
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionSuivisClient.fxml"));
            Parent root = loader.load();


            // Passer l'ID de la réclamation à la nouvelle scène
            GestionSuivisClientController gestionSuivisRecClientController = loader.getController();
            gestionSuivisRecClientController.setRelamationId(reclamationId);

            // Créer une nouvelle scène et l'afficher
            Stage stage = new Stage();
            stage.setTitle("Ajouter un suivi");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Erreur lors de l'ouverture de la fenêtre de gestion des suivis : " + e.getMessage());
        }
    }

    @FXML
    private void RechercheReclamation(ActionEvent actionEvent) {
        String searchText = txtRechercherRec.getText().trim().toLowerCase(); // Texte de recherche en minuscules

        if (searchText.isEmpty()) {
            // Charger toutes les réclamations de l'utilisateur
            loadReclamationsForUser(currentUser, false);
        } else {
            // Appliquer un filtrage en fonction du texte saisi
            List<Reclamation> filtered = reclamations.stream()
                    .filter(rec ->
                            rec.getTitre().toLowerCase().contains(searchText) ||
                                    rec.getDescription_rec().toLowerCase().contains(searchText) ||
                                    rec.getType_rec().toLowerCase().contains(searchText) ||
                                    rec.getStatus_rec().toLowerCase().contains(searchText))
                    .collect(Collectors.toList());

            TableReclamation.setItems(FXCollections.observableArrayList(filtered));
        }
    }


    private void searchReclamations(String searchText) {
        String query = "SELECT r.*, u.email FROM reclamation r " +
                "JOIN user u ON r.user_id = u.id " +
                "WHERE (r.titre LIKE ? OR r.type_rec LIKE ? OR r.status_rec LIKE ? OR u.email LIKE ?) " +
                "AND r.user_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            String searchPattern = "%" + searchText + "%";
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);
            ps.setString(3, searchPattern);
            ps.setString(4, searchPattern);
            ps.setLong(5, currentUser.getId()); // ← filtrage par l'utilisateur connecté

            try (ResultSet rs = ps.executeQuery()) {
                reclamations.clear();

                while (rs.next()) {
                    Long id = rs.getLong("id");
                    LocalDate dateCreation = rs.getDate("date_creation_rec") != null ? rs.getDate("date_creation_rec").toLocalDate() : null;
                    LocalDate dateResolution = rs.getDate("date_resolution_rec") != null ? rs.getDate("date_resolution_rec").toLocalDate() : null;

                    Reclamation rec = new Reclamation(
                            id,
                            rs.getString("titre"),
                            rs.getString("mail_client_rec"),
                            rs.getString("type_rec"),
                            rs.getString("description_rec"),
                            rs.getString("status_rec"),
                            dateCreation,
                            dateResolution,
                            rs.getString("fichier_pj_rec")
                    );

                    User user = new User();
                    user.setEmail(rs.getString("email"));
                    rec.setUser(user);

                    reclamations.add(rec);
                }

                TableReclamation.setItems(FXCollections.observableArrayList(reclamations));
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du filtrage des réclamations : " + e.getMessage());
        }
    }


    @FXML
    public void TrieCroissant(ActionEvent actionEvent) {
        String query = "SELECT * FROM reclamation ORDER BY titre  ASC"; // Tri par date_creation_rec croissant

        try (PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            reclamations.clear(); // Effacer les réclamations actuelles dans la liste

            // Parcourir les résultats de la requête et ajouter les réclamations triées à la liste
            while (rs.next()) {
                Long id = rs.getLong("id");
                LocalDate dateCreation = rs.getDate("date_creation_rec") != null ? rs.getDate("date_creation_rec").toLocalDate() : null;
                LocalDate dateResolution = rs.getDate("date_resolution_rec") != null ? rs.getDate("date_resolution_rec").toLocalDate() : null;

                Reclamation rec = new Reclamation(
                        id,
                        rs.getString("titre"),
                        rs.getString("mail_client_rec"),
                        rs.getString("type_rec"),
                        rs.getString("description_rec"),
                        rs.getString("status_rec"),
                        dateCreation,
                        dateResolution,
                        rs.getString("fichier_pj_rec")
                );
                reclamations.add(rec); // Ajouter la réclamation à la liste
            }

            // Mettre à jour le TableView avec les réclamations triées
            TableReclamation.setItems(FXCollections.observableArrayList(reclamations));

        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du tri des réclamations : " + e.getMessage());
        }
    }

    public void setUser(User user) {
        this.currentUser=user;
        loadReclamationsForUser(user, false);
    }


    public void GoToChatbot(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ChatBot.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to load chatbot page: " + e.getMessage());
        }
    }


}
