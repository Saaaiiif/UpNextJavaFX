package controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

public class home {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button AddEvent;

    @FXML
    private Button AddProduct;

    @FXML
    private Text AuthenticatedUser;

    @FXML
    private Label EmailError;

    @FXML
    private Hyperlink EventLink;

    @FXML
    private Hyperlink Logout;

    @FXML
    private Label PasswordError;

    @FXML
    private Hyperlink ProductLink;

    @FXML
    private ImageView ProfileImage;

    @FXML
    private Hyperlink ProfileLink;

    @FXML
    private Hyperlink RegisterLink;

    @FXML
    private Hyperlink VerifiedArtistLink;

    @FXML
    private Button adminDashboardButton;

    @FXML
    private Text authentifiedFirstname;

    @FXML
    private TextField search;

    @FXML
    void GoToAdminDashboard(ActionEvent event) {

    }

    @FXML
    void GoToEvent(ActionEvent event) {

    }

    @FXML
    void GoToProduct(ActionEvent event) {

    }

    @FXML
    void GoToVerifiedArtist(ActionEvent event) {

    }

    @FXML
    void Logout(ActionEvent event) {

    }

    @FXML
    void goToProfile(ActionEvent event) {

    }

    @FXML
    void goToRegister(ActionEvent event) {

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
