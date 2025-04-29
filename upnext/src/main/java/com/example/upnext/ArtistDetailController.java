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

public class ArtistDetailController {

    @FXML
    private ImageView logoImage;

    @FXML
    private ImageView artistImage;

    @FXML
    private Label artistName;

    @FXML
    private Label artistDescription;

    @FXML
    private GridPane communitiesGrid;

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
    private Label monthlyListenersLabel;

    @FXML
    private Label keywordsLabel;

    private int artistId;
    private String currentTrackId;
    private boolean isPlaying = false;
    private boolean isDarkMode = true; // Default is dark mode

    @FXML
    public void initialize() {
        // Load logo
        Image logo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/logo.png")));
        logoImage.setImage(logo);

        // Initialize isDarkMode from SessionManager
        isDarkMode = SessionManager.getInstance().isDarkMode();

        // Highlight the current page's navigation label
        if (artistsButton != null) {
            artistsButton.getStyleClass().add("active");
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
    }

    public void loadArtistDetails(int artistId) {
        this.artistId = artistId;
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

        // Load artist details
        Artist artist = dbService.getArtistById(artistId);
        if (artist != null) {
            displayArtistDetails(artist);

            // Load related communities
            List<Community> communities = dbService.getCommunitiesByArtistId(artistId);
            displayCommunities(communities);

            // Load Spotify track for the artist
            loadSpotifyTrack(artist.getName());
        }
    }

    private void loadSpotifyTrack(String artistName) {
        // Run in a background thread to avoid freezing the UI
        // Use a daemon thread to ensure it doesn't prevent application shutdown
        Thread spotifyThread = new Thread(() -> {
            String artistId = SpotifyService.getArtistId(artistName);
            if (artistId != null) {
                // Get followers count
                int followers = SpotifyService.getArtistFollowers(artistId);
                if (followers > 0) {
                    // Update UI on the JavaFX application thread
                    javafx.application.Platform.runLater(() -> {
                        String formattedCount = SpotifyService.formatListenerCount(followers);
                        monthlyListenersLabel.setText("(" + formattedCount + " followers)");
                    });
                }

                // Get latest track instead of top track
                Map<String, String> trackInfo = SpotifyService.getLatestTrack(artistId);
                if (trackInfo != null) {
                    currentTrackId = trackInfo.get("id");

                    // Update UI on the JavaFX application thread
                    javafx.application.Platform.runLater(() -> {
                        // Update track info
                        trackNameLabel.setText(trackInfo.get("name"));
                        artistNameLabel.setText(trackInfo.get("artist"));

                        // Always display "Latest Release" regardless of the release type
                        nowPlayingLabel.setText("Latest Release");

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

    private void displayArtistDetails(Artist artist) {
        // Set artist name
        artistName.setText(artist.getName());

        // Set artist image
        byte[] imageData = artist.getImage();
        if (imageData != null) {
            // Load image with size constraints to prevent loading overly large images
            Image image = new Image(new ByteArrayInputStream(imageData), 500, 500, true, true);
            artistImage.setImage(image);
        }

        // Get artist description from database
        DatabaseService dbService = new DatabaseService();
        String description = dbService.getArtistDescription(artist.getId());

        if (description != null && !description.isEmpty()) {
            artistDescription.setText(description);

            // Extract and display keywords
            extractAndDisplayKeywords(description);
        } else {
            // Set default description if none is available
            artistDescription.setText("Information about " + artist.getName() + " will be displayed here.");
            keywordsLabel.setText("");
        }
    }

    /**
     * Extracts keywords from the artist description using OpenAI API
     * and displays them as hashtags.
     *
     * @param description The description to extract keywords from
     */
    private void extractAndDisplayKeywords(String description) {
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

    private void displayCommunities(List<Community> communities) {
        communitiesGrid.getChildren().clear();

        if (communities.isEmpty()) {
            Label noCommunitiesLabel = new Label("No related communities found");
            noCommunitiesLabel.getStyleClass().add("no-communities-label");
            communitiesGrid.add(noCommunitiesLabel, 0, 0);
            return;
        }

        int column = 0;
        int row = 0;
        int maxColumns = 3;

        for (Community community : communities) {
            ImageView imageView = createCommunityImageView(community);

            Label nameLabel = new Label(community.getName());
            nameLabel.getStyleClass().add("artist-name-label");

            VBox communityBox = new VBox(imageView, nameLabel);
            communityBox.setSpacing(10);
            communityBox.setAlignment(Pos.CENTER);
            communityBox.getStyleClass().add("artist-box");

            // Add click handler to navigate to community detail view
            final int communityId = community.getId();
            communityBox.setOnMouseClicked(event -> {
                try {
                    // Navigate to community detail view
                    CommunityDetailController controller = SceneTransitionUtil.changeContent(
                            "/com/example/upnext/community-detail-view.fxml",
                        SceneTransitionUtil.TransitionType.FADE, 
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

            ArtistsController controller = SceneTransitionUtil.changeContent(
                    "/com/example/upnext/artists-view.fxml",
                SceneTransitionUtil.TransitionType.SLIDE_RIGHT, 
                ArtistsController.class
            );
            controller.loadArtists();
        } catch (IOException e) {
            System.err.println("Failed to load artists-view.fxml: " + e.getMessage());
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
