package edu.up_next.services;

import edu.up_next.entities.Event;
import edu.up_next.interfaces.IService;
import edu.up_next.tools.MyConnexion;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.sql.DriverManager.getConnection;

public class EventServices implements IService<Event> {
    @Override
    public void addEntity(Event event) {
        try {
            String requete = "INSERT INTO event (host_id, title, descrip, location, startdate, enddate, status_event,image, ticket_count, guest_id, ticket_price) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement ps = MyConnexion.getInstance().getCnx().prepareStatement(requete);

            // Remplissage des placeholders avec les valeurs de l'objet `event`
            ps.setInt(1, event.getHost_id());            // host_id
            ps.setString(2, event.getTitle());           // title
            ps.setString(3, event.getDescrip());         // descrip
            ps.setString(4, event.getLocation());        // location
            ps.setObject(5, event.getStartdate());       // startdate (LocalDateTime)
            ps.setObject(6, event.getEnddate());         // enddate (LocalDateTime)
            ps.setString(7, event.getStatus_event());    // status_event
            ps.setString(8, event.getImage());    // status_event
            ps.setInt(9, event.getTicket_count());       // ticket_count
            ps.setInt(10, event.getGuest_id());           // guest_id
            ps.setDouble(11, event.getTicket_price());   // ticket_price

            ps.executeUpdate();

            System.out.println("Événement ajouté avec succès !");

        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout de l'événement : " + e.getMessage());
        }
    }

    @Override
    public void deleteEntity(Event event) throws SQLException{
        try {
            // Requête SQL pour supprimer l'événement en fonction de son ID
            String requete = "DELETE FROM event WHERE id = ?";

            // Préparer la requête
            PreparedStatement ps = MyConnexion.getInstance().getCnx().prepareStatement(requete);

            // Remplir le paramètre de la requête avec l'ID de l'événement
            ps.setInt(1, event.getId());

            // Exécuter la requête de suppression
            int rowsAffected = ps.executeUpdate();

            // Vérifier si l'événement a été supprimé avec succès
            if (rowsAffected > 0) {
                System.out.println("Événement supprimé avec succès !");
            } else {
                System.out.println("Aucun événement trouvé avec l'ID : " + event.getId());
            }

        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression de l'événement : " + e.getMessage());
        }
    }

    public String getEventTitleById(int eventId) throws SQLException {
        String query = "SELECT title FROM event WHERE id = ?";
        PreparedStatement ps = MyConnexion.getInstance().getCnx().prepareStatement(query);
        ps.setInt(1, eventId);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getString("title");
        } else {
            return "Inconnu";
        }
    }

    @Override
    public void updateEntity(Event event, int id)  throws SQLException {
        try {
            String requete = "UPDATE event SET host_id = ?, title = ?, descrip = ?, startdate = ?, enddate = ?, " +
                    "status_event = ?, ticket_count = ?, guest_id = ?, location = ?, image = ?, ticket_price = ? WHERE id = ?";

            PreparedStatement ps = MyConnexion.getInstance().getCnx().prepareStatement(requete);

            // Remplissage des paramètres
            ps.setInt(1, event.getHost_id());
            ps.setString(2, event.getTitle());
            ps.setString(3, event.getDescrip());
            ps.setObject(4, event.getStartdate());
            ps.setObject(5, event.getEnddate());
            ps.setString(6, event.getStatus_event());
            ps.setInt(7, event.getTicket_count());
            ps.setInt(8, event.getGuest_id());
            ps.setString(9, event.getLocation());
            ps.setString(10, event.getImage());
            ps.setDouble(11, event.getTicket_price());
            ps.setInt(12, id); // Utilise le paramètre passé à la méthode

            ps.executeUpdate();
            System.out.println("Événement mis à jour avec succès !");

        } catch (SQLException e) {
            System.out.println("Erreur lors de la mise à jour de l'événement : " + e.getMessage());
        }
    }


    @Override
    public int ajouterEntite(Event event) {
        return 0;
    }

    @Override
    public void modifierEntite(Event event) {

    }

    @Override
    public void supprimerEntite(Event event, int id) {

    }

    @Override
    public void addUser(Event user) {

    }

    @Override
    public void deleteUser(Event user) {

    }

    @Override
    public void updateUser(Event user) {

    }

    public List<Event> getAllData() {
        List<Event> events = new ArrayList<>();
        try {
            String requete = "SELECT * FROM event";
            Statement st = null;
            st = MyConnexion.getInstance().getCnx().createStatement();
            ResultSet rs = st.executeQuery(requete);
            while (rs.next()) {
                Event event = new Event();
                event.setId(rs.getInt("id"));
                event.setHost_id(rs.getInt("host_id"));
                event.setTitle(rs.getString("title"));
                event.setDescrip(rs.getString("descrip"));
                event.setLocation(rs.getString("location"));
                event.setStartdate(rs.getObject("startdate", LocalDateTime.class));
                event.setEnddate(rs.getObject("enddate", LocalDateTime.class));
                event.setStatus_event(rs.getString("status_event"));
                event.setTicket_count(rs.getInt("ticket_count"));
                event.setGuest_id(rs.getInt("guest_id"));
                event.setTicket_price(rs.getDouble("ticket_price"));
                event.setImage(rs.getString("image"));
                events.add(event);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return events;
    }

    public void updateStatus(Event event) {
        // Utilisation de la connexion singleton de MyConnexion
        String query = "UPDATE events SET status_event = ? WHERE id = ?";

        try (Connection conn = MyConnexion.getInstance().getConnection(); // Récupérer la connexion à la base de données
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // Remplir les paramètres de la requête avec les valeurs appropriées
            stmt.setString(1, event.getStatus_event()); // Le nouveau statut
            stmt.setInt(2, event.getId()); // L'ID de l'événement

            // Exécuter la requête de mise à jour
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace(); // Si une exception survient, l'afficher
        }
    }


    @Override
    public void updateEntity(Event event) {
        String query = "UPDATE event SET host_id = ?, title = ?, descrip = ?, location = ?, startdate = ?, enddate = ?, " +
                "status_event = ?, image = ?, ticket_count = ?, guest_id = ?, ticket_price = ? WHERE id = ?";

        try (PreparedStatement ps = MyConnexion.getInstance().getCnx().prepareStatement(query)) {
            // Remplissage des paramètres
            ps.setInt(1, event.getHost_id());
            ps.setString(2, event.getTitle());
            ps.setString(3, event.getDescrip());
            ps.setString(4, event.getLocation());
            ps.setObject(5, event.getStartdate());
            ps.setObject(6, event.getEnddate());
            ps.setString(7, event.getStatus_event());
            ps.setString(8, event.getImage());
            ps.setInt(9, event.getTicket_count());
            ps.setInt(10, event.getGuest_id());
            ps.setDouble(11, event.getTicket_price());
            ps.setInt(12, event.getId());

            // Exécuter la mise à jour
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Événement mis à jour avec succès !");
            } else {
                throw new SQLException("Aucun événement trouvé avec l'ID : " + event.getId());
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la mise à jour de l'événement : " + e.getMessage());
            throw new RuntimeException("Échec de la mise à jour de l'événement", e);
        }
    }
}

