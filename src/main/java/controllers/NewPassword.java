package controllers;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;
import edu.up_next.tools.MyConnexion;
import at.favre.lib.crypto.bcrypt.BCrypt;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class NewPassword {

    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private PasswordField newPassword;
    @FXML private PasswordField confirme;
    @FXML private Label errorLabel;
    @FXML private Button resetPassword;
    @FXML private VBox passwordRulesBox;
    @FXML private Label lengthLabel;
    @FXML private Label letterLabel;
    @FXML private Label digitLabel;

    private String email;

    public void setEmail(String email) {
        this.email = email;
    }

    @FXML
    void resetPassword(ActionEvent event) {
        errorLabel.setVisible(false);
        String newPasswordText = newPassword.getText();
        String confirmPasswordText = confirme.getText();

        // Validate inputs
        if (newPasswordText.isEmpty() || confirmPasswordText.isEmpty()) {
            errorLabel.setText("Both fields are required.");
            errorLabel.setVisible(true);
            return;
        }
        if (!newPasswordText.equals(confirmPasswordText)) {
            errorLabel.setText("Passwords do not match.");
            errorLabel.setVisible(true);
            return;
        }
        // Check password strength
        if (!isPasswordStrong(newPasswordText)) {
            errorLabel.setText("Password does not meet requirements.");
            errorLabel.setVisible(true);
            return;
        }

        // Update the password and clear the reset code in the database
        try {
            MyConnexion db = new MyConnexion();
            Connection conn = db.getConnection();

            String query = "UPDATE user SET password = ?, reset_code = NULL, reset_code_expiry = NULL WHERE email = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            String hashedPassword = BCrypt.withDefaults().hashToString(13, newPasswordText.toCharArray());
            stmt.setString(1, hashedPassword);
            stmt.setString(2, email);
            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Password Reset Successful");
                alert.setHeaderText(null);
                alert.setContentText("Your password has been reset successfully. You can now log in with your new password.");
                alert.showAndWait();

                goToLogin(event);
            } else {
                errorLabel.setText("Failed to reset password. Try again.");
                errorLabel.setVisible(true);
            }

            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            errorLabel.setText("Database error.");
            errorLabel.setVisible(true);
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
        assert newPassword != null : "fx:id=\"newPassword\" was not injected: check your FXML file 'newPassword.fxml'.";
        assert confirme != null : "fx:id=\"confirme\" was not injected: check your FXML file 'newPassword.fxml'.";
        assert errorLabel != null : "fx:id=\"errorLabel\" was not injected: check your FXML file 'newPassword.fxml'.";
        assert resetPassword != null : "fx:id=\"resetPassword\" was not injected: check your FXML file 'newPassword.fxml'.";
        assert passwordRulesBox != null : "fx:id=\"passwordRulesBox\" was not injected: check your FXML file 'newPassword.fxml'.";
        assert lengthLabel != null : "fx:id=\"lengthLabel\" was not injected: check your FXML file 'newPassword.fxml'.";
        assert letterLabel != null : "fx:id=\"letterLabel\" was not injected: check your FXML file 'newPassword.fxml'.";
        assert digitLabel != null : "fx:id=\"digitLabel\" was not injected: check your FXML file 'newPassword.fxml'.";

        // Toggle visibility of passwordRulesBox based on focus
        ChangeListener<Boolean> focusListener = (obs, oldVal, newVal) -> {
            passwordRulesBox.setVisible(newPassword.isFocused());
        };
        newPassword.focusedProperty().addListener(focusListener);

        // Update validation live as user types
        newPassword.textProperty().addListener((obs, oldText, newText) -> {
            validatePassword(newText);
        });

        // Initially hide the passwordRulesBox
        passwordRulesBox.setVisible(false);
    }

    private void validatePassword(String password) {
        lengthLabel.setText(password.length() >= 8 ? "✅ At least 8 characters" : "❌ At least 8 characters");
        letterLabel.setText(password.matches(".*[a-zA-Z].*") ? "✅ At least 1 letter" : "❌ At least 1 letter");
        digitLabel.setText(password.matches(".*\\d.*") ? "✅ At least 1 number" : "❌ At least 1 number");
    }

    private boolean isPasswordStrong(String password) {
        return password.length() >= 8 &&
                password.matches(".*[a-zA-Z].*") &&
                password.matches(".*\\d.*");
    }
}