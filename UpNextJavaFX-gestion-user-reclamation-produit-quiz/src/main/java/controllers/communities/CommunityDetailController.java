package controllers.communities;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.text.Font;
import javafx.scene.web.WebView;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import edu.up_next.entities.User;

public class CommunityDetailController {

    @FXML
    private ImageView logoImage;

    @FXML
    private ImageView communityImage;

    @FXML
    private Label communityName;

    @FXML
    private Label communityDescription;

    @FXML
    private Label keywordsLabel;

    @FXML
    private GridPane artistsGrid;

    @FXML
    private ImageView trackCoverImage;

    @FXML
    private Label trackNameLabel;

    @FXML
    private Label artistNameLabel;

    @FXML
    private Label nowPlayingLabel;

    //@FXML
    //private WebView spotifyWebView;

    @FXML
    private Button communitiesButton;

    @FXML
    private Button artistsButton;

    @FXML
    private Label followersCountLabel;

    @FXML
    private Button mediaPlayerButton;

    @FXML
    private Button commentsButton;

    @FXML
    private VBox mediaPlayerContent;

    @FXML
    private VBox commentsContent;

    @FXML
    private VBox commentsContainer;

    @FXML
    private TextField commentTextField;

    private int communityId;
    private String currentTrackId;
    private boolean isPlaying = false;
    private boolean isDarkMode = true; // Default is dark mode
    private InstagramService instagramService = new InstagramService();
    private User currentUser; // Current logged-in user

