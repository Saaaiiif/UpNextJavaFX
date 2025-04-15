package controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

public class Admin {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Text AdminFirstname;

    @FXML
    private Text AuthenticatedUser;

    @FXML
    private Hyperlink Carpooling;

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
    private Hyperlink QuizManagement;

    @FXML
    private Hyperlink Reclamation;

    @FXML
    private Hyperlink UsersManagement;

    @FXML
    private TextField search;

    @FXML
    private BarChart<?, ?> usersChart;

    @FXML
    void GoToCarpooling(ActionEvent event) {

    }

    @FXML
    void GoToEvent(ActionEvent event) {

    }

    @FXML
    void GoToProduct(ActionEvent event) {

    }

    @FXML
    void GoToQuizManagement(ActionEvent event) {

    }

    @FXML
    void GoToReclamation(ActionEvent event) {

    }

    @FXML
    void GoToUsersManagement(ActionEvent event) {

    }

    @FXML
    void Logout(ActionEvent event) {

    }

    @FXML
    void goToProfile(ActionEvent event) {

    }

    @FXML
    void initialize() {
        assert AdminFirstname != null : "fx:id=\"AdminFirstname\" was not injected: check your FXML file 'admin.fxml'.";
        assert AuthenticatedUser != null : "fx:id=\"AuthenticatedUser\" was not injected: check your FXML file 'admin.fxml'.";
        assert Carpooling != null : "fx:id=\"Carpooling\" was not injected: check your FXML file 'admin.fxml'.";
        assert EmailError != null : "fx:id=\"EmailError\" was not injected: check your FXML file 'admin.fxml'.";
        assert EventLink != null : "fx:id=\"EventLink\" was not injected: check your FXML file 'admin.fxml'.";
        assert Logout != null : "fx:id=\"Logout\" was not injected: check your FXML file 'admin.fxml'.";
        assert PasswordError != null : "fx:id=\"PasswordError\" was not injected: check your FXML file 'admin.fxml'.";
        assert ProductLink != null : "fx:id=\"ProductLink\" was not injected: check your FXML file 'admin.fxml'.";
        assert ProfileImage != null : "fx:id=\"ProfileImage\" was not injected: check your FXML file 'admin.fxml'.";
        assert ProfileLink != null : "fx:id=\"ProfileLink\" was not injected: check your FXML file 'admin.fxml'.";
        assert QuizManagement != null : "fx:id=\"QuizManagement\" was not injected: check your FXML file 'admin.fxml'.";
        assert Reclamation != null : "fx:id=\"Reclamation\" was not injected: check your FXML file 'admin.fxml'.";
        assert UsersManagement != null : "fx:id=\"UsersManagement\" was not injected: check your FXML file 'admin.fxml'.";
        assert search != null : "fx:id=\"search\" was not injected: check your FXML file 'admin.fxml'.";
        assert usersChart != null : "fx:id=\"usersChart\" was not injected: check your FXML file 'admin.fxml'.";

    }

}
