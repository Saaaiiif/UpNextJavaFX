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
             ResultSet rs = stmt.executeQuery("SELECT id, name, image, description, status, social FROM communities")) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                byte[] image = rs.getBytes("image");
                String description = rs.getString("description");
                int status = rs.getInt("status");
                String social = rs.getString("social");
                communities.add(new Community(id, name, image, description, status, social));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return communities;
    }

    public List<Community> getCommunitiesWithStatus2() {
        List<Community> communities = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name, image, description, social FROM communities WHERE status = 2")) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                byte[] image = rs.getBytes("image");
                String description = rs.getString("description");
                String social = rs.getString("social");
                communities.add(new Community(id, name, image, description, 2, social));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return communities;
    }

    public List<Community> searchCommunities(String searchTerm) {
        List<Community> communities = new ArrayList<>();
        String query = "SELECT id, name, image, description, status, social FROM communities WHERE name LIKE ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, "%" + searchTerm + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                byte[] image = rs.getBytes("image");
                String description = rs.getString("description");
                int status = rs.getInt("status");
                String social = rs.getString("social");
                communities.add(new Community(id, name, image, description, status, social));
            }
        } catch (SQLException e) {
            System.err.println("Failed to search communities: " + e.getMessage());
            e.printStackTrace();
        }
        return communities;
    }

    public void addCommunity(String name) {
        String query = "INSERT INTO communities (name, status) VALUES (?, 2)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to add community: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteCommunity(int id) {
        String query = "DELETE FROM communities WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to delete community: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateCommunity(int id, String newName) {
        String query = "UPDATE communities SET name = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, newName);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to update community: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateCommunityImage(int id, byte[] imageData) {
        String query = "UPDATE communities SET image = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setBytes(1, imageData);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to update community image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateCommunityDescription(int id, String description) {
        String query = "UPDATE communities SET description = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, description);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to update community description: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateCommunityStatus(int id, int status) {
        String query = "UPDATE communities SET status = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, status);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to update community status: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean idExists(int id) {
        String query = "SELECT id FROM communities WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addCommunityWithId(int id, String name, byte[] image) {
        addCommunityWithId(id, name, image, "");
    }

    public void addCommunityWithId(int id, String name, byte[] image, String description) {
        addCommunityWithId(id, name, image, description, 2, "");
    }

    public void addCommunityWithId(int id, String name, byte[] image, String description, int status, String social) {
        String query = "INSERT INTO communities (id, name, image, description, status, social) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.setString(2, name);
            stmt.setBytes(3, image);
            stmt.setString(4, description);
            stmt.setInt(5, status);
            stmt.setString(6, social);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to add community: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Community getCommunityById(int id) {
        String query = "SELECT id, name, image, description, status, social FROM communities WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String name = rs.getString("name");
                byte[] image = rs.getBytes("image");
                String description = rs.getString("description");
                int status = rs.getInt("status");
                String social = rs.getString("social");
                return new Community(id, name, image, description, status, social);
            }
        } catch (SQLException e) {
            System.err.println("Failed to get community by id: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public List<Artist> getArtistsByCommunityId(int communityId) {
        List<Artist> artists = new ArrayList<>();
        String query = "SELECT id_artist, artist_name, artist_image FROM artists WHERE id_community = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, communityId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id_artist");
                String name = rs.getString("artist_name");
                byte[] image = rs.getBytes("artist_image");
                artists.add(new Artist(id, name, image, communityId));
            }
        } catch (SQLException e) {
            System.err.println("Failed to get artists by community id: " + e.getMessage());
            e.printStackTrace();
        }
        return artists;
    }

    public List<Artist> getAllArtists() {
        List<Artist> artists = new ArrayList<>();
        String query = "SELECT id_artist, artist_name, artist_image, id_community FROM artists";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id_artist");
                String name = rs.getString("artist_name");
                byte[] image = rs.getBytes("artist_image");
                int communityId = rs.getInt("id_community");
                artists.add(new Artist(id, name, image, communityId));
            }
        } catch (SQLException e) {
            System.err.println("Failed to get all artists: " + e.getMessage());
            e.printStackTrace();
        }
        return artists;
    }
}
