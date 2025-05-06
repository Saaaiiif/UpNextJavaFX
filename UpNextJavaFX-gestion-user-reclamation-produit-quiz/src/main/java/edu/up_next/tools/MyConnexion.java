package edu.up_next.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnexion {
   private String url = "jdbc:mysql://localhost:3306/upnext-111";
    //private String url = "jdbc:mysql://localhost:3306/upnext11";
    private String login = "root";
    private String pwd = "";
    static Connection cnx;
    private static MyConnexion instance;

    public MyConnexion() {
        try {
            // Optionally load the driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            cnx = DriverManager.getConnection(url, login, pwd);
            testConnection(); // Test the connection immediately
            System.out.println("-----------------------------------------------------------------\n" +
                    "************   Connexion established successfully ! **************\n" +
                    "------------------------------------------------------------------");
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to connect to database: " + e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            System.err.println("Driver not found: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("MySQL JDBC Driver not found: " + e.getMessage(), e);
        }
    }

    private void testConnection() throws SQLException {
        if (cnx == null || cnx.isClosed()) {
            throw new SQLException("Database connection is null or closed");
        }
        // Test if we can actually query the database
        cnx.createStatement().execute("SELECT 1");
    }

    public static MyConnexion getInstance() {
        if (instance == null) {
            instance = new MyConnexion();
        }
        return instance;
    }

    public static Connection getCnx() {
        try {
            if (cnx == null || cnx.isClosed()) {
                instance = new MyConnexion(); // Recreate the connection if it's closed
                return instance.cnx;
            }
            return cnx;
        } catch (SQLException e) {
            System.err.println("Error checking connection status: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Database connection error: " + e.getMessage(), e);
        }
    }

    public Connection getConnection() {
        return getCnx();
    }
}
