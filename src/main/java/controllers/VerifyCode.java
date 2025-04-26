package controllers;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import edu.up_next.tools.MyConnexion;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class VerifyCode {

    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private Hyperlink RegisterLink;
    @FXML private TextField code;
    @FXML private Label errorLabel;

    @FXML
    private Button verifiedCode;

    private String email;

    // Add the setEmail method to receive the email from ResetPasswordRequest
    public void setEmail(String email) {
        this.email = email;
    }

    @FXML
    void goToNewPassword(ActionEvent event) {
        errorLabel.setVisible(false);

        String enteredCode = code.getText().trim();
        if (enteredCode.isEmpty()) {
            errorLabel.setText("Code is required.");
            errorLabel.setVisible(true);
            return;
        }

        // Retrieve the reset code and expiry from the database
        String storedCode = null;
        LocalDateTime codeExpiry = null;

        try {
            MyConnexion db = new MyConnexion();
            Connection conn = db.getConnection();
            String query = "SELECT reset_code, reset_code_expiry FROM user WHERE email = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                storedCode = rs.getString("reset_code");
                codeExpiry = rs.getTimestamp("reset_code_expiry") != null ? rs.getTimestamp("reset_code_expiry").toLocalDateTime() : null;
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            errorLabel.setText("Database error.");
            errorLabel.setVisible(true);
            return;
        }

        // Validate the code
        if (storedCode == null || codeExpiry == null) {
            errorLabel.setText("No reset code found. Request a new code.");
            errorLabel.setVisible(true);
            return;
        }

        if (LocalDateTime.now().isAfter(codeExpiry)) {
            errorLabel.setText("Reset code has expired.");
            errorLabel.setVisible(true);
            return;
        }

        if (!enteredCode.equals(storedCode)) {
            errorLabel.setText("Invalid code.");
            errorLabel.setVisible(true);
            return;
        }

        // Code is valid, navigate to the new password page
        try {
            URL fxmlLocation = getClass().getResource("/newPassword.fxml");
            if (fxmlLocation == null) {
                System.err.println("Error: /newPassword.fxml not found in resources");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            NewPassword controller = (NewPassword) loader.getController();
            controller.setEmail(email);

            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error loading new password page.");
            errorLabel.setVisible(true);
        }
    }

    @FXML
    void goToRegister(ActionEvent event) {
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
    void goToLogin(ActionEvent event) {
        try {
            URL fxmlLocation = getClass().getResource("/login.fxml");
            if (fxmlLocation == null) {
                System.err.println("Error: /login.fxml not found in resources");
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
            System.err.println("❌ Error loading login page: " + e.getMessage());
        }
    }

    @FXML
    void initialize() {
        assert RegisterLink != null : "fx:id=\"RegisterLink\" was not injected: check your FXML file 'verifyCode.fxml'.";
        assert code != null : "fx:id=\"code\" was not injected: check your FXML file 'verifyCode.fxml'.";
        assert errorLabel != null : "fx:id=\"errorLabel\" was not injected: check your FXML file 'verifyCode.fxml'.";
    }
}