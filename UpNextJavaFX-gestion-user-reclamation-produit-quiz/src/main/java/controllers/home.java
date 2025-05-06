package controllers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import controllers.admin.AdminHome;
import controllers.communities.ArtistsController;
import controllers.communities.CommunitiesController;
import controllers.communities.RootLayoutController;
import controllers.communities.SceneTransitionUtil;
import controllers.communities.SessionManager;
import controllers.communities.SessionType;
import controllers.communities.UserCommunitiesController;
import controllers.event_reser_ouma.*;
import edu.up_next.ReclamationController;
import edu.up_next.entities.Event;
import edu.up_next.entities.User;
import edu.up_next.services.EventServices;
import edu.up_next.services.ReservationServices;
import edu.up_next.services.UserServices;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class home {

    //ouma(reser+event)
    @FXML private VBox eventList;


    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private Button AddEvent;
    @FXML private Button AddProduct;

    //ouma(reser+event)
    @FXML private Button MyReservations;

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
    //private User user;

    //ouma(reser+event)
    private EventServices eventServices = new EventServices();
    private ReservationServices reservationServices = new ReservationServices();
    private ObservableList<Event> allEvents;


    private User currentUser;
    private UserServices userService = new UserServices();

    public void setUser(User user) {
        this.currentUser = user;
        // Also set the user in SessionManager for use in community controllers
        SessionManager.getInstance().setCurrentUser(user);
        refreshUserInfo();
    }

    /**
     * Refreshes the user information and events display.
     * This method should be called when returning to the home view to ensure
     * that the user's information and events are properly displayed.
     */
    public void refreshUserInfo() {
        if (currentUser == null) {
            System.out.println("No user to refresh in home controller");
            return;
        }

        updateUI();
        //ouma(reser+event)
        EventServices es = new EventServices();
        List<Event> allEvents = es.getAllData(); // Récupère tous les événements
        System.out.println("All events before filter: " + allEvents.size());

        // Vérifie les données récupérées
        allEvents.forEach(event -> {
            System.out.println("Event: " + event.getTitle() + ", User ID: " + event.getId());
        });

        // Filtrer uniquement les événements de l'utilisateur connecté
        List<Event> userEvents = allEvents.stream()
                .filter(e -> e.getHost_id() == currentUser.getId()) // Utilise la bonne méthode pour récupérer l'ID
                .toList();

        System.out.println("User events: " + userEvents.size());

        // Mise à jour de l'interface graphique
        Platform.runLater(() -> {
            displayEvents(userEvents);
        });
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
            System.out.println("No image path provided for user");
            // Set a default image
            try {
                Image defaultImage = new Image(getClass().getResourceAsStream("/user.png"));
                ProfileImage.setImage(defaultImage);
            } catch (Exception e) {
                System.out.println("Default image not found: " + e.getMessage());
            }
        }
        String roles = currentUser.getRoles();
        boolean isAdmin = roles != null && roles.contains("ROLE_ADMIN");
        boolean isArtist = roles != null && roles.contains("ROLE_ARTIST");
        //ou(reser+event)
        boolean isClient = roles != null && roles.contains("ROLE_CLIENT");
        adminDashboardButton.setVisible(isAdmin);
        adminDashboardButton.setManaged(isAdmin);
        AddEvent.setVisible(isArtist);
        AddEvent.setManaged(isArtist);
        AddProduct.setVisible(isArtist);
        AddProduct.setManaged(isArtist);
        //ou(reser+event)
        MyReservations.setVisible(isClient);
        MyReservations.setManaged(isClient);
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
    void addProduct(ActionEvent event) throws IOException {
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
        stage.show();
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
            } else   {
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
    void GoToVerifiedArtist(ActionEvent event) {
        try {
            URL fxmlLocation = getClass().getResource("/verifiedArtist.fxml");
            if (fxmlLocation == null) {
                System.err.println("Error: /verifiedArtist.fxml not found in resources");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();
            VerifiedArtist controller = loader.getController();
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
        try {
            // Si l'utilisateur est admin ou artist, va vers la page admin
            if (hasRole("ROLE_ADMIN") || hasRole("ROLE_ARTIST")) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/main_admin.fxml"));
                Parent root = loader.load();
                controllers.MainController controller = loader.getController();
                controller.setUser(currentUser);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } else if (hasRole("ROLE_CLIENT")) {
                // Si l'utilisateur est client, va vers la page user
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/main_user.fxml"));
                Parent root = loader.load();
                controllers.UserController userController = loader.getController();
                userController.setUser(currentUser);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } else {
                new Alert(Alert.AlertType.ERROR, "Vous n'avez pas les permissions nécessaires pour accéder à cette page.").showAndWait();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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


    //ouma(reser+event)
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
            System.err.println("❌ Error loading event page: " + e.getMessage());
        }
    }
    @FXML
    public void GoToAddEvent(ActionEvent event) {
        try {
            System.out.println("GoToAddEvent: Loading /addevent.fxml with currentUser ID=" + (currentUser != null ? currentUser.getId() : "null"));
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/addevent.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("Fichier FXML /addevent.fxml introuvable dans les ressources");
            }
            Parent root = loader.load();
            Addevent controller = loader.getController();
            if (controller == null) {
                throw new IOException("Contrôleur pour /addevent.fxml non instancié");
            }
            controller.setUser(currentUser);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
            System.out.println("GoToAddEvent: Successfully loaded /addevent.fxml");
        } catch (IOException e) {
            System.err.println("Échec du chargement de la page d'ajout d'événement : " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Échec du chargement");
            alert.setContentText("Impossible de charger la page d'ajout d'événement : " + e.getMessage());
            alert.showAndWait();
        }
    }
    public void displayEvents(List<Event> events) {
        eventList.getChildren().clear();

        if (currentUser != null) {
            boolean client = hasRole("ROLE_CLIENT");
            boolean artist = hasRole("ROLE_ARTIST");

            for (Event event : events) {
                VBox eventBox = new VBox();
                eventBox.setSpacing(5);
                eventBox.setStyle("-fx-padding: 10; -fx-border-color: #ccc; -fx-border-width: 1;");

                String imagePath = event.getImage();
                if (imagePath != null && !imagePath.isEmpty()) {
                    // Try to load from resources directory
                    File imageFile = new File("src/main/resources/images/" + imagePath);
                    if (imageFile.exists()) {
                        Image image = new Image(imageFile.toURI().toString());
                        ImageView imageView = new ImageView(image);
                        imageView.setFitWidth(100);
                        imageView.setFitHeight(100);
                        imageView.setPreserveRatio(true);
                        eventBox.getChildren().add(imageView);
                    } else {
                        // Try to load from uploads directory
                        File uploadsImageFile = new File("uploads/" + imagePath);
                        if (uploadsImageFile.exists()) {
                            Image image = new Image(uploadsImageFile.toURI().toString());
                            ImageView imageView = new ImageView(image);
                            imageView.setFitWidth(100);
                            imageView.setFitHeight(100);
                            imageView.setPreserveRatio(true);
                            eventBox.getChildren().add(imageView);
                        } else {
                            System.out.println("Image non trouvée: " + imagePath);
                            Label noImageLabel = new Label("Image non disponible");
                            eventBox.getChildren().add(noImageLabel);
                        }
                    }
                }

                Label titleLabel = new Label("Titre: " + event.getTitle());
                Label dateLabel = new Label("Date: " + event.getStartdate().toString());
                Label locationLabel = new Label("Lieu: " + event.getLocation());

                eventBox.getChildren().addAll(titleLabel, dateLabel, locationLabel);

                HBox buttonBox = new HBox(10);
                buttonBox.setSpacing(10);
                String style = "-fx-background-color: #0078d7; -fx-text-fill: white; -fx-padding: 5 10;";

                if (client) {
                    Button b = new Button("Book Now");
                    b.setStyle(style);
                    b.setOnAction(e -> booknow(event)); // Passer l'événement spécifique
                    buttonBox.getChildren().add(b);
                }
                if (artist) {
                    Button eBtn = new Button("Edit");
                    eBtn.setStyle(style);
                    eBtn.setOnAction(e -> {
                        try {
                            edit_event(event);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Erreur");
                            alert.setHeaderText("Échec de l'édition de l'événement");
                            alert.setContentText(ex.getMessage());
                            alert.showAndWait();
                        }
                    });

                    Button dBtn = new Button("Delete");
                    dBtn.setStyle(style + " -fx-background-color:#d70015;");
                    dBtn.setOnAction(e -> delete_event(event));
                    buttonBox.getChildren().addAll(eBtn, dBtn);
                }

                eventBox.getChildren().add(buttonBox);
                eventList.getChildren().add(eventBox);
            }
        }
    }
    public void displayReservationsForClient(List<Map<String, Object>> reservations) {
        eventList.getChildren().clear();

        if (reservations.isEmpty()) {
            Label noReservationsLabel = new Label("Aucune réservation trouvée.");
            eventList.getChildren().add(noReservationsLabel);
            return;
        }

        for (Map<String, Object> reservation : reservations) {
            VBox reservationBox = new VBox();
            reservationBox.setSpacing(5);
            reservationBox.setStyle("-fx-padding: 10; -fx-border-color: #ccc; -fx-border-width: 1;");

            String imagePath = (String) reservation.get("event_image");
            if (imagePath != null && !imagePath.isEmpty()) {
                // Try to load from resources directory
                File imageFile = new File("src/main/resources/images/" + imagePath);
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(100);
                    imageView.setFitHeight(100);
                    imageView.setPreserveRatio(true);
                    reservationBox.getChildren().add(imageView);
                } else {
                    // Try to load from uploads directory
                    File uploadsImageFile = new File("uploads/" + imagePath);
                    if (uploadsImageFile.exists()) {
                        Image image = new Image(uploadsImageFile.toURI().toString());
                        ImageView imageView = new ImageView(image);
                        imageView.setFitWidth(100);
                        imageView.setFitHeight(100);
                        imageView.setPreserveRatio(true);
                        reservationBox.getChildren().add(imageView);
                    } else {
                        System.out.println("Image non trouvée: " + imagePath);
                        Label noImageLabel = new Label("Image non disponible");
                        reservationBox.getChildren().add(noImageLabel);
                    }
                }
            }

            Label titleLabel = new Label("Titre: " + reservation.get("event_title"));
            Label dateLabel = new Label("Date: " + reservation.get("event_startdate").toString());
            Label locationLabel = new Label("Lieu: " + reservation.get("event_location"));
            Label quantityLabel = new Label("Quantité: " + reservation.get("quantity"));
            Label totalPriceLabel = new Label("Prix total: " + reservation.get("total_price") + " €");

            reservationBox.getChildren().addAll(titleLabel, dateLabel, locationLabel, quantityLabel, totalPriceLabel);

            HBox buttonBox = new HBox(10);
            buttonBox.setSpacing(10);
            String style = "-fx-background-color: #0078d7; -fx-text-fill: white; -fx-padding: 5 10;";

            Button cancelButton = new Button("Annuler Réservation");
            cancelButton.setStyle(style + " -fx-background-color:#d70015;");
            cancelButton.setOnAction(e -> cancelReservation((Integer) reservation.get("id")));
            buttonBox.getChildren().add(cancelButton);

            reservationBox.getChildren().add(buttonBox);
            eventList.getChildren().add(reservationBox);
        }
    }
    private void cancelReservation(int reservationId) {
        try {
            reservationServices.cancelReservation(reservationId);
            List<Map<String, Object>> updatedReservations = reservationServices.getReservationsByUserId(currentUser.getId());
            Platform.runLater(() -> displayReservationsForClient(updatedReservations));
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText("Réservation Annulée");
            alert.setContentText("La réservation a été annulée avec succès.");
            alert.showAndWait();
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Échec de l'annulation");
            alert.setContentText("Impossible d'annuler la réservation : " + e.getMessage());
            alert.showAndWait();
        }
    }
    void edit_event(Event event) throws IOException {
        Editevent.eventToEdit = event;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/editevent.fxml"));
        Parent root = loader.load();
        Editevent controller = loader.getController();
        controller.setUser(this.currentUser);
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.show();
    }

    void delete_event(Event event) {
        try {
            eventServices.deleteEntity(event);
            List<Event> userEvents = eventServices.getAllData().stream()
                    .filter(e -> e.getHost_id() == currentUser.getId())
                    .toList();
            Platform.runLater(() -> displayEvents(userEvents));
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Échec de la suppression");
            alert.setContentText("Impossible de supprimer l'événement : " + e.getMessage());
            alert.showAndWait();
        }
    }
    void booknow(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/addreservation.fxml"));
            Parent root = loader.load();
            Addreservation controller = loader.getController();
            controller.setSelectedEvent(event);
            eventList.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Échec du chargement");
            alert.setContentText("Impossible de charger la page de réservation : " + e.getMessage());
            alert.showAndWait();
        }
    }
    private boolean hasRole(String roleName) {
        String roles = currentUser != null ? currentUser.getRoles() : null;
        return roles != null && roles.toLowerCase().contains(roleName.toLowerCase());
    }

    public void GoToMyReservations(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/reservationlist.fxml"));
            Parent root = loader.load();

            // Obtenir le contrôleur et passer le currentUser
            Reservationlist controller = loader.getController();
            controller.setUser(currentUser); // << important

            // Mettre à jour la scène
            Stage stage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Chargement échoué");
            alert.setContentText("Impossible de charger la page reservationlist.fxml : " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    void GoToQuizManagement(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/quiz.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error loading quiz management page: " + e.getMessage());
        }
    }

    @FXML
    void GoToCommunities(ActionEvent event) {
        try {
            String contentFxmlPath;
            Class<?> controllerClass;

            // Set the session type in SessionManager based on the user's role
            if (hasRole("ROLE_ARTIST")) {
                // For artists, load the user communities view
                contentFxmlPath = "/views/communities/user-communities-view.fxml";
                controllerClass = UserCommunitiesController.class;
                SessionManager.getInstance().setSessionType(SessionType.ARTIST);
            } else if (hasRole("ROLE_ADMIN")) {
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
}
