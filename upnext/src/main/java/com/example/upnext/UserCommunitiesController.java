package com.example.upnext;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.text.Font;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class UserCommunitiesController {

    @FXML
    private ImageView logoImage;

    @FXML
    private GridPane communitiesGrid;

    @FXML
    private TextField searchField;

    @FXML
    private ImageView searchIcon;

    @FXML
    private Button communitiesButton;

    @FXML
    private Button createCommunityButton;

    @FXML
    private Button artistsButton;

    private boolean isDarkMode = true; // Default is dark mode

    @FXML
    public void initialize() {
        Font.loadFont(getClass().getResourceAsStream("/Feather.ttf"), 18);
        Image logo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/logo.png")));
        logoImage.setImage(logo);

        // Initialize isDarkMode from SessionManager
        isDarkMode = SessionManager.getInstance().isDarkMode();

        // Set visibility of createCommunityButton based on session type
        SessionType currentSession = SessionManager.getInstance().getSessionType();
        if (currentSession == SessionType.ARTIST) {
            createCommunityButton.setVisible(true);
        } else {
            createCommunityButton.setVisible(false);
        }

        // Add right-click handler to Communities button if it exists
        if (communitiesButton != null) {
            communitiesButton.setOnMouseClicked(event -> {
                // No special handling needed for right-click in user communities view
                // as we're already in this view
            });

            // Highlight the current page's navigation label
            communitiesButton.getStyleClass().add("active");
        }

        // Set up search field listener
        if (searchField != null) {
            searchField.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    handleSearch();
                }
            });
        }

        // Register as a theme change listener
        SessionManager.getInstance().addThemeChangeListener(this::handleThemeChange);

        // Load communities
        loadCommunities();
    }

    /**
     * Handles theme changes from SessionManager.
     * 
     * @param isDarkMode true for dark mode, false for light mode
     */
    private void handleThemeChange(boolean isDarkMode) {
        this.isDarkMode = isDarkMode;
        // No need to update UI directly, as the root layout will handle that
        // This ensures that any new dialogs or views created by this controller will use the correct theme
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
        try {
            SceneTransitionUtil.changeContentWithPreload(
                    "/com/example/upnext/artists-view.fxml",
                SceneTransitionUtil.TransitionType.FADE, 
                ArtistsController.class,
                controller -> controller.loadArtists()
            );
        } catch (IOException e) {
            System.err.println("Failed to load artists-view.fxml: " + e.getMessage());
            e.printStackTrace();
        }
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
                    SceneTransitionUtil.TransitionType.SLIDE_LEFT, 
                    CommunitiesController.class,
                    controller -> controller.loadCommunities()
                );
            } else {
                // User and Artist users stay on user-communities-view
                // No need to navigate since we're already here
                // Just reload the communities to refresh the view
                loadCommunities();
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


    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText().trim();
        DatabaseService dbService = new DatabaseService();
        List<Community> communities;

        if (!searchTerm.isEmpty()) {
            // Use the same search method as CommunitiesController
            communities = dbService.searchCommunities(searchTerm);
            // Filter to only show verified communities (status 2)
            communities = communities.stream()
                .filter(community -> community.getStatus() == 2)
                .toList();
        } else {
            // If search field is empty, show all verified communities
            communities = dbService.getCommunitiesWithStatus2();
        }

        displayCommunities(communities);
    }

    public void loadCommunities() {
        DatabaseService dbService = new DatabaseService();
        List<Community> communities = dbService.getCommunitiesWithStatus2();
        displayCommunities(communities);
    }

    private void displayCommunities(List<Community> communities) {
        communitiesGrid.getChildren().clear();

        int column = 0;
        int row = 0;
        int maxColumns = 3;

        for (Community community : communities) {
            ImageView imageView = createCommunityImageView(community);

            Label nameLabel = new Label(community.getName());
            nameLabel.getStyleClass().add("name-label");

            VBox communityBox = new VBox(imageView, nameLabel);
            communityBox.setSpacing(10);
            communityBox.setAlignment(Pos.CENTER);
            communityBox.getStyleClass().add("user-community-box");

            // Add click handler to navigate to community detail view
            final int communityId = community.getId(); // Create a final copy for lambda
            communityBox.setOnMouseClicked(event -> {
                try {
                    CommunityDetailController controller = SceneTransitionUtil.changeContent(
                            "/com/example/upnext/community-detail-view.fxml",
                        SceneTransitionUtil.TransitionType.SLIDE_LEFT,
                        CommunityDetailController.class
                    );
                    controller.loadCommunityDetails(communityId);
                } catch (IOException e) {
                    System.err.println("Failed to load community-detail-view.fxml: " + e.getMessage());
                    e.printStackTrace();
                }
            });

            communitiesGrid.add(communityBox, column, row);

            column++;
            if (column >= maxColumns) {
                column = 0;
                row++;
            }
        }
    }

    private ImageView createCommunityImageView(Community community) {
        ImageView imageView = new ImageView();
        byte[] imageData = community.getImage();
        if (imageData != null) {
            // Load image with size constraints to prevent loading overly large images
            // This improves performance for large images while maintaining display quality
            Image image = new Image(new ByteArrayInputStream(imageData), 300, 300, true, true);
            imageView.setImage(image);
        }
        imageView.setFitWidth(250);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(true);

        // Apply rounded corners using CSS
        imageView.getStyleClass().add("rounded-image-view");

        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(10);
        dropShadow.setOffsetX(0);
        dropShadow.setOffsetY(0);
        dropShadow.setColor(javafx.scene.paint.Color.rgb(0, 0, 0, 0.3));
        imageView.setEffect(dropShadow);

        return imageView;
    }

    /**
     * Sets the theme mode for this controller.
     * 
     * @param isDarkMode true for dark mode, false for light mode
     */
    public void setThemeMode(boolean isDarkMode) {
        this.isDarkMode = isDarkMode;
    }
}
