package controllers;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import edu.up_next.entities.User;
import edu.up_next.tools.MyConnexion;
import javafx.stage.Stage;

public class UsersManagement {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Text AuthenticatedUser;

    @FXML
    private Hyperlink Carpooling;

    @FXML
    private Hyperlink EventLink;

    @FXML
    private Hyperlink Logout;

    @FXML
    private Hyperlink ProductLink;

    @FXML
    private ImageView ProfileImage;

    @FXML
    private Hyperlink ProfileLink;

    @FXML
    private Hyperlink QuizManagement;

    @FXML
    private Hyperlink Reclamation;

    @FXML
    private Hyperlink UsersManagement;

    @FXML
    private TextField search;

    @FXML
    private TableView<User> usersTable;

    // Table columns
    @FXML
    private TableColumn<User, Integer> idColumn;
    @FXML
    private TableColumn<User, String> emailColumn;
    @FXML
    private TableColumn<User, String> rolesColumn;
    @FXML
    private TableColumn<User, String> firstnameColumn;
    @FXML
    private TableColumn<User, String> lastnameColumn;
    @FXML
    private TableColumn<User, String> specialityColumn;
    @FXML
    private TableColumn<User, Boolean> activeColumn;
    @FXML
    private TableColumn<User, Boolean> verifiedColumn;



    private ObservableList<User> usersList = FXCollections.observableArrayList();

    // ... (keep all your existing navigation methods unchanged)




    private void loadUsersData() {
        usersList.clear();

        MyConnexion db = new MyConnexion();
        Connection conn = db.getConnection();
        String query = "SELECT id, email, roles, firstname, lastname, speciality, is_active, is_verified FROM user";

        try {
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                // Create User object with only the required parameters
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("email"),
                        rs.getString("roles"),
                        "", // password
                        rs.getString("firstname"),
                        rs.getString("lastname"),
                        rs.getString("speciality")
                        // Only include parameters that match your User constructor
                );

                // Set additional fields using setters if available

                user.setIs_active(rs.getBoolean("is_active"));
                user.setIs_verified(rs.getBoolean("is_verified"));

                usersList.add(user);
            }

            usersTable.setItems(usersList);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void initialize() {
        // ... (keep all your existing assertions)

        // Initialize table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        rolesColumn.setCellValueFactory(new PropertyValueFactory<>("roles"));
        firstnameColumn.setCellValueFactory(new PropertyValueFactory<>("firstname"));
        lastnameColumn.setCellValueFactory(new PropertyValueFactory<>("lastname"));
        specialityColumn.setCellValueFactory(new PropertyValueFactory<>("speciality"));
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("is_active"));
        verifiedColumn.setCellValueFactory(new PropertyValueFactory<>("is_verified"));

        // Load data
        loadUsersData();
    }
}