package controllers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;
import edu.up_next.entities.User;
import edu.up_next.services.UserServices;
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
    @FXML private Button Cancel;

    private User currentUser;
    private File selectedImageFile;
    private final UserServices userServices = new UserServices();
    private static final int MAX_CHARS = 250;

    public void setUser(User user) {
        this.currentUser = user;
        initializeUI();
    }

    private void initializeUI() {
        if (currentUser == null) {
            System.out.println("No user provided to EditProfile controller");
            return;
        }

        firstnameField.setText(currentUser.getFirstname());
        lastnameField.setText(currentUser.getLastname());
        number.setText(String.valueOf(currentUser.getNum()));
        descriptionField.setText(currentUser.getDescription() != null ? currentUser.getDescription() : "");
        speciality.setValue(currentUser.getSpeciality() != null ? currentUser.getSpeciality() : "----------");

        counter.setText(descriptionField.getText().length() + "/" + MAX_CHARS);

        String imagePath = currentUser.getImage();
        if (imagePath != null && !imagePath.isEmpty()) {
            File imageFile = new File("D:/PI java/up-next/uploads/" + imagePath);
            if (imageFile.exists()) {
                Image image = new Image(imageFile.toURI().toString());
                ProfileImage1.setImage(image);
            }
        }

        String roles = currentUser.getRoles();
        boolean isArtist = roles != null && roles.contains("ROLE_ARTIST");
        specialityLabel.setVisible(isArtist);
        speciality.setVisible(isArtist);
        speciality.setDisable(!isArtist);
    }

    @FXML
    void uploadimage(ActionEvent event) {
        System.out.println("UploadImage button clicked: Opening FileChooser...");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        // Use mainPane to get the stage, ensuring we avoid null stage issues
        Stage stage = (Stage) mainPane.getScene().getWindow();
        if (stage == null) {
            System.err.println("Error: Stage is null. Cannot open FileChooser.");
            return;
        }

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            System.out.println("Image selected: " + file.getAbsolutePath());
            if (!file.exists()) {
                System.err.println("Error: Selected file does not exist: " + file.getAbsolutePath());
                return;
            }
            selectedImageFile = file;
            try {
                Image image = new Image(file.toURI().toString(), true); // Load image asynchronously
                ProfileImage1.setImage(image);
                System.out.println("Image displayed in UI successfully.");
            } catch (Exception e) {
                System.err.println("Error loading image into ImageView: " + e.getMessage());
            }
        } else {
            System.out.println("No image selected (FileChooser cancelled).");
        }
    }

    @FXML
    void UpdateProfile(ActionEvent event) {
        FirstnameError.setVisible(false);
        LastnameError.setVisible(false);

        boolean hasError = false;

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

        String userDesc = descriptionField.getText();
        int userNumber;
        try {
            userNumber = Integer.parseInt(number.getText());
        } catch (NumberFormatException e) {
            System.err.println("Invalid phone number format: " + number.getText());
            return;
        }
        String selectedSpeciality = speciality.isVisible() ? speciality.getValue() : currentUser.getSpeciality();
        String imageName = currentUser.getImage();

        try {
            if (selectedImageFile != null) {
                System.out.println("Processing new image: " + selectedImageFile.getAbsolutePath());
                File destDir = new File("D:/PI java/up-next/uploads");
                if (!destDir.exists()) {
                    System.out.println("Creating directory: " + destDir.getAbsolutePath());
                    destDir.mkdirs();
                }
                if (!destDir.canWrite()) {
                    System.err.println("Error: Cannot write to directory " + destDir.getAbsolutePath());
                    return;
                }
                imageName = System.currentTimeMillis() + "_" + selectedImageFile.getName();
                File destinationFile = new File(destDir, imageName);
                System.out.println("Saving image to: " + destinationFile.getAbsolutePath());
                Files.copy(selectedImageFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Image saved successfully!");
            } else {
                System.out.println("No new image selected; keeping existing image: " + imageName);
            }

            currentUser.setFirstname(userFirstname);
            currentUser.setLastname(userLastname);
            currentUser.setNum(userNumber);
            currentUser.setDescription(userDesc);
            currentUser.setSpeciality(selectedSpeciality);
            currentUser.setImage(imageName);

            userServices.updateUserProfile(currentUser);

            URL fxmlLocation = getClass().getResource("/profile.fxml");
            if (fxmlLocation == null) {
                System.err.println("Error: /profile.fxml not found in resources");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            profile profileController = loader.getController();
            profileController.setUser(currentUser);

            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

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
    void backToProfile(ActionEvent event) {
        try {
            URL fxmlLocation = getClass().getResource("/profile.fxml");
            if (fxmlLocation == null) {
                System.err.println("Error: /profile.fxml not found in resources");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            profile profileController = loader.getController();
            profileController.setUser(currentUser);

            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error loading profile page: " + e.getMessage());
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
        assert Cancel != null : "fx:id=\"Cancel\" was not injected: check your FXML file 'EditProfile.fxml'.";

        firstnameField.setEditable(true);
        lastnameField.setEditable(true);
        number.setEditable(true);
        descriptionField.setEditable(true);
        AddressField.setEditable(true);
        cityField.setEditable(true);
        countryField.setEditable(true);

        speciality.getItems().addAll("----------", "Musique", "Peinture", "Photographie", "Danse", "Animation", "Sculpture");
        speciality.setValue("----------");

        descriptionField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > MAX_CHARS) {
                descriptionField.setText(newValue.substring(0, MAX_CHARS));
            }
            counter.setText(descriptionField.getText().length() + "/" + MAX_CHARS);
        });

        number.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                number.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }
}