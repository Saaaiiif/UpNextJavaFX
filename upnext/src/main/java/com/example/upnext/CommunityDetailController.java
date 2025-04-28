package com.example.upnext;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.web.WebView;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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

    @FXML
    private WebView spotifyWebView;

    @FXML
    private Button communitiesButton;

    @FXML
    private Button artistsButton;

    @FXML
    private Label followersCountLabel;

    private int communityId;
    private String currentTrackId;
    private boolean isPlaying = false;
    private boolean isDarkMode = true; // Default is dark mode
    private InstagramService instagramService = new InstagramService();

    @FXML
    public void initialize() {
        // Load logo
        Image logo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/logo.png")));
        logoImage.setImage(logo);

        // Initialize isDarkMode from SessionManager
        isDarkMode = SessionManager.getInstance().isDarkMode();

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
        spotifyWebView.setVisible(false);
        spotifyWebView.setManaged(false);
        // Check if parent is not null before applying style class
        if (spotifyWebView.getParent() != null) {
            spotifyWebView.getParent().getStyleClass().remove("spotify-player-visible");
            spotifyWebView.getParent().getStyleClass().add("spotify-player-hidden");
        }

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
        // Run in a background thread to avoid freezing the UI
        // Use a daemon thread to ensure it doesn't prevent application shutdown
        Thread spotifyThread = new Thread(() -> {
            String artistId = SpotifyService.getArtistId(artistName);
            if (artistId != null) {
                Map<String, String> trackInfo = SpotifyService.getTopTrack(artistId);
                if (trackInfo != null) {
                    currentTrackId = trackInfo.get("id");

                    // Update UI on the JavaFX application thread
                    javafx.application.Platform.runLater(() -> {
                        // Update track info
                        trackNameLabel.setText(trackInfo.get("name"));
                        artistNameLabel.setText(trackInfo.get("artist"));
                        nowPlayingLabel.setText("Now Playing");

                        // Show the WebView and restore the drop shadow effect
                        spotifyWebView.setVisible(true);
                        spotifyWebView.setManaged(true);
                        // Check if parent is not null before applying style class
                        if (spotifyWebView.getParent() != null) {
                            spotifyWebView.getParent().getStyleClass().remove("spotify-player-hidden");
                            spotifyWebView.getParent().getStyleClass().add("spotify-player-visible");
                        }

                        // Load album cover
                        String imageUrl = trackInfo.get("imageUrl");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            trackCoverImage.setImage(new Image(imageUrl));
                        }

                        // Load the Spotify player automatically
                        loadSpotifyPlayer();
                    });
                } else {
                    // No track found for this artist
                    javafx.application.Platform.runLater(() -> {
                        nowPlayingLabel.setText("No song to play");
                        trackNameLabel.setText("");
                        artistNameLabel.setText("");
                        // Hide the WebView and remove the drop shadow effect
                        spotifyWebView.setVisible(false);
                        spotifyWebView.setManaged(false);
                        // Check if parent is not null before applying style class
                        if (spotifyWebView.getParent() != null) {
                            spotifyWebView.getParent().getStyleClass().remove("spotify-player-visible");
                            spotifyWebView.getParent().getStyleClass().add("spotify-player-hidden");
                        }
                    });
                }
            } else {
                // Artist not found
                javafx.application.Platform.runLater(() -> {
                    nowPlayingLabel.setText("No song to play");
                    trackNameLabel.setText("");
                    artistNameLabel.setText("");
                    // Hide the WebView and remove the drop shadow effect
                    spotifyWebView.setVisible(false);
                    spotifyWebView.setManaged(false);
                    // Check if parent is not null before applying style class
                    if (spotifyWebView.getParent() != null) {
                        spotifyWebView.getParent().getStyleClass().remove("spotify-player-visible");
                        spotifyWebView.getParent().getStyleClass().add("spotify-player-hidden");
                    }
                });
            }
        });

        // Set as daemon thread so it doesn't prevent application shutdown
        spotifyThread.setDaemon(true);
        spotifyThread.start();
    }

    private void loadSpotifyPlayer() {
        if (currentTrackId != null) {
            // Load Spotify embed player
            String embedUrl = SpotifyService.getTrackEmbedUrl(currentTrackId);
            spotifyWebView.getEngine().load(embedUrl);

            // Add a load listener to ensure the WebView is fully loaded
            spotifyWebView.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == javafx.concurrent.Worker.State.SUCCEEDED) {
                    // Now it's safe to interact with the iframe
                    isPlaying = true;

                    // Remove any black box overlay by setting background to transparent
                    spotifyWebView.getEngine().executeScript(
                        "document.body.style.backgroundColor = 'black';" +
                        "var iframes = document.querySelectorAll('iframe');" +
                        "for(var i = 0; i < iframes.length; i++) {" +
                        "    try {" +
                        "        iframes[i].style.backgroundColor = 'transparent';" +
                        "        if(iframes[i].contentDocument) {" +
                        "            iframes[i].contentDocument.body.style.backgroundColor = 'transparent';" +
                        "        }" +
                        "    } catch(e) { console.log('Cannot access iframe content: ' + e); }" +
                        "}"
                    );

                    // Send play command to the iframe
                    spotifyWebView.getEngine().executeScript(
                        "var iframe = document.querySelector('iframe'); " +
                        "if (iframe && iframe.contentWindow) { " +
                        "    iframe.contentWindow.postMessage('{\"command\":\"play\"}', '*'); " +
                        "}"
                    );
                } else if (newValue == javafx.concurrent.Worker.State.FAILED) {
                    // Handle load failure
                    isPlaying = false;
                    System.err.println("Failed to load Spotify embed player");
                }
            });
        }
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
     */
    private void pauseSpotifyPlayer() {
        if (spotifyWebView != null) {
            if (isPlaying) {
                // Execute JavaScript to pause the player first
                spotifyWebView.getEngine().executeScript(
                    "var iframe = document.querySelector('iframe'); " +
                    "if (iframe && iframe.contentWindow) { " +
                    "    iframe.contentWindow.postMessage('{\"command\":\"pause\"}', '*'); " +
                    "}"
                );
            }

            // Load about:blank to completely stop the player and free resources
            spotifyWebView.getEngine().load("about:blank");
            isPlaying = false;
            currentTrackId = null;
        }
    }

    @FXML
    private void handleUpNextButtonClick() {
        try {
            // Pause the player before navigating away
            pauseSpotifyPlayer();

            SceneTransitionUtil.changeContent(
                    "/com/example/upnext/hello-view.fxml",
                SceneTransitionUtil.TransitionType.SLIDE_RIGHT, 
                MainController.class
            );
        } catch (IOException e) {
            System.err.println("Failed to load hello-view.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleArtistsButtonClick() {
        try {
            // Pause the player before navigating away
            pauseSpotifyPlayer();

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
            // Pause the player before navigating away
            pauseSpotifyPlayer();

            // Check the session type to determine which view to show
            SessionType currentSession = SessionManager.getInstance().getSessionType();

            if (currentSession == SessionType.ADMIN) {
                // Admin users see the communities-view
                CommunitiesController controller = SceneTransitionUtil.changeContent(
                        "/com/example/upnext/communities-view.fxml",
                    SceneTransitionUtil.TransitionType.SLIDE_LEFT, 
                    CommunitiesController.class
                );
                controller.loadCommunities();
            } else {
                // User and Artist users see the user-communities-view
                UserCommunitiesController controller = SceneTransitionUtil.changeContent(
                        "/com/example/upnext/user-communities-view.fxml",
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

            UserCommunitiesController controller = SceneTransitionUtil.changeContent(
                    "/com/example/upnext/user-communities-view.fxml",
                SceneTransitionUtil.TransitionType.SLIDE_RIGHT, 
                UserCommunitiesController.class
            );
            controller.loadCommunities();
        } catch (IOException e) {
            System.err.println("Failed to load user-communities-view.fxml: " + e.getMessage());
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
}
