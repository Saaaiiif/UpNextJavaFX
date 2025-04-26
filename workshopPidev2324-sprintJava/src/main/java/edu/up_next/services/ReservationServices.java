package edu.up_next.services;

import edu.up_next.entities.Reservation;
import edu.up_next.interfaces.IService;
import edu.up_next.tools.MyConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ReservationServices implements IService <Reservation> {
    @Override
    public void addEntity(Reservation reservation ) {
        try {
            String requete = "INSERT INTO reservation (event_id, user_id, quantity, total_price) " +
                    "VALUES (?, ?, ?, ?)";

            PreparedStatement ps = MyConnection.getInstance().getCnx().prepareStatement(requete);

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

    @Override
    public void deleteEntity(Reservation reservation) {
        try {
            String requete = "DELETE FROM reservation WHERE id = ?";
            PreparedStatement ps = MyConnection.getInstance().getCnx().prepareStatement(requete);
            ps.setInt(1, reservation.getId());
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Reservation supprimé avec succès !");
            } else {
                System.out.println("Aucun Reservation trouvé avec l'ID : " + reservation.getId());
            }
        } catch (SQLException e) {
        System.out.println("Erreur lors de la suppression de la reservation : " + e.getMessage());
    }
    }

    @Override
    public void updateEntity(Reservation reservation, int id) {
        try {
            String requete = "UPDATE reservation SET event_id = ?, user_id = ?, quantity = ?, total_price = ? WHERE id = ?";

            PreparedStatement ps = MyConnection.getInstance().getCnx().prepareStatement(requete);

            ps.setInt(1, reservation.getEvent_id());
            ps.setInt(2, reservation.getUser_id());
            ps.setInt(3, reservation.getQuantity());
            ps.setDouble(4, reservation.getTotal_price());
            ps.setInt(5, id);

            ps.executeUpdate();
            System.out.println("Réservation modifiée avec succès !");

        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification de la réservation : " + e.getMessage());
        }
    }

    @Override
    public List getAllData() {
        List<Reservation> reservations = new ArrayList<>();
        try {
            String requete = "SELECT * FROM reservation";
            Statement st = null;
            st = MyConnection.getInstance().getCnx().createStatement();
            ResultSet rs = st.executeQuery(requete);
            while (rs.next()) {
                Reservation reservation = new Reservation();
                reservation.setEvent_id(rs.getInt("event_id"));
                reservation.setUser_id(rs.getInt("user_id"));
                reservation.setQuantity(rs.getInt("quantity"));
                reservation.setTotal_price(rs.getInt("total_price"));

                reservations.add(reservation);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return reservations;
    }

}
