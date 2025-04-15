package edu.up_next.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnexion {
    private String url = "jdbc:mysql://localhost:3307/upnext-111";

    private String login = "root";
    private String pwd = "";
    Connection cnx;

    public MyConnexion() {
        try {
            // Optionally load the driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            cnx = DriverManager.getConnection(url, login, pwd);
            System.out.println("-----------------------------------------------------------------\n" +
                    "************   Connexion established successfully ! **************\n" +
                    "------------------------------------------------------------------");
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("Driver not found: " + e.getMessage());
        }
    }

    public Connection getConnection() {
        return cnx;
    }
}
