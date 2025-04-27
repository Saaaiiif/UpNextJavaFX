package com.example.upnext;

import javafx.fxml.FXML;
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

    private int communityId;
    private String currentTrackId;
    private boolean isPlaying = false;

    @FXML
    public void initialize() {
        // Load logo
        Image logo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/logo.png")));
        logoImage.setImage(logo);
    }

    public void loadCommunityDetails(int communityId) {
        this.communityId = communityId;
        DatabaseService dbService = new DatabaseService();

        // Set default text for nowPlayingLabel
        nowPlayingLabel.setText("No song to play");

        // Hide the WebView and remove the drop shadow effect by default
        spotifyWebView.setVisible(false);
        spotifyWebView.setManaged(false);
        // Check if parent is not null before setting style
        if (spotifyWebView.getParent() != null) {
            spotifyWebView.getParent().setStyle("-fx-effect: none; -fx-background-color: transparent;");
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
                        // Check if parent is not null before setting style
                        if (spotifyWebView.getParent() != null) {
                            spotifyWebView.getParent().setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0); -fx-background-color: rgba(0, 0, 0, 0);");
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
                        // Check if parent is not null before setting style
                        if (spotifyWebView.getParent() != null) {
                            spotifyWebView.getParent().setStyle("-fx-effect: none; -fx-background-color: transparent;");
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
                    // Check if parent is not null before setting style
                    if (spotifyWebView.getParent() != null) {
                        spotifyWebView.getParent().setStyle("-fx-effect: none; -fx-background-color: transparent;");
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
            Image image = new Image(new ByteArrayInputStream(imageData));
            communityImage.setImage(image);
        }

        // Set community description
        String description = community.getDescription();
        if (description != null && !description.isEmpty()) {
            communityDescription.setText(description);
        } else {
            communityDescription.setText("No description available");
        }
    }

    private void displayArtists(List<Artist> artists) {
        artistsGrid.getChildren().clear();

        if (artists.isEmpty()) {
            Label noArtistsLabel = new Label("No related artists found");
            noArtistsLabel.setStyle("-fx-text-fill: #acacac; -fx-font-size: 16px; -fx-font-family: 'Feather Bold'");
            artistsGrid.add(noArtistsLabel, 0, 0);
            return;
        }

        int column = 0;
        int row = 0;
        int maxColumns = 3;

        for (Artist artist : artists) {
            ImageView imageView = createArtistImageView(artist);

            Label nameLabel = new Label(artist.getName());
            nameLabel.setStyle("-fx-text-fill: #000000; -fx-font-size: 16px; -fx-font-family: 'Feather Bold'");

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
            Image image = new Image(new ByteArrayInputStream(imageData));
            imageView.setImage(image);
        }
        imageView.setFitWidth(150);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);

        // Apply rounded corners using CSS
        imageView.setStyle("-fx-background-radius: 8px; -fx-border-radius: 8px;");

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
    private void handleCommunitiesButtonClick() {
        try {
            // Pause the player before navigating away
            pauseSpotifyPlayer();

            // Check the session type to determine which view to show
            SessionType currentSession = SessionManager.getInstance().getSessionType();

            if (currentSession == SessionType.ADMIN) {
                // Admin users see the communities-view
                MainController controller = SceneTransitionUtil.changeContent(
                    "/com/example/upnext/communities-view.fxml", 
                    SceneTransitionUtil.TransitionType.SLIDE_LEFT, 
                    MainController.class
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
}
