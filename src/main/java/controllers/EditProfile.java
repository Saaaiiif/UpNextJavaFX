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
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class EditProfile {

    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private TextField AddressField;
    @FXML private Label FirstnameError;
    @FXML private Label LastnameError;
    @FXML private ImageView ProfileImage1;
    @FXML private Button SaveChanges;
    @FXML private Button UploadImage;
    @FXML private Button addLocationButton;
    @FXML private TextField cityField;
    @FXML private Label counter;
    @FXML private TextField countryField;
    @FXML private TextField descriptionField;
    @FXML private TextField firstnameField;
    @FXML private TextField lastnameField;
    @FXML private AnchorPane mainPane;
    @FXML private TextField number;
    @FXML private ComboBox<String> pay;
    @FXML private ComboBox<String> speciality;
    @FXML private Label specialityLabel;

    // Email field (non-editable)
    @FXML private TextField emailField;

    // Password field (non-editable)
    @FXML private TextField passwordField;

    private User currentUser;
    private File selectedImageFile;
    private final UserServices userServices = new UserServices();
    private static final int MAX_CHARS = 250;

    // Method to set the current user
    public void setUser(User user) {
        this.currentUser = user;
        initializeUI();
    }

    private void initializeUI() {
        if (currentUser == null) {
            System.out.println("No user provided to EditProfile controller");
            return;
        }

        // Populate fields with current user data
        firstnameField.setText(currentUser.getFirstname());
        lastnameField.setText(currentUser.getLastname());
        emailField.setText(currentUser.getEmail());
        emailField.setEditable(false); // Email is not editable
        passwordField.setText("********"); // Placeholder for password (not editable)
        passwordField.setEditable(false); // Password is not editable
        number.setText(String.valueOf(currentUser.getNum()));
        descriptionField.setText(currentUser.getDescription() != null ? currentUser.getDescription() : "");
        speciality.setValue(currentUser.getSpeciality() != null ? currentUser.getSpeciality() : "----------");

        // Initialize character counter for description
        counter.setText(descriptionField.getText().length() + "/" + MAX_CHARS);

        // Load current profile image
        String imagePath = currentUser.getImage();
        if (imagePath != null && !imagePath.isEmpty()) {
            File imageFile = new File("D:/PI java/up-next/uploads/" + imagePath);
            if (imageFile.exists()) {
                Image image = new Image(imageFile.toURI().toString());
                ProfileImage1.setImage(image);
            }
        }

        // Speciality field visibility and editability
        String roles = currentUser.getRoles();
        boolean isArtist = roles != null && roles.contains("ROLE_ARTIST");
        specialityLabel.setVisible(isArtist);
        speciality.setVisible(isArtist);
        speciality.setDisable(!isArtist); // Disable if not an artist
    }

    @FXML
    void UpdateProfile(ActionEvent event) {
        // Reset error labels
        FirstnameError.setVisible(false);
        LastnameError.setVisible(false);

        boolean hasError = false;

        // Validate firstname and lastname
        String userFirstname = firstnameField.getText().trim();
        String userLastname = lastnameField.getText().trim();

        if (userFirstname.isEmpty()) {
            FirstnameError.setText("Required");
            FirstnameError.setVisible(true);
            hasError = true;
        }

        if (userLastname.isEmpty()) {
            LastnameError.setText("Required");
            LastnameError.setVisible(true);
            hasError = true;
        }

        if (hasError) return;

        // Collect updated data
        String userDesc = descriptionField.getText();
        int userNumber = Integer.parseInt(number.getText());
        String selectedSpeciality = speciality.isVisible() ? speciality.getValue() : currentUser.getSpeciality();
        String imageName = currentUser.getImage();

        try {
            // Handle image upload if a new image is selected
            if (selectedImageFile != null) {
                File destDir = new File("D:/PI java/up-next/uploads");
                if (!destDir.exists()) destDir.mkdirs();
                imageName = System.currentTimeMillis() + "_" + selectedImageFile.getName();
                File destinationFile = new File(destDir, imageName);
                Files.copy(selectedImageFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            // Update user object
            currentUser.setFirstname(userFirstname);
            currentUser.setLastname(userLastname);
            currentUser.setNum(userNumber);
            currentUser.setDescription(userDesc);
            currentUser.setSpeciality(selectedSpeciality);
            currentUser.setImage(imageName);

            // Update user in the database
            userServices.updateUser(currentUser);

            // Navigate back to profile page with success message
            URL fxmlLocation = getClass().getResource("/profile.fxml");
            if (fxmlLocation == null) {
                System.err.println("Error: /profile.fxml not found in resources");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            // Pass the updated user back to the profile controller
            profile profileController = loader.getController();
            profileController.setUser(currentUser);

            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

            // Show success message
            System.out.println("User data updated successfully!");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("❌ Error uploading image: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error updating user: " + e.getMessage());
        }
    }

    @FXML
    void select(ActionEvent event) {
        // Not needed since speciality is set based on the user's role and not editable
    }

    @FXML
    void upload(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File file = fileChooser.showOpenDialog(UploadImage.getScene().getWindow());
        if (file != null) {
            selectedImageFile = file;
            Image image = new Image(file.toURI().toString());
            ProfileImage1.setImage(image);
        }
    }

    @FXML
    void initialize() {
        assert AddressField != null : "fx:id=\"AddressField\" was not injected: check your FXML file 'EditProfile.fxml'.";
        assert FirstnameError != null : "fx:id=\"FirstnameError\" was not injected: check your FXML file 'EditProfile.fxml'.";
        assert LastnameError != null : "fx:id=\"LastnameError\" was not injected: check your FXML file 'EditProfile.fxml'.";
        assert ProfileImage1 != null : "fx:id=\"ProfileImage1\" was not injected: check your FXML file 'EditProfile.fxml'.";
        assert SaveChanges != null : "fx:id=\"SaveChanges\" was not injected: check your FXML file 'EditProfile.fxml'.";
        assert UploadImage != null : "fx:id=\"UploadImage\" was not injected: check your FXML file 'EditProfile.fxml'.";
        assert addLocationButton != null : "fx:id=\"addLocationButton\" was not injected: check your FXML file 'EditProfile.fxml'.";
        assert cityField != null : "fx:id=\"cityField\" was not injected: check your FXML file 'EditProfile.fxml'.";
        assert counter != null : "fx:id=\"counter\" was not injected: check your FXML file 'EditProfile.fxml'.";
        assert countryField != null : "fx:id=\"countryField\" was not injected: check your FXML file 'EditProfile.fxml'.";
        assert descriptionField != null : "fx:id=\"descriptionField\" was not injected: check your FXML file 'EditProfile.fxml'.";
        assert firstnameField != null : "fx:id=\"firstnameField\" was not injected: check your FXML file 'EditProfile.fxml'.";
        assert lastnameField != null : "fx:id=\"lastnameField\" was not injected: check your FXML file 'EditProfile.fxml'.";
        assert mainPane != null : "fx:id=\"mainPane\" was not injected: check your FXML file 'EditProfile.fxml'.";
        assert number != null : "fx:id=\"number\" was not injected: check your FXML file 'EditProfile.fxml'.";
        assert pay != null : "fx:id=\"pay\" was not injected: check your FXML file 'EditProfile.fxml'.";
        assert speciality != null : "fx:id=\"speciality\" was not injected: check your FXML file 'EditProfile.fxml'.";
        assert specialityLabel != null : "fx:id=\"specialityLabel\" was not injected: check your FXML file 'EditProfile.fxml'.";
        assert emailField != null : "fx:id=\"emailField\" was not injected: check your FXML file 'EditProfile.fxml'.";
        assert passwordField != null : "fx:id=\"passwordField\" was not injected: check your FXML file 'EditProfile.fxml'.";

        // Initialize speciality options
        speciality.getItems().addAll("----------", "Musique", "Peinture", "Photographie", "Danse", "Animation", "Sculpture");
        speciality.setValue("----------");

        // Character counter for description
        descriptionField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > MAX_CHARS) {
                descriptionField.setText(newValue.substring(0, MAX_CHARS));
            }
            counter.setText(descriptionField.getText().length() + "/" + MAX_CHARS);
        });

        // Restrict phone number to digits only
        number.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                number.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }
}