package com.example.upnext;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseService {
    private static final String URL = "jdbc:mysql://localhost:3306/upnext";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public List<Community> getCommunities() {
        List<Community> communities = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name, image FROM communities")) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                byte[] image = rs.getBytes("image");
                communities.add(new Community(id, name, image));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return communities;
    }

    public List<Community> searchCommunities(String searchTerm) {
        List<Community> communities = new ArrayList<>();
        String query = "SELECT id, name, image FROM communities WHERE name LIKE ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, "%" + searchTerm + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                byte[] image = rs.getBytes("image");
                communities.add(new Community(id, name, image));
            }
        } catch (SQLException e) {
            System.err.println("Failed to search communities: " + e.getMessage());
            e.printStackTrace();
        }
        return communities;
    }
}