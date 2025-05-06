package edu.up_next;


import edu.up_next.entities.Reclamation;
import edu.up_next.entities.Suivi;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;
import edu.up_next.tools.MyConnexion;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class GestionSuivisClientController {
    @FXML
    private TableView<Suivi> TableSuiviClient;

    @FXML
    private RadioButton oui_radio_satis;

    @FXML
    private RadioButton non_radio_satis;

    @FXML
    private RadioButton ignored_radio_satis;

    @FXML
    private Button btn_satisfait;




    @FXML
    private TableColumn<?, ?> DateSuiviCol;

    @FXML
    private TableColumn<?, ?> DescSuiviCol;

    @FXML
    private TableColumn<?, ?> SatisfactionCol;


    @FXML
    private Label titleLabel;

    @FXML
    private Label mailLabel;

    @FXML
    private Label labelFermeture;  // Assurez-vous d'avoir un label dans votre fichier FXML pour afficher la fermeture

    private Timeline countdownTimeline;

    private Long reclamationId;
    private Connection connection;
    private ObservableList<Suivi> suivis;
    private Reclamation reclamation;

    public GestionSuivisClientController() {
        this.connection = MyConnexion.getInstance().getConnection();
        this.suivis = FXCollections.observableArrayList();  // Initialiser la liste des suivis

    }


@FXML
    public void initialize() {
    this.suivis = FXCollections.observableArrayList();

        // Vérifier que la liste des suivis est bien passée au contrôleur
        if (suivis != null) {
            DescSuiviCol.setCellValueFactory(new PropertyValueFactory<>("descriptionSuivi"));
            DateSuiviCol.setCellValueFactory(new PropertyValueFactory<>("dateSuivi"));
            SatisfactionCol.setCellValueFactory(new PropertyValueFactory<>("satisfaction"));

            //DateResRecCol.setCellValueFactory(new PropertyValueFactory<>("dateResRec"));
            // Ajouter les suivis à la TableView
            TableSuiviClient.setItems(suivis);
        } else {
            showAlert("Erreur", "Aucun suivi disponible pour cette réclamation.");
        }

        // Vérifier si la réclamation est passée et afficher ses informations
        if (reclamation != null) {
            titleLabel.setText("Titre de la réclamation  : " + reclamation.getTitre());
            mailLabel.setText("Mail du client : " + reclamation.getMail_client_rec());
        }



    }

    public void setReclamation(Reclamation reclamation) {
        this.reclamation = reclamation;
    }

    private void checkSatisfactionAndDisableIfNeeded() {
        String query = "SELECT statisfaction FROM suivi WHERE reclamation_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setLong(1, reclamationId); // Assure-toi que cette variable est bien définie dans le contrôleur
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String satisfaction = rs.getString("statisfaction");
                if (satisfaction != null && satisfaction.equalsIgnoreCase("oui")) {
                    // Si au moins un suivi a une satisfaction "oui", on désactive les boutons
                    oui_radio_satis.setDisable(true);
                    non_radio_satis.setDisable(true);
                    ignored_radio_satis.setDisable(true);
                    btn_satisfait.setDisable(true);
                    return; // On sort dès qu'on trouve un "oui"
                }
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la vérification de la satisfaction : " + e.getMessage());
        }
    }

    private void loadSuivis() {
        if (reclamationId == null) {
            showAlert("Erreur", "Aucun ID de réclamation disponible.");
            return;
        }
        String query = "SELECT s.id, s.reclamation_id, s.description_suivi, s.date_suivi, s.statisfaction, r.titre, r.mail_client_rec,r.date_resolution_rec " +
                "FROM suivi s " +
                "JOIN reclamation r ON s.reclamation_id = r.id " +
                "WHERE s.reclamation_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setLong(1, reclamationId); // Passer l'ID de la réclamation dans la requête
            ResultSet rs = ps.executeQuery();


            while (rs.next()) {
              //  Timestamp closureTimestamp = rs.getTimestamp("date_resolution_rec");
              //  LocalDateTime closureDate = closureTimestamp.toLocalDateTime();
              //  startCountdown(closureDate);  // Démarrer le compte à rebours avec la date de fermeture

                Long id = rs.getLong("id");
                Long reclamationId = rs.getLong("reclamation_id");
                String descriptionSuivi = rs.getString("description_suivi");
                LocalDate dateSuivi = rs.getDate("date_suivi").toLocalDate();
                String satisfaction = rs.getString("statisfaction");
                String titreReclamation = rs.getString("titre");
                String mailClientRec = rs.getString("mail_client_rec");

                Suivi suivi = new Suivi(id, reclamationId, descriptionSuivi, dateSuivi, satisfaction, titreReclamation, mailClientRec);
                suivis.add(suivi); // Ajouter à la liste observable
            }

            // Mettre à jour la TableView avec les données
            TableSuiviClient.setItems(suivis);


            checkSatisfactionAndDisableIfNeeded();

        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des suivis : " + e.getMessage());
        }
    }

    @FXML
    public void modifier_Avis(ActionEvent actionEvent) {
        Suivi suiviSelectionne = TableSuiviClient.getSelectionModel().getSelectedItem();
        if (suiviSelectionne != null) {
            String satisfaction = "";

            // Récupérer la valeur sélectionnée
            if (oui_radio_satis.isSelected()) {
                satisfaction = "Oui";
                updateReclamationStatus("Fermé");
             //   stopCountdown();  // Arrêter le compte à rebours
            } else if (non_radio_satis.isSelected()) {
                satisfaction = "Non";
                updateReclamationStatus("Ouvert");
               // stopCountdown();  // Arrêter le compte à rebours
            } else if (ignored_radio_satis.isSelected()) {
                satisfaction = "Ignored";
                addDelayedClosure();
                //stopCountdown();  // Arrêter le compte à rebours
            }

            // Mettre à jour le suivi avec la satisfaction
            updateSuivi(suiviSelectionne.getId(), satisfaction);

            // Rafraîchir la TableView

        } else {
            showAlert("Erreur", "Aucun suivi sélectionné.");
        }
    }
    private void stopCountdown() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();  // Arrêter la timeline
        }
    }

    private void checkReclamationStatus() {
        // Vérifier si un suivi de la réclamation a "Oui" comme satisfaction
        String query = "SELECT COUNT(*) FROM suivi WHERE reclamation_id = ? AND satisfaction = 'Oui'";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setLong(1, reclamationId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int count = rs.getInt(1);

                // Si au moins un suivi a la satisfaction "Oui", on ferme la réclamation
                if (count > 0) {
                    updateReclamationStatus("Fermé");
                } else {
                    // Sinon, la réclamation reste ouverte
                    updateReclamationStatus("Ouvert");
                }
            }

        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la vérification du statut de la réclamation : " + e.getMessage());
        }
    }

    private void updateReclamationStatus(String satisfaction) {

        String status;
        // Définir le statut selon la satisfaction
        // Utilisation d'un if simple pour définir le statut
        //if (satisfaction.equals("Oui")) {
        //    status = "Fermé";
        //} else {
        //    status = "Ouvert";
        //}


      //  showAlert("satisfaction", "satisfaction est " + satisfaction);
     //   showAlert("status", "status est " + status);  //9aad martine ouvert


        // Date de résolution (si la réclamation est fermée)
        LocalDateTime closureDate = satisfaction.equals("Fermé") ? LocalDateTime.now() : null;

        // Requête pour mettre à jour le statut et la date de résolution
        String query = "UPDATE reclamation SET status_rec = ?, date_resolution_rec = ? WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, satisfaction);  // Mettre à jour le statut
            ps.setTimestamp(2, closureDate != null ? Timestamp.valueOf(closureDate) : null);  // Mettre à jour la date de fermeture si "Fermé"
            ps.setLong(3, reclamationId);  // ID de la réclamation
            ps.executeUpdate();  // Exécuter la mise à jour
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la mise à jour du statut de la réclamation : " + e.getMessage());
        }
    }



    private void updateSuivi(Long suiviId, String satisfaction) {
        String query = "UPDATE suivi SET statisfaction = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, satisfaction);
            ps.setLong(2, suiviId);
            ps.executeUpdate();
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la mise à jour du suivi : " + e.getMessage());
        }
    }
