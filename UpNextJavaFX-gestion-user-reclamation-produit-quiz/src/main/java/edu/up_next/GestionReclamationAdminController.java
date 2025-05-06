package edu.up_next;

import edu.up_next.entities.Reclamation;
import edu.up_next.entities.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import edu.up_next.tools.MyConnexion;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;


public class GestionReclamationAdminController {
    @FXML
    private TableColumn<Reclamation, String> DateCreRecCol;

    @FXML
    private TableColumn<Reclamation, String> DateResRecCol;

    @FXML
    private TableColumn<Reclamation, String> DescriptionCol;

    @FXML
    private TableColumn<Reclamation, String> FichierCol;

    @FXML
    private TableColumn<Reclamation, String> MailCol;

    @FXML
    private TableColumn<Reclamation, String> StatusRec;

    @FXML
    private TableView<Reclamation> TableRecAdm;

    @FXML
    private TableColumn<Reclamation, String> TitreCol;

    @FXML
    private TableColumn<Reclamation, String> TypeCol;

    @FXML
    private TableColumn<Reclamation, String> user_rec;




    @FXML
    private Button btn_suivi;

    @FXML
    private Label labelDescSuiviError; // Le label d'erreur


    @FXML
    private Button btn_generate_pdf;

    private ObservableList<Reclamation> reclamationsList = FXCollections.observableArrayList();
    private Connection connection;

