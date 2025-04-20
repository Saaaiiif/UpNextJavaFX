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
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class home {
    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private Button AddEvent;
    @FXML private Button AddProduct;
    @FXML private Text AuthenticatedUser;
    @FXML private Label EmailError;
    @FXML private Hyperlink EventLink;
    @FXML private Hyperlink Logout;
    @FXML private Label PasswordError;
    @FXML private Hyperlink ProductLink;
    @FXML private ImageView ProfileImage;
    @FXML private Hyperlink ProfileLink;
    @FXML private Hyperlink RegisterLink;
    @FXML private Hyperlink VerifiedArtistLink;
    @FXML private Button adminDashboardButton;
    @FXML private Text authentifiedFirstname;
    @FXML private TextField search;

    private User currentUser;

    public void setUser(User user) {
        this.currentUser = user;
        updateUI();
    }

    private void updateUI() {
        if (currentUser == null) {
            System.out.println("No user provided to home controller");
            return;
        }

        // Set greeting
        authentifiedFirstname.setText( currentUser.getFirstname());

        // Set full name
        AuthenticatedUser.setText(currentUser.getFirstname() + " " + currentUser.getLastname());

        // Set profile image
        String imagePath = currentUser.getImage();
        if (imagePath != null && !imagePath.isEmpty()) {
            File imageFile = new File("D:/PI java/up-next/uploads/" + imagePath);
            if (imageFile.exists()) {
                Image image = new Image(imageFile.toURI().toString());
                ProfileImage.setImage(image);
            } else {
                System.out.println("Image file not found: " + imagePath);
            }
        } else {
            System.out.println("No image path provided for user");
        }

        // Handle roles
        String roles = currentUser.getRoles();
        boolean isAdmin = roles != null && roles.contains("ROLE_ADMIN");
        boolean isArtist = roles != null && roles.contains("ROLE_ARTIST");

        // Show admin button only for ROLE_ADMIN
        adminDashboardButton.setVisible(isAdmin);
        adminDashboardButton.setManaged(isAdmin);

        // Show add event/product buttons only for ROLE_ARTIST
        AddEvent.setVisible(isArtist);
        AddEvent.setManaged(isArtist);
        AddProduct.setVisible(isArtist);
        AddProduct.setManaged(isArtist);
    }

    @FXML
    void GoToAdminDashboard(ActionEvent event) {
        // TODO: Implement admin dashboard navigation
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
            controller.setUser(currentUser);
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
    void initialize() {
        assert AddEvent != null : "fx:id=\"AddEvent\" was not injected: check your FXML file 'home.fxml'.";
        assert AddProduct != null : "fx:id=\"AddProduct\" was not injected: check your FXML file 'home.fxml'.";
        assert AuthenticatedUser != null : "fx:id=\"AuthenticatedUser\" was not injected: check your FXML file 'home.fxml'.";
        assert EmailError != null : "fx:id=\"EmailError\" was not injected: check your FXML file 'home.fxml'.";
        assert EventLink != null : "fx:id=\"EventLink\" was not injected: check your FXML file 'home.fxml'.";
        assert Logout != null : "fx:id=\"Logout\" was not injected: check your FXML file 'home.fxml'.";
        assert PasswordError != null : "fx:id=\"PasswordError\" was not injected: check your FXML file 'home.fxml'.";
        assert ProductLink != null : "fx:id=\"ProductLink\" was not injected: check your FXML file 'home.fxml'.";
        assert ProfileImage != null : "fx:id=\"ProfileImage\" was not injected: check your FXML file 'home.fxml'.";
        assert ProfileLink != null : "fx:id=\"ProfileLink\" was not injected: check your FXML file 'home.fxml'.";
        assert RegisterLink != null : "fx:id=\"RegisterLink\" was not injected: check your FXML file 'home.fxml'.";
        assert VerifiedArtistLink != null : "fx:id=\"VerifiedArtistLink\" was not injected: check your FXML file 'home.fxml'.";
        assert adminDashboardButton != null : "fx:id=\"adminDashboardButton\" was not injected: check your FXML file 'home.fxml'.";
        assert authentifiedFirstname != null : "fx:id=\"authentifiedFirstname\" was not injected: check your FXML file 'home.fxml'.";
        assert search != null : "fx:id=\"search\" was not injected: check your FXML file 'home.fxml'.";
    }
}