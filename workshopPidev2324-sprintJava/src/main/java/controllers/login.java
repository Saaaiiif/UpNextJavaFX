package controllers;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import edu.up_next.entities.User;
import edu.up_next.tools.MyConnexion;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import at.favre.lib.crypto.bcrypt.BCrypt;
import javafx.stage.Stage;

public class login {
    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;
    @FXML
    private Label EmailError;
    @FXML
    private Label PasswordError;
    @FXML
    private Hyperlink RegisterLink;
    @FXML
    private TextField email;
    @FXML
    private PasswordField password;
    @FXML
    private Hyperlink resetPassword;

    @FXML
    void Login(ActionEvent event) {
        EmailError.setVisible(false);
        PasswordError.setVisible(false);
        String emailInput = email.getText().trim();
        String passwordInput = password.getText();

        // Clear previous errors
        EmailError.setText("");
        PasswordError.setText("");

        if (emailInput.isEmpty() || passwordInput.isEmpty()) {
            EmailError.setText("Email is required");
            EmailError.setTextFill(Color.RED);
            EmailError.setVisible(true);
            PasswordError.setText("Password is required");
            PasswordError.setTextFill(Color.RED);
            PasswordError.setVisible(true);
            return;
        }

        try {
            MyConnexion db = new MyConnexion();
            Connection conn = db.getConnection();
            String query = "SELECT id, email, password, firstname, lastname, roles, speciality, description, image, num, is_active, is_verified FROM user WHERE email = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, emailInput);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String hashedPassword = rs.getString("password");
                BCrypt.Result result = BCrypt.verifyer().verify(passwordInput.toCharArray(), hashedPassword);
                if (result.verified) {
                    // Create User object
                    User user = new User(
                            rs.getInt("id"),
                            rs.getString("email"),
                            rs.getString("roles"),
                            rs.getString("password"),
                            rs.getString("firstname"),
                            rs.getString("lastname"),
                            rs.getString("speciality"),
                            rs.getString("description"),
                            rs.getString("image"),
                            rs.getBoolean("is_verified"),
                            rs.getInt("num"),
                            rs.getBoolean("is_active")
                    );

                    PasswordError.setText("Login successful!");
                    PasswordError.setTextFill(Color.GREEN);
                    PasswordError.setVisible(true);

                    System.out.println("✅ User logged in successfully!");
                    System.out.println("👤 Email: " + emailInput);
                    System.out.println("🕒 Login time: " + java.time.LocalDateTime.now());

                    // Redirect to home page with user data
                    redirectToHome(event, user);
                } else {
                    PasswordError.setText("Invalid email or password");
                    PasswordError.setTextFill(Color.RED);
                    PasswordError.setVisible(true);
                }
            } else {
                EmailError.setText("User not found");
                EmailError.setTextFill(Color.RED);
                EmailError.setVisible(true);
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            PasswordError.setText("Database error.");
            PasswordError.setTextFill(Color.RED);
            PasswordError.setVisible(true);
        }
    }

    private void redirectToHome(ActionEvent event, User user) {
        try {
            // Load the home FXML file
            URL fxmlLocation = getClass().getResource("/home.fxml");
            if (fxmlLocation == null) {
                System.err.println("Error: /home.fxml not found in resources");
                PasswordError.setText("Error loading home page.");
                PasswordError.setTextFill(Color.RED);
                PasswordError.setVisible(true);
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent homeRoot = loader.load();

            // Get the Home controller and pass the user
            home controller = loader.getController();
            controller.setUser(user);

            // Get the current stage
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            Scene scene = new Scene(homeRoot);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error loading home page: " + e.getMessage());
            PasswordError.setText("Error loading home page.");
            PasswordError.setTextFill(Color.RED);
            PasswordError.setVisible(true);
        }
    }

    @FXML
    void goToRegister(MouseEvent event) {
        try {
            URL fxmlLocation = getClass().getResource("/inscription.fxml");
            if (fxmlLocation == null) {
                System.err.println("Error: /inscription.fxml not found in resources");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error loading registration page: " + e.getMessage());
        }
    }

    @FXML
    void goToResetPassword(ActionEvent event) {
        // TODO: Implement reset password redirect
    }

    @FXML
    void initialize() {
        assert EmailError != null : "fx:id=\"EmailError\" was not injected: check your FXML file 'login.fxml'.";
        assert PasswordError != null : "fx:id=\"PasswordError\" was not injected: check your FXML file 'login.fxml'.";
        assert RegisterLink != null : "fx:id=\"RegisterLink\" was not injected: check your FXML file 'login.fxml'.";
        assert email != null : "fx:id=\"email\" was not injected: check your FXML file 'login.fxml'.";
        assert password != null : "fx:id=\"password\" was not injected: check your FXML file 'login.fxml'.";
        assert resetPassword != null : "fx:id=\"resetPassword\" was not injected: check your FXML file 'login.fxml'.";
    }
}