    @FXML
    public void initialize() {
        // Load Feather font
        Font.loadFont(getClass().getResourceAsStream("/Feather.ttf"), 18);
                // Register the font as "Feather Bold" for use in CSS
                Font boldFont = Font.font("Feather Bold", 18);

        // Load logo
        Image logo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/logo.png")));
        if (logoImage != null) {
            logoImage.setImage(logo);
        }

        // Initialize isDarkMode from SessionManager
        isDarkMode = SessionManager.getInstance().isDarkMode();

        // Get the current user from SessionManager
        this.currentUser = SessionManager.getInstance().getCurrentUser();

        // Highlight the current page's navigation label
        if (communitiesButton != null) {
            communitiesButton.getStyleClass().add("active");
        }

        // Register as a theme change listener
        SessionManager.getInstance().addThemeChangeListener(this::handleThemeChange);
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


    public void loadCommunityDetails(int communityId) {
        this.communityId = communityId;
        DatabaseService dbService = new DatabaseService();

        // Set default text for nowPlayingLabel
        nowPlayingLabel.setText("No song to play");

        // Hide the WebView and remove the drop shadow effect by default
        //spotifyWebView.setVisible(false);
       // spotifyWebView.setManaged(false);
        // Check if parent is not null before applying style class
        //if (spotifyWebView.getParent() != null) {
        //    spotifyWebView.getParent().getStyleClass().remove("spotify-player-visible");
       //     spotifyWebView.getParent().getStyleClass().add("spotify-player-hidden");
       // }

        // Initialize the media player button as active and comments button as inactive
        mediaPlayerButton.getStyleClass().add("active-toggle");
        commentsButton.getStyleClass().remove("active-toggle");

        // Show media player content and hide comments content by default
        mediaPlayerContent.setVisible(true);
        mediaPlayerContent.setManaged(true);
        commentsContent.setVisible(false);
        commentsContent.setManaged(false);

        // Show "Now Playing" label by default since media player content is visible
        nowPlayingLabel.setVisible(true);
        nowPlayingLabel.setManaged(true);

        // Load community details
        Community community = dbService.getCommunityById(communityId);
        if (community != null) {
            displayCommunityDetails(community);

            // Load related artists
            List<Artist> artists = dbService.getArtistsByCommunityId(communityId);
            displayArtists(artists);

            // Load Spotify track for the first artist if available
            if (!artists.isEmpty()) {
                loadSpotifyTrack(artists.get(0).getName());
            }
        }
    }

    private void loadSpotifyTrack(String artistName) {
        // Spotify API is disabled
        javafx.application.Platform.runLater(() -> {
            nowPlayingLabel.setText("Spotify API is disabled");
            trackNameLabel.setText("Music player is currently unavailable");
            artistNameLabel.setText(artistName);

            // Hide the WebView and remove the drop shadow effect
           // spotifyWebView.setVisible(false);
           // spotifyWebView.setManaged(false);

            // Check if parent is not null before applying style class
           // if (spotifyWebView.getParent() != null) {
           //     spotifyWebView.getParent().getStyleClass().remove("spotify-player-visible");
          //      spotifyWebView.getParent().getStyleClass().add("spotify-player-hidden");
          //  }

            // Set a placeholder image for the track cover
            try {
                java.io.InputStream is = getClass().getResourceAsStream("/logo.png");
                if (is != null) {
                    trackCoverImage.setImage(new Image(is));
                } else {
                    System.err.println("Failed to load logo image: Resource not found");
                }
            } catch (Exception e) {
                System.err.println("Failed to load logo image: " + e.getMessage());
            }
        });
    }

    private void loadSpotifyPlayer() {
        // Spotify API is disabled, so we don't load the player
        // This method is kept for compatibility with existing code
        isPlaying = false;
        currentTrackId = null;
    }

    private void displayCommunityDetails(Community community) {
        // Set community name
        communityName.setText(community.getName());

        // Set community image
        byte[] imageData = community.getImage();
        if (imageData != null) {
            // Load image with size constraints to prevent loading overly large images
            // This improves performance for large images while maintaining display quality
            Image image = new Image(new ByteArrayInputStream(imageData), 500, 500, true, true);
            communityImage.setImage(image);
        }

        // Fetch and display Instagram followers count if social link is available
        String socialLink = community.getSocial();
        if (socialLink != null && !socialLink.isEmpty() && socialLink.contains("instagram.com")) {
            // Clear previous followers count
            followersCountLabel.setText("");

            // Use a separate thread to avoid blocking the UI
            new Thread(() -> {
                try {
                    int followersCount = instagramService.getFollowerCount(socialLink);
                    if (followersCount >= 0) {
                        String formattedCount = instagramService.formatFollowerCount(followersCount);
                        // Update the UI on the JavaFX application thread
                        javafx.application.Platform.runLater(() -> {
                            followersCountLabel.setText("(" + formattedCount + " followers)");
                        });
                    }
                } catch (Exception e) {
                    System.err.println("Error fetching Instagram followers: " + e.getMessage());
                    e.printStackTrace();
                }
            }).start();
        } else {
            // No Instagram link available
            followersCountLabel.setText("");
        }

        // Set community description
        String description = community.getDescription();
        if (description != null && !description.isEmpty()) {
            communityDescription.setText(description);

            // Extract and display keywords if not already available
            if (community.getKeywords() == null || community.getKeywords().isEmpty()) {
                extractAndDisplayKeywords(community);
            } else {
                displayKeywords(community.getKeywords());
            }
        } else {
            communityDescription.setText("No description available");
            keywordsLabel.setText("");
        }
    }

    /**
     * Extracts keywords from the community description using OpenAI API
     * and displays them as hashtags.
     *
     * @param community The community to extract keywords from
     */
    private void extractAndDisplayKeywords(Community community) {
        String description = community.getDescription();
        if (description == null || description.isEmpty()) {
            keywordsLabel.setText("");
            return;
        }

        // Show loading message
        keywordsLabel.setText("Extracting keywords...");

        // Use a separate thread to avoid blocking the UI
        new Thread(() -> {
            try {
                // Get API key from environment variable or configuration
                String apiKey = System.getenv("OPENAI_API_KEY");
                if (apiKey == null || apiKey.isEmpty()) {
                    // Use a default key for testing or development
                    apiKey = "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"; // Replace with your actual API key
                }

                OpenAIService openAIService = new OpenAIService(apiKey);
                List<String> keywords = openAIService.extractKeywords(description);

                // Update the community with the extracted keywords
                community.setKeywords(keywords);

                // Update the database with the extracted keywords
                DatabaseService dbService = new DatabaseService();
                // Note: You would need to add a method to DatabaseService to save keywords
                // dbService.updateCommunityKeywords(community.getId(), keywords);

                // Update the UI on the JavaFX application thread
                javafx.application.Platform.runLater(() -> {
                    displayKeywords(keywords);
                });
            } catch (Exception e) {
                System.err.println("Error extracting keywords: " + e.getMessage());
                e.printStackTrace();

                // Update the UI on the JavaFX application thread
                javafx.application.Platform.runLater(() -> {
                    keywordsLabel.setText("");
                });
            }
        }).start();
    }

    /**
     * Displays the keywords as hashtags.
     *
     * @param keywords The list of keywords to display
     */
    private void displayKeywords(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            keywordsLabel.setText("");
            return;
        }

        // Format keywords as hashtags
        String hashtagText = keywords.stream()
                .map(keyword -> "#" + keyword.trim().replace(" ", ""))
                .collect(Collectors.joining(" "));

        keywordsLabel.setText(hashtagText);
    }