//
/*private void addDelayedClosure() {
    // Démarrer un cooldown de 4 secondes
    Timeline cooldown = new Timeline(
            new KeyFrame(Duration.seconds(60), event -> {
                // Après 60 secondes, fermer la réclamation
                LocalDateTime fermetureDate = LocalDateTime.now();

                String query = "UPDATE reclamation SET status_rec = 'Fermé', date_resolution_rec = ? WHERE id = ?";
                // Désactiver les boutons après fermeture
                oui_radio_satis.setDisable(true);
                non_radio_satis.setDisable(true);
                ignored_radio_satis.setDisable(true);
                btn_satisfait.setDisable(true);

                try (PreparedStatement ps = connection.prepareStatement(query)) {
                    ps.setTimestamp(1, Timestamp.valueOf(fermetureDate));
                    ps.setLong(2, reclamationId);
                    ps.executeUpdate();

                   // updateFermetureLabel(fermetureDate); // Met à jour le label
                    labelFermeture.setText("Cette réclamation a été ferméee automatiquement avec succès.");
                    labelFermeture.setStyle("-fx-text-fill: red;");


                } catch (SQLException e) {
                    showAlert("Erreur", "Erreur lors de la fermeture automatique : " + e.getMessage());
                }
            })
    );
    cooldown.setCycleCount(1); // Exécute une seule fois après 4 secondes
    cooldown.play();

    labelFermeture.setText("Réclamation sera fermée dans 60 secondes...");
}
*/

    private void addDelayedClosure() {
        final int[] secondsRemaining = {60}; // Compteur de secondes

        Timeline countdown = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> {
                    secondsRemaining[0]--; // Décrémenter
                    labelFermeture.setText("Réclamation sera fermée dans " + secondsRemaining[0] + " secondes...");

                    if (secondsRemaining[0] <= 0) {
                        // Temps écoulé, fermer la réclamation
                        LocalDateTime fermetureDate = LocalDateTime.now();

                        String query = "UPDATE reclamation SET status_rec = 'Fermé', date_resolution_rec = ? WHERE id = ?";

                        try (PreparedStatement ps = connection.prepareStatement(query)) {
                            ps.setTimestamp(1, Timestamp.valueOf(fermetureDate));
                            ps.setLong(2, reclamationId);
                            ps.executeUpdate();

                            // Mise à jour des éléments UI
                            oui_radio_satis.setDisable(true);
                            non_radio_satis.setDisable(true);
                            ignored_radio_satis.setDisable(true);
                            btn_satisfait.setDisable(true);

                            labelFermeture.setText("Cette réclamation a été fermée automatiquement avec succès.");
                            labelFermeture.setStyle("-fx-text-fill: red;");
                        } catch (SQLException e) {
                            showAlert("Erreur", "Erreur lors de la fermeture automatique : " + e.getMessage());
                        }
                    }
                })
        );

        countdown.setCycleCount(61); // 60 secondes + déclenchement initial
        countdown.play();

        labelFermeture.setText("Réclamation sera fermée dans 60 secondes...");
    }


    private void startCountdown(LocalDateTime fermetureDate) {
        countdownTimeline = new Timeline(
                new KeyFrame(Duration.seconds(3600), event -> {
                    // Calculer la différence entre la date actuelle et la date de fermeture
                    LocalDateTime now = LocalDateTime.now();

                    // Si la date de fermeture est déjà passée
                    if (now.isAfter(fermetureDate)) {
                        labelFermeture.setText("Réclamation fermée");
                        countdownTimeline.stop();  // Arrêter le compte à rebours
                    } else {
                        // Calcul des jours, heures, minutes, secondes restantes
                        long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(now, fermetureDate);
                        long hoursRemaining = java.time.temporal.ChronoUnit.HOURS.between(now, fermetureDate) % 24;
                        long minutesRemaining = java.time.temporal.ChronoUnit.MINUTES.between(now, fermetureDate) % 60;
                        long secondsRemaining = java.time.temporal.ChronoUnit.SECONDS.between(now, fermetureDate) % 60;

                        String countdownText = String.format("Temps restant : %d jour(s) %d heure(s) %d minute(s) %d seconde(s)",
                                daysRemaining, hoursRemaining, minutesRemaining, secondsRemaining);
                        labelFermeture.setText(countdownText);
                    }
                })
        );

        // Démarrer le compte à rebours
        countdownTimeline.setCycleCount(Timeline.INDEFINITE);  // Continuer indéfiniment
        countdownTimeline.play();
    }



/*
    private void updateFermetureLabel(LocalDateTime fermetureDate) {
        // Formater la date pour l'affichage
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss");
        String dateText = "Réclamation fermée le : " + fermetureDate.format(formatter);

        // Mettre à jour le label avec la date de fermeture
        labelFermeture.setText(dateText);
    }
*/

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setSuivis(ObservableList<Suivi> suivis) {
        this.suivis = suivis;  // Assurez-vous que suivis est mis à jour
        TableSuiviClient.setItems(suivis);
    }


    public void setRelamationId(Long reclamationId) {
        this.reclamationId = reclamationId;
        loadSuivis();
    }
}
