package edu.up_next.services;

import edu.up_next.entities.Event;
import edu.up_next.entities.User;
import edu.up_next.interfaces.IService;
import edu.up_next.tools.MyConnexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserServices implements IService<User> {

    @Override
    public int ajouterEntite(User user) {
        return 0;
    }

    @Override
    public void modifierEntite(User user) {

    }

    @Override
    public void supprimerEntite(User user, int id) {

    }

    @Override
    public void addUser(User user) {
        try {
            String sql = "INSERT INTO user (id, email, roles, password, firstname, lastname, speciality, description, image, is_verified, num, is_active) " +
                    "VALUES ('" + user.getId() + "', '" + user.getEmail() + "', '" + user.getRoles() + "', '" + user.getPassword() + "', '" +
                    user.getFirstname() + "', '" + user.getLastname() + "', '" + user.getSpeciality() + "', '" + user.getDescription() + "', '" +
                    user.getImage() + "', " + user.isIs_verified() + ", " + user.getNum() + ", " + user.isIs_active() + ")";

            Statement stmt = new MyConnexion().getConnection().createStatement();
            stmt.executeUpdate(sql);
            System.out.println("User " + user.getFirstname() + " has been added to the database");
        } catch (SQLException e) {
            System.out.println("Error adding user: " + e.getMessage());
        }
    }

    public void addUser1(User user) {
        try {
            String requet = "INSERT INTO user (id, email, roles, password, firstname, lastname, speciality, description, image, is_verified, num, is_active) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement stmt = new MyConnexion().getConnection().prepareStatement(requet);
            stmt.setInt(1, user.getId());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getRoles());
            stmt.setString(4, user.getPassword());
            stmt.setString(5, user.getFirstname());
            stmt.setString(6, user.getLastname());
            stmt.setString(7, user.getSpeciality());
            stmt.setString(8, user.getDescription());
            stmt.setString(9, user.getImage());
            stmt.setBoolean(10, user.isIs_verified());
            stmt.setInt(11, user.getNum());
            stmt.setBoolean(12, user.isIs_active());

            stmt.executeUpdate();
            System.out.println("User " + user.getFirstname() + " has been added to the database");
        } catch (SQLException e) {
            throw new RuntimeException("Error adding user: " + e.getMessage());
        }
    }

    public boolean emailExists(String email) {
        try {
            String query = "SELECT COUNT(*) FROM user WHERE email = ?";
            PreparedStatement stmt = new MyConnexion().getConnection().prepareStatement(query);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking email: " + e.getMessage());
        }
        return false;
    }

    @Override
    public void deleteUser(User user) {
        try {
            String sql = "DELETE FROM user WHERE id = ?";
            PreparedStatement stmt = new MyConnexion().getConnection().prepareStatement(sql);
            stmt.setInt(1, user.getId());
            stmt.executeUpdate();
            System.out.println("User with id " + user.getId() + " has been deleted from the database.");
        } catch (SQLException e) {
            System.out.println("Error deleting user: " + e.getMessage());
        }
    }

    @Override
    public void updateUser(User user) {
        try {
            String sql = "UPDATE user SET email = ?, roles = ?, password = ?, firstname = ?, lastname = ?, speciality = ?, description = ?, image = ?, is_verified = ?, num = ?, is_active = ? WHERE id = ?";

            PreparedStatement stmt = new MyConnexion().getConnection().prepareStatement(sql);

            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getRoles());
            stmt.setString(3, user.getPassword());
            stmt.setString(4, user.getFirstname());
            stmt.setString(5, user.getLastname());
            stmt.setString(6, user.getSpeciality());
            stmt.setString(7, user.getDescription());
            stmt.setString(8, user.getImage());
            stmt.setBoolean(9, user.isIs_verified());
            stmt.setInt(10, user.getNum());
            stmt.setBoolean(11, user.isIs_active());
            stmt.setInt(12, user.getId());

            stmt.executeUpdate();
            System.out.println("User with id " + user.getId() + " has been updated in the database.");
        } catch (SQLException e) {
            System.out.println("Error updating user: " + e.getMessage());
        }
    }

    public void updateUserProfile(User user) {
        try {
            String sql = "UPDATE user SET roles = ?, firstname = ?, lastname = ?, speciality = ?, description = ?, image = ?, is_verified = ?, num = ?, is_active = ? WHERE id = ?";
            PreparedStatement stmt = new MyConnexion().getConnection().prepareStatement(sql);
            stmt.setString(1, user.getRoles());
            stmt.setString(2, user.getFirstname());
            stmt.setString(3, user.getLastname());
            stmt.setString(4, user.getSpeciality());
            stmt.setString(5, user.getDescription());
            stmt.setString(6, user.getImage());
            stmt.setBoolean(7, user.isIs_verified());
            stmt.setInt(8, user.getNum());
            stmt.setBoolean(9, user.isIs_active());
            stmt.setInt(10, user.getId());
            stmt.executeUpdate();
            System.out.println("User profile with id " + user.getId() + " has been updated in the database (email and password unchanged).");
        } catch (SQLException e) {
            System.out.println("Error updating user profile: " + e.getMessage());
        }
    }

    @Override
    public List<User> getAllData() {
        List<User> users = new ArrayList<>();
        try {
            String sql = "SELECT * FROM user";
            Statement stmt = new MyConnexion().getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setEmail(rs.getString("email"));
                user.setRoles(rs.getString("roles"));
                user.setPassword(rs.getString("password"));
                user.setFirstname(rs.getString("firstname"));
                user.setLastname(rs.getString("lastname"));
                user.setSpeciality(rs.getString("speciality"));
                user.setDescription(rs.getString("description"));
                user.setImage(rs.getString("image"));
                user.setIs_verified(rs.getBoolean("is_verified"));
                user.setNum(rs.getInt("num"));
                user.setIs_active(rs.getBoolean("is_active"));

                users.add(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching users: " + e.getMessage(), e);
        }

        for (User user : users) {
            System.out.println(user);
        }

        return users;
    }

    @Override
    public void addEntity(User user) {

    }

    @Override
    public void deleteEntity(User user) throws SQLException {

    }

    @Override
    public void updateEntity(User user, int id) throws SQLException {

    }

    @Override
    public void updateEntity(Event event) {

    }

    public List<User> getVerifiedArtists() throws SQLException {
        List<User> verifiedArtists = new ArrayList<>();
        String query = "SELECT * FROM user WHERE is_verified = true AND roles LIKE '%ROLE_ARTIST%'";
        try (Connection conn = new MyConnexion().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setEmail(rs.getString("email"));
                user.setRoles(rs.getString("roles"));
                user.setPassword(rs.getString("password"));
                user.setFirstname(rs.getString("firstname"));
                user.setLastname(rs.getString("lastname"));
                user.setSpeciality(rs.getString("speciality"));
                user.setDescription(rs.getString("description"));
                user.setImage(rs.getString("image"));
                user.setIs_verified(rs.getBoolean("is_verified"));
                user.setNum(rs.getInt("num"));
                user.setIs_active(rs.getBoolean("is_active"));
                verifiedArtists.add(user);
            }
        }
        return verifiedArtists;
    }

    public List<User> searchUsers(String searchTerm) throws SQLException {
        List<User> users = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = new MyConnexion().getConnection();
            String query = "SELECT id, firstname, lastname, email, roles, image FROM user WHERE firstname LIKE ? OR lastname LIKE ?";
            stmt = conn.prepareStatement(query);
            String likeTerm = "%" + searchTerm + "%";
            stmt.setString(1, likeTerm);
            stmt.setString(2, likeTerm);
            rs = stmt.executeQuery();

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setFirstname(rs.getString("firstname"));
                user.setLastname(rs.getString("lastname"));
                user.setEmail(rs.getString("email"));
                user.setRoles(rs.getString("roles"));
                user.setImage(rs.getString("image"));
                users.add(user);
            }
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return users;
    }

    public List<User> searchUsers1(String keyword) {
        List<User> users = new ArrayList<>();
        try {
            String sql = "SELECT * FROM user WHERE firstname LIKE ? OR lastname LIKE ? OR email LIKE ? OR roles LIKE ?";
            PreparedStatement stmt = new MyConnexion().getConnection().prepareStatement(sql);

            String searchPattern = "%" + keyword + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            stmt.setString(4, searchPattern);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error searching users: " + e.getMessage());
        }
        return users;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setEmail(rs.getString("email"));
        user.setRoles(rs.getString("roles"));
        user.setPassword(rs.getString("password"));
        user.setFirstname(rs.getString("firstname"));
        user.setLastname(rs.getString("lastname"));
        user.setSpeciality(rs.getString("speciality"));
        user.setDescription(rs.getString("description"));
        user.setImage(rs.getString("image"));
        user.setIs_verified(rs.getBoolean("is_verified"));
        user.setNum(rs.getInt("num"));
        user.setIs_active(rs.getBoolean("is_active"));

        return user;
    }

    public List<User> advancedSearch(String role, Boolean isActive, String keyword) {
        List<User> users = new ArrayList<>();
        try {
            String sql = "SELECT * FROM user WHERE 1=1";

            if (role != null && !role.isEmpty()) sql += " AND roles LIKE ?";
            if (isActive != null) sql += " AND is_active = ?";
            if (keyword != null && !keyword.isEmpty()) sql += " AND (firstname LIKE ? OR lastname LIKE ? OR email LIKE ?)";

            PreparedStatement stmt = new MyConnexion().getConnection().prepareStatement(sql);
            int index = 1;

            if (role != null && !role.isEmpty()) stmt.setString(index++, "%" + role + "%");
            if (isActive != null) stmt.setBoolean(index++, isActive);
            if (keyword != null && !keyword.isEmpty()) {
                String searchPattern = "%" + keyword + "%";
                stmt.setString(index++, searchPattern);
                stmt.setString(index++, searchPattern);
                stmt.setString(index++, searchPattern);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error in advanced search: " + e.getMessage());
        }
        return users;
    }

    public List<User> getSortedUsers(String sortBy, String order) {
        List<User> users = new ArrayList<>();
        try {
            String validColumns = "firstname, lastname, email, id";
            if (!validColumns.contains(sortBy)) sortBy = "id";

            String sql = "SELECT * FROM user ORDER BY " + sortBy + " " + (order.equalsIgnoreCase("DESC") ? "DESC" : "ASC");

            Statement stmt = new MyConnexion().getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error sorting users: " + e.getMessage());
        }
        return users;
    }

    //ouma(reser+event)
    public List<User> getArtists() throws SQLException {
        List<User> artists = new ArrayList<>();
        String query = "SELECT * FROM user WHERE roles LIKE '%ROLE_ARTIST%'";
        try (Connection conn = new MyConnexion().getCnx();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                artists.add(mapResultSetToUser(rs));
            }
        }
        return artists;
    }
}