    private void displayArtists(List<Artist> artists) {
        artistsGrid.getChildren().clear();

        if (artists.isEmpty()) {
            Label noArtistsLabel = new Label("No related artists found");
            noArtistsLabel.getStyleClass().add("no-artists-label");
            artistsGrid.add(noArtistsLabel, 0, 0);
            return;
        }

        int column = 0;
        int row = 0;
        int maxColumns = 3;

        for (Artist artist : artists) {
            ImageView imageView = createArtistImageView(artist);

            Label nameLabel = new Label(artist.getName());
            nameLabel.getStyleClass().add("artist-name-label");

            VBox artistBox = new VBox(imageView, nameLabel);
            artistBox.setSpacing(10);
            artistBox.setAlignment(Pos.CENTER);
            artistBox.getStyleClass().add("artist-box");

            // Add click handler to navigate to artist detail view
            final int artistId = artist.getId();
            artistBox.setOnMouseClicked(event -> {
                try {
                    // Navigate to artist detail view
                    ArtistDetailController controller = SceneTransitionUtil.changeContent(
                            "/com/example/upnext/artist-detail-view.fxml",
                        SceneTransitionUtil.TransitionType.FADE, 
                        ArtistDetailController.class
                    );
                    controller.loadArtistDetails(artistId);
                } catch (IOException e) {
                    System.err.println("Failed to load artist-detail-view.fxml: " + e.getMessage());
                    e.printStackTrace();
                }
            });

            artistsGrid.add(artistBox, column, row);

            column++;
            if (column >= maxColumns) {
                column = 0;
                row++;
            }
        }
    }

