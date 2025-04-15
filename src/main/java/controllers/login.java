package controllers;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

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
import javafx.scene.paint.Color;
import edu.up_next.tools.MyConnexion;
import at.favre.lib.crypto.bcrypt.BCrypt;
import javafx.stage.Stage;

import java.sql.*;

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

            String query = "SELECT * FROM user WHERE email = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, emailInput);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String hashedPassword = rs.getString("password");

                // ✅ Correct BCrypt verification (using favre.lib)
                BCrypt.Result result = BCrypt.verifyer().verify(passwordInput.toCharArray(), hashedPassword);

                if (result.verified) {
                    // ✅ Login success
                    PasswordError.setText("Login successful!");
                    PasswordError.setTextFill(Color.GREEN);
                    PasswordError.setVisible(true);

                    // Message dans le terminal
                    System.out.println("✅ User logged in successfully!");
                    System.out.println("👤 Email: " + emailInput);
                    System.out.println("🕒 Login time: " + java.time.LocalDateTime.now());

                    // TODO: Redirect to dashboard
                } else {
                    // ❌ Incorrect password
                    PasswordError.setText("Invalid email or password");
                    PasswordError.setTextFill(Color.RED);
                    PasswordError.setVisible(true);
                }
            } else {
                // ❌ Email not found
                EmailError.setText("User not found");
                EmailError.setTextFill(Color.RED);
                EmailError.setVisible(true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            PasswordError.setText("Database error.");
            PasswordError.setTextFill(Color.RED);
            PasswordError.setVisible(true);
        }
    }

    private void redirectToHome(ActionEvent event) {
        try {
            // Load the home FXML file
            Parent homeRoot = FXMLLoader.load(getClass().getResource("home.fxml"));

            // Get the current stage
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();

            // Create new scene and replace the current one
            Scene scene = new Scene(homeRoot);
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ Error loading home page");
        }
    }

    @FXML
    void goToRegister(ActionEvent event) {
        // TODO: switch to registration scene
    }

    @FXML
    void goToResetPassword(ActionEvent event) {
        // TODO: switch to reset password scene
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
