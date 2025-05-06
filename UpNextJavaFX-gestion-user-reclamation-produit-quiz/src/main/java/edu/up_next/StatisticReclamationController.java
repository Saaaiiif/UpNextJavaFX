package edu.up_next;

import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import edu.up_next.tools.MyConnexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StatisticReclamationController {

    @FXML
    private Label label_nbre_recs;
    @FXML
    private CategoryAxis xAxis;

    @FXML
    private NumberAxis yAxis;
    @FXML
    private ProgressIndicator prog_indicator_satisfaction;

    @FXML
    private ProgressBar progress_satisfaction;


    @FXML
    private Label label_nbre_recs_non_traite;

    @FXML
    private Label label_nbre_suivis;

    @FXML
    private LineChart<?, ?> line_chart_recs_date;

    @FXML
    private PieChart pie_chart_recs_type;
    private Connection connection;

    public StatisticReclamationController() {
        // Établir la connexion avec la base de données
        connection = MyConnexion.getInstance().getConnection();
    }

    @FXML
    public void initialize() {
        afficherNombreReclamations();
        afficherNombreSuivis();
        afficherNombreReclamationsNonTraitees();
        afficherStatistiquesParType();
        afficherStatistiquesParDate();
        afficherTauxSatisfaction();
    }

    private void afficherNombreReclamations() {
        String query = "SELECT COUNT(*) FROM reclamation";
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                label_nbre_recs.setText(String.valueOf(resultSet.getInt(1)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void afficherNombreSuivis() {
        String query = "SELECT COUNT(*) FROM suivi";
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                label_nbre_suivis.setText(String.valueOf(resultSet.getInt(1)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void afficherNombreReclamationsNonTraitees() {
        String query = "SELECT COUNT(*) FROM reclamation WHERE status_rec = 'Ouvert'";
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                label_nbre_recs_non_traite.setText(String.valueOf(resultSet.getInt(1)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void afficherStatistiquesParType() {
        String query = "SELECT type_rec, COUNT(*) FROM reclamation GROUP BY type_rec";
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                String type = resultSet.getString(1);
                int count = resultSet.getInt(2);
                pie_chart_recs_type.getData().add(new PieChart.Data(type, count));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void afficherStatistiquesParDate() {
        String query = "SELECT Date(date_creation_rec), COUNT(*) FROM reclamation GROUP BY Date(date_creation_rec) ";

        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Réclamations par date");

            while (resultSet.next()) {
                String date = resultSet.getString(1);  // La date en format String
                int count = resultSet.getInt(2);  // Le nombre de réclamations

                series.getData().add(new XYChart.Data<>(date, count));
            }

            line_chart_recs_date.getData().clear(); // Nettoyer avant d'ajouter
            line_chart_recs_date.getData().add((XYChart.Series) series); // Cast explicite pour éviter l'erreur
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void afficherTauxSatisfaction() {
        String queryTotal = "SELECT COUNT(*) FROM suivi WHERE statisfaction IN ('Oui', 'Non','Ignored')";
        String queryOui = "SELECT COUNT(*) FROM suivi WHERE statisfaction = 'Oui'";

        int totalSatisfactions = 0;
        int nombreOui = 0;

        try (PreparedStatement stmtTotal = connection.prepareStatement(queryTotal);
             PreparedStatement stmtOui = connection.prepareStatement(queryOui);
             ResultSet resultSetTotal = stmtTotal.executeQuery();
             ResultSet resultSetOui = stmtOui.executeQuery()) {

            if (resultSetTotal.next()) {
                totalSatisfactions = resultSetTotal.getInt(1);
            }
            if (resultSetOui.next()) {
                nombreOui = resultSetOui.getInt(1);
            }

            // Éviter la division par zéro
            double tauxSatisfaction = (totalSatisfactions > 0) ? (double) nombreOui / totalSatisfactions : 0;

            // Mise à jour des composants graphiques (de 0 à 1)
            progress_satisfaction.setProgress(tauxSatisfaction);
            prog_indicator_satisfaction.setProgress(tauxSatisfaction);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }






}
