package com.example.upnext;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.text.Font;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class ArtistsController {

    @FXML
    private ImageView logoImage;

    @FXML
    private GridPane artistsGrid;

    @FXML
    private Button artistsButton;

    @FXML
    private Button communitiesButton;

    @FXML
    private Button createCommunityButton;

    private boolean isDarkMode = true; // Default is dark mode

    @FXML
    public void initialize() {
        Font.loadFont(getClass().getResourceAsStream("/Feather.ttf"), 18);
        Image logo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/logo.png")));
        logoImage.setImage(logo);

        // Get the theme preference from SessionManager
        isDarkMode = SessionManager.getInstance().isDarkMode();

        // Display session type information
        SessionType currentSession = SessionManager.getInstance().getSessionType();
        if (currentSession == SessionType.ARTIST) {
            createCommunityButton.setVisible(true);
        } else {
            createCommunityButton.setVisible(false);
        }

        // Highlight the current page's navigation label
        if (artistsButton != null) {
            artistsButton.getStyleClass().add("active");
        }

        // Register as a theme change listener
        SessionManager.getInstance().addThemeChangeListener(this::handleThemeChange);

        // Load artists
        loadArtists();
    }

    /**
     * Handles theme changes from SessionManager.
     * 
     * @param isDarkMode true for dark mode, false for light mode
     */
    private void handleThemeChange(boolean isDarkMode) {
        this.isDarkMode = isDarkMode;
        // No need to update UI directly, as the root layout will handle that
    }

    @FXML
    private void handleUpNextButtonClick() {
        try {
            SceneTransitionUtil.changeContentWithPreload(
                    "/com/example/upnext/hello-view.fxml",
                SceneTransitionUtil.TransitionType.SLIDE_RIGHT, 
                MainController.class,
                controller -> {} // No data loading needed for this view
            );
        } catch (IOException e) {
            System.err.println("Failed to load hello-view.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleArtistsButtonClick() {
        // We're already on the artists view, just refresh
        loadArtists();
    }

    @FXML
    private void handleCommunitiesButtonClick() {
        try {
            // Check the session type to determine which view to show
            SessionType currentSession = SessionManager.getInstance().getSessionType();

            if (currentSession == SessionType.ADMIN) {
                // Admin users see the communities-view
                SceneTransitionUtil.changeContentWithPreload(
                        "/com/example/upnext/communities-view.fxml",
                    SceneTransitionUtil.TransitionType.FADE, 
                    CommunitiesController.class,
                    controller -> controller.loadCommunities()
                );
            } else {
                // User and Artist users see the user-communities-view
                SceneTransitionUtil.changeContentWithPreload(
                        "/com/example/upnext/user-communities-view.fxml",
                    SceneTransitionUtil.TransitionType.FADE, 
                    UserCommunitiesController.class,
                    controller -> controller.loadCommunities()
                );
            }
        } catch (IOException e) {
            System.err.println("Failed to load communities view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCreateCommunityButtonClick() {
        try {
            // Navigate to the create community page
            SceneTransitionUtil.changeContentWithPreload(
                    "/com/example/upnext/create-community-view.fxml",
                SceneTransitionUtil.TransitionType.FADE, 
                AddCommunityController.class,
                controller -> {
                    // Set the theme mode
                    controller.setThemeMode(SessionManager.getInstance().isDarkMode());
                    // Generate a unique ID automatically
                    controller.handleGenerateId();
                }
            );
        } catch (IOException e) {
            System.err.println("Failed to load create-community-view.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadArtists() {
        DatabaseService dbService = new DatabaseService();
        List<Artist> artists = dbService.getAllArtists();
        displayArtists(artists);
    }

    private void displayArtists(List<Artist> artists) {
        // Clear existing content
        artistsGrid.getChildren().clear();
        
        // Reset grid constraints
        artistsGrid.getRowConstraints().clear();
        artistsGrid.getColumnConstraints().clear();

        if (artists.isEmpty()) {
            Label noArtistsLabel = new Label("No artists found");
            noArtistsLabel.getStyleClass().add("no-artists-label");
            artistsGrid.add(noArtistsLabel, 0, 0);
            return;
        }

        int column = 0;
        int row = 0;
        final int MAX_COLUMNS = 4; // Display 4 artists per row

        for (Artist artist : artists) {
            // Create artist card
            VBox artistBox = createArtistCard(artist);
            
            // Add to grid
            artistsGrid.add(artistBox, column, row);
            
            // Update grid position
            column++;
            if (column >= MAX_COLUMNS) {
                column = 0;
                row++;
            }
        }
    }

    private VBox createArtistCard(Artist artist) {
        // Create image view for artist
        ImageView imageView = new ImageView();
        imageView.setFitWidth(150);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);
        
        // Set artist image if available
        if (artist.getImage() != null) {
            Image image = new Image(new ByteArrayInputStream(artist.getImage()));
            imageView.setImage(image);
        } else {
            // Set default image if no image is available
            Image defaultImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/logo.png")));
            imageView.setImage(defaultImage);
        }
        
        // Apply rounded corners to the image
        imageView.getStyleClass().add("rounded-image-view");
        
        // Create a container for the image
        javafx.scene.layout.StackPane imageContainer = new javafx.scene.layout.StackPane();
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.getChildren().add(imageView);
        
        // Create label for artist name
        Label nameLabel = new Label(artist.getName());
        nameLabel.getStyleClass().add("name-label");
        
        // Create artist card
        VBox artistBox = new VBox(imageContainer, nameLabel);
        artistBox.setSpacing(10);
        artistBox.setAlignment(Pos.CENTER);
        artistBox.getStyleClass().add("artist-box");
        
        return artistBox;
    }
}