    private ImageView createArtistImageView(Artist artist) {
        ImageView imageView = new ImageView();
        byte[] imageData = artist.getImage();
        if (imageData != null) {
            // Load image with size constraints to prevent loading overly large images
            // This improves performance for large images while maintaining display quality
            Image image = new Image(new ByteArrayInputStream(imageData), 150, 150, true, true);
            imageView.setImage(image);
        }
        imageView.setFitWidth(150);
        imageView.setFitHeight(150);
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
     * Helper method to stop the Spotify player completely
     * (Currently a no-op since Spotify API is disabled)
     */
    private void pauseSpotifyPlayer() {
        // Spotify API is disabled, so we don't need to pause the player
        // Just reset the state variables
        isPlaying = false;
        currentTrackId = null;
    }

    @FXML
    private void handleUpNextButtonClick() {
        try {
            // Pause the player before navigating away
            pauseSpotifyPlayer();

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
            // Pause the player before navigating away
            pauseSpotifyPlayer();

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
            // Pause the player before navigating away
            pauseSpotifyPlayer();

            // Check the session type to determine which view to show
            SessionType currentSession = SessionManager.getInstance().getSessionType();

            if (currentSession == SessionType.ADMIN) {
                // Admin users see the communities-view
                CommunitiesController controller = SceneTransitionUtil.changeContent(
                        "/views/communities/communities-view.fxml",
                    SceneTransitionUtil.TransitionType.SLIDE_LEFT, 
                    CommunitiesController.class
                );
                controller.loadCommunities();
            } else {
                // User and Artist users see the user-communities-view
                UserCommunitiesController controller = SceneTransitionUtil.changeContent(
                        "/views/communities/user-communities-view.fxml",
                    SceneTransitionUtil.TransitionType.SLIDE_LEFT, 
                    UserCommunitiesController.class
                );
                controller.loadCommunities();
            }
        } catch (IOException e) {
            System.err.println("Failed to load communities view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackButtonClick() {
        try {
            // Pause the player before navigating away
            pauseSpotifyPlayer();

            // Load the home view directly
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
    private void handleRecentViewButtonClick() {
        try {
            // Pause the player before navigating away
            pauseSpotifyPlayer();

            // Check the session type to determine which view to show
            SessionType currentSession = SessionManager.getInstance().getSessionType();

            if (currentSession == SessionType.ADMIN) {
                // Admin users see the communities-view
                SceneTransitionUtil.changeContentWithPreload(
                        "/views/communities/communities-view.fxml",
                    SceneTransitionUtil.TransitionType.FADE, 
                    CommunitiesController.class,
                    controller -> controller.loadCommunities()
                );
            } else {
                // User and Artist users see the user-communities-view
                SceneTransitionUtil.changeContentWithPreload(
                        "/views/communities/user-communities-view.fxml",
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

    /**
     * Sets the theme mode for this controller.
     * 
     * @param isDarkMode true for dark mode, false for light mode
     */
    public void setThemeMode(boolean isDarkMode) {
        this.isDarkMode = isDarkMode;
    }

    /**
     * Handles the media player button click.
     * Shows the media player content and hides the comments content.
     */
    @FXML
    private void handleMediaPlayerButtonClick() {
        // Show media player content
        mediaPlayerContent.setVisible(true);
        mediaPlayerContent.setManaged(true);

        // Hide comments content
        commentsContent.setVisible(false);
        commentsContent.setManaged(false);

        // Show "Now Playing" label
        nowPlayingLabel.setVisible(true);
        nowPlayingLabel.setManaged(true);

        // Update button styles
        mediaPlayerButton.getStyleClass().add("active-toggle");
        commentsButton.getStyleClass().remove("active-toggle");
    }

    /**
     * Handles the comments button click.
     * Shows the comments content and hides the media player content.
     */
    @FXML
    private void handleCommentsButtonClick() {
        // Show comments content
        commentsContent.setVisible(true);
        commentsContent.setManaged(true);

        // Hide media player content
        mediaPlayerContent.setVisible(false);
        mediaPlayerContent.setManaged(false);

        // Hide "Now Playing" label
        nowPlayingLabel.setVisible(false);
        nowPlayingLabel.setManaged(false);

        // Update button styles
        commentsButton.getStyleClass().add("active-toggle");
        mediaPlayerButton.getStyleClass().remove("active-toggle");

        // Load comments if not already loaded
        loadComments();
    }

    /**
     * Handles the post comment button click.
     * Adds a new comment to the community.
     */
    @FXML
    private void handlePostCommentButtonClick() {
        String commentText = commentTextField.getText().trim();
        if (commentText.isEmpty()) {
            return;
        }

        // Get the current username and user ID
        String username = "Anonymous";
        int userId = -1;

        if (currentUser != null) {
            username = currentUser.getFirstname() + " " + currentUser.getLastname();
            userId = currentUser.getId();
        }

        // Add the comment to the database
        DatabaseService dbService = new DatabaseService();
        int commentId = dbService.addComment(communityId, commentText, username, userId);

        if (commentId != -1) {
            // Clear the text field
            commentTextField.clear();

            // Reload comments to show the new comment
            loadComments();
        }
    }

    /**
     * Loads comments for the current community.
     */
    private void loadComments() {
        // Clear existing comments
        commentsContainer.getChildren().clear();

        // Get comments from the database
        DatabaseService dbService = new DatabaseService();
        List<Comment> comments = dbService.getCommentsByCommunityId(communityId);

        // Check if we're in admin mode
        boolean isAdmin = SessionManager.getInstance().isAdminSession();

        if (comments.isEmpty()) {
            // Show a message if there are no comments
            Label noCommentsLabel = new Label("No comments yet. Be the first to comment!");
            noCommentsLabel.getStyleClass().add("no-comments-label");
            commentsContainer.getChildren().add(noCommentsLabel);
        } else {
            // Add each comment to the container
            for (Comment comment : comments) {
                // Create a VBox for the comment
                VBox commentBox = new VBox(5);
                commentBox.getStyleClass().add("comment-box");

                // Create a label for the username
                Label usernameLabel = new Label(comment.getUsername());
                usernameLabel.getStyleClass().add("comment-username");

                // Create a label for the comment text
                Label commentLabel = new Label(comment.getComment());
                commentLabel.getStyleClass().add("comment-text");
                commentLabel.setWrapText(true);

                // Add the labels to the comment box
                commentBox.getChildren().addAll(usernameLabel, commentLabel);

                // If admin, add context menu for comment management
                if (isAdmin) {
                    // Create context menu
                    ContextMenu contextMenu = new ContextMenu();
                    MenuItem deleteItem = new MenuItem("Delete Comment");
                    contextMenu.getItems().add(deleteItem);

                    // Set action for delete item
                    deleteItem.setOnAction(e -> {
                        // Delete the comment from the database
                        boolean success = dbService.deleteComment(comment.getId());
                        if (success) {
                            // Reload comments to reflect the changes
                            loadComments();
                        } else {
                            // Show error alert if deletion failed
                            Alert alert = new Alert(AlertType.ERROR);
                            alert.setTitle("Error");
                            alert.setHeaderText("Delete Failed");
                            alert.setContentText("Failed to delete the comment. Please try again.");
                            alert.showAndWait();
                        }
                    });

                    // Store the comment in the user data for reference
                    commentBox.setUserData(comment);

                    // Show context menu on right-click
                    commentBox.setOnContextMenuRequested(event -> {
                        contextMenu.show(commentBox, event.getScreenX(), event.getScreenY());
                    });
                }

                // Add the comment box to the container
                commentsContainer.getChildren().add(commentBox);
            }
        }
    }
}
