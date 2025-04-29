package com.example.upnext;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

public class DatabaseService {
    private static final String URL = "jdbc:mysql://localhost:3306/upnext";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public List<Community> getCommunities() {
        List<Community> communities = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name, image, description, status, social, genre FROM communities")) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                byte[] image = rs.getBytes("image");
                String description = rs.getString("description");
                int status = rs.getInt("status");
                String social = rs.getString("social");
                String genre = rs.getString("genre");
                communities.add(new Community(id, name, image, description, status, social, genre));
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
             ResultSet rs = stmt.executeQuery("SELECT id, name, image, description, social, genre FROM communities WHERE status = 2")) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                byte[] image = rs.getBytes("image");
                String description = rs.getString("description");
                String social = rs.getString("social");
                String genre = rs.getString("genre");
                communities.add(new Community(id, name, image, description, 2, social, genre));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return communities;
    }

    public List<Community> searchCommunities(String searchTerm) {
        List<Community> communities = new ArrayList<>();
        String query = "SELECT id, name, image, description, status, social, genre FROM communities WHERE name LIKE ?";
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
                String genre = rs.getString("genre");
                communities.add(new Community(id, name, image, description, status, social, genre));
            }
        } catch (SQLException e) {
            System.err.println("Failed to search communities: " + e.getMessage());
            e.printStackTrace();
        }
        return communities;
    }

    public List<Community> searchCommunitiesByGenre(String genre) {
        List<Community> communities = new ArrayList<>();
        String query = "SELECT id, name, image, description, status, social, genre FROM communities WHERE genre = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, genre);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                byte[] image = rs.getBytes("image");
                String description = rs.getString("description");
                int status = rs.getInt("status");
                String social = rs.getString("social");
                String genreResult = rs.getString("genre");
                communities.add(new Community(id, name, image, description, status, social, genreResult));
            }
        } catch (SQLException e) {
            System.err.println("Failed to search communities by genre: " + e.getMessage());
            e.printStackTrace();
        }
        return communities;
    }

    public void addCommunity(String name) {
        addCommunity(name, "");
    }

    public void addCommunity(String name, String genre) {
        String query = "INSERT INTO communities (name, status, genre) VALUES (?, 2, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setString(2, genre);
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
        addCommunityWithId(id, name, image, description, status, social, "");
    }

    public void addCommunityWithId(int id, String name, byte[] image, String description, int status, String social, String genre) {
        String query = "INSERT INTO communities (id, name, image, description, status, social, genre) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.setString(2, name);
            stmt.setBytes(3, image);
            stmt.setString(4, description);
            stmt.setInt(5, status);
            stmt.setString(6, social);
            stmt.setString(7, genre);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to add community: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Community getCommunityById(int id) {
        String query = "SELECT id, name, image, description, status, social, genre FROM communities WHERE id = ?";
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
                String genre = rs.getString("genre");
                return new Community(id, name, image, description, status, social, genre);
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

    public List<String> getAllGenres() {
        List<String> genres = new ArrayList<>();
        String query = "SELECT DISTINCT genre FROM genres ORDER BY genre";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String genre = rs.getString("genre");
                if (genre != null && !genre.isEmpty()) {
                    genres.add(genre);
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to get all genres: " + e.getMessage());
            e.printStackTrace();
        }
        return genres;
    }

    public void updateCommunityGenre(int id, String genre) {
        String query = "UPDATE communities SET genre = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, genre);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to update community genre: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Updates the social media link for a community.
     * 
     * @param id The ID of the community to update
     * @param social The new social media link
     */
    public void updateCommunitySocial(int id, String social) {
        String query = "UPDATE communities SET social = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, social);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to update community social link: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gets an artist by their ID.
     * 
     * @param artistId The ID of the artist to retrieve
     * @return The Artist object or null if not found
     */
    public Artist getArtistById(int artistId) {
        String query = "SELECT id_artist, artist_name, artist_image, id_community FROM artists WHERE id_artist = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, artistId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String name = rs.getString("artist_name");
                byte[] image = rs.getBytes("artist_image");
                int communityId = rs.getInt("id_community");
                return new Artist(artistId, name, image, communityId);
            }
        } catch (SQLException e) {
            System.err.println("Failed to get artist by id: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets communities related to an artist.
     * This method returns the community the artist belongs to and other communities with the same genre.
     * 
     * @param artistId The ID of the artist
     * @return A list of related communities
     */
    public List<Community> getCommunitiesByArtistId(int artistId) {
        List<Community> communities = new ArrayList<>();

        // First, get the artist's community
        Artist artist = getArtistById(artistId);
        if (artist == null) {
            return communities;
        }

        int communityId = artist.getCommunityId();
        Community primaryCommunity = getCommunityById(communityId);

        if (primaryCommunity != null) {
            communities.add(primaryCommunity);

            // Then get other communities with the same genre
            String genre = primaryCommunity.getGenre();
            if (genre != null && !genre.isEmpty()) {
                String query = "SELECT id, name, image, description, status, social, genre FROM communities " +
                               "WHERE genre = ? AND id != ? LIMIT 5";
                try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                     PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, genre);
                    stmt.setInt(2, communityId);
                    ResultSet rs = stmt.executeQuery();

                    while (rs.next()) {
                        int id = rs.getInt("id");
                        String name = rs.getString("name");
                        byte[] image = rs.getBytes("image");
                        String description = rs.getString("description");
                        int status = rs.getInt("status");
                        String social = rs.getString("social");
                        String genreResult = rs.getString("genre");
                        communities.add(new Community(id, name, image, description, status, social, genreResult));
                    }
                } catch (SQLException e) {
                    System.err.println("Failed to get related communities: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        return communities;
    }

    /**
     * Gets the description for an artist from the database.
     * 
     * @param artistId The ID of the artist
     * @return The artist's description or null if not found
     */
    public String getArtistDescription(int artistId) {
        String query = "SELECT description FROM artists WHERE id_artist = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, artistId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("description");
            }
        } catch (SQLException e) {
            System.err.println("Failed to get artist description: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets all comments for a specific community.
     * 
     * @param communityId The ID of the community
     * @return A list of comments for the community
     */
    public List<Comment> getCommentsByCommunityId(int communityId) {
        List<Comment> comments = new ArrayList<>();
        String query = "SELECT id, id_community, comment FROM comments WHERE id_community = ? ORDER BY id DESC";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, communityId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String comment = rs.getString("comment");
                String username = "Unknown";
                comments.add(new Comment(id, communityId, comment, username));
            }
        } catch (SQLException e) {
            System.err.println("Failed to get comments by community id: " + e.getMessage());
            e.printStackTrace();
        }
        return comments;
    }

    /**
     * Adds a new comment to a community.
     * 
     * @param communityId The ID of the community
     * @param comment The comment text
     * @param username The username of the commenter (optional)
     * @return The ID of the newly added comment, or -1 if the operation failed
     */
    public int addComment(int communityId, String comment, String username) {
        // Generate a unique 8-digit ID
        int commentId = generateUniqueCommentId();

        String query = "INSERT INTO comments (id, id_community, comment) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, commentId);
            stmt.setInt(2, communityId);
            stmt.setString(3, comment);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating comment failed, no rows affected.");
            }

            return commentId;
        } catch (SQLException e) {
            System.err.println("Failed to add comment: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Generates a unique 8-digit ID for comments.
     * 
     * @return A unique 8-digit integer
     */
    private int generateUniqueCommentId() {
        // Generate a random 8-digit number (between 10000000 and 99999999)
        int min = 10000000;
        int max = 99999999;
        int commentId;
        boolean isUnique = false;

        // Keep generating IDs until we find a unique one
        do {
            commentId = min + (int)(Math.random() * ((max - min) + 1));
            isUnique = isCommentIdUnique(commentId);
        } while (!isUnique);

        return commentId;
    }

    /**
     * Checks if a comment ID is unique in the database.
     * 
     * @param commentId The ID to check
     * @return true if the ID is unique, false otherwise
     */
    private boolean isCommentIdUnique(int commentId) {
        String query = "SELECT COUNT(*) FROM comments WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, commentId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 0; // If count is 0, ID is unique
            }
        } catch (SQLException e) {
            System.err.println("Failed to check if comment ID is unique: " + e.getMessage());
            e.printStackTrace();
        }
        return true; // In case of error, assume it's unique to avoid infinite loop
    }

    /**
     * Deletes a comment from the database.
     * 
     * @param commentId The ID of the comment to delete
     * @return true if the comment was deleted successfully, false otherwise
     */
    public boolean deleteComment(int commentId) {
        String query = "DELETE FROM comments WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, commentId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Failed to delete comment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
