package controllers.admin;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import controllers.ListArtist;
import controllers.event_reser_ouma.AdminEventManagement;
import edu.up_next.GestionReclamationAdminController;
import edu.up_next.ReclamationController;
import edu.up_next.entities.User;
import controllers.ListAdministrateur;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class AdminHome {

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
        AdminFirstname.setText(currentUser.getFirstname());
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
    }

    @FXML
    void GoToUsersManagement(ActionEvent event) {
        try {
            URL fxmlLocation = getClass().getResource("/UsersManagement.fxml");
            if (fxmlLocation == null) {
                System.err.println("Error: /UsersManagement.fxml not found in resources");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();
            UsersManagement usersManagementController = loader.getController();
            usersManagementController.setUser(currentUser);
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error loading UsersManagement page: " + e.getMessage());
        }
    }

    @FXML
    void GoToCarpooling(ActionEvent event) {

    }

    @FXML
    void GoToEvent(ActionEvent event) {
        try {
            URL fxmlLocation = getClass().getResource("/AdminEventManagement.fxml");
            if (fxmlLocation == null) {
                System.err.println("Error: /UsersManagement.fxml not found in resources");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();
            AdminEventManagement AdminEventManagement = loader.getController();
            AdminEventManagement.setUser(currentUser);
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error loading UsersManagement page: " + e.getMessage());
        }

    }

    @FXML
    void GoToProduct(ActionEvent event) {
        try {
            if(currentUser.getRoles().indexOf("ROLE_ARTIST") != -1) {
                URL fxmlLocation = getClass().getResource("/ListArtist.fxml");
                if (fxmlLocation == null) {
                    System.err.println("Error: /ListAdministrateur.fxml not found in resources");
                    return;
                }
                FXMLLoader loader = new FXMLLoader(fxmlLocation);
                Parent root = loader.load();

                Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Products Management");
                stage.show();


            } else if (currentUser.getRoles().indexOf("ROLE_ADMIN") != -1) {
                URL fxmlLocation = getClass().getResource("/ListAdministrateur.fxml");
                if (fxmlLocation == null) {
                    System.err.println("Error: /ListAdministrateur.fxml not found in resources");
                    return;
                }
                FXMLLoader loader = new FXMLLoader(fxmlLocation);
                Parent root = loader.load();

                // Get the controller and pass the current user
                ListAdministrateur listAdminController = loader.getController();
                listAdminController.setUser(currentUser);
                Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Products Management");
                stage.show();
            } else {
                URL fxmlLocation = getClass().getResource("/ListUser.fxml");
                if (fxmlLocation == null) {
                    System.err.println("Error: /ListUser.fxml not found in resources");
                    return;
                }
                FXMLLoader loader = new FXMLLoader(fxmlLocation);
                Parent root = loader.load();

                // Get the co
                Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Products Management");
                stage.show();

            }




        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error loading products management page: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    void GoToQuiz(ActionEvent event) {
    }

    @FXML
    void GoToReclamationAdmin(ActionEvent event) {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionReclamationAdmin.fxml"));
            Parent root = loader.load();

            // Si tu veux passer un utilisateur à ce contrôleur
            GestionReclamationAdminController controller = loader.getController();
            //controller.setUser(currentUser); // assure-toi que la méthode setUser existe

          //  System.out.println("currentUser home vers rec"+currentUser);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            //  showAlert("Erreur", "Impossible de charger la page gestionRec.fxml : " + e.getMessage());
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