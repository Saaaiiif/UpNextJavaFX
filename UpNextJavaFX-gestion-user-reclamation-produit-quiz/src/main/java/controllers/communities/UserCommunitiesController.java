package controllers.communities;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.text.Font;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.animation.Timeline;
import javafx.animation.KeyValue;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import edu.up_next.entities.User;

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

    @FXML
    private ComboBox<String> genreComboBox;

    @FXML
    private VBox statusSummaryBox;

    @FXML
    private HBox addCardContainer;

    @FXML
    private CheckBox statusFilter0;

    @FXML
    private CheckBox statusFilter1;

    @FXML
    private CheckBox statusFilter2;

    private boolean isDarkMode = true; // Default is dark mode
    private User currentUser; // Current logged-in user

    @FXML
    public void initialize() {
        Font.loadFont(getClass().getResourceAsStream("/Feather.ttf"), 18);
                // Register the font as "Feather Bold" for use in CSS
                Font boldFont = Font.font("Feather Bold", 18);
        Image logo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/logo.png")));
        logoImage.setImage(logo);

        // Initialize isDarkMode from SessionManager
        isDarkMode = SessionManager.getInstance().isDarkMode();

        // Get the current user from SessionManager
        this.currentUser = SessionManager.getInstance().getCurrentUser();

        // Set visibility of createCommunityButton based on session type
        SessionType currentSession = SessionManager.getInstance().getSessionType();
        if (currentSession == SessionType.ARTIST) {
            createCommunityButton.setVisible(true);
        } else {
            createCommunityButton.setVisible(false);
        }

        // Hide filter bar and chart for non-admin users
        boolean isAdmin = SessionManager.getInstance().isAdminSession();

        // Hide status summary box (chart) for non-admin users
        if (statusSummaryBox != null) {
            statusSummaryBox.setVisible(isAdmin);
            statusSummaryBox.setManaged(isAdmin);
        }

        // Hide add card container for non-admin users
        if (addCardContainer != null) {
            addCardContainer.setVisible(isAdmin);
            addCardContainer.setManaged(isAdmin);
        }

        // Find and hide filter bars for non-admin users
        if (!isAdmin) {
            // Get the parent of the search bar (which is the HBox containing all filter bars)
            if (searchField != null && searchField.getParent() != null && searchField.getParent().getParent() != null) {
                HBox filterContainer = (HBox) searchField.getParent().getParent();

                // Hide all filter bars except the search bar
                for (Node node : filterContainer.getChildren()) {
                    if (node instanceof HBox && ((HBox) node).getStyleClass().contains("filter-bar")) {
                        node.setVisible(false);
                        node.setManaged(false);
                    }
                }
            }
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

        // Status filter listeners removed as filter by status box has been removed

        // Populate genre dropdown
        populateGenreDropdown();

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
            // Load the home view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/home.fxml"));
            Parent root = loader.load();

            // Get the home controller and set the user from SessionManager
            controllers.home homeController = loader.getController();
            homeController.setUser(SessionManager.getInstance().getCurrentUser());

            // Set the scene
            Stage stage = (Stage) logoImage.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Failed to load home.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleArtistsButtonClick() {
        try {
            SceneTransitionUtil.changeContentWithPreload(
                    "/views/communities/artists-view.fxml",
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
                        "/views/communities/communities-view.fxml",
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
                    "/views/communities/create-community-view.fxml",
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
        String selectedGenre = genreComboBox.getValue();
        DatabaseService dbService = new DatabaseService();
        List<Community> communities;

        if (!searchTerm.isEmpty()) {
            // Use the same search method as CommunitiesController
            communities = dbService.searchCommunities(searchTerm);
        } else {
            // If search field is empty, show all communities
            communities = dbService.getCommunities();
        }

        // Filter by genre if a specific genre is selected
        if (selectedGenre != null && !selectedGenre.equals("All Genres")) {
            communities = communities.stream()
                .filter(community -> selectedGenre.equals(community.getGenre()))
                .collect(Collectors.toList());
        }

        // Always filter by status (filter by status box has been removed)
        communities = filterCommunitiesByStatus(communities);

        displayCommunities(communities);
    }

    public void loadCommunities() {
        DatabaseService dbService = new DatabaseService();
        List<Community> communities = dbService.getCommunities();

        // Apply genre filter if a specific genre is selected
        String selectedGenre = genreComboBox != null ? genreComboBox.getValue() : null;
        if (selectedGenre != null && !selectedGenre.equals("All Genres")) {
            communities = communities.stream()
                .filter(community -> selectedGenre.equals(community.getGenre()))
                .collect(Collectors.toList());
        }

        // Always filter by status (filter by status box has been removed)
        communities = filterCommunitiesByStatus(communities);

        // Update the status summary with ALL communities, not just filtered ones, but only for admin users
        boolean isAdmin = SessionManager.getInstance().isAdminSession();
        if (isAdmin) {
            List<Community> allCommunities = dbService.getCommunities();
            updateStatusSummary(allCommunities);
        }

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
                            "/views/communities/community-detail-view.fxml",
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

    /**
     * Filters communities based on the status filter checkboxes.
     * For non-admin users, only verified communities (status = 2) are shown.
     * 
     * @param communities The list of communities to filter
     * @return The filtered list of communities
     */
    private List<Community> filterCommunitiesByStatus(List<Community> communities) {
        boolean isAdmin = SessionManager.getInstance().isAdminSession();

        // For non-admin users, only show verified communities
        if (!isAdmin) {
            return communities.stream()
                .filter(community -> community.getStatus() == 2) // Only show verified communities
                .collect(Collectors.toList());
        }

        // For admin users, show all communities (filter by status box has been removed)
        return communities;
    }


    // Constants for status labels
    private static final String STATUS_UNVERIFIED = "Unverified";
    private static final String STATUS_PENDING = "Pending";
    private static final String STATUS_VERIFIED = "Verified";

    /**
     * Updates the status summary with community status data.
     * 
     * @param communities The list of communities to analyze
     */
    private void updateStatusSummary(List<Community> communities) {
        if (statusSummaryBox == null) return;

        // Count communities by status
        int unverifiedCount = 0;
        int pendingCount = 0;
        int verifiedCount = 0;

        for (Community community : communities) {
            int status = community.getStatus();
            if (status == 0) {
                unverifiedCount++;
            } else if (status == 1) {
                pendingCount++;
            } else if (status == 2) {
                verifiedCount++;
            }
        }

        // Update the status summary
        updateStatusSummary(verifiedCount, pendingCount, unverifiedCount);
    }

    /**
     * Updates the status summary with the given counts.
     * 
     * @param verified The number of verified communities
     * @param pending The number of pending communities
     * @param unverified The number of unverified communities
     */
    private void updateStatusSummary(int verified, int pending, int unverified) {
        statusSummaryBox.getChildren().clear();

        int total = verified + pending + unverified;
        if (total == 0) return;

        int delayIncrementMs = 150; // Delay between each bar (in milliseconds)

        addAnimatedStatusBar("✔ Verified", verified, total, "#4CAF50", 0 * delayIncrementMs);     // Green
        addAnimatedStatusBar("⏳ Pending", pending, total, "#FFC107", 1 * delayIncrementMs);      // Amber
        addAnimatedStatusBar("❌ Unverified", unverified, total, "#F44336", 2 * delayIncrementMs); // Red
    }

    /**
     * Adds an animated status bar to the status summary box.
     * 
     * @param label The label for the status bar
     * @param value The value for the status bar
     * @param total The total value for calculating the percentage
     * @param colorHex The color for the status bar
     * @param delayMs The delay before starting the animation (in milliseconds)
     */
    private void addAnimatedStatusBar(String label, int value, int total, String colorHex, int delayMs) {
        double percent = (double) value / total;
        int percentInt = (int) (percent * 100);

        Label nameLabel = new Label(label);
        nameLabel.getStyleClass().add("status-bar-name-label");

        Region bar = new Region();
        bar.setPrefHeight(8);
        bar.setPrefWidth(0); // Start with width 0
        bar.setOpacity(0);   // Start fully transparent
        bar.setStyle("-fx-background-color: " + colorHex + "; -fx-background-radius: 5;");

        Label percentLabel = new Label(percentInt + "%");
        percentLabel.getStyleClass().add("status-bar-percent-label");

        HBox row = new HBox(8, nameLabel, bar, percentLabel);
        row.setAlignment(Pos.CENTER_LEFT);

        statusSummaryBox.getChildren().add(row);

        // Animate width and opacity
        Timeline timeline = new Timeline();

        double targetWidth = percent * 180; // Max width 180

        KeyValue widthValue = new KeyValue(bar.prefWidthProperty(), targetWidth);
        KeyValue fadeValue = new KeyValue(bar.opacityProperty(), 1.0); // Fade to fully visible

        // Add delay to the animation
        KeyFrame keyFrame = new KeyFrame(Duration.millis(800), widthValue, fadeValue);

        timeline.getKeyFrames().add(keyFrame);

        // Set the delay before starting the animation
        timeline.setDelay(Duration.millis(delayMs));

        timeline.play();
    }

    /**
     * Populates the genre dropdown with genres from the database.
     */
    private void populateGenreDropdown() {
        DatabaseService dbService = new DatabaseService();
        List<String> genres = dbService.getAllGenres();

        // Add "All Genres" option at the beginning
        ObservableList<String> genreOptions = FXCollections.observableArrayList();
        genreOptions.add("All Genres");
        genreOptions.addAll(genres);

        genreComboBox.setItems(genreOptions);
        genreComboBox.getSelectionModel().selectFirst(); // Select "All Genres" by default
    }

    /**
     * Handles genre filter selection.
     */
    @FXML
    private void handleGenreFilter() {
        handleSearch();
    }
}
