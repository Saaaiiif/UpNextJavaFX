package edu.up_next;

import edu.up_next.entities.Suivi;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import edu.up_next.tools.MyConnexion;

import java.awt.event.ActionEvent;
import java.sql.*;
import java.time.LocalDate;

public class GestionSuivisRecController {


    @FXML
    private TableColumn<Suivi, LocalDate> DateSuiviCol;

    @FXML
    private TableColumn<Suivi, String> DescSuiviCol;

    @FXML
    private TableColumn<Suivi, String> MailCol;

    @FXML
    private TableColumn<Suivi, String> SatisfactionCol;

    @FXML
    private TableView<Suivi> TablleSuivieAdm;

    @FXML
    private TableColumn<Suivi, String> TitreCol;

    @FXML
    private Button btn_ajouter_Suivi;

    @FXML
    private Button btn_modifier_suivi;

    @FXML
    private Button btn_supprimer_suivi;
    @FXML
    private Label labelDescSuiviError; // Le label d'erreur

    @FXML
    private TextArea txtDescSuivie;

    private Long reclamationId;
    private Connection connection;
    private ObservableList<Suivi> suivisList = FXCollections.observableArrayList(); // Liste des suivis
    private String statut; // "ouverte" ou "fermée"


    public GestionSuivisRecController() {
        // Initialiser la connexion au moment où le contrôleur est créé
        this.connection = MyConnexion.getInstance().getConnection(); // Assurez-vous que cette méthode retourne une connexion valide
    }


