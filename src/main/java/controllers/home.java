package controllers;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

import controllers.admin.AdminHome;
import edu.up_next.entities.User;
import edu.up_next.services.UserServices;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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
    @FXML private Hyperlink HomeLink;
    @FXML private Hyperlink QuizLink;
    @FXML private Text result;
    @FXML private VBox searchResult;

    private User currentUser;
    private UserServices userService = new UserServices();

    public void setUser(User user) {
        this.currentUser = user;
        updateUI();
    }

    private void updateUI() {
        if (currentUser == null) {
            System.out.println("No user provided to home controller");
            return;
        }
        authentifiedFirstname.setText(currentUser.getFirstname());
        AuthenticatedUser.setText(currentUser.getFirstname() + " " + currentUser.getLastname());
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
        String roles = currentUser.getRoles();
        boolean isAdmin = roles != null && roles.contains("ROLE_ADMIN");
        boolean isArtist = roles != null && roles.contains("ROLE_ARTIST");
        adminDashboardButton.setVisible(isAdmin);
        adminDashboardButton.setManaged(isAdmin);
        AddEvent.setVisible(isArtist);
        AddEvent.setManaged(isArtist);
        AddProduct.setVisible(isArtist);
        AddProduct.setManaged(isArtist);
    }

    @FXML
    void GoToAdminDashboard(ActionEvent event) {
        try {
            URL fxmlLocation = getClass().getResource("/admin.fxml");
            if (fxmlLocation == null) {
                System.err.println("Error: /admin.fxml not found in resources");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();
            AdminHome controller = loader.getController();
            controller.setUser(currentUser);
            // Pass the current user to AdminHome controller (if it has a setUser method)
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error loading AdminHome page: " + e.getMessage());
        }
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
        try {
            URL fxmlLocation = getClass().getResource("/verifiedArtist.fxml");
            if (fxmlLocation == null) {
                System.err.println("Error: /verifiedArtist.fxml not found in resources");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();
            VerifiedArtist controller = loader.getController(); // Changed from 'profile' to 'VerifiedArtist'
            controller.setUser(currentUser);
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error loading VerifiedArtist page: " + e.getMessage());
        }
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
    void GoToQuiz(ActionEvent event) {
    }

    @FXML
    void goToHome(ActionEvent event) {
        updateUI();
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
    void ActivateSearchResult(ActionEvent event) {
        // This method can remain empty or be used for other purposes (e.g., pressing Enter to select a result)
    }

    private void performSearch(String searchTerm) {
        searchResult.getChildren().clear();
        if (searchTerm.isEmpty()) {
            searchResult.setVisible(false);
            return;
        }

        try {
            List<User> users = userService.searchUsers(searchTerm);
            if (users.isEmpty()) {
                searchResult.setVisible(false);
                return;
            }

            searchResult.setVisible(true);
            for (User user : users) {
                HBox resultBox = createResultBox(user);
                searchResult.getChildren().add(resultBox);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            searchResult.setVisible(false);
        }
    }

    private HBox createResultBox(User user) {
        HBox hbox = new HBox();
        hbox.setSpacing(10);
        hbox.setPadding(new javafx.geometry.Insets(5));
        hbox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand;");

        Label nameLabel = new Label(user.getFirstname() + " " + user.getLastname());
        nameLabel.setStyle("-fx-font-family: 'Dubai Medium'; -fx-font-size: 14; -fx-text-fill: #333;");

        String role = user.getRoles().replaceAll("[\\[\\]\"ROLE_]", "");
        Label roleLabel = new Label("(" + role + ")");
        roleLabel.setStyle("-fx-font-family: 'Dubai Light'; -fx-font-size: 12; -fx-text-fill: #666;");

        hbox.getChildren().addAll(nameLabel, roleLabel);

        hbox.setOnMouseClicked(e -> navigateToUserProfile(user));
        return hbox;
    }

    private void navigateToUserProfile(User user) {
        try {
            URL fxmlLocation = getClass().getResource("/UserView.fxml");
            if (fxmlLocation == null) {
                System.err.println("Error: /UserView.fxml not found in resources");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();
            UserView controller = loader.getController();
            controller.setUsers(currentUser, user); // Pass both the logged-in user and the selected user
            Stage stage = (Stage) search.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error loading UserView page: " + e.getMessage());
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
        assert searchResult != null : "fx:id=\"searchResult\" was not injected: check your FXML file 'home.fxml'.";
        assert result != null : "fx:id=\"result\" was not injected: check your FXML file 'home.fxml'.";

        // Show the searchResult VBox when the search TextField is clicked
        search.setOnMouseClicked(event -> {
            searchResult.setVisible(true);
            performSearch(search.getText().trim());
        });

        search.textProperty().addListener((obs, oldValue, newValue) -> {
            performSearch(newValue.trim());
        });
    }
}