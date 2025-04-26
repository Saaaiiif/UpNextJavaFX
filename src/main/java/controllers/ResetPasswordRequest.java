package controllers;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.ResourceBundle;
import edu.up_next.tools.MyConnexion;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class ResetPasswordRequest {

    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private Hyperlink RegisterLink;
    @FXML private TextField email;
    @FXML private Label emailErrorLabel;
    @FXML private Button emailLink;

    @FXML
    void requestReset(ActionEvent event) {
        emailErrorLabel.setVisible(false);
        String emailInput = email.getText().trim();

        if (emailInput.isEmpty()) {
            emailErrorLabel.setText("Email is required.");
            emailErrorLabel.setVisible(true);
            return;
        }

        // Check if email exists in the database and retrieve first name and last name
        String generatedCode = String.format("%06d", (int)(Math.random() * 1000000));
        LocalDateTime codeExpiry = LocalDateTime.now().plusMinutes(10); // Code valid for 10 minutes
        String firstName = null;
        String lastName = null;

        try {
            MyConnexion db = new MyConnexion();
            Connection conn = db.getConnection();

            // Check if email exists and fetch first name and last name
            String query = "SELECT firstname, lastname FROM user WHERE email = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, emailInput);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                emailErrorLabel.setText("Email not found.");
                emailErrorLabel.setVisible(true);
                rs.close();
                stmt.close();
                conn.close();
                return;
            }

            // Retrieve first name and last name
            firstName = rs.getString("firstname");
            lastName = rs.getString("lastname");
            rs.close();
            stmt.close();

            // Store the reset code and expiry in the database
            String updateQuery = "UPDATE user SET reset_code = ?, reset_code_expiry = ? WHERE email = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
            updateStmt.setString(1, generatedCode);
            updateStmt.setObject(2, codeExpiry);
            updateStmt.setString(3, emailInput);
            updateStmt.executeUpdate();
            updateStmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            emailErrorLabel.setText("Database error.");
            emailErrorLabel.setVisible(true);
            return;
        }

        // Send the reset code via email
        boolean emailSent = sendResetEmail(emailInput, generatedCode, firstName, lastName);
        if (emailSent) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Reset Code Sent");
            alert.setHeaderText(null);
            alert.setContentText("A reset code has been sent to your email.");
            alert.showAndWait();
            // Navigate to the code verification page
            navigateToVerifyCode(event, emailInput);
        } else {
            emailErrorLabel.setText("Failed to send reset email. Try again later.");
            emailErrorLabel.setVisible(true);
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
        assert RegisterLink != null : "fx:id=\"RegisterLink\" was not injected: check your FXML file 'resetPassword.fxml'.";
        assert email != null : "fx:id=\"email\" was not injected: check your FXML file 'resetPassword.fxml'.";
        assert emailErrorLabel != null : "fx:id=\"emailErrorLabel\" was not injected: check your FXML file 'resetPassword.fxml'.";
    }

    private boolean sendResetEmail(String email, String code, String firstName, String lastName) {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        String senderEmail = "nesrineriahi216@gmail.com"; // Replace with your email
        String senderPassword = "ajtc aupj oeuo sqhw"; // Replace with your app-specific password

        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject("Password Reset Code - Up Next");

            // Customize the email body with HTML
            String htmlContent = "<html>" +
                    "<body style='font-family: Arial, sans-serif; color: #333;'>" +
                    "<h2 style='color: #ff3333;'>Password Reset Request</h2>" +
                    "<p>Dear <strong>" + firstName + " " + lastName + "</strong>,</p>" +
                    "<p>We received a request to reset your password for your Up Next account. " +
                    "Please use the following code to reset your password:</p>" +
                    "<h3 style='color: #ff3333;'>" + code + "</h3>" +
                    "<p>This code is valid for <strong>10 minutes</strong>. If you did not request a password reset, please ignore this email.</p>" +
                    "<p>Thank you,<br>The Up Next Team</p>" +
                    "</body>" +
                    "</html>";

            message.setContent(htmlContent, "text/html");

            Transport.send(message);
            System.out.println("Reset code email sent to " + email + ": " + code);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void navigateToVerifyCode(ActionEvent event, String email) {
        try {
            URL fxmlLocation = getClass().getResource("/verifyCode.fxml");
            if (fxmlLocation == null) {
                System.err.println("Error: /verifyCode.fxml not found in resources");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();
            VerifyCode controller = (VerifyCode) loader.getController();
            controller.setEmail(email);
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error loading verify code page: " + e.getMessage());
        }
    }
}