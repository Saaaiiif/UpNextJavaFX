package controllers;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import edu.up_next.entities.User;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class UserView {

    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private Text AuthenticatedUser;
    @FXML private Hyperlink EventLink;
    @FXML private Hyperlink HomeLink;
    @FXML private Hyperlink Logout;
    @FXML private Hyperlink ProductLink;
    @FXML private ImageView ProfileImage;
    @FXML private ImageView ProfileImage1;
    @FXML private Hyperlink ProfileLink;
    @FXML private Hyperlink QuizLink;
    @FXML private Hyperlink VerifiedArtistLink1;
    @FXML private Button contact;
    @FXML private TextField descriptionField;
    @FXML private TextField emailField;
    @FXML private TextField firstnameField;
    @FXML private TextField lastnameField;
    @FXML private AnchorPane mainPane;
    @FXML private TextField phoneField;
    @FXML private TextField roleField;
    @FXML private TextField search;
    @FXML private TextField specialityField;
    @FXML private Label specialityLabel;
    @FXML private ImageView verifiedTik;

    private User loggedInUser; // User who is currently logged in (for sidebar)
    private User viewedUser;   // User whose profile is being viewed (for main content)

    // Method to set both the logged-in user and the viewed user
    public void setUsers(User loggedInUser, User viewedUser) {
        this.loggedInUser = loggedInUser;
        this.viewedUser = viewedUser;
        updateUI();
    }

    private void updateUI() {
        // Update sidebar with logged-in user's info
        if (loggedInUser == null) {
            System.out.println("No logged-in user provided to UserView controller");
            return;
        }
        AuthenticatedUser.setText(loggedInUser.getFirstname() + " " + loggedInUser.getLastname());
        String loggedInImagePath = loggedInUser.getImage();
        if (loggedInImagePath != null && !loggedInImagePath.isEmpty()) {
            File imageFile = new File("D:/PI java/up-next/uploads/" + loggedInImagePath);
            if (imageFile.exists()) {
                Image image = new Image(imageFile.toURI().toString());
                ProfileImage.setImage(image);
            } else {
                System.out.println("Image file not found for logged-in user: " + loggedInImagePath);
            }
        } else {
            System.out.println("No image path provided for logged-in user");
        }

        // Update main content with viewed user's info
        if (viewedUser == null) {
            System.out.println("No viewed user provided to UserView controller");
            return;
        }
        firstnameField.setText(viewedUser.getFirstname());
        lastnameField.setText(viewedUser.getLastname());
        emailField.setText(viewedUser.getEmail());
        descriptionField.setText(viewedUser.getDescription() != null ? viewedUser.getDescription() : "");
        phoneField.setText(String.valueOf(viewedUser.getNum()));

        // Handle roles
        String roles = viewedUser.getRoles().replaceAll("[\\[\\]\"ROLE_]", "");
        roleField.setText(roles);

        // Show speciality only for artists
        boolean isArtist = viewedUser.getRoles().contains("ROLE_ARTIST");
        specialityLabel.setVisible(isArtist);
        specialityLabel.setManaged(isArtist);
        specialityField.setVisible(isArtist);
        specialityField.setManaged(isArtist);
        if (isArtist) {
            specialityField.setText(viewedUser.getSpeciality() != null ? viewedUser.getSpeciality() : "");
        }

        // Show verified tick only for verified users
        verifiedTik.setVisible(viewedUser.isIs_verified());
        verifiedTik.setManaged(viewedUser.isIs_verified());

        // Update profile image in the main content area
        String viewedImagePath = viewedUser.getImage();
        if (viewedImagePath != null && !viewedImagePath.isEmpty()) {
            File imageFile = new File("D:/PI java/up-next/uploads/" + viewedImagePath);
            if (imageFile.exists()) {
                Image image = new Image(imageFile.toURI().toString());
                ProfileImage1.setImage(image);
            } else {
                System.out.println("Image file not found for viewed user: " + viewedImagePath);
            }
        } else {
            System.out.println("No image path provided for viewed user");
        }
    }

    @FXML
    void GoToContact(ActionEvent event) {
        // TODO: Implement contact navigation
    }

    @FXML
    void GoToEvent(ActionEvent event) {
        // TODO: Implement event navigation
    }

    @FXML
    void GoToProduct(ActionEvent event) {
        // TODO: Implement product navigation
    }

    @FXML
    void GoToQuiz(ActionEvent event) {
        // TODO: Implement quiz navigation
    }

    @FXML
    void GoToVerifiedArtist(ActionEvent event) {
        // TODO: Implement verified artist navigation
    }

    @FXML
    void Logout(ActionEvent event) {
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
    void goToHome(ActionEvent event) {
        try {
            URL fxmlLocation = getClass().getResource("/home.fxml");
            if (fxmlLocation == null) {
                System.err.println("Error: /home.fxml not found in resources");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();
            home controller = loader.getController();
            controller.setUser(loggedInUser);
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error loading home page: " + e.getMessage());
        }
    }

    @FXML
    void goToProfile(ActionEvent event) {
        try {
            URL fxmlLocation = getClass().getResource("/profile.fxml");
            if (fxmlLocation == null) {
                System.err.println("Error: /profile.fxml not found in resources");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();
            profile controller = loader.getController();
            controller.setUser(loggedInUser);
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
        assert AuthenticatedUser != null : "fx:id=\"AuthenticatedUser\" was not injected: check your FXML file 'UserView.fxml'.";
        assert EventLink != null : "fx:id=\"EventLink\" was not injected: check your FXML file 'UserView.fxml'.";
        assert HomeLink != null : "fx:id=\"HomeLink\" was not injected: check your FXML file 'UserView.fxml'.";
        assert Logout != null : "fx:id=\"Logout\" was not injected: check your FXML file 'UserView.fxml'.";
        assert ProductLink != null : "fx:id=\"ProductLink\" was not injected: check your FXML file 'UserView.fxml'.";
        assert ProfileImage != null : "fx:id=\"ProfileImage\" was not injected: check your FXML file 'UserView.fxml'.";
        assert ProfileImage1 != null : "fx:id=\"ProfileImage1\" was not injected: check your FXML file 'UserView.fxml'.";
        assert ProfileLink != null : "fx:id=\"ProfileLink\" was not injected: check your FXML file 'UserView.fxml'.";
        assert QuizLink != null : "fx:id=\"QuizLink\" was not injected: check your FXML file 'UserView.fxml'.";
        assert VerifiedArtistLink1 != null : "fx:id=\"VerifiedArtistLink1\" was not injected: check your FXML file 'UserView.fxml'.";
        assert contact != null : "fx:id=\"contact\" was not injected: check your FXML file 'UserView.fxml'.";
        assert descriptionField != null : "fx:id=\"descriptionField\" was not injected: check your FXML file 'UserView.fxml'.";
        assert emailField != null : "fx:id=\"emailField\" was not injected: check your FXML file 'UserView.fxml'.";
        assert firstnameField != null : "fx:id=\"firstnameField\" was not injected: check your FXML file 'UserView.fxml'.";
        assert lastnameField != null : "fx:id=\"lastnameField\" was not injected: check your FXML file 'UserView.fxml'.";
        assert mainPane != null : "fx:id=\"mainPane\" was not injected: check your FXML file 'UserView.fxml'.";
        assert phoneField != null : "fx:id=\"phoneField\" was not injected: check your FXML file 'UserView.fxml'.";
        assert roleField != null : "fx:id=\"roleField\" was not injected: check your FXML file 'UserView.fxml'.";
        assert search != null : "fx:id=\"search\" was not injected: check your FXML file 'UserView.fxml'.";
        assert specialityField != null : "fx:id=\"specialityField\" was not injected: check your FXML file 'UserView.fxml'.";
        assert specialityLabel != null : "fx:id=\"specialityLabel\" was not injected: check your FXML file 'UserView.fxml'.";
        assert verifiedTik != null : "fx:id=\"verifiedTik\" was not injected: check your FXML file 'UserView.fxml'.";
    }
}