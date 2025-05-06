package controllers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import controllers.communities.ArtistsController;
import controllers.communities.CommunitiesController;
import controllers.communities.RootLayoutController;
import controllers.communities.SceneTransitionUtil;
import controllers.communities.SessionManager;
import controllers.communities.SessionType;
import controllers.communities.UserCommunitiesController;
import controllers.event_reser_ouma.Eventlist;
import edu.up_next.ReclamationController;
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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class profile {
    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private Text AuthenticatedUser;
    @FXML private Label AuthenticatedUser1;
    @FXML private Label AuthenticatedUser11;
    @FXML private ImageView verifiedTik;
    @FXML private Hyperlink EventLink;
    @FXML private Hyperlink Logout;
    @FXML private Hyperlink ProductLink;
    @FXML private ImageView ProfileImage;
    @FXML private ImageView ProfileImage1;
    @FXML private Hyperlink ProfileLink;
    @FXML private Hyperlink VerifiedArtistLink;
    @FXML private Button addLocationButton;
    @FXML private TextField cityField;
    @FXML private TextField countryField;
    @FXML private TextField descriptionField;
    @FXML private Button editProfileButton;
    @FXML private TextField emailField;
    @FXML private TextField firstnameField;
    @FXML private TextField lastnameField;
    @FXML private TextField phoneField;
    @FXML private TextField roleField;
    @FXML private TextField search;
    @FXML private TextField specialityField;
    @FXML private Label specialityLabel;
    @FXML private TextField AddressField;
    @FXML private Hyperlink HomeLink;
    @FXML private Hyperlink QuizLink;

    private User currentUser;

    public void setUser(User user) {
        this.currentUser = user;
        initializeUI();
    }

    private void initializeUI() {
        if (currentUser == null) {
            System.out.println("No user provided to profile controller");
            return;
        }

        AuthenticatedUser.setText(currentUser.getFirstname() + " " + currentUser.getLastname());
        AuthenticatedUser1.setText(currentUser.getFirstname() + " " + currentUser.getLastname());

        if (verifiedTik != null) {
            verifiedTik.setVisible(currentUser.isIs_verified());
        } else {
            System.err.println("Warning: verifiedTik is null. Check fx:id in profile.fxml");
        }

        String imagePath = currentUser.getImage();
        if (imagePath != null && !imagePath.isEmpty()) {
            // Try to load from uploads directory
            File imageFile = new File("uploads/" + imagePath);
            if (imageFile.exists()) {
                Image image = new Image(imageFile.toURI().toString());
                if (ProfileImage != null) {
                    ProfileImage.setImage(image);
                }
                if (ProfileImage1 != null) {
                    ProfileImage1.setImage(image);
                }
            } else {
                // Try to load from resources directory
                File resourceImageFile = new File("src/main/resources/images/" + imagePath);
                if (resourceImageFile.exists()) {
                    Image image = new Image(resourceImageFile.toURI().toString());
                    if (ProfileImage != null) {
                        ProfileImage.setImage(image);
                    }
                    if (ProfileImage1 != null) {
                        ProfileImage1.setImage(image);
                    }
                } else {
                    System.out.println("Image file not found: " + imagePath);
                    // Set a default image
                    try {
                        Image defaultImage = new Image(getClass().getResourceAsStream("/user.png"));
                        if (ProfileImage != null) {
                            ProfileImage.setImage(defaultImage);
                        }
                        if (ProfileImage1 != null) {
                            ProfileImage1.setImage(defaultImage);
                        }
                    } catch (Exception e) {
                        System.out.println("Default image not found: " + e.getMessage());
                    }
                }
            }
        } else {
            System.out.println("No image path provided for user");
            // Set a default image
            try {
                Image defaultImage = new Image(getClass().getResourceAsStream("/user.png"));
                if (ProfileImage != null) {
                    ProfileImage.setImage(defaultImage);
                }
                if (ProfileImage1 != null) {
                    ProfileImage1.setImage(defaultImage);
                }
            } catch (Exception e) {
                System.out.println("Default image not found: " + e.getMessage());
            }
        }

        firstnameField.setText(currentUser.getFirstname());
        lastnameField.setText(currentUser.getLastname());
        emailField.setText(currentUser.getEmail());

        String role = currentUser.getRoles();
        if (role != null && !role.isEmpty()) {
            String cleanedRole = role.replaceAll("[\\[\\]]", "")
                    .replace("ROLE_", "")
                    .toLowerCase();
            cleanedRole = cleanedRole.substring(0, 1).toUpperCase() + cleanedRole.substring(1);
            roleField.setText(cleanedRole);
        } else {
            roleField.setText("Unknown");
        }

        phoneField.setText(String.valueOf(currentUser.getNum()));
        descriptionField.setText(currentUser.getDescription() != null ? currentUser.getDescription() : "");

        String roles = currentUser.getRoles();
        boolean isArtist = roles != null && roles.contains("ROLE_ARTIST");
        specialityLabel.setVisible(isArtist);
        specialityField.setVisible(isArtist);
        if (isArtist) {
            specialityField.setText(currentUser.getSpeciality() != null ? currentUser.getSpeciality() : "");
        }

        ProfileLink.setUnderline(true);
    }

    @FXML
    void goToEditProfile(ActionEvent event) {
        try {
            URL fxmlLocation = getClass().getResource("/EditProfile.fxml");
            if (fxmlLocation == null) {
                System.err.println("Error: /EditProfile.fxml not found in resources");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            EditProfile editProfileController = loader.getController();
            editProfileController.setUser(currentUser);

            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error loading EditProfile page: " + e.getMessage());
        }
    }

    @FXML
    void GoToProduct(ActionEvent event) {
        try {
            URL fxmlLocation = getClass().getResource("/product.fxml");
            if (fxmlLocation == null) {
                System.err.println("Error: /product.fxml not found in resources");
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
            System.err.println("❌ Error loading product page: " + e.getMessage());
        }
    }

    @FXML
    void GoToEvent(ActionEvent event) {
        try{
            URL fxmlLocation = getClass().getResource("/eventlist.fxml");
            if (fxmlLocation == null) {
                System.err.println("Error: /eventlist.fxml not found in resources");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();
            Eventlist controller = loader.getController();
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
    void goToProfile(ActionEvent event) {
        // Already on profile page, no action needed
    }

    @FXML
    void GoToQuiz(ActionEvent event) {
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

            // Pass the current user to the home controller
            home homeController = loader.getController();
            homeController.setUser(currentUser);

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
    public void GoToReclamationClient(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionRec.fxml"));
            Parent root = loader.load();

            // Si tu veux passer un utilisateur à ce contrôleur
            ReclamationController controller = loader.getController();
            controller.setUser(currentUser); // assure-toi que la méthode setUser existe

            System.out.println("currentUser home vers rec"+currentUser);

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
            String roles = currentUser.getRoles();
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
            SessionManager.getInstance().setCurrentUser(currentUser);

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

    @FXML
    void initialize() {
        assert AuthenticatedUser != null : "fx:id=\"AuthenticatedUser\" was not injected: check your FXML file 'profile.fxml'.";
        assert AuthenticatedUser1 != null : "fx:id=\"AuthenticatedUser1\" was not injected: check your FXML file 'profile.fxml'.";
        assert AuthenticatedUser11 != null : "fx:id=\"AuthenticatedUser11\" was not injected: check your FXML file 'profile.fxml'.";
        assert verifiedTik != null : "fx:id=\"verifiedTik\" was not injected: check your FXML file 'profile.fxml'.";
        assert EventLink != null : "fx:id=\"EventLink\" was not injected: check your FXML file 'profile.fxml'.";
        assert Logout != null : "fx:id=\"Logout\" was not injected: check your FXML file 'profile.fxml'.";
        assert ProductLink != null : "fx:id=\"ProductLink\" was not injected: check your FXML file 'profile.fxml'.";
        assert ProfileImage != null : "fx:id=\"ProfileImage\" was not injected: check your FXML file 'profile.fxml'.";
        assert ProfileImage1 != null : "fx:id=\"ProfileImage1\" was not injected: check your FXML file 'profile.fxml'.";
        assert ProfileLink != null : "fx:id=\"ProfileLink\" was not injected: check your FXML file 'profile.fxml'.";
        assert VerifiedArtistLink != null : "fx:id=\"VerifiedArtistLink\" was not injected: check your FXML file 'profile.fxml'.";
        assert addLocationButton != null : "fx:id=\"addLocationButton\" was not injected: check your FXML file 'profile.fxml'.";
        assert cityField != null : "fx:id=\"cityField\" was not injected: check your FXML file 'profile.fxml'.";
        assert countryField != null : "fx:id=\"countryField\" was not injected: check your FXML file 'profile.fxml'.";
        assert descriptionField != null : "fx:id=\"descriptionField\" was not injected: check your FXML file 'profile.fxml'.";
        assert editProfileButton != null : "fx:id=\"editProfileButton\" was not injected: check your FXML file 'profile.fxml'.";
        assert emailField != null : "fx:id=\"emailField\" was not injected: check your FXML file 'profile.fxml'.";
        assert firstnameField != null : "fx:id=\"firstnameField\" was not injected: check your FXML file 'profile.fxml'.";
        assert lastnameField != null : "fx:id=\"lastnameField\" was not injected: check your FXML file 'profile.fxml'.";
        assert phoneField != null : "fx:id=\"phoneField\" was not injected: check your FXML file 'profile.fxml'.";
        assert roleField != null : "fx:id=\"roleField\" was not injected: check your FXML file 'profile.fxml'.";
        assert search != null : "fx:id=\"search\" was not injected: check your FXML file 'profile.fxml'.";
        assert specialityField != null : "fx:id=\"specialityField\" was not injected: check your FXML file 'profile.fxml'.";
        assert specialityLabel != null : "fx:id=\"specialityLabel\" was not injected: check your FXML file 'profile.fxml'.";
        assert AddressField != null : "fx:id=\"AddressField\" was not injected: check your FXML file 'profile.fxml'.";
    }
}