    // Affichage des messages d'alerte
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
/*
    public void generatePDF(ActionEvent actionEvent) {
        PDDocument document = new PDDocument();

        try {
            PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.beginText();  // Commencez un seul flux de texte
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.newLineAtOffset(100, 700);

            contentStream.showText("Liste des Réclamations :");
            contentStream.newLineAtOffset(0, -20);
            contentStream.showText("Titre                   Mail :");
            contentStream.newLineAtOffset(0, -20);

            // Ajouter les réclamations sous forme de texte

            for (Reclamation rec : reclamationsList) {
                contentStream.showText("Titre : " + rec.getTitre());
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Mail : " + rec.getMail_client_rec());
                contentStream.newLineAtOffset(0, -20);
                // Continuez à ajouter les autres champs des réclamations
            }



            // Sauvegarder le fichier PDF
            document.save("reclamations2.pdf");

            // Ouvrir automatiquement le PDF après sa génération
            try {
                File file = new File("reclamations2.pdf");
                if (file.exists()) {
                    Desktop.getDesktop().open(file);
                } else {
                    System.out.println("Fichier PDF non trouvé !");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (document != null) {
                    document.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        showAlert("Succès", "Le fichier PDF a été généré avec succès.");
        }

*/
    public void generatePDF(ActionEvent actionEvent) {
        PDDocument document = new PDDocument();
        PDFont boldFont = PDType1Font.HELVETICA_BOLD;
        float fontSize = 12;
        try {
            PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.setFont(PDType1Font.COURIER, 12); // Police à espacement fixe
            contentStream.setLeading(20f); // Espace vertical entre lignes
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 700); // Départ (X=50, Y=700)


            contentStream.showText(String.format("%-40s%-50s", "Titre", "Mail"));
            contentStream.newLine();

            // Affichage des réclamations

            for (Reclamation rec : reclamationsList) {
                String titre = rec.getTitre();
                String mail = rec.getMail_client_rec();

                // Limite les longueurs et aligne le texte
                contentStream.showText(String.format("%-40.40s%-50.50s", titre, mail));
                contentStream.newLine();
            }

            contentStream.endText(); // Fin de l'écriture du texte
            contentStream.close();   // Fermeture du stream

            document.save("reclamations2.pdf");

            // Ouvrir automatiquement le fichier
            File file = new File("reclamations2.pdf");
            if (file.exists()) {
                Desktop.getDesktop().open(file);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (document != null) {
                    document.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        showAlert("Succès", "Le fichier PDF a été généré avec succès.");
    }

    // Charger les réclamations depuis la base de données
    private void loadReclamations() {
        String query = "SELECT r.*, u.email FROM reclamation r JOIN user u ON r.user_id = u.id";
        try (PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            reclamationsList.clear();

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

                reclamationsList.add(rec);
            }

        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des réclamations : " + e.getMessage());
        }
    }


    // Méthode pour initialiser la table
    @FXML
    public void initialize() {
        connection = MyConnexion.getInstance().getConnection();
        loadReclamations();

        // Lier les colonnes aux propriétés de l'objet Reclamation
        TitreCol.setCellValueFactory(new PropertyValueFactory<>("titre"));
        MailCol.setCellValueFactory(new PropertyValueFactory<>("mail_client_rec"));
        DateCreRecCol.setCellValueFactory(new PropertyValueFactory<>("date_creation_rec"));
        TypeCol.setCellValueFactory(new PropertyValueFactory<>("type_rec"));
        DescriptionCol.setCellValueFactory(new PropertyValueFactory<>("description_rec"));
        StatusRec.setCellValueFactory(new PropertyValueFactory<>("status_rec"));
        DateResRecCol.setCellValueFactory(new PropertyValueFactory<>("date_resolution_rec"));
        FichierCol.setCellValueFactory(new PropertyValueFactory<>("fichier_pj_rec"));
        user_rec.setCellValueFactory(cellData -> {
            User user = cellData.getValue().getUser();
            return new SimpleStringProperty(user != null ? user.getEmail() : "Inconnu");
        });

        // Charger les données dans la TableView
        TableRecAdm.setItems(reclamationsList);

        // Personnaliser la colonne FichierCol
        FichierCol.setCellFactory(column -> new TableCell<Reclamation, String>() {
            private final ImageView imageView = new ImageView();
            private final Hyperlink downloadLink = new Hyperlink();

            @Override
            protected void updateItem(String filePath, boolean empty) {
                super.updateItem(filePath, empty);

                if (empty || filePath == null || filePath.isEmpty()) {
                    setGraphic(null);
                    setText(null);
                } else {
                    File file = new File(filePath);
                    String extension = getFileExtension(file);

                    if (isImage(extension)) {
                        // Afficher l'image si c'est une image
                        imageView.setImage(new Image(file.toURI().toString()));
                        imageView.setFitWidth(50); // Taille de l'image
                        imageView.setFitHeight(50);
                        setGraphic(imageView);
                        setText(null);
                    } else {
                        // Afficher le lien de téléchargement si ce n'est pas une image
                        downloadLink.setText("Télécharger");
                        downloadLink.setOnAction(event -> openFile(file));
                        setGraphic(downloadLink);
                        setText(filePath);
                    }
                }
            }
        });
    }

    // Méthode pour ouvrir le fichier
    private void openFile(File file) {
        try {
            Desktop.getDesktop().open(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Méthode pour récupérer l'extension d'un fichier
    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndex = name.lastIndexOf(".");
        return lastIndex == -1 ? "" : name.substring(lastIndex + 1).toLowerCase();
    }

    // Vérifier si l'extension est une image
    private boolean isImage(String extension) {
        return false;
    }

    public void VoirSuivi(ActionEvent actionEvent) {
        // Détecter la sélection d'une ligne dans la TableView
        TableRecAdm.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // Récupérer l'ID de la réclamation sélectionnée
                Long selectedReclamationId = newValue.getId();
                // Appeler directement la méthode de redirection après avoir récupéré l'ID
                redirectToSuiviPage(selectedReclamationId);
            }
        });
    }
    private void redirectToSuiviPage(Long reclamationId) {
        try {
            // Charger la nouvelle scène FXML pour l'ajout de suivi
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionSuivisRecAdmin.fxml"));
            Parent root = loader.load();

            // Passer l'ID de la réclamation à la nouvelle scène
            GestionSuivisRecController gestionSuivisRecController = loader.getController();
            gestionSuivisRecController.setRelamationId(reclamationId);

            // Créer une nouvelle scène et l'afficher
            Stage stage = new Stage();
            stage.setTitle("Ajouter un suivi");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la redirection vers la page de suivi : " + e.getMessage());
        }
    }


    @FXML
    void goToStatsRecs(MouseEvent event) {

        try {
            // Charger la nouvelle scène FXML pour l'ajout de suivi
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/StatisticsReclamations.fxml"));
            Parent root = loader.load();

            // Passer l'ID de la réclamation à la nouvelle scène
            StatisticReclamationController statsRecControoler = loader.getController();


            // Créer une nouvelle scène et l'afficher
            Stage stage = new Stage();
            stage.setTitle("Statistics");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la redirection vers la page de suivi : " + e.getMessage());
        }


    }




}
