package edu.gestion_quiz.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/quiz?autoReconnect=true&useSSL=false";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static Connection cnx;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            cnx = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connexion réussie !");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getCnx() {
        return cnx;
    }
}