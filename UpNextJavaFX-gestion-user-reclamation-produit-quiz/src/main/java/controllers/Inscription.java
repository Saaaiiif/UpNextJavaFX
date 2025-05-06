package controllers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;

import edu.up_next.entities.User;
import edu.up_next.services.UserServices;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import at.favre.lib.crypto.bcrypt.BCrypt;
import javafx.stage.Stage;

public class Inscription {

    @FXML private ResourceBundle resources;
    @FXML private URL location;

    @FXML private TextField description;
    @FXML private TextField email;
    @FXML private PasswordField password;
    @FXML private TextField firstname;
    @FXML private TextField lastname;
    @FXML private TextField number;
    @FXML private ComboBox<String> pay;
    @FXML private ComboBox<String> roles;
    @FXML private ComboBox<String> speciality;
    @FXML private Button image;
    @FXML private Label imagePathLabel;

    @FXML private Label emailErrorLabel;
    @FXML private Label emailrequireerror;
    @FXML private Label passwordErrorLabel;
    @FXML private Label passwordrequireerror;
    @FXML private Label firstnamerequireerror;
    @FXML private Label lastnamerequireerror;
    @FXML private Label rolerequireerror;
    @FXML private Label specialityrequireerror;
    @FXML private Hyperlink loginLink;
    @FXML private Button register;

    int maxChars = 250;

    @FXML
    private Label charCountLabel;

    @FXML private VBox passwordRulesBox;
    @FXML private Label lengthLabel, letterLabel, digitLabel;

    private File selectedImageFile;
    private final UserServices us = new UserServices();

    @FXML
    void SignUp(ActionEvent event) {
        // Reset all error labels
        emailErrorLabel.setVisible(false);
        emailrequireerror.setVisible(false);
        passwordErrorLabel.setVisible(false);
        passwordrequireerror.setVisible(false);
        firstnamerequireerror.setVisible(false);
        lastnamerequireerror.setVisible(false);
        rolerequireerror.setVisible(false);
        specialityrequireerror.setVisible(false);
        passwordRulesBox.setVisible(false);

        boolean hasError = false;

        String userEmail = email.getText().trim();
        String userPassword = password.getText();
        String userFirstname = firstname.getText().trim();
        String userLastname = lastname.getText().trim();
        String selectedRole = roles.getValue() != null ? roles.getValue() : "";
        String selectedSpeciality = selectedRole.equalsIgnoreCase("client") ? "----------" : speciality.getValue();

        // === Input validation ===
        if (userEmail.isEmpty()) {
            emailrequireerror.setText("Email is required.");
            emailrequireerror.setVisible(true);
            hasError = true;
        } else if (us.emailExists(userEmail)) {
            emailErrorLabel.setText("*This email is already in use");
            emailErrorLabel.setVisible(true);
            hasError = true;
        }

        if (userPassword.isEmpty()) {
            passwordrequireerror.setText("Password is required.");
            passwordrequireerror.setVisible(true);
            hasError = true;
        } else if (userPassword.length() < 8) {
            passwordErrorLabel.setText("Password must be at least 8 characters.");
            passwordErrorLabel.setVisible(true);
            passwordErrorLabel.setVisible(true);
            hasError = true;
        }

        if (userFirstname.isEmpty()) {
            firstnamerequireerror.setText("First name is required.");
            firstnamerequireerror.setVisible(true);
            hasError = true;
        }

        if (userLastname.isEmpty()) {
            lastnamerequireerror.setText("Last name is required.");
            lastnamerequireerror.setVisible(true);
            hasError = true;
        }

        if (selectedRole.isEmpty()) {
            rolerequireerror.setText("Select a role.");
            rolerequireerror.setVisible(true);
            hasError = true;
        }

        if (selectedRole.equalsIgnoreCase("Artist") && (selectedSpeciality.isEmpty() || selectedSpeciality.equals("----------"))) {
            specialityrequireerror.setText("Speciality is required for artists.");
            specialityrequireerror.setVisible(true);
            hasError = true;
        }

        if (hasError) return;

        // Collect additional fields
        int userNumber = Integer.parseInt(number.getText());
        String userDesc = description.getText();
        String hashedPassword = BCrypt.withDefaults().hashToString(13, userPassword.toCharArray());
        String formattedRole = "[\"ROLE_" + selectedRole.toUpperCase() + "\"]";
        String imageName = null;

        try {
            // Copy image if selected
            if (selectedImageFile != null) {
                File destDir = new File("uploads");//a changer with the symfony folder
                if (!destDir.exists()) destDir.mkdirs();

                imageName = System.currentTimeMillis() + "_" + selectedImageFile.getName();
                File destinationFile = new File(destDir, imageName);
                Files.copy(selectedImageFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                imagePathLabel.setText("Image uploaded to: " + selectedImageFile.getAbsolutePath());
            } else {
                imagePathLabel.setText("No image selected.");
            }

            // Create and save user
            User user = new User();
            user.setId(0);
            user.setEmail(userEmail);
            user.setPassword(hashedPassword);
            user.setFirstname(userFirstname);
            user.setLastname(userLastname);
            user.setNum(userNumber);
            user.setDescription(userDesc);
            user.setRoles(formattedRole);
            user.setSpeciality(selectedSpeciality);
            user.setImage(imageName);
            user.setIs_active(true);
            user.setIs_verified(false);

            us.addUser1(user);

            System.out.println("User added successfully!");

            // Navigate to login page after successful registration
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
            imagePathLabel.setText("Registration failed. Check the data.");
        }
    }

    @FXML
    void upload(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = fileChooser.showOpenDialog(image.getScene().getWindow());

        if (file != null) {
            selectedImageFile = file;
            imagePathLabel.setText(file.getAbsolutePath());
        } else {
            imagePathLabel.setText("No image selected.");
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
    void select(ActionEvent event) {
        String selectedRole = roles.getValue();

        if ("Client".equalsIgnoreCase(selectedRole)) {
            speciality.setValue("----------");
            speciality.setDisable(true);
        } else {
            speciality.setDisable(false);
        }
    }

    @FXML
    void initialize() {
        roles.getItems().addAll("Artist", "Client");
        speciality.getItems().addAll("----------", "Musique", "Peinture", "Photographie", "Danse", "Animation", "Sculpture");
        speciality.setValue("----------"); // Default value

        // Initial count of description
        charCountLabel.setText("0/" + maxChars);

        // Add listener to update on text change
        description.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > maxChars) {
                description.setText(newValue.substring(0, maxChars)); // trim if over limit
            }
            charCountLabel.setText(description.getText().length() + "/" + maxChars);
        });

        // Allow only digits in the phone field
        number.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                number.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        // Toggle visibility based on which field is focused
        ChangeListener<Boolean> focusListener = (obs, oldVal, newVal) -> {
            if (password.isFocused()) {
                passwordRulesBox.setVisible(true);
            } else {
                passwordRulesBox.setVisible(false);
            }
        };
        password.focusedProperty().addListener(focusListener);

        // Update validation live as user types
        password.textProperty().addListener((obs, oldText, newText) -> {
            validatePassword(newText);
        });
    }

    private void validatePassword(String password) {
        lengthLabel.setText(password.length() >= 8 ? "✅ At least 8 characters" : "❌ At least 8 characters");
        letterLabel.setText(password.matches(".*[a-zA-Z].*") ? "✅ At least 1 letter" : "❌ At least 1 letter");
        digitLabel.setText(password.matches(".*\\d.*") ? "✅ At least 1 number" : "❌ At least 1 number");
    }
}