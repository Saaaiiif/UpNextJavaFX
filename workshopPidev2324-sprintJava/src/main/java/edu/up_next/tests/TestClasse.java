package edu.up_next.tests;

import edu.up_next.entities.Event;
import edu.up_next.entities.Reservation;
import edu.up_next.services.EventServices;
import edu.up_next.services.ReservationServices;
import edu.up_next.tools.MyConnection;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class TestClasse {
    public static void main(String[] args) throws SQLException {
        MyConnection myConnection = new MyConnection();
        Event event = new Event(
                33,  // id de l'événement
                3,  // host_id : l'utilisateur qui organise l'événement
                "Festival de Musique",  // titre de l'événement
                "Un festival de Musique avec des artistes internationaux.",  // description
                LocalDateTime.of(2025, 6, 20, 18, 0),  // date et heure de début
                LocalDateTime.of(2025, 6, 20, 22, 0),  // date et heure de fin
                "Programmé",  // statut de l'événement
                3,  // nombre de billets disponibles
                3001,  // guest_id
                "London, England",  // lieu
                "immmmmg.jpg",  // Chemin ou URL de l'image
                3.0   // prix du billet
        );

        Reservation reservation = new Reservation(
                13,      // id de la réservation
                32,      // event_id
                6,       // user_id
                20.0F,   // total_price
                4        // quantite
        );


        EventServices eventServices = new EventServices();//on instancie EventServices pour pouvoir utiliser addEntity
        ReservationServices reservationServices = new ReservationServices();

        //reservationServices.addEntity(reservation);
        //eventServices.addEntity(event);

        //getAllData retourne une liste des objets j'ai creer un objet e qui va stocker les objet indiv de la liste retournee
        /*for (Event e : eventServices.getAllData()) {
            System.out.println(e);//affichage indiv de chaque objet e(=chaque enregistrement)
        }:*/
        for (Object r : reservationServices.getAllData()) {
            System.out.println(r);//affichage indiv de chaque objet e(=chaque enregistrement)
        }
        //eventServices.updateEntity(event,30);
        //reservationServices.updateEntity(reservation,13);

        //eventServices.deleteEntity(event);
        reservationServices.deleteEntity(reservation);
    }
}
