package edu.up_next.services;

import edu.up_next.entities.Event;
import edu.up_next.entities.Reservation;
import edu.up_next.interfaces.IService;
import edu.up_next.tools.MyConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReservationServices implements IService<Reservation> {

    @Override
    public void addEntity(Reservation reservation) {
        try {
            String requete = "INSERT INTO reservation (event_id, user_id, quantity, total_price) " +
                    "VALUES (?, ?, ?, ?)";

            PreparedStatement ps = MyConnexion.getInstance().getCnx().prepareStatement(requete);

            ps.setInt(1, reservation.getEvent_id());
            ps.setInt(2, reservation.getUser_id());
            ps.setInt(3, reservation.getQuantity());
            ps.setDouble(4, reservation.getTotal_price());

            ps.executeUpdate();
            System.out.println("reservation ajouté avec succès !");

        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout de la reservation : " + e.getMessage());
        }
    }

    public void cancelReservation(int reservationId) throws SQLException {
        String requete = "DELETE FROM reservation WHERE id = ?";
        try (Connection conn = MyConnexion.getInstance().getCnx();
             PreparedStatement ps = conn.prepareStatement(requete)) {
            ps.setInt(1, reservationId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Aucune réservation trouvée avec l'ID: " + reservationId);
            }
        }
    }

    @Override
    public void deleteEntity(Reservation reservation) {
        try {
            cancelReservation(reservation.getId());
            System.out.println("Réservation supprimée avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression de la réservation : " + e.getMessage());
        }
    }

    @Override
    public void updateEntity(Reservation reservation, int id) {
        try {
            String requete = "UPDATE reservation SET event_id = ?, user_id = ?, quantity = ?, total_price = ? WHERE id = ?";

            PreparedStatement ps = MyConnexion.getInstance().getCnx().prepareStatement(requete);

            ps.setInt(1, reservation.getEvent_id());
            ps.setInt(2, reservation.getUser_id());
            ps.setInt(3, reservation.getQuantity());
            ps.setDouble(4, reservation.getTotal_price());
            ps.setInt(5, reservation.getId());

            ps.executeUpdate();
            System.out.println("Réservation modifiée avec succès !");

        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification de la réservation : " + e.getMessage());
        }
    }

    @Override
    public void updateEntity(Event event) {

    }

    @Override
    public int ajouterEntite(Reservation reservation) {
        return 0;
    }

    @Override
    public void modifierEntite(Reservation reservation) {

    }

    @Override
    public void supprimerEntite(Reservation reservation, int id) {

    }

    @Override
    public void addUser(Reservation user) {
    }

    @Override
    public void deleteUser(Reservation user) {
    }

    @Override
    public void updateUser(Reservation user) {
    }

    public List<Reservation> getReservationsByUser(int userId) {
        List<Reservation> reservations = new ArrayList<>();

        String requete = "SELECT * FROM reservation WHERE user_id = ?";
        try {
            PreparedStatement ps = MyConnexion.getInstance().getCnx().prepareStatement(requete);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Reservation r = new Reservation();
                r.setId(rs.getInt("id"));
                r.setUser_id(rs.getInt("user_id"));
                r.setEvent_id(rs.getInt("event_id"));
                r.setQuantity(rs.getInt("quantity"));
                r.setTotal_price(rs.getFloat("total_price"));

                reservations.add(r);
            }

        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des réservations : " + e.getMessage());
        }

        return reservations;
    }

    public List<Map<String, Object>> getReservationsByUserId(int userId) {
        List<Map<String, Object>> reservations = new ArrayList<>();
        String query = "SELECT r.id, r.user_id, r.event_id, r.quantity, r.total_price, " +
                "e.title AS event_title, e.startdate AS event_startdate, e.location AS event_location, e.image AS event_image " +
                "FROM reservation r JOIN event e ON r.event_id = e.id WHERE r.user_id = ?";
        try (Connection conn = MyConnexion.getInstance().getCnx();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> reservationData = new HashMap<>();
                reservationData.put("id", rs.getInt("id"));
                reservationData.put("user_id", rs.getInt("user_id"));
                reservationData.put("event_id", rs.getInt("event_id"));
                reservationData.put("quantity", rs.getInt("quantity"));
                reservationData.put("total_price", rs.getFloat("total_price"));
                reservationData.put("event_title", rs.getString("event_title"));
                reservationData.put("event_startdate", rs.getTimestamp("event_startdate") != null ?
                        rs.getTimestamp("event_startdate").toLocalDateTime() : null);
                reservationData.put("event_location", rs.getString("event_location"));
                reservationData.put("event_image", rs.getString("event_image"));
                reservations.add(reservationData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error fetching reservations: " + e.getMessage());
        }
        return reservations;
    }

    @Override
    public List<Reservation> getAllData() {
        List<Reservation> reservations = new ArrayList<>();
        try {
            String requete = "SELECT * FROM reservation";
            Statement st = MyConnexion.getInstance().getCnx().createStatement();
            ResultSet rs = st.executeQuery(requete);
            while (rs.next()) {
                Reservation reservation = new Reservation();
                reservation.setId(rs.getInt("id"));
                reservation.setEvent_id(rs.getInt("event_id"));
                reservation.setUser_id(rs.getInt("user_id"));
                reservation.setQuantity(rs.getInt("quantity"));
                reservation.setTotal_price(rs.getFloat("total_price"));
                reservations.add(reservation);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return reservations;
    }
}