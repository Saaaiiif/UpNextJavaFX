package edu.up_next.interfaces;

import java.sql.SQLException;
import java.util.List;

public interface IService <T>{
    void addEntity(T t);
    void deleteEntity(T t)throws SQLException;
    void updateEntity(T t,int id) throws SQLException ;
    List<T> getAllData() throws SQLException;

}
