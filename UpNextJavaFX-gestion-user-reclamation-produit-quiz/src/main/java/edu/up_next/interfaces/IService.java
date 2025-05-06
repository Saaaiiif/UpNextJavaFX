package edu.up_next.interfaces;

import edu.up_next.entities.Event;

import java.sql.SQLException;
import java.util.List;

public interface IService<T> {

    int ajouterEntite(T t);
    void modifierEntite(T t);
    void supprimerEntite(T t, int id);
    void addUser(T user);

    void deleteUser(T user);

    void updateUser(T user);

    List<T> getAllData();


    //ouma(reser+event)

    void addEntity(T t);
    void deleteEntity(T t)throws SQLException;
    void updateEntity(T t,int id) throws SQLException ;

    void updateEntity(Event event);
}