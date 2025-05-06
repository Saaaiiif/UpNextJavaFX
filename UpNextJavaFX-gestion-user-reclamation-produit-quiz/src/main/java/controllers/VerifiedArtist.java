package controllers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

import controllers.communities.ArtistsController;
import controllers.communities.CommunitiesController;
import controllers.communities.RootLayoutController;
import controllers.communities.SceneTransitionUtil;
import controllers.communities.SessionManager;
import controllers.communities.SessionType;
import controllers.communities.UserCommunitiesController;
import edu.up_next.ReclamationController;
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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class VerifiedArtist {

    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private Text AuthenticatedUser;
    @FXML private Hyperlink EventLink;
    @FXML private Hyperlink HomeLink;
    @FXML private Hyperlink Logout;
    @FXML private Hyperlink ProductLink;
    @FXML private ImageView ProfileImage;
    @FXML private Hyperlink ProfileLink;
    @FXML private Hyperlink QuizLink;
    @FXML private Hyperlink VerifiedArtistLink1;
    @FXML private AnchorPane mainPane;
    @FXML private TextField search;
    @FXML private VBox artistList;

    private User loggedInUser;
    private UserServices userService = new UserServices();

    public void setUser(User loggedInUser) {
        this.loggedInUser = loggedInUser;
        updateUI();
        loadVerifiedArtists();
    }

    private void updateUI() {
        if (loggedInUser == null) {
            System.out.println("No logged-in user provided to VerifiedArtist controller");
            return;
        }
        AuthenticatedUser.setText(loggedInUser.getFirstname() + " " + loggedInUser.getLastname());
        String imagePath = loggedInUser.getImage();
        if (imagePath != null && !imagePath.isEmpty()) {
            // Try to load from uploads directory
            File imageFile = new File("uploads/" + imagePath);
            if (imageFile.exists()) {
                Image image = new Image(imageFile.toURI().toString());
                ProfileImage.setImage(image);
            } else {
                // Try to load from resources directory
                File resourceImageFile = new File("src/main/resources/images/" + imagePath);
                if (resourceImageFile.exists()) {
                    Image image = new Image(resourceImageFile.toURI().toString());
                    ProfileImage.setImage(image);
                } else {
                    System.out.println("Image file not found: " + imagePath);
                    // Set a default image
                    try {
                        Image defaultImage = new Image(getClass().getResourceAsStream("/user.png"));
                        ProfileImage.setImage(defaultImage);
                    } catch (Exception e) {
                        System.out.println("Default image not found: " + e.getMessage());
                    }
                }
            }
        } else {
            System.out.println("No image path provided for logged-in user");
            // Set a default image
            try {
                Image defaultImage = new Image(getClass().getResourceAsStream("/user.png"));
                ProfileImage.setImage(defaultImage);
            } catch (Exception e) {
                System.out.println("Default image not found: " + e.getMessage());
            }
        }
    }

    private void loadVerifiedArtists() {
        artistList.getChildren().clear();
        try {
            List<User> verifiedArtists = userService.getVerifiedArtists();
            for (User artist : verifiedArtists) {
                HBox artistBox = createArtistBox(artist);
                artistList.getChildren().add(artistBox);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("❌ Error loading verified artists: " + e.getMessage());
        }
    }

    private HBox createArtistBox(User artist) {
        HBox hbox = new HBox();
        hbox.setPrefHeight(150.0);
        hbox.setPrefWidth(600.0);
        hbox.setSpacing(15);
        hbox.setPadding(new javafx.geometry.Insets(15));
        hbox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #b7b7b7; -fx-border-radius: 10; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        // Add hover effect to the HBox
        hbox.setOnMouseEntered(event -> hbox.setStyle("-fx-background-color: #f8f8f8; -fx-border-color: #ff5757; -fx-border-radius: 10; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 3);"));
        hbox.setOnMouseExited(event -> hbox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #b7b7b7; -fx-border-radius: 10; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"));

        // Profile Image
        ImageView profileImage = new ImageView();
        profileImage.setFitHeight(104.0);
        profileImage.setFitWidth(98.0);
        profileImage.setPickOnBounds(true);
        profileImage.setPreserveRatio(true);
        String imagePath = artist.getImage();
        if (imagePath != null && !imagePath.isEmpty()) {
            // Try to load from uploads directory
            File imageFile = new File("uploads/" + imagePath);
            if (imageFile.exists()) {
                Image image = new Image(imageFile.toURI().toString());
                profileImage.setImage(image);
            } else {
                // Try to load from resources directory
                File resourceImageFile = new File("src/main/resources/images/" + imagePath);
                if (resourceImageFile.exists()) {
                    Image image = new Image(resourceImageFile.toURI().toString());
                    profileImage.setImage(image);
                } else {
                    // Use default image if both attempts fail
                    profileImage.setImage(new Image(getClass().getResourceAsStream("/user.png")));
                }
            }
        } else {
            // Use default image if no image path is provided
            profileImage.setImage(new Image(getClass().getResourceAsStream("/user.png")));
        }
        Circle clip = new Circle(50.0, 50.0, 50.0);
        profileImage.setClip(clip);

        // Verified Tick
        ImageView verifiedTick = new ImageView();
        verifiedTick.setFitHeight(38.0);
        verifiedTick.setFitWidth(36.0);
        verifiedTick.setPickOnBounds(true);
        verifiedTick.setPreserveRatio(true);
        verifiedTick.setImage(new Image(getClass().getResourceAsStream("/verified-tik.png")));

        // VBox for stacking text elements vertically
        VBox textContainer = new VBox();
        textContainer.setSpacing(5);
        textContainer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label firstnameLabel = new Label(artist.getFirstname());
        firstnameLabel.setStyle("-fx-font-family: 'Dubai Medium'; -fx-font-size: 16; -fx-text-fill: #333;");

        Label lastnameLabel = new Label(artist.getLastname());
        lastnameLabel.setStyle("-fx-font-family: 'Dubai Medium'; -fx-font-size: 16; -fx-text-fill: #333;");

        Label specialityLabel = new Label("Speciality");
        specialityLabel.setTextFill(javafx.scene.paint.Color.web("#888888"));
        specialityLabel.setFont(new javafx.scene.text.Font("System", 12.0));

        Label specialityValue = new Label(artist.getSpeciality() != null ? artist.getSpeciality() : "N/A");
        specialityValue.setStyle("-fx-font-family: 'Dubai Light'; -fx-font-size: 14; -fx-text-fill: #666;");

        // Add "You" label if this artist is the logged-in user
        if (loggedInUser != null && loggedInUser.getId() == artist.getId()) {
            Label youLabel = new Label("You");
            youLabel.setStyle("-fx-font-family: 'Dubai Medium'; -fx-font-size: 12; -fx-text-fill: #ff5757;");
            textContainer.getChildren().add(youLabel);
        }

        textContainer.getChildren().addAll(firstnameLabel, lastnameLabel, specialityLabel, specialityValue);

        // View Details Button
        Button viewButton = new Button("View details");
        viewButton.setPrefHeight(40.0);
        viewButton.setPrefWidth(150.0);
        viewButton.setStyle("-fx-background-color: #FF0000; -fx-background-radius: 20; -fx-font-family: 'Dubai Medium'; -fx-font-size: 14; -fx-text-fill: #ffffff; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 1);");
        viewButton.setOnAction(event -> navigateToUserView(artist));
        viewButton.setOnMouseEntered(event -> viewButton.setStyle("-fx-background-color: #e60000; -fx-background-radius: 20; -fx-font-family: 'Dubai Medium'; -fx-font-size: 14; -fx-text-fill: #ffffff; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 2);"));
        viewButton.setOnMouseExited(event -> viewButton.setStyle("-fx-background-color: #FF0000; -fx-background-radius: 20; -fx-font-family: 'Dubai Medium'; -fx-font-size: 14; -fx-text-fill: #ffffff; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 1);"));

        // Align the verified tick and button to the right
        HBox.setHgrow(textContainer, javafx.scene.layout.Priority.ALWAYS);
        HBox rightContainer = new HBox(10);
        rightContainer.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        rightContainer.getChildren().addAll(verifiedTick, viewButton);

        hbox.getChildren().addAll(profileImage, textContainer, rightContainer);
        return hbox;
    }

    private void navigateToUserView(User artist) {
        try {
            URL fxmlLocation = getClass().getResource("/UserView.fxml");
            if (fxmlLocation == null) {
                System.err.println("Error: /UserView.fxml not found in resources");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();
            UserView controller = loader.getController();

            // Check if the logged-in user is a verified artist and the clicked artist is the logged-in user
            if (loggedInUser != null && loggedInUser.getRoles().contains("ROLE_ARTIST") && loggedInUser.getId() == artist.getId()) {
                controller.setUsers(loggedInUser, loggedInUser); // Redirect to the logged-in user's profile
            } else {
                controller.setUsers(loggedInUser, artist); // Default: Navigate to the selected artist's UserView page
            }

            Stage stage = (Stage) mainPane.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error loading UserView page: " + e.getMessage());
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
    void GoToQuiz(ActionEvent event) {
        // TODO: Implement quiz navigation
    }

    @FXML
    void GoToVerifiedArtist(ActionEvent event) {
        loadVerifiedArtists();
    }

    @FXML
    void GoToViewUser(ActionEvent event) {
        // This method is not needed since we're using dynamic buttons in createArtistBox
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
        assert AuthenticatedUser != null : "fx:id=\"AuthenticatedUser\" was not injected: check your FXML file 'verifiedArtist.fxml'.";
        assert EventLink != null : "fx:id=\"EventLink\" was not injected: check your FXML file 'verifiedArtist.fxml'.";
        assert HomeLink != null : "fx:id=\"HomeLink\" was not injected: check your FXML file 'verifiedArtist.fxml'.";
        assert Logout != null : "fx:id=\"Logout\" was not injected: check your FXML file 'verifiedArtist.fxml'.";
        assert ProductLink != null : "fx:id=\"ProductLink\" was not injected: check your FXML file 'verifiedArtist.fxml'.";
        assert ProfileImage != null : "fx:id=\"ProfileImage\" was not injected: check your FXML file 'verifiedArtist.fxml'.";
        assert ProfileLink != null : "fx:id=\"ProfileLink\" was not injected: check your FXML file 'verifiedArtist.fxml'.";
        assert QuizLink != null : "fx:id=\"QuizLink\" was not injected: check your FXML file 'verifiedArtist.fxml'.";
        assert VerifiedArtistLink1 != null : "fx:id=\"VerifiedArtistLink1\" was not injected: check your FXML file 'verifiedArtist.fxml'.";
        assert mainPane != null : "fx:id=\"mainPane\" was not injected: check your FXML file 'verifiedArtist.fxml'.";
        assert search != null : "fx:id=\"search\" was not injected: check your FXML file 'verifiedArtist.fxml'.";
        assert artistList != null : "fx:id=\"artistList\" was not injected: check your FXML file 'verifiedArtist.fxml'.";
    }

    public void GoToReclamationClient(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionRec.fxml"));
            Parent root = loader.load();

            // Si tu veux passer un utilisateur à ce contrôleur
            ReclamationController controller = loader.getController();
            controller.setUser(loggedInUser); // assure-toi que la méthode setUser existe

            System.out.println("currentUser home vers rec"+loggedInUser);

            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            //  showAlert("Erreur", "Impossible de charger la page gestionRec.fxml : " + e.getMessage());
        }
    }

    @FXML
    void GoToCommunities(ActionEvent event) {
        try {
            String contentFxmlPath;
            Class<?> controllerClass;

            // Set the session type in SessionManager based on the user's role
            String roles = loggedInUser.getRoles();
            if (roles != null && roles.contains("ROLE_ARTIST")) {
                // For artists, load the user communities view
                contentFxmlPath = "/views/communities/user-communities-view.fxml";
                controllerClass = UserCommunitiesController.class;
                SessionManager.getInstance().setSessionType(SessionType.ARTIST);
            } else if (roles != null && roles.contains("ROLE_ADMIN")) {
                // For admins, load the main communities view (with admin capabilities)
                contentFxmlPath = "/views/communities/communities-view.fxml";
                controllerClass = CommunitiesController.class;
                SessionManager.getInstance().setSessionType(SessionType.ADMIN);
            } else {
                // For regular users, load the user communities view
                contentFxmlPath = "/views/communities/user-communities-view.fxml";
                controllerClass = UserCommunitiesController.class;
                SessionManager.getInstance().setSessionType(SessionType.USER);
            }

            // Set the current user in SessionManager
            SessionManager.getInstance().setCurrentUser(loggedInUser);

            // First, load the root layout
            FXMLLoader rootLoader = new FXMLLoader(getClass().getResource("/views/communities/root-layout.fxml"));
            Parent rootView = rootLoader.load();

            // Get the root layout controller
            RootLayoutController rootController = rootLoader.getController();

            // Set the stage on the root controller
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            rootController.setStage(stage);

            // Set the root controller in SceneTransitionUtil
            SceneTransitionUtil.setRootController(rootController);

            // Load the content into the root layout's content area
            rootController.loadContent(contentFxmlPath, controllerClass);

            // Set the scene with the root layout
            Scene scene = new Scene(rootView);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error loading communities page: " + e.getMessage());
        }
    }
}