    public void setRelamationId(Long reclamationId) {
        this.reclamationId = reclamationId;
        checkReclamationStatus(); // Vérifier si la réclamation est fermée
        loadSuivis(); // Charger les suivis liés à cette réclamation
    }
    private void checkReclamationStatus() {
        String query = "SELECT status_rec FROM reclamation WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setLong(1, reclamationId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String statut = rs.getString("status_rec");

                if ("Fermée".equalsIgnoreCase(statut) || "Fermé".equalsIgnoreCase(statut) || "Ferme".equalsIgnoreCase(statut)) {
                    // Désactiver les boutons si la réclamation est fermée
                    btn_ajouter_Suivi.setDisable(true);
                    btn_modifier_suivi.setDisable(true);
                    btn_supprimer_suivi.setDisable(true);
                } else {
                    // Activer les boutons si la réclamation est ouverte
                    btn_ajouter_Suivi.setDisable(false);
                    btn_modifier_suivi.setDisable(false);
                    btn_supprimer_suivi.setDisable(false);
                }
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la vérification du statut de la réclamation : " + e.getMessage());
        }
    }



    @FXML
    void AjouterSuivi(ActionEvent event) {
        // Ajouter un suivi
    }

    @FXML
    void ModifierSuivi(ActionEvent event) {
        // Modifier un suivi
    }

    @FXML
    void SupprimerSuivi(ActionEvent event) {
        // Supprimer un suivi
    }

    @FXML
    public void initialize() {
        initializeColumns();
        if (reclamationId != null) {
            checkReclamationStatus();
            loadSuivis();
        }
    }

    // Méthode pour configurer les colonnes
    private void initializeColumns() {
        DescSuiviCol.setCellValueFactory(new PropertyValueFactory<>("descriptionSuivi"));
        DateSuiviCol.setCellValueFactory(new PropertyValueFactory<>("dateSuivi"));
        SatisfactionCol.setCellValueFactory(new PropertyValueFactory<>("satisfaction"));
        TitreCol.setCellValueFactory(new PropertyValueFactory<>("titreReclamation"));
        MailCol.setCellValueFactory(new PropertyValueFactory<>("mailClientRec"));

    }


    // Charger les suivis associés à la réclamation
    private void loadSuivis() {
        if (reclamationId == null) {
            showAlert("Erreur", "Aucun ID de réclamation disponible.");
            return;
        }
        String query = "SELECT s.id, s.reclamation_id, s.description_suivi, s.date_suivi, s.statisfaction, r.titre, r.mail_client_rec " +
                "FROM suivi s " +
                "JOIN reclamation r ON s.reclamation_id = r.id " +
                "WHERE s.reclamation_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setLong(1, reclamationId); // Passer l'ID de la réclamation dans la requête
            ResultSet rs = ps.executeQuery();

            suivisList.clear(); // Vider la liste avant d'ajouter les nouveaux suivis

            while (rs.next()) {
                Long id = rs.getLong("id");
                Long reclamationId = rs.getLong("reclamation_id");
                String descriptionSuivi = rs.getString("description_suivi");
                LocalDate dateSuivi = rs.getDate("date_suivi").toLocalDate();
                String satisfaction = rs.getString("statisfaction");
                String titreReclamation = rs.getString("titre");
                String mailClientRec = rs.getString("mail_client_rec");
               // LocalDate dateResolutionRec = rs.getDate("date_resolution_rec") != null ? rs.getDate("date_resolution_rec").toLocalDate() : null;


                Suivi suivi = new Suivi(id, reclamationId, descriptionSuivi, dateSuivi, satisfaction, titreReclamation, mailClientRec);
                suivisList.add(suivi);
            }

            // Mettre à jour la TableView avec les données
            TablleSuivieAdm.setItems(suivisList);

        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des suivis : " + e.getMessage());
        }
    }

    // Méthode pour afficher une alerte
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void showAlertError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void AjouterSuivi(javafx.event.ActionEvent actionEvent) {
        String descriptionSuivi = txtDescSuivie.getText();
        LocalDate dateSuivi = LocalDate.now(); // Utilisez la date actuelle comme date du suivi
        String satisfaction = "Ignored"; // Valeur par défaut pour la satisfaction, vous pouvez ajouter un champ pour ça

        // Vérification de la description du suivi
        if (descriptionSuivi.isEmpty()) {
            labelDescSuiviError.setText("La description du suivi ne peut pas être vide.");
            labelDescSuiviError.setVisible(true); // Affiche le message d'erreur
            return;
        } else if (descriptionSuivi.length() <= 10) {
            labelDescSuiviError.setText("La description doit contenir plus de 10 caractères.");
            labelDescSuiviError.setVisible(true); // Affiche le message d'erreur
            return;
        } else {
            labelDescSuiviError.setVisible(false); // Masque le message d'erreur si la saisie est correcte
        }

        String query = "INSERT INTO suivi (reclamation_id, description_suivi, date_suivi, statisfaction) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setLong(1, reclamationId);
            ps.setString(2, descriptionSuivi);
            ps.setDate(3, Date.valueOf(dateSuivi)); // Conversion LocalDate en java.sql.Date
            ps.setString(4, satisfaction);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                showAlert("Succès", "Le suivi a été ajouté avec succès.");
                loadSuivis(); // Recharger les suivis après l'ajout
            } else {
                showAlertError("Erreur", "Une erreur est survenue lors de l'ajout du suivi.");
            }
        } catch (SQLException e) {
            showAlertError("Erreur", "Erreur lors de l'ajout du suivi : " + e.getMessage());
        }
    }

    public void ModifierSuivi(javafx.event.ActionEvent actionEvent) {
        Suivi selectedSuivi = TablleSuivieAdm.getSelectionModel().getSelectedItem();

        if (selectedSuivi == null) {
            showAlertError("Erreur", "Aucun suivi sélectionné.");
            return;
        }

        String descriptionSuivi = txtDescSuivie.getText();
        LocalDate dateSuivi = LocalDate.now(); // Utilisez la date actuelle
        String satisfaction = "Ignored"; // Valeur par défaut pour la satisfaction

        // Vérification de la description du suivi
        if (descriptionSuivi.isEmpty()) {
            labelDescSuiviError.setText("La description du suivi ne peut pas être vide.");
            labelDescSuiviError.setVisible(true); // Affiche le message d'erreur
            return;
        } else if (descriptionSuivi.length() <= 10) {
            labelDescSuiviError.setText("La description doit contenir plus de 10 caractères.");
            labelDescSuiviError.setVisible(true); // Affiche le message d'erreur
            return;
        } else {
            labelDescSuiviError.setVisible(false); // Masque le message d'erreur si la saisie est correcte
        }

        String query = "UPDATE suivi SET description_suivi = ?, date_suivi = ?, statisfaction = ? WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, descriptionSuivi);
            ps.setDate(2, Date.valueOf(dateSuivi)); // Conversion LocalDate en java.sql.Date
            ps.setString(3, satisfaction);
            ps.setLong(4, selectedSuivi.getId());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                showAlert("Succès", "Le suivi a été modifié avec succès.");
                loadSuivis(); // Recharger les suivis après modification
            } else {
                showAlert("Succès", "Une erreur est survenue lors de la modification du suivi.");
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la modification du suivi : " + e.getMessage());
        }
    }

    public void SupprimerSuivi(javafx.event.ActionEvent actionEvent) {
        Suivi selectedSuivi = TablleSuivieAdm.getSelectionModel().getSelectedItem();

        if (selectedSuivi == null) {
            showAlert("Erreur", "Aucun suivi sélectionné.");
            return;
        }

        String query = "DELETE FROM suivi WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setLong(1, selectedSuivi.getId());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                showAlert("Succès", "Le suivi a été supprimé avec succès.");
                loadSuivis(); // Recharger les suivis après suppression
            } else {
                showAlert("Erreur", "Une erreur est survenue lors de la suppression du suivi.");
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la suppression du suivi : " + e.getMessage());
        }
    }
}
