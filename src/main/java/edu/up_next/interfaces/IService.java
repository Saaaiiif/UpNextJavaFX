package edu.up_next.interfaces;

import java.util.List;

public interface IService<T> {

    void addUser(T user);

    void deleteUser(T user);

    void updateUser(T user);

    List<T> getAllData